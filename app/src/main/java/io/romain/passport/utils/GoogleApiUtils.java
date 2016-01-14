package io.romain.passport.utils;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class GoogleApiUtils {

	public static boolean isGooglePlayServicesAvailable(Context context) {
		int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
		return result == ConnectionResult.SUCCESS || result == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;
	}

}
