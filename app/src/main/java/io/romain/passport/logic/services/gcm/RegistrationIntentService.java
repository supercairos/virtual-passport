package io.romain.passport.logic.services.gcm;

import android.accounts.Account;
import android.content.Intent;
import android.text.TextUtils;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import io.romain.passport.BuildConfig;
import io.romain.passport.logic.helpers.AccountHelper;
import io.romain.passport.logic.services.BaseIntentService;
import io.romain.passport.model.User;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.PlayServicesUtils;
import retrofit2.Response;

public class RegistrationIntentService extends BaseIntentService {

	private static final String TAG = "RegIntentService";
	private static final String[] TOPICS = {
			"global"
	};

	public RegistrationIntentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Account account = AccountHelper.getAccount(mAccountManager);
		if(account == null) {
			return;
		}

		try {
			// In the (unlikely) event that multiple refresh operations occur simultaneously,
			// ensure that they are processed sequentially.
			synchronized (TAG) {
				// [START register_for_gcm]
				// Initially this call goes out to the network to retrieve the token, subsequent calls
				// are local.
				// [START get_token]
				InstanceID instanceID = InstanceID.getInstance(this);
				String token = instanceID.getToken(BuildConfig.GCM_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
				// [END get_token]
				Dog.i("GCM Registration Token: " + token);

				sendRegistrationToServer(token);

				// Subscribe to topic channels
				subscribeTopics(token);

				// You should store a boolean that indicates whether the generated token has been
				// sent to your server. If the boolean is false, send the token to your server,
				// otherwise your server should have already received the token.
				PlayServicesUtils.storeRegistrationId(this, token);
				// [END register_for_gcm]
			}
		} catch (Exception e) {
			Dog.d(e, "Failed to complete token refresh");
			// If an exception happens while fetching the new token or updating our registration data
			// on a third-party server, this ensures that we'll attempt the update at a later time.
			PlayServicesUtils.forgetRegistrationId(this);
		}
	}

	/**
	 * Persist registration to third-party servers.
	 * <p>
	 * Modify this method to associate the user's GCM registration token with any server-side account
	 * maintained by your application.
	 *
	 * @param regid The new token.
	 */
	private void sendRegistrationToServer(String regid) {
		Dog.d("Got token : %s", regid);
		try {
			String userId = User.getServerId(mAccountManager);
			if (!TextUtils.isEmpty(userId)) {
				Response<User> user = mRetrofit.create(User.UserService.class).registerGcm(regid).execute();
				if (user.isSuccessful()) {
					Dog.d("GCM Register completed! (" + user + ")");
				}
			}
		} catch (IOException e) {
			Dog.e(e, "Error !");
		}
	}

	/**
	 * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
	 *
	 * @param token GCM token
	 * @throws IOException if unable to reach the GCM PubSub service
	 */
	// [START subscribe_topics]
	private void subscribeTopics(String token) throws IOException {
		for (String topic : TOPICS) {
			GcmPubSub pubSub = GcmPubSub.getInstance(this);
			pubSub.subscribe(token, "/topics/" + topic, null);
		}
	}
	// [END subscribe_topics]
}