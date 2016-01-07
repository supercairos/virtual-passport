package io.romain.passport.logic.services.account;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;

import io.romain.passport.utils.Dog;


/**
 * Define a Service that returns an IBinder for the
 * sync adapter class, allowing the sync adapter framework to call
 * onPerformSync().
 */
public class SyncService extends Service {

	// Storage for an instance of the sync adapter
	private static SyncAdapter sSyncAdapter = null;
	// Object to use as a thread-safe lock
	private static final Object sSyncAdapterLock = new Object();

	/*
	 * Instantiate the sync adapter object.
	 */
	@Override
	public void onCreate() {
		/*
		 * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
		synchronized (sSyncAdapterLock) {
			if (sSyncAdapter == null) {
				sSyncAdapter = new SyncAdapter(getApplicationContext(), true, false);
			}
		}
	}

	/**
	 * Return an object that allows the system to invoke
	 * the sync adapter.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		/*
		 * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
		return sSyncAdapter.getSyncAdapterBinder();
	}

	private static class SyncAdapter extends AbstractThreadedSyncAdapter {

		/**
		 * Set up the sync adapter. This form of the
		 * constructor maintains compatibility with Android 3.0
		 * and later platform versions
		 */
		public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
			super(context, autoInitialize, allowParallelSyncs);
		}

		@Override
		public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
//			try {
//
//			} catch (IOException e) {
//				onError();
//				Dog.e(e, "IOException");
//				syncResult.stats.numIoExceptions++;
//			} catch (NetworkException e) {
//				onError();
//				Dog.e(e, "LoginException");
//				syncResult.stats.numAuthExceptions++;
//			} catch (final RetrofitError e) {
//				onError();
//				Dog.e(e, "RetrofitError");
//				if (e.getKind() == RetrofitError.Kind.CONVERSION) {
//					Dog.e("RetrofitError kind RetrofitError.Kind.CONVERSION");
//					syncResult.stats.numParseExceptions++;
//				} else if (e.getKind() == RetrofitError.Kind.HTTP) {
//					Dog.e("RetrofitError kind RetrofitError.Kind.HTTP");
//					syncResult.stats.numAuthExceptions++;
//				} else if (e.getKind() == RetrofitError.Kind.NETWORK) {
//					Dog.e("RetrofitError kind RetrofitError.Kind.NETWORK");
//					syncResult.stats.numIoExceptions++;
//				}
//			}

			Dog.d("onPerformSync(" + account.name + ")");
		}

		private void onError() {

		}
	}

}
