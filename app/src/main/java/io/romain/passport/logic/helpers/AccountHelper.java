package io.romain.passport.logic.helpers;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentResolver;
import android.os.Build;
import android.os.Bundle;

import java.io.IOException;

import io.romain.passport.utils.Dog;
import io.romain.passport.utils.constants.AccountConstants;
import io.romain.passport.utils.constants.AuthenticatorConstants;

public final class AccountHelper {

	public static Account getAccount(AccountManager manager) {

		Account[] accounts = manager.getAccountsByType(AuthenticatorConstants.ACCOUNT_TYPE);
		if (accounts.length > 0) {
			return accounts[0];
		} else {
			return null;
		}
	}

	public static String getDisplayName(AccountManager manager) {
		Account account = getAccount(manager);
		if (account != null) {
			return manager.getUserData(account, AccountConstants.KEY_NAME);
		} else {
			return null;
		}
	}

	public static String getToken(AccountManager manager) throws IOException {
		try {
			Account[] accounts = manager.getAccountsByType(AuthenticatorConstants.ACCOUNT_TYPE);
			if (accounts.length > 0) {
				return manager.blockingGetAuthToken(accounts[0], AuthenticatorConstants.AUTH_TOKEN_TYPE_FULL, true);
			} else {
				return "";
			}
		} catch (AuthenticatorException | OperationCanceledException e) {
			Dog.e(e, "Error");
			return "";
		}
	}

	public static String peekToken(AccountManager manager) {

		Account[] accounts = manager.getAccountsByType(AuthenticatorConstants.ACCOUNT_TYPE);
		if (accounts.length > 0) {
			return manager.peekAuthToken(accounts[0], AuthenticatorConstants.AUTH_TOKEN_TYPE_FULL);
		} else {
			return "";
		}
	}

	public static void setSyncable(AccountManager manager, String provider, boolean enabled) {
		Dog.d("Start setSyncable");
		Account account = getAccount(manager);
		if (account != null) {
			ContentResolver.setIsSyncable(account, provider, enabled ? 1 : -1);
		} else {
			Dog.e("Account was null while starting sync...");
		}
	}

	public static void startSync(AccountManager manager, String provider) {
		Dog.d("Start sync");
		/*
		 * Signal the framework to run your sync adapter. Assume that
		 * app initialization has already created the account.
		 */
		Account account = getAccount(manager);
		if (account != null) {
			Bundle bundle = new Bundle();
			bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
			bundle.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_BACKOFF, true);
			bundle.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, true);
			bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

			ContentResolver.requestSync(account, provider, bundle);
		} else {
			Dog.e("Account was null while starting sync...");
		}
	}

	@SuppressWarnings("deprecation")
	public static void removeAccount(final AccountManager manager, final AccountRemovedCallback callback) {
		Dog.d("Loging out");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
			manager.removeAccount(getAccount(manager), null, future -> {
				try {
					Bundle bundle = future.getResult();
					if (bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)) {
						callback.onSuccess();
					}
				} catch (OperationCanceledException | IOException | AuthenticatorException e) {
					Dog.e(e, "Failed to delete the account... :(");
					callback.onFailure();
				}
			}, null);
		} else {
			manager.removeAccount(getAccount(manager), future -> {
				try {
					if (future.getResult()) {
						callback.onSuccess();
					}
				} catch (OperationCanceledException | IOException | AuthenticatorException e) {
					Dog.e(e, "Failed to delete the account... :(");
					callback.onFailure();
				}
			}, null);
		}
	}

	public interface AccountRemovedCallback {
		void onSuccess();

		void onFailure();
	}
}