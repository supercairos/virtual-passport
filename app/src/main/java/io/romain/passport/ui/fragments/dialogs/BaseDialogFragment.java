package io.romain.passport.ui.fragments.dialogs;

import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import javax.inject.Inject;

import io.romain.passport.MyApplication;
import io.romain.passport.logic.helpers.SharedPrefHelper;
import retrofit2.Retrofit;


public abstract class BaseDialogFragment extends DialogFragment {

	@Inject
	public SharedPrefHelper mSharedPref;

	@Inject
	public Retrofit mRetrofit;

	@Inject
	AccountManager mAccountManager;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		MyApplication.getApplication(getContext()).getApplicationComponent().inject(this);
	}
}
