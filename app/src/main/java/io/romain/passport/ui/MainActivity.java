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
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.OnClick;
import io.romain.passport.R;
import io.romain.passport.logic.services.gcm.RegistrationIntentService;
import io.romain.passport.ui.fragments.dialogs.LogoutDialogFragment;
import io.romain.passport.ui.transitions.FabDialogMorphSetup;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.PlayServicesUtils;

public class MainActivity extends DrawerActivity {

	private static final int REQUEST_CODE_ADD_CITY = 1337;

	@Bind(R.id.action_bar)
	Toolbar mActionBar;

	@Bind(R.id.floating_action_button)
	FloatingActionButton mFloatingActionButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// enable ActionBar app icon to behave as action to toggle nav drawer
		setSupportActionBar(mActionBar);
		//noinspection ConstantConditions
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		registerGcm();
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
				break;
		}

		mDrawerLayout.closeDrawer(mDrawerNavigation);
		return true;
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
}
