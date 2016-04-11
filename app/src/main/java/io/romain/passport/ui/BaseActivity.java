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
package io.romain.passport.ui;

import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.romain.passport.MyApplication;
import io.romain.passport.logic.helpers.SharedPrefHelper;
import retrofit2.Retrofit;

public abstract class BaseActivity extends AppCompatActivity {

	@Inject
	public SharedPrefHelper mSharedPref;

	@Inject
	Retrofit mRetrofit;

	@Inject
	AccountManager mAccountManager;

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		onPostContentView();
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		onPostContentView();
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		super.setContentView(view, params);
		onPostContentView();
	}

	private void onPostContentView() {
		ButterKnife.bind(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApplication.getApplication(this).getApplicationComponent().inject(this);
	}
}
