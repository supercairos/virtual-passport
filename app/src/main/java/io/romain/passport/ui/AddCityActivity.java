/*
 *    Copyright 2016 Romain
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.romain.passport.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.IOException;

import butterknife.Bind;
import butterknife.OnClick;
import io.romain.passport.R;
import io.romain.passport.logic.observables.CitySaverObservable;
import io.romain.passport.logic.observables.GeocoderObservable;
import io.romain.passport.logic.observables.LocationUpdatesObservable;
import io.romain.passport.logic.observables.ReverseGeocoderObservable;
import io.romain.passport.model.City;
import io.romain.passport.model.CityAutocomplete;
import io.romain.passport.ui.adaptater.CitySearchAdapter;
import io.romain.passport.ui.transitions.FabDialogMorphSetup;
import io.romain.passport.ui.views.LocationAutocompleteTextView;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.SimpleAnimatorListener;
import io.romain.passport.utils.SimpleTextWatcher;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddCityActivity extends LocationPopupActivity {

    @Bind(R.id.dialog_root)
    ViewGroup mRoot;

    @Bind(R.id.dialog_loading)
    ProgressBar mLoading;

    @Bind(R.id.dialog_container)
    ViewGroup mContainer;

    @Bind(R.id.add_city_city)
    LocationAutocompleteTextView mEditText;

    @Bind(R.id.add_city_ok)
    Button mValidateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
        FabDialogMorphSetup.setupSharedElementTransitions(
                this,
                mRoot,
                getResources().getDimensionPixelSize(R.dimen.dialog_corners)
        );

        Dog.d("Main thread is : " + Looper.getMainLooper().getThread().getId());

        final CitySearchAdapter adapter = new CitySearchAdapter(this);
        mEditText.setAdapter(adapter);
        mEditText.setOnItemClickListener((parent, view, position, id) -> {
            showLoadingSpinner();
            CityAutocomplete autocomplete = (CityAutocomplete) mEditText.getAdapter().getItem(position);
            mRetrofit.create(City.CityService.class)
                    .resolve(autocomplete.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(city -> CitySaverObservable.create(AddCityActivity.this, city))
                    .subscribe(uri -> {
                        Dog.d("Dismissing (Thread ID is : " + Thread.currentThread().getId() + ")");
                        dismiss(Activity.RESULT_OK);
                    }, throwable -> {
                        Dog.e(throwable, "EE >> ");
                        showContainerFrame();
                    });
        });
        mEditText.setDrawableListener((text, touched) -> {
            switch (touched) {
                case LocationAutocompleteTextView.DRAWABLE_RIGHT:
                    showLoadingSpinner();
                    LocationUpdatesObservable.create(getGoogleApiClient(), getLocationRequest())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .filter(location -> location.getAccuracy() < 5000)
                            .flatMap(location -> Observable.zip(
                                    ReverseGeocoderObservable.create(AddCityActivity.this, location),
                                    mRetrofit.create(City.CityService.class).picture(location.getLatitude(), location.getLongitude()),
                                    (address, body) -> {
                                        if (body != null) {
                                            try {
                                                return new City(
                                                        address.getLocality(),
                                                        address.getCountryName(),
                                                        address.getLatitude(),
                                                        address.getLongitude(),
                                                        body.string()
                                                );
                                            } catch (IOException e) {
                                                Dog.e(e, "IOException");
                                            }
                                        }

                                        return new City(
                                                address.getLocality(),
                                                address.getCountryName(),
                                                address.getLatitude(),
                                                address.getLongitude(),
                                                null
                                        );
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                            )
                            .filter(city -> city != null)
                            .filter(city -> !TextUtils.isEmpty(city.name) && !TextUtils.isEmpty(city.country))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .flatMap(city -> CitySaverObservable.create(AddCityActivity.this, city))
                            .subscribe(uri -> {
                                Dog.d("Dismissing (Thread ID is : " + Thread.currentThread().getId() + ")");
                                dismiss(Activity.RESULT_OK);
                            }, throwable -> {
                                Dog.e(throwable, "An error occurred :'(");
                                showContainerFrame();
                            });

                    return true;
            }

            return false;
        });
        mEditText.setThreshold(2);
        mEditText.addTextChangedListener(new SimpleTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                mValidateButton.setEnabled(s.length() > 0);
            }
        });
    }

    @OnClick({R.id.dialog_outer_frame, R.id.add_city_cancel})
    void dismiss() {
        dismiss(Activity.RESULT_CANCELED);
    }

    private void dismiss(int result) {
        setResult(result);
        finishAfterTransition();
    }

    @OnClick(R.id.add_city_ok)
    public void onValidate() {
        showLoadingSpinner();
        GeocoderObservable.create(this, mEditText.getText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(address -> Observable.zip(
                        Observable.just(address),
                        mRetrofit.create(City.CityService.class).picture(address.getLatitude(), address.getLongitude()),
                        (address1, body) -> {
                            if (body != null) {
                                try {
                                    return new City(
                                            address1.getLocality(),
                                            address1.getCountryName(),
                                            address1.getLatitude(),
                                            address1.getLongitude(),
                                            body.string()
                                    );
                                } catch (IOException e) {
                                    Dog.e(e, "IOException");
                                }
                            }

                            return new City(
                                    address1.getLocality(),
                                    address1.getCountryName(),
                                    address1.getLatitude(),
                                    address1.getLongitude(),
                                    null
                            );
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                )
                .filter(city -> city != null)
                .filter(city -> !TextUtils.isEmpty(city.name) && !TextUtils.isEmpty(city.country))
                .flatMap(city -> CitySaverObservable.create(AddCityActivity.this, city))
                .subscribe(uri -> {
                    Dog.d("Dismissing (Thread ID is : " + Thread.currentThread().getId() + ")");
                    dismiss(Activity.RESULT_OK);
                }, throwable -> {
                    Dog.e(throwable, "An error occurred :'(");
                    showContainerFrame();
                });
    }

    private void showContainerFrame() {
        ObjectAnimator a = ObjectAnimator.ofFloat(mLoading, View.ALPHA, 1, 0);
        a.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoading.setVisibility(View.GONE);
            }
        });
        ObjectAnimator b = ObjectAnimator.ofFloat(mContainer, View.ALPHA, 0, 1);
        b.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mContainer.setAlpha(0);
                mContainer.setVisibility(View.VISIBLE);
                mEditText.requestFocus();
                mEditText.setError(getString(R.string.add_city_error));

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            }
        });

        AnimatorSet transition = new AnimatorSet();
        transition.playTogether(a, b);
        transition.setDuration(300);
        transition.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in));

        transition.start();
    }

    private void showLoadingSpinner() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        ObjectAnimator a = ObjectAnimator.ofFloat(mLoading, View.ALPHA, 0, 1);
        a.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mLoading.setAlpha(0);
                mLoading.setVisibility(View.VISIBLE);
            }
        });
        ObjectAnimator b = ObjectAnimator.ofFloat(mContainer, View.ALPHA, 1, 0);
        b.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mContainer.setVisibility(View.INVISIBLE);
            }
        });

        AnimatorSet transition = new AnimatorSet();
        transition.playTogether(a, b);
        transition.setDuration(300);
        transition.setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in));

        transition.start();
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

}