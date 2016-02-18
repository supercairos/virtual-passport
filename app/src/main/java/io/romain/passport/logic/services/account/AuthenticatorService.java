package io.romain.passport.logic.services.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.squareup.okhttp.Credentials;

import java.io.IOException;

import javax.inject.Inject;

import io.romain.passport.MyApplication;
import io.romain.passport.model.User;
import io.romain.passport.ui.LandingActivity;
import io.romain.passport.ui.RegisterActivity;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.constants.AuthenticatorConstants;
import retrofit.Response;
import retrofit.Retrofit;

public class AuthenticatorService extends Service {

	private Authenticator mAuthenticator;

	@Override
	public void onCreate() {
		// Create a new authenticator object
		mAuthenticator = new Authenticator(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (intent.getAction().equals(AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
			return mAuthenticator.getIBinder();
		}

		return null;
	}

	public static class Authenticator extends AbstractAccountAuthenticator {

		private final Context mContext;
		private final AccountManager mAccountManager;

		@Inject
		Retrofit mRetrofit;

		public Authenticator(Context context) {
			super(context);
			mContext = context;
			mAccountManager = AccountManager.get(mContext);

			MyApplication.getApplication(context).getApplicationComponent().inject(this);
		}

		@Override
		public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) {
			Dog.d("addAccount");

			final Intent intent = new Intent(mContext, RegisterActivity.class);
			intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

			final Bundle bundle = new Bundle();
			bundle.putParcelable(AccountManager.KEY_INTENT, intent);
			return bundle;
		}

		@Override
		public Bundle getAuthToken(AccountAuthenticatorResponse authenticatorResponse, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
			Dog.v("> getAuthToken (%s)", authTokenType);

			// If the caller requested an authToken type we don't support, then
			// return an error
			if (!authTokenType.equals(AuthenticatorConstants.AUTH_TOKEN_TYPE_FULL)) {
				final Bundle result = new Bundle();
				result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
				return result;
			}

			// Extract the username and password from the Account Manager
			final String password = mAccountManager.getPassword(account);

			// Lets give another try to authenticate the user
			if (password != null) {
				Dog.d("> re-authenticating with the existing password");
				try {
					Response<User> response = mRetrofit.create(User.UserService.class).login(Credentials.basic(account.name, password), false).execute();
					if(response.isSuccess()) {
						User user = response.body();
						if (!TextUtils.isEmpty(user.token)) {
							final Bundle result = new Bundle();
							result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
							result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
							result.putString(AccountManager.KEY_AUTHTOKEN, user.token);
							return result;
						}
					}
				} catch (IOException e) {
					// Do nothing
				}
			}

			// If we get here, then we couldn't access the user's password - so we
			// need to ask the user to re-login in our app;
			final Intent intent = new Intent(mContext, LandingActivity.class);
			intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, authenticatorResponse);
			intent.putExtra(AuthenticatorConstants.KEY_ACCOUNT_AUTHENTICATOR_FAILED, true);
			// TODO: Show toast to tell user why he needs to re-login

			final Bundle bundle = new Bundle();
			bundle.putParcelable(AccountManager.KEY_INTENT, intent);
			return bundle;
		}

		@Override
		public String getAuthTokenLabel(String authTokenType) {
			return null;
		}

		@NonNull
		@Override
		public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {

			Bundle result = new Bundle();
			result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);// or whatever logic you want here
			return result;
		}

		@Override
		public Bundle hasFeatures(AccountAuthenticatorResponse response,
		                          Account account, String[] features) {
			final Bundle result = new Bundle();
			result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
			return result;
		}

		@Override
		public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
			return null;
		}

		@Override
		public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
			return null;
		}

		@Override
		public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
			return null;
		}
	}
}
