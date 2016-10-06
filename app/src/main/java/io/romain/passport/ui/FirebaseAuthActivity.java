package io.romain.passport.ui;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.romain.passport.utils.Dog;

public abstract class FirebaseAuthActivity extends BaseActivity {

	private final FirebaseAuth.AuthStateListener mAuthListener = auth -> {
		FirebaseUser user = auth.getCurrentUser();
		if (user != null) {
			// User is signed in
			Dog.d("onAuthStateChanged:signed_in:" + user.getUid());
			onUserSignedIn(user);
		} else {
			// User is signed out
			Dog.d("onAuthStateChanged:signed_out");
			onUserSignedOut();
		}
	};

	abstract void onUserSignedOut();

	abstract void onUserSignedIn(FirebaseUser user);

	@Override
	public void onStart() {
		super.onStart();
		mAuth.addAuthStateListener(mAuthListener);
	}

	@Override
	public void onStop() {
		super.onStop();
		mAuth.removeAuthStateListener(mAuthListener);
	}
}
