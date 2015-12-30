package io.romain.passport.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.romain.virtualpassport.R;

import butterknife.Bind;
import io.romain.passport.model.User;
import io.romain.passport.ui.views.SvgLogoView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class LandingActivity extends BaseActivity {

	private Handler mHandler = new Handler();

	@Bind(R.id.background)
	ImageView mBackground;

	@Bind(R.id.background_blur)
	ImageView mBackgroundBlur;

	@Bind(R.id.svg_animated_logo)
	SvgLogoView mLogoView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		String token = AccountUtils.peekToken();
//		if (!TextUtils.isEmpty(token)) {
//			Dog.d("Auto login : %s", token);
//			gotoMain();
//		}

		setContentView(R.layout.activity_landing);
		mLogoView.setOnStateChangeListenerListener(state -> {
			switch (state) {
				case SvgLogoView.STATE_FILL_STARTED:
					// Bug in older versions where set.setInterpolator didn't work
					AnimatorSet set = new AnimatorSet();
					Interpolator interpolator = new DecelerateInterpolator();
					ObjectAnimator a1 = ObjectAnimator.ofFloat(mLogoView, View.TRANSLATION_Y, 0);

					ObjectAnimator a4 = ObjectAnimator.ofFloat(mBackgroundBlur, View.ALPHA, 1);
					ObjectAnimator a5 = ObjectAnimator.ofFloat(mBackground, View.ALPHA, 0);

					a1.setInterpolator(interpolator);

					set.setDuration(500).playTogether(a1, a4, a5);
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

		mLogoView.setOnLongClickListener(v -> {

			User.UserService service = mRetrofitService.create(User.UserService.class);
			Call<User> user = service.register(new User("super.cairos@gmail.com", "Romain Caire", "romain22"));
			user.enqueue(new Callback<User>() {
				@Override
				public void onResponse(Response<User> response, Retrofit retrofit) {
					if (response.isSuccess()) {
						Toast.makeText(LandingActivity.this, "Registration success", Toast.LENGTH_LONG).show();
					}
				}

				@Override
				public void onFailure(Throwable t) {
					Toast.makeText(LandingActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
				}
			});

			return true;
		});


	}

	@Override
	protected void onStart() {
		super.onStart();
		mHandler.postDelayed(this::start, 500);
	}

	private void start() {
		mLogoView.start();

		mBackground.setVisibility(View.VISIBLE);
		mBackground.setAlpha(1.0f);
		mBackgroundBlur.setVisibility(View.GONE);
	}

	private void gotoMain() {
//		Intent intent = new Intent(this, MainActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		startActivity(intent);
//		finish();
	}
}
