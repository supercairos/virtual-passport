package io.romain.passport.logic.modules;

import com.google.firebase.auth.FirebaseAuth;

import dagger.Module;
import dagger.Provides;

@Module
public class FirebaseAuthModule {

	@Provides
	FirebaseAuth get() {
		return FirebaseAuth.getInstance();
	}

}
