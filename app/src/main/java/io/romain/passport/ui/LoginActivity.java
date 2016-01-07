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

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.okhttp.Credentials;

import java.net.HttpURLConnection;

import butterknife.Bind;
import butterknife.OnClick;
import io.romain.passport.R;
import io.romain.passport.logic.helpers.UserHelper;
import io.romain.passport.model.User;
import io.romain.passport.utils.loaders.ProfileLoader;
import io.romain.passport.utils.validators.EmailValidator;
import io.romain.passport.utils.validators.PasswordValidator;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class LoginActivity extends BaseActivity {

	private static final int LOADER_ID = 8888;

	@Bind(R.id.edit_login_email)
	EditText mEmail;
	@Bind(R.id.edit_login_email_layout)
	TextInputLayout mEmailLayout;

	@Bind(R.id.edit_login_password)
	EditText mPassword;
	@Bind(R.id.edit_login_password_layout)
	TextInputLayout mPasswordLayout;

	@Bind(R.id.action_bar)
	Toolbar mActionBar;

	@Bind(R.id.login_button_login)
	Button mLoginButton;

	private ProgressDialog mDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		setSupportActionBar(mActionBar);
		//noinspection ConstantConditions
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mEmail.addTextChangedListener(mLoginFieldWatcher);
		mPassword.addTextChangedListener(mLoginFieldWatcher);
		mPassword.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				onLoginClicked();
				 return true;
			}
			return false;
		});

		getSupportLoaderManager().initLoader(LOADER_ID, null, new ProfileLoader(this) {
			@Override
			protected void onProfileLoaded(UserProfile profile) {
				mEmail.setText(profile.email);
				mPassword.requestFocus();
			}
		});
	}

	@OnClick(R.id.login_button_login)
	void onLoginClicked() {
		String email = mEmail.getEditableText().toString();
		String password = mPassword.getEditableText().toString();

		boolean isEverythingOk = true;
		if (!EmailValidator.getInstance().validate(email)) {
			mEmailLayout.setError(getString(R.string.error_email));
			mEmail.requestFocus();
			isEverythingOk = false;
		} else {
			mEmailLayout.setError(null);
		}

		if (!PasswordValidator.isAcceptablePassword(password)) {
			mPasswordLayout.setError(getString(R.string.error_password));
			mPassword.requestFocus();
			isEverythingOk = false;
		} else {
			mPasswordLayout.setError(null);
		}

		if (isEverythingOk) {
			mDialog = ProgressDialog.show(this, "Login", "Please wait...", true);
			mDialog.show();

			mRetrofit.create(User.UserService.class).login(Credentials.basic(email, password)).enqueue(new Callback<User>() {
				@Override
				public void onResponse(Response<User> response, Retrofit retrofit) {
					if (mDialog != null) {
						mDialog.dismiss();
					}

					if (response.isSuccess()) {
						User user = response.body();
						Toast.makeText(LoginActivity.this, "Welcome : " + user.name + "(" + user.id + ")", Toast.LENGTH_LONG).show();
						UserHelper.save(mAccountManager, user);
						UserHelper.next(LoginActivity.this);
					} else {
						switch (response.code()) {
							case HttpURLConnection.HTTP_UNAUTHORIZED: // Unauthorized -> Wrong Login
								Toast.makeText(LoginActivity.this, "Wrong Login!!", Toast.LENGTH_LONG).show();
								break;
							default:
								Toast.makeText(LoginActivity.this, "Server returned  (" + response.code() + ")", Toast.LENGTH_LONG).show();
								break;

						}
					}
				}

				@Override
				public void onFailure(Throwable t) {
					// ...
					Toast.makeText(LoginActivity.this, "Failure!! (" + t.getMessage() + ")", Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	@Override
	protected void onStop() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
		super.onStop();
	}

	private TextWatcher mLoginFieldWatcher = new TextWatcher() {
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

		@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

		@Override
		public void afterTextChanged(Editable s) {
			mLoginButton.setEnabled(isLoginValid());
		}
	};

	private boolean isLoginValid() {
		return mEmail.length() > 0 && mPassword.length() > 0;
	}
}
