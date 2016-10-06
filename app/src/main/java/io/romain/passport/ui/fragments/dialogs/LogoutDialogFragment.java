package io.romain.passport.ui.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.io.File;

import io.romain.passport.R;
import io.romain.passport.data.sources.local.PassportContentProvider;
import io.romain.passport.ui.LandingActivity;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.PlayServicesUtils;

public class LogoutDialogFragment extends BaseDialogFragment {

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Handle Logout;
		final Context context = getActivity().getApplicationContext();
		return new AlertDialog.Builder(getActivity())
				.setMessage(R.string.logout_message)
				.setTitle(R.string.logout_title)
				.setPositiveButton(android.R.string.ok, (dialog, id) -> {
					xxx
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create();
	}

	private static class CleanupThread extends AsyncTask<Void, Void, Boolean> {
		private final Context mContext;

		public CleanupThread(Context context) {
			this.mContext = context;
		}

		@Override
		protected Boolean doInBackground(Void... param) {
			Dog.d("Cleaning up the shit this user made MOFO!");
			// Keep order because of foreign keys
			mContext.getContentResolver().delete(PassportContentProvider.Cities.CONTENT_URI, null, null);
			PlayServicesUtils.forgetRegistrationId(mContext);

			return trimCache(mContext);
		}


		@Override
		protected void onPostExecute(Boolean success) {
			if (success) {
				LandingActivity.start(mContext);
			}
		}

		public static boolean trimCache(Context context) {
			File dir = context.getCacheDir();
			if (dir != null && dir.isDirectory()) {
				String[] children = dir.list();
				for (String child : children) {
					File f = new File(dir, child);
					if (f.isDirectory()) {
						return deleteDir(new File(dir, child));
					} else if (f.isFile()) {
						if (!f.delete()) {
							Dog.e("Could not delete file : %s", f);
							return false;
						}
					}

				}
			}

			// Nothing to delete
			return true;
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
