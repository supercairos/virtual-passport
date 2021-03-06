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
package io.romain.passport.logic.helpers;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import io.romain.passport.logic.services.account.AuthenticatorService;
import io.romain.passport.model.User;
import io.romain.passport.ui.MainActivity;

public class UserHelper {

	@SuppressWarnings("deprecation")
	public static void save(Activity context, User user, String password) {
		AccountManager manager = AccountManager.get(context);
		Account[] accounts = manager.getAccountsByType(AuthenticatorService.ACCOUNT_TYPE);
		for (Account account : accounts) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
				manager.removeAccountExplicitly(account);
			} else {
				manager.removeAccount(account, null, null);
			}
		}

		Account myAccount = new Account(user.email(), AuthenticatorService.ACCOUNT_TYPE);

		Bundle data = new Bundle();
		data.putString(User.KEY_SERVER_ID, user.id());
		data.putString(User.KEY_NAME, user.name());
		data.putString(User.KEY_PROFILE_PICTURE, user.picture() != null ? user.picture().toString() : null);

		manager.addAccountExplicitly(myAccount, password, data);
		manager.setAuthToken(myAccount, AuthenticatorService.AUTH_TOKEN_TYPE_FULL, user.token());

		MainActivity.start(context);
	}
}
