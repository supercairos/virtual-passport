package io.romain.passport.logic.observables.auth;

import android.app.Activity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import io.romain.passport.utils.Dog;
import rx.Observable;
import rx.Subscriber;

public class FirebaseRegisterObservable implements Observable.OnSubscribe<AuthResult> {

	private final Activity mContext;
	private final String mUser;
	private final String mPassword;

	public static Observable<AuthResult> create(Activity context, String user, String password) {
		return Observable.create(new FirebaseRegisterObservable(context, user, password));
	}

	private FirebaseRegisterObservable(Activity context, String user, String password) {
		this.mContext = context;
		this.mUser = user;
		this.mPassword = password;
	}

	@Override
	public void call(Subscriber<? super AuthResult> subscriber) {
		subscriber.onStart();
		FirebaseAuth.getInstance()
				.createUserWithEmailAndPassword(mUser, mPassword)
				.addOnCompleteListener(mContext, task -> {
					Dog.d("signInWithEmail:onComplete:" + task.isSuccessful());

					// If sign in fails, display a message to the user. If sign in succeeds
					// the auth state listener will be notified and logic to handle the
					// signed in user can be handled in the listener.
					if (!task.isSuccessful()) {
						subscriber.onError(task.getException());
					} else {
						subscriber.onNext(task.getResult());
						subscriber.onCompleted();
					}
				});
	}
}
