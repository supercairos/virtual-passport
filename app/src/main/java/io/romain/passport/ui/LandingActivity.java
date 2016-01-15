package io.romain.passport.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.OnClick;
import io.romain.passport.R;
import io.romain.passport.logic.helpers.AccountHelper;
import io.romain.passport.logic.helpers.UserHelper;
import io.romain.passport.ui.views.SvgLogoView;
import io.romain.passport.utils.Dog;

public class LandingActivity extends BaseActivity {

	private final Handler mHandler = new Handler();

	@Bind(R.id.background)
	ImageView mBackground;

	@Bind(R.id.background_blur)
	ImageView mBackgroundBlur;

	@Bind(R.id.svg_animated_logo)
	SvgLogoView mLogoView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String token = AccountHelper.peekToken(mAccountManager);
		if (!TextUtils.isEmpty(token)) {
			Dog.d("Auto login : %s", token.substring(0, token.length() < 10 ? token.length() : 10));
			UserHelper.next(this);
		}

		setContentView(R.layout.activity_landing);
		mLogoView.setOnStateChangeListenerListener(state -> {
			switch (state) {
				case SvgLogoView.STATE_FILL_STARTED:
					ObjectAnimator a4 = ObjectAnimator.ofFloat(mBackgroundBlur, View.ALPHA, 1);
					ObjectAnimator a5 = ObjectAnimator.ofFloat(mBackground, View.ALPHA, 0);

					AnimatorSet set = new AnimatorSet();
					set.setDuration(1500).play(a4).with(a5);
					set.addListener(new Animator.AnimatorListener() {
						@Override
						public void onAnimationStart(Animator animation) {
							mBackground.setVisibility(View.VISIBLE);
							mBackground.setAlpha(1.0f);

							mBackgroundBlur.setVisibility(View.VISIBLE);
							mBackgroundBlur.setAlpha(0.0f);
						}

						@Override
						public void onAnimationEnd(Animator animation) {
							mBackground.setVisibility(View.GONE);
						}

						@Override
						public void onAnimationCancel(Animator animation) {

						}

						@Override
						public void onAnimationRepeat(Animator animation) {

						}
					});
					set.start();
					break;
			}
		});
	}


	@Override
	protected void onStart() {
		super.onStart();
		mHandler.postDelayed(this::start, 500);
	}


	private void start() {
		mBackground.setVisibility(View.VISIBLE);
		mBackground.setAlpha(1.0f);
		mBackgroundBlur.setVisibility(View.GONE);

		mLogoView.start();
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
