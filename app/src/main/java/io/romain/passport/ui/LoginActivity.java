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
import android.widget.Toast;

import com.squareup.okhttp.Credentials;

import java.net.HttpURLConnection;

import butterknife.Bind;
import butterknife.OnClick;
import io.romain.passport.R;
import io.romain.passport.logic.helpers.UserHelper;
import io.romain.passport.model.User;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.SimpleTextWatcher;
import io.romain.passport.utils.loaders.ProfileLoader;
import io.romain.passport.utils.validators.EmailValidator;
import io.romain.passport.utils.validators.PasswordValidator;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

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

        setProfileAutocomplete();
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
            mDialog = ProgressDialog.show(this, getString(R.string.login), getString(R.string.please_wait), true);
            mDialog.show();

            mRetrofit.create(User.UserService.class)
                    .login(Credentials.basic(email, password))
                    .enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Response<User> response, Retrofit retrofit) {
                            if (mDialog != null) {
                                mDialog.dismiss();
                            }

                            if (response.isSuccess()) {
                                UserHelper.save(LoginActivity.this, response.body());
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
                            Dog.e(t, "Me sad :'(");
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
