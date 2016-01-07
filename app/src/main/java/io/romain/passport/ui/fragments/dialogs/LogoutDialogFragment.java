package io.romain.passport.ui.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.io.File;

import io.romain.passport.R;
import io.romain.passport.logic.helpers.AccountHelper;
import io.romain.passport.model.database.PassportContentProvider;
import io.romain.passport.ui.LandingActivity;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.PlayServicesUtils;

public class LogoutDialogFragment extends BaseDialogFragment {

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Handle Logout;
		final Context context = getContext().getApplicationContext();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		return builder.setMessage(R.string.logout_message)
				.setTitle(R.string.logout_title)
				.setPositiveButton(android.R.string.ok, (dialog, id) -> {
					AccountHelper.removeAccount(mAccountManager, new AccountHelper.AccountRemovedCallback() {
						@Override
						public void onSuccess() {
							Intent intent = new Intent(context, LandingActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(intent);
							new CleanupThread(context).start();
						}

						@Override
						public void onFailure() {
							Toast.makeText(getActivity(), "Could not remove this account", Toast.LENGTH_LONG).show();
						}
					});
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create();
	}

	private static class CleanupThread extends Thread {
		private final Context mContext;

		public CleanupThread(Context context) {
			super("CleanupThread");
			this.mContext = context.getApplicationContext();
		}

		@Override
		public void run() {
			Dog.d("Cleaning up the shit this user made MOFO!");
			// Keep order because of foreign keys
			mContext.getContentResolver().delete(PassportContentProvider.Cities.CONTENT_URI, null, null);

			PlayServicesUtils.forgetRegistrationId(mContext);
			trimCache(mContext);
		}

		public static void trimCache(Context context) {
			File dir = context.getCacheDir();
			if (dir != null && dir.isDirectory()) {
				String[] children = dir.list();
				for (String child : children) {
					File f = new File(dir, child);
					if (f.isDirectory()) {
						deleteDir(new File(dir, child));
					} else if (f.isFile()) {
						if (!f.delete()) {
							Dog.e("Could not delete file : %s", f);
						}
					}

				}
			}
		}

		public static boolean deleteDir(File dir) {
			if (dir != null && dir.isDirectory()) {
				String[] children = dir.list();
				for (String child : children) {
					boolean success = deleteDir(new File(dir, child));
					if (!success) {
						return false;
					}
				}
			}

			// The directory is now empty so delete it
			return dir != null && dir.delete();
		}
	}
}
