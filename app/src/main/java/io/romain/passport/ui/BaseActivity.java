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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.romain.passport.MyApplication;
import io.romain.passport.logic.managers.SharedPrefManager;
import io.romain.passport.utils.debug.ViewServer;
import retrofit.Retrofit;

public class BaseActivity extends Activity {

	@Inject
	Retrofit mRetrofitService;

	@Inject
	SharedPrefManager mSharedPref;

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
		ViewServer.get(this).addWindow(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApplication.getApplication(this).getApplicationComponent().inject(this);
	}


	@Override
	protected void onResume() {
		super.onResume();
		ViewServer.get(this).setFocusedWindow(this);
	}

	public void onDestroy() {
		super.onDestroy();
		ViewServer.get(this).removeWindow(this);
	}

}
