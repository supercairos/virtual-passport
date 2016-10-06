package io.romain.passport.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.OnClick;
import io.romain.passport.R;
import io.romain.passport.utils.GoogleApiUtils;
import io.romain.passport.utils.SimpleAnimatorListener;

public class LandingActivity extends FirebaseAuthActivity {

	private static final long ICON_DISOLVE_DURATION = 500;
	private static final long FADE_IN_DURATION = 300;
	private static final long CROSSFADE_DURATION = 500;

	@BindView(R.id.content_root_view)
	FrameLayout mRootView;
	@BindView(R.id.landing_splash_screen)
	ViewGroup mSplashScreen;
	@BindView(R.id.landing_button_screen)
	ViewGroup mButtonScreen;
	@BindView(R.id.landing_icon)
	ImageView mLandingIcon;
	@BindView(R.id.landing_button_layout)
	ViewGroup mLandingButtonLayout;

	public static void start(Context context) {
		Intent intent = new Intent(context, LandingActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Check if user is already logged in
		if (!GoogleApiUtils.isGooglePlayServicesAvailable(this)) {
			Toast.makeText(this, getString(R.string.common_google_play_services_install_text_phone, getString(R.string.app_name)), Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		setTheme(R.style.Passport_Home);
		setContentView(R.layout.activity_landing);
	}

	@Override
	void onUserSignedIn(FirebaseUser user) {
		MainActivity.start(this);
	}

	@Override
	void onUserSignedOut() {
		// Black
		ObjectAnimator a1 = ObjectAnimator.ofFloat(mLandingIcon, View.SCALE_X, 0.0f).setDuration(ICON_DISOLVE_DURATION);
		ObjectAnimator a2 = ObjectAnimator.ofFloat(mLandingIcon, View.SCALE_Y, 0.0f).setDuration(ICON_DISOLVE_DURATION);
		ObjectAnimator a3 = ObjectAnimator.ofFloat(mButtonScreen, View.ALPHA, 1.0f).setDuration(FADE_IN_DURATION);
		a3.setStartDelay(ICON_DISOLVE_DURATION - FADE_IN_DURATION);

		ObjectAnimator a4 = ObjectAnimator.ofFloat(mSplashScreen, View.ALPHA, 0.0f).setDuration(FADE_IN_DURATION);
		a4.setStartDelay(ICON_DISOLVE_DURATION - FADE_IN_DURATION);

		// Rest
		ObjectAnimator a6 = ObjectAnimator.ofFloat(mLandingButtonLayout, View.TRANSLATION_Y, 0.0f).setDuration(CROSSFADE_DURATION);
		ObjectAnimator a7 = ObjectAnimator.ofFloat(mLandingButtonLayout, View.ALPHA, 1.0f).setDuration(CROSSFADE_DURATION);

		final AnimatorSet animator = new AnimatorSet();
		animator.playTogether(a6, a7);

		final AnimatorSet set = new AnimatorSet();
		set.playTogether(a1, a2, a3, a4);
		set.addListener(new SimpleAnimatorListener() {

			@Override
			public void onAnimationEnd(Animator animation) {
				mSplashScreen.setVisibility(View.GONE);
				animator.start();
			}
		});

		mRootView.postDelayed(set::start, 1500);
	}

	@OnClick(R.id.button_register)
	void onButtonRegisterClicked() {
		startActivity(new Intent(this, RegisterActivity.class));
	}

	@OnClick(R.id.button_login)
	void onButtonLoginClicked() {
		startActivity(new Intent(this, LoginActivity.class));
	}

}
