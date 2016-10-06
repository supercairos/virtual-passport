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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.romain.passport.MyApplication;
import io.romain.passport.logic.helpers.SharedPrefHelper;
import retrofit2.Retrofit;

public abstract class BaseFragment extends Fragment {

	@Inject
	public SharedPrefHelper mSharedPref;

	@Inject
	public Retrofit mRetrofit;

	@Inject
	public AccountManager mAccountManager;
	private Unbinder mButterKnifeBinder;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		MyApplication.getApplication(context).getApplicationComponent().inject(this);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mButterKnifeBinder = ButterKnife.bind(this, view);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mButterKnifeBinder.unbind();
	}
}
