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
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import io.romain.passport.model.User;
import io.romain.passport.ui.MainActivity;
import io.romain.passport.utils.constants.AccountConstants;
import io.romain.passport.utils.constants.AuthenticatorConstants;

public class UserHelper {

	@SuppressWarnings("deprecation")
	public static void save(Activity context, User user) {
		AccountManager manager = AccountManager.get(context);
		Account[] accounts = manager.getAccountsByType(AuthenticatorConstants.ACCOUNT_TYPE);
		for (Account account : accounts) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
				manager.removeAccountExplicitly(account);
			} else {
				manager.removeAccount(account, null, null);
			}
		}

		Account myAccount = new Account(user.email, AuthenticatorConstants.ACCOUNT_TYPE);

		Bundle data = new Bundle();
		data.putString(AccountConstants.KEY_SERVER_ID, user.id);
		data.putString(AccountConstants.KEY_NAME, user.name);
		data.putString(AccountConstants.KEY_PROFILE_PICTURE, user.picture != null ? user.picture.toString() : null);

		manager.addAccountExplicitly(myAccount, user.password, data);
		manager.setAuthToken(myAccount, AuthenticatorConstants.AUTH_TOKEN_TYPE_FULL, user.token);

		UserHelper.next(context);
	}

	public static void next(Activity context) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
		context.finish();
	}
}
