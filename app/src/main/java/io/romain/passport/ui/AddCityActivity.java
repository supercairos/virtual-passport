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

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.OnClick;
import io.romain.passport.R;
import io.romain.passport.logic.observables.CitySaverObservable;
import io.romain.passport.logic.observables.GeocoderObservable;
import io.romain.passport.logic.observables.GoogleApiObservable;
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
import io.romain.passport.utils.SimpleTransitionListener;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddCityActivity extends LocationPermissionActivity {

    private static final int REQUEST_CHECK_SETTINGS = 1222;

    private static final long LOCATION_REQUEST_TIMEOUT = 5L * 60L * 1000L; // request for at most 5 minutes
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10L * 1000L; // 10s
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final LocationRequest sLocationRequest = new LocationRequest()
            // Sets the desired interval for active location updates. This interval is
            // inexact. You may not receive updates at all if no location sources are available, or
            // you may receive them slower than requested. You may also receive updates faster than
            // requested if other applications are requesting location at a faster interval.
            .setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
            .setExpirationDuration(LOCATION_REQUEST_TIMEOUT)

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates faster than this value.
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
            .setNumUpdates(5)
            .setPriority(LocationRequest.PRIORITY_LOW_POWER);

    public static final String EXTRA_CITY_RESULT = "extra_city_result";
    public static final String EXTRA_INITIAL_TEXT = "extra_initial_text";
    private Subscription mSubscription;

    public static LocationRequest getLocationRequest() {
        return sLocationRequest;
    }

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

        Dog.d("Main thread is : " + Looper.getMainLooper().getThread().getName());
        final CitySearchAdapter adapter = new CitySearchAdapter(this);
        mEditText.setAdapter(adapter);
        mEditText.setOnItemClickListener((parent, view, position, id) -> {
            showLoadingSpinner();
            CityAutocomplete autocomplete = (CityAutocomplete) mEditText.getAdapter().getItem(position);
            unsubscribe();
            mSubscription = mRetrofit.create(City.CityService.class)
                    .resolve(autocomplete.id)
                    .subscribeOn(Schedulers.io())
                    .map(city -> CitySaverObservable.save(this, city))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(city -> {
                        dismiss(Activity.RESULT_OK, city);
                    }, throwable -> {
                        hideLoadingSpinner();
                    });
        });
        mEditText.setDrawableListener((text, touched) -> {
            switch (touched) {
                case LocationAutocompleteTextView.DRAWABLE_RIGHT:
                    getUserLocation();
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

        String initial = getIntent().getStringExtra(EXTRA_INITIAL_TEXT);
        if(!TextUtils.isEmpty(initial)) {
            mEditText.setText(initial);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);
        if (getWindow().getSharedElementEnterTransition() != null) {
            getWindow().getSharedElementEnterTransition().addListener(new SimpleTransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    if (!isResolvingError()) {
                        setLocationPermission();
                    }
                }
            });
        } else {
            if (!isResolvingError()) {
                setLocationPermission();
            }
        }
    }

    private void getUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            showLoadingSpinner();
            unsubscribe();
            mSubscription = GoogleApiObservable
                    .checkLocation(getGoogleApiClient(), getLocationRequest())
                    .doOnNext(result -> {
                        final Status status = result.getStatus();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                // All location settings are satisfied. The client can initialize location
                                // requests here.
                                Dog.i("All location settings are satisfied.");
                                // Our work is done here
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied. But could be fixed by showing the user
                                // a dialog.
                                Dog.i("Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    status.startResolutionForResult(
                                            AddCityActivity.this,
                                            REQUEST_CHECK_SETTINGS
                                    );
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                    Dog.i("PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.
                                Dog.i("Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                                hideLocationIcon();
                                break;
                        }
                    })
                    .flatMap(request -> LocationUpdatesObservable.create(getGoogleApiClient(), getLocationRequest()))
                    .timeout(60L, TimeUnit.SECONDS)
                    .filter(location -> location.getAccuracy() < 5000)
                    .take(1)
                    .flatMap(location -> Observable.zip(
                            ReverseGeocoderObservable.create(AddCityActivity.this, location),
                            mRetrofit.create(City.CityService.class).picture(location.getLatitude(), location.getLongitude()),
                            (address1, body) -> {
                                if (body != null) {
                                    try {
                                        String picture = body.string();
                                        if (!TextUtils.isEmpty(picture)) {
                                            return new City(
                                                    address1.getLocality(),
                                                    address1.getCountryName(),
                                                    address1.getLatitude(),
                                                    address1.getLongitude(),
                                                    picture
                                            );
                                        }
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
                    )
                    .filter(city -> city != null)
                    .filter(city -> !TextUtils.isEmpty(city.name) && !TextUtils.isEmpty(city.country))
                    .observeOn(Schedulers.io())
                    .map(city -> CitySaverObservable.save(this, city))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(city -> {
                        dismiss(Activity.RESULT_OK, city);
                    }, throwable -> {
                        Dog.e(throwable, "An error occurred :'(");
                        hideLoadingSpinner();
                    });

        } else {
            setLocationPermission();
        }
    }

    @OnClick({R.id.add_city_cancel})
        // R.id.dialog_outer_frame,
    void dismiss() {
        dismiss(Activity.RESULT_CANCELED, null);
    }

    private void dismiss(int result, City city) {
        if (city != null) {
            setResult(result, new Intent().putExtra(EXTRA_CITY_RESULT, city));
        } else {
            setResult(result);
        }
        finishAfterTransition();
    }

    @OnClick(R.id.add_city_ok)
    public void onValidate() {
        showLoadingSpinner();
        unsubscribe();
        mSubscription = GeocoderObservable.create(this, mEditText.getText().toString())
                .subscribeOn(Schedulers.io())
                .flatMap(address -> Observable.zip(
                        Observable.just(address),
                        mRetrofit.create(City.CityService.class).picture(address.getLatitude(), address.getLongitude()),
                        (address1, body) -> {
                            if (body != null) {
                                try {
                                    String picture = body.string();
                                    if (!TextUtils.isEmpty(picture)) {
                                        return new City(
                                                address1.getLocality(),
                                                address1.getCountryName(),
                                                address1.getLatitude(),
                                                address1.getLongitude(),
                                                picture
                                        );
                                    }
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
                )
                .filter(city -> city != null)
                .filter(city -> !TextUtils.isEmpty(city.name) && !TextUtils.isEmpty(city.country))
                .map(city -> CitySaverObservable.save(this, city))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(city -> {
                    dismiss(Activity.RESULT_OK, city);
                }, throwable -> {
                    Dog.e(throwable, "An error occurred :'(");
                    hideLoadingSpinner();
                });
    }

    private void unsubscribe() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        unsubscribe();
    }

    private void hideLoadingSpinner() {
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

    private void hideLocationIcon() {
        mEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
    }

    private void showLocationIcon() {
        mEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_my_location_black_24dp, 0);
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Dog.i("User agreed to make required location settings changes.");
                        getUserLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not
                        Dog.i("User chose not to make required location settings changes.");
                        hideLocationIcon();
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    protected void onLocationDenied(boolean isRecoverable) {
        if (!isRecoverable) {
            hideLocationIcon();
        } else {
            showLocationIcon();
        }
    }

    @Override
    protected void onLocationGranted() {
        showLocationIcon();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}