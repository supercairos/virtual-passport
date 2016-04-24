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
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import javax.net.ssl.HttpsURLConnection;

import butterknife.Bind;
import butterknife.OnClick;
import io.romain.passport.R;
import io.romain.passport.logic.helpers.UserHelper;
import io.romain.passport.model.User;
import io.romain.passport.ui.fragments.dialogs.ErrorDialogFragment;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.SimpleTextWatcher;
import io.romain.passport.utils.loaders.ProfileLoader;
import io.romain.passport.utils.validators.EmailValidator;
import io.romain.passport.utils.validators.PasswordValidator;
import okhttp3.Credentials;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginActivity extends BaseActivity {

	private static final int LOADER_ID = 8888;

	private static final int PERMISSIONS_REQUEST = 12;

	@Bind(R.id.edit_login_email)
	EditText mEmail;
	@Bind(R.id.edit_login_email_layout)
	TextInputLayout mEmailLayout;

	@Bind(R.id.permission_checkbox)
	CheckBox mPermissionCheckbox;

	@Bind(R.id.edit_login_password)
	EditText mPassword;
	@Bind(R.id.edit_login_password_layout)
	TextInputLayout mPasswordLayout;

	@Bind(R.id.action_bar)
	Toolbar mActionBar;

	@Bind(R.id.login_button_login)
	Button mLoginButton;

	private ProgressDialog mDialog;
	private boolean isSaving = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.Passport_Login);
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

		if (isSaving) {
			showSavingDialog();
		}

		setProfileAutocomplete();
	}

	private void showSavingDialog() {
		mDialog = ProgressDialog.show(this, getString(R.string.login_dialog_login), getString(R.string.login_dialog_please_wait), true);
		mDialog.show();
		isSaving = true;
	}

	private void hideSavingDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}

		isSaving = false;
	}

	@OnClick(R.id.login_button_login)
	void onLoginClicked() {
		final String email = mEmail.getEditableText().toString();
		final String password = mPassword.getEditableText().toString();

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
			showSavingDialog();
			mRetrofit.create(User.UserService.class)
					.login(Credentials.basic(email, password))
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(user -> {
						hideSavingDialog();
						UserHelper.save(LoginActivity.this, user, password);
					}, throwable -> {
						Dog.d(throwable, "Ex!");
						hideSavingDialog();
						if (throwable instanceof HttpException) {
							int code = ((HttpException) throwable).code();
							switch (code) {
								case HttpsURLConnection.HTTP_UNAUTHORIZED:
									mEmailLayout.setError(getString(R.string.error_wrong_login));
									mEmail.requestFocus();
									mEmail.setSelection(mEmail.length());
									return;
								default:
							}
						}

						ErrorDialogFragment.newInstance(LoginActivity.this, R.string.error_occurred_please_retry).show(getSupportFragmentManager(), "Dialog");
					});
		}
	}

	@Override
	protected void onStop() {
		hideSavingDialog();
		super.onStop();
	}

	private void setProfileAutocomplete() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

			mPermissionCheckbox.setVisibility(View.GONE);
			getSupportLoaderManager().initLoader(LOADER_ID, null, new ProfileLoader(this) {
				@Override
				protected void onProfileLoaded(UserProfile profile) {
					mEmail.setText(profile.email);
					mPassword.requestFocus();
				}
			});
		} else {
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS) ||
					ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
				setPermissionCheckbox();
			} else {
				mPermissionCheckbox.setVisibility(View.GONE);
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.GET_ACCOUNTS}, PERMISSIONS_REQUEST);
			}
		}
	}

	private void setPermissionCheckbox() {
		mPermissionCheckbox.setChecked(false);
		mPermissionCheckbox.setVisibility(View.VISIBLE);
		mPermissionCheckbox.setOnCheckedChangeListener((view, isChecked) -> {
			if (isChecked) {
				ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.GET_ACCOUNTS}, PERMISSIONS_REQUEST);
			}
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSIONS_REQUEST) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
				setProfileAutocomplete();
			} else {
				if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS) ||
						ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
					setPermissionCheckbox();
				} else {
					// The user doesn't want us helping him :'( #me-sad
					mPermissionCheckbox.setVisibility(View.GONE);
				}
			}
		}
	}

	private final TextWatcher mLoginFieldWatcher = new SimpleTextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			mLoginButton.setEnabled(isLoginValid());
		}
	};

	private boolean isLoginValid() {
		return mEmail.length() > 0 && mPassword.length() > 0;
	}
}
