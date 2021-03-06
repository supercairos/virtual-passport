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

import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.OnClick;
import io.romain.passport.R;
import io.romain.passport.logic.helpers.AccountHelper;
import io.romain.passport.logic.observables.CitySaverObservable;
import io.romain.passport.logic.observables.LocationUpdatesObservable;
import io.romain.passport.logic.observables.ReverseGeocoderObservable;
import io.romain.passport.logic.services.gcm.RegistrationIntentService;
import io.romain.passport.model.City;
import io.romain.passport.model.database.PassportContentProvider;
import io.romain.passport.ui.animation.Rotate3DAnimation;
import io.romain.passport.ui.fragments.CityListFragment;
import io.romain.passport.ui.fragments.dialogs.LogoutDialogFragment;
import io.romain.passport.ui.transitions.FabDialogMorphSetup;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.PlayServicesUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends DrawerActivity implements SearchView.OnQueryTextListener {

	private static final int REQUEST_CODE_ADD_CITY = 1337;
	private static final int DISTANCE = 20;

	@Bind(R.id.action_bar)
	Toolbar mActionBar;

	@Bind(R.id.floating_action_button_coordinator)
	CoordinatorLayout mFabCoordinatorLayout;
	@Bind(R.id.floating_action_button)
	FloatingActionButton mFloatingActionButton;

	@Bind(R.id.detected_position_layout)
	ViewAnimator mDetectedPositionLayout;
	@Bind(R.id.detected_position_available_layout)
	ViewGroup mDetectedPositionAvailableLayout;
	@Bind(R.id.detected_position_available_text_view)
	TextView mDetectedPositionAvailableTextView;
	@Bind(R.id.detected_position_available_close)
	ImageView mDetectedPositionAvailableClose;
	@Bind(R.id.detected_position_loading_layout)
	ViewGroup mDetectedPositionLoadingLayout;

	private CityListFragment mCityListFragment;

	public static void start(Context context) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String token = AccountHelper.peekToken(mAccountManager);
		if (TextUtils.isEmpty(token)) {
			Dog.d("Auto disconnect");
			AccountHelper.removeAccount(mAccountManager, new AccountHelper.AccountRemovedCallback() {
				@Override
				public void onSuccess() {
					LandingActivity.start(MainActivity.this);
				}

				@Override
				public void onFailure() {
					LandingActivity.start(MainActivity.this);
				}
			});
		}

		setTheme(R.style.Passport_Main);
		setContentView(R.layout.activity_main);

		// enable ActionBar app icon to behave as action to toggle nav drawer
		setSupportActionBar(mActionBar);
		//noinspection ConstantConditions
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		mDetectedPositionLayout.setOutAnimation(new Rotate3DAnimation(Rotate3DAnimation.Rotate.LEFT, Rotate3DAnimation.Angle.FROM_DEGREES_0, 300, true));
		mDetectedPositionLayout.setInAnimation(new Rotate3DAnimation(Rotate3DAnimation.Rotate.LEFT, Rotate3DAnimation.Angle.TO_DEGREES_0, 300, false));

		registerGcm();

		mDetectedPositionLayout.setOnClickListener(v -> {
			final City city = (City) mDetectedPositionAvailableLayout.getTag(R.id.data);
			mDetectedPositionLayout.showNext();
			mDetectedPositionLayout.setEnabled(false);
			mRetrofit.create(City.CityService.class)
					.picture(city.latitude(), city.longitude())
					.map(body -> {
						if (body != null) {
							try {
								String picture = body.string();
								if (!TextUtils.isEmpty(picture)) {
									// FIXME : Find a better method to do that.
									return City.create(city.name(), city.country(), city.longitude(), city.latitude(), Uri.parse(picture));
								}
							} catch (IOException e) {
								Dog.e(e, "IOException");
							}
						}

						return city;
					})
					.subscribeOn(Schedulers.io())
					.map(city1 -> CitySaverObservable.save(MainActivity.this, city1))
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(city1 -> {
						mDetectedPositionLayout.showPrevious();
						mDetectedPositionLayout.setEnabled(true);
						mDetectedPositionLayout.setVisibility(View.GONE);
						Snackbar.make(mFabCoordinatorLayout, getString(R.string.city_saved_successfully, city.name()), Snackbar.LENGTH_SHORT).show();
					}, throwable -> {
						Dog.e(throwable, "An error occurred :'(");
						Toast.makeText(MainActivity.this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
						mDetectedPositionLayout.showNext();
						mDetectedPositionLayout.setEnabled(true);
					});
		});

		mDetectedPositionAvailableClose.setOnClickListener(v -> mDetectedPositionLayout.setVisibility(View.GONE));
	}

	@Override
	public void onConnected(Bundle bundle) {
		super.onConnected(bundle);
		// Damn you google!!!
		if (!isResolvingError()) {
			setLocationPermission();
		}
	}

	@Override
	protected void onLocationDenied(boolean isRecoverable) {
		// Do nothing
	}

	@Override
	protected void onLocationGranted() {
		if (getLocationMode() != Settings.Secure.LOCATION_MODE_OFF) {
			Dog.i("Trying to get the user location");
			LocationUpdatesObservable.create(getGoogleApiClient(), AddCityActivity.getLocationRequest())
					.subscribeOn(Schedulers.io())
					.timeout(120L, TimeUnit.SECONDS)
					.filter(location -> location.getAccuracy() < 5000)
					.flatMap(location -> ReverseGeocoderObservable.create(MainActivity.this, location))
					.filter(address -> !TextUtils.isEmpty(address.getLocality()) && !TextUtils.isEmpty(address.getCountryName()))
					.take(1)
					.filter(address -> {
						double delta_lon = Math.abs((DISTANCE * 360) / (Math.cos(address.getLatitude()) * 40075 * 2));
						double delta_lat = Math.abs((DISTANCE * 360) / (Math.cos(address.getLongitude()) * 40075 * 2));

						Cursor c = getContentResolver().query(
								PassportContentProvider.Cities.CONTENT_URI,
								null,
								City.LATITUDE + " < ? AND " + City.LONGITUDE + " < ? AND " +
										City.LATITUDE + " > ? AND " + City.LONGITUDE + " > ?",
								new String[]{
										String.valueOf(address.getLatitude() + delta_lat),
										String.valueOf(address.getLongitude() + delta_lon),
										String.valueOf(address.getLatitude() - delta_lat),
										String.valueOf(address.getLongitude() - delta_lon)
								},
								null);

						if (c != null) {
							int count = c.getCount();
							c.close();
							return count == 0;
						} else {
							return false;
						}

					})
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(address -> {
						if(mDetectedPositionAvailableLayout != null) {
							mDetectedPositionAvailableLayout.setTag(R.id.data, City.create(address.getLocality(), address.getCountryName(), address.getLongitude(), address.getLatitude()));
							mDetectedPositionAvailableTextView.setText(getString(R.string.main_are_you_in, address.getLocality()));

							mDetectedPositionLayout.setVisibility(View.VISIBLE);
						}
					}, throwable -> {
						Dog.e(throwable, "An error occurred :'(");
					});
		}
	}

	private int getLocationMode() {
		return Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_city_list_activity, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setOnQueryTextListener(this);

		return true;
	}

	private void registerGcm() {
		if (PlayServicesUtils.checkPlayServices(this)) {
			if (TextUtils.isEmpty(PlayServicesUtils.getRegistrationId(this))) {
				startService(new Intent(this, RegistrationIntentService.class));
			}
		} else {
			Dog.i("No valid Google Play Services APK found.");
		}
	}

	@Override
	public boolean onNavItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.navigation_account_logout:
				showLogoutDialog();
				mDrawerLayout.closeDrawer(mDrawerNavigation);
				return true;
			case R.id.navigation_add_city:
				startActivityForResult(new Intent(this, AddCityActivity.class), REQUEST_CODE_ADD_CITY);
				mDrawerLayout.closeDrawer(mDrawerNavigation);
				return true;
		}

		mDrawerLayout.closeDrawer(mDrawerNavigation);
		return false;
	}

	@OnClick(R.id.floating_action_button)
	protected void onFloatingActionButtonClicked() {
		Intent intent = new Intent(this, AddCityActivity.class);
		intent.putExtra(FabDialogMorphSetup.EXTRA_SHARED_ELEMENT_START_COLOR, ContextCompat.getColor(this, R.color.accent));
		ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, mFloatingActionButton, getString(R.string.transition_add_city));
		startActivityForResult(intent, REQUEST_CODE_ADD_CITY, options.toBundle());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQUEST_CODE_ADD_CITY:
				switch (resultCode) {
					case RESULT_OK:
						City city = data.getParcelableExtra(AddCityActivity.EXTRA_CITY_RESULT);
						Snackbar.make(mFabCoordinatorLayout, getString(R.string.city_saved_successfully, city.name()), Snackbar.LENGTH_SHORT).show();
						break;
					case RESULT_CANCELED:
						break;
				}
		}
	}

	private void showLogoutDialog() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}

		ft.addToBackStack(null);

		// Create and show the dialog.
		new LogoutDialogFragment().show(ft, "dialog");
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		getCityListFragment().setQuery(newText);
		return true;
	}

	private CityListFragment getCityListFragment() {
		if (mCityListFragment == null) {
			mCityListFragment = (CityListFragment) getSupportFragmentManager().findFragmentById(R.id.city_list_fragment);
		}

		return mCityListFragment;
	}

	public ViewGroup getCoordinatorLayout() {
		return mFabCoordinatorLayout;
	}
}
