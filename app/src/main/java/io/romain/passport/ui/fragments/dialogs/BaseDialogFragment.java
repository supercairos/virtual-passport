package io.romain.passport.ui.fragments.dialogs;

import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.romain.passport.MyApplication;
import io.romain.passport.logic.helpers.SharedPrefHelper;
import retrofit.Retrofit;


public abstract class BaseDialogFragment extends DialogFragment {

	@Inject
	public SharedPrefHelper mSharedPref;

	@Inject
	public Retrofit mRetrofit;

	@Inject
	public AccountManager mAccountManager;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this, view);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		((MyApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
	}
}