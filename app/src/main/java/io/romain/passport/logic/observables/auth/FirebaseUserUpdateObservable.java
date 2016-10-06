package io.romain.passport.logic.observables.auth;

import android.app.Activity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import io.romain.passport.data.Profile;
import rx.Observable;
import rx.Subscriber;

public class FirebaseUserUpdateObservable implements Observable.OnSubscribe<FirebaseUser> {

	private final Activity mContext;
	private final Profile mProfile;

	public static Observable<FirebaseUser> create(Activity context, Profile profile) {
		return Observable.create(new FirebaseUserUpdateObservable(context, profile));
	}

	private FirebaseUserUpdateObservable(Activity context, Profile profile) {
		this.mContext = context;
		this.mProfile = profile;
	}

	@Override
	public void call(Subscriber<? super FirebaseUser> subscriber) {
		subscriber.onStart();

		UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
				.setDisplayName(mProfile.name())
				.setPhotoUri(mProfile.picture())
				.build();

		FirebaseAuth.getInstance()
				.getCurrentUser()
				.updateProfile(profile)
				.addOnCompleteListener(task -> {
					if (!task.isSuccessful()) {
						subscriber.onError(task.getException());
					} else {
						subscriber.onNext(FirebaseAuth.getInstance().getCurrentUser());
						subscriber.onCompleted();
					}
				});
	}
}
