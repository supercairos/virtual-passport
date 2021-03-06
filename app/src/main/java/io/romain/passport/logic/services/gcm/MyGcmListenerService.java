package io.romain.passport.logic.services.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import io.romain.passport.R;
import io.romain.passport.ui.LandingActivity;
import io.romain.passport.utils.Dog;

public class MyGcmListenerService extends GcmListenerService {

	private static final int NOTIFICATION_ID = 1;

	/**
	 * Called when message is received.
	 *
	 * @param from SenderID of the sender.
	 * @param data Data bundle containing message data as key/value pairs.
	 *             For Set of keys use data.keySet().
	 */
	// [START receive_message]
	@Override
	public void onMessageReceived(String from, Bundle data) {
		Dog.d("From: " + from);
		Dog.d("Data: " + data);


		/**
		 * Production applications would usually process the message here.
		 * Eg: - Syncing with server.
		 *     - Store message in local database.
		 *     - Update UI.
		 */

		/**
		 * In some cases it may be useful to show a notification indicating to the user
		 * that a message was received.
		 */

		sendNotification(data.getString("message"));
	}
	// [END receive_message]

	/**
	 * Create and show a simple notification containing the received GCM message.
	 *
	 * @param message GCM message received.
	 */
	private void sendNotification(String message) {
		Intent intent = new Intent(this, LandingActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(android.R.drawable.ic_menu_add)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(message)
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setVibrate(new long[]{100})
				.setAutoCancel(true)
				.setSound(defaultSoundUri)
				.setContentIntent(pendingIntent);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID /* ID of notification */, notificationBuilder.build());
	}
}
