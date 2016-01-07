package io.romain.passport.utils.loaders;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import io.romain.passport.utils.Dog;

public abstract class ProfileLoader implements LoaderManager.LoaderCallbacks<Cursor> {

	private Context mContext;

	public ProfileLoader(Context context) {
		this.mContext = context.getApplicationContext();
	}

	/**
	 * Column index for the email address in the profile query results
	 */
	private static final int EMAIL = 0;
	private static final int EMAIL_IS_PRIMARY = 1;

	/**
	 * Column index for the family name in the profile query results
	 */
	private static final int FAMILY_NAME = 2;
	/**
	 * Column index for the given name in the profile query results
	 */
	private static final int GIVEN_NAME = 3;
	/**
	 * Column index for the phone number in the profile query results
	 */
	private static final int PHONE_NUMBER = 4;
	private static final int PHONE_IS_PRIMARY = 5;

	/**
	 * Column index for the photo in the profile query results
	 */
	private static final int PHOTO = 6;
	/**
	 * Column index for the MIME type in the profile query results
	 */
	private static final int MIME_TYPE = 7;


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(mContext,
				Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
				new String[]{
						ContactsContract.CommonDataKinds.Email.ADDRESS,
						ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
						ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
						ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
						ContactsContract.CommonDataKinds.Phone.NUMBER,
						ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
						ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
						ContactsContract.Contacts.Data.MIMETYPE
				},
				ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
						+ ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
						+ ContactsContract.Contacts.Data.MIMETYPE + "=?",
				new String[]{
						ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
						ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
						ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
				}, ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor != null) {
			try {
				String mime;
				boolean isPrimary;
				UserProfile profile = new UserProfile();
				while (cursor.moveToNext()) {
					mime = cursor.getString(MIME_TYPE);
					switch (mime) {
						case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE:
							isPrimary = cursor.getInt(EMAIL_IS_PRIMARY) > 0;
							if (TextUtils.isEmpty(profile.email) || isPrimary) {
								profile.email = cursor.getString(EMAIL);
							}
							break;
						case ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
							if (TextUtils.isEmpty(profile.givenName)) {
								profile.givenName = cursor.getString(GIVEN_NAME);
							}

							if (TextUtils.isEmpty(profile.familyName)) {
								profile.familyName = cursor.getString(FAMILY_NAME);
							}
							break;
						case ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE:
							if (profile.photo == null) {
								profile.photo = Uri.parse(cursor.getString(PHOTO));
							}
							break;
					}

				}

				if (TextUtils.isEmpty(profile.email)) {
					// Try to get the email from the google account name;
					profile.email = getGoogleEmail();
				}

				onProfileLoaded(profile);
			} catch (final Exception e) {
				Dog.e(e, "Exception");
			}
		}
	}

	private String getGoogleEmail() {
		AccountManager accountManager = AccountManager.get(mContext);
		Account account = getAccount(accountManager);
		if (account == null) {
			return null;
		} else {
			return account.name;
		}
	}

	private static Account getAccount(AccountManager accountManager) {
		Account[] accounts = accountManager.getAccountsByType("com.google");
		Account account;
		if (accounts.length > 0) {
			account = accounts[0];
		} else {
			account = null;
		}
		return account;
	}

	protected abstract void onProfileLoaded(UserProfile profile);

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	public static class UserProfile {

		/**
		 * The primary email address
		 */
		public String email;

		/**
		 * The primary name
		 */
		public String givenName;

		/**
		 * The primary name
		 */
		public String familyName;

		/**
		 * A possible photo for the user
		 */
		public Uri photo;

		@Override
		public String toString() {
			return "UserProfile{" +
					"email='" + email + '\'' +
					", givenName='" + givenName + '\'' +
					", familyName='" + familyName + '\'' +
					", photo=" + photo +
					'}';
		}
	}
}