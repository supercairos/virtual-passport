/*
 *    Copyright 2015 Romain
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
package io.romain.passport.ui.fragments;

import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import javax.inject.Inject;

import io.romain.passport.MyApplication;
import io.romain.passport.logic.helpers.SharedPrefHelper;
import io.romain.passport.utils.GoogleApiUtils;
import retrofit.Retrofit;

public abstract class BaseFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	@Inject
	public SharedPrefHelper mSharedPref;

	@Inject
	public Retrofit mRetrofit;

	@Inject
	public AccountManager mAccountManager;
	private GoogleApiClient mGoogleApiClient;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		((MyApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (GoogleApiUtils.isGooglePlayServicesAvailable(getContext())) {
			mGoogleApiClient = new GoogleApiClient.Builder(getContext())
					.addApi(LocationServices.API)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (GoogleApiUtils.isGooglePlayServicesAvailable(getContext())) {
			if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.disconnect();
			}
		}
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}
}
