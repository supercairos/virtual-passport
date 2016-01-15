/*
 *    Copyright 2016 Romain
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.romain.passport.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.OnClick;
import io.romain.passport.R;
import io.romain.passport.model.City;
import io.romain.passport.model.database.PassportContentProvider;
import io.romain.passport.ui.adaptater.CitySearchAdapter;
import io.romain.passport.ui.transitions.FabDialogMorphSetup;
import io.romain.passport.ui.views.LocationAutocompleteTextView;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.SimpleTextWatcher;

public class AddCityActivity extends BaseActivity {

	@Bind(R.id.dialog_container)
	ViewGroup mContainer;

	@Bind(R.id.add_city_city)
	LocationAutocompleteTextView mEditText;

	@Bind(R.id.add_city_ok)
	Button mValidateButton;

	private boolean isDismissing = false;

	private City mCity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_city);
		FabDialogMorphSetup.setupSharedEelementTransitions(
				this,
				mContainer,
				getResources().getDimensionPixelSize(R.dimen.dialog_corners)
		);

		mEditText.setListener(text -> {
			Toast.makeText(AddCityActivity.this, "Icon touched", Toast.LENGTH_LONG).show();
			return true;
		});

		final CitySearchAdapter adapter = new CitySearchAdapter(this);
		mEditText.setAdapter(adapter);
		mEditText.setOnItemClickListener((parent, view, position, id) -> mCity = adapter.getItem(position));
		mEditText.setThreshold(2);
		mEditText.addTextChangedListener(new SimpleTextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				mValidateButton.setEnabled(s.length() > 0);
			}
		});
	}

	@OnClick({R.id.dialog_outer_frame, R.id.add_city_cancel})
	void dismiss() {
		dismiss(Activity.RESULT_CANCELED);
	}

	private void dismiss(int result) {
		isDismissing = true;
		setResult(result);
		finishAfterTransition();
	}

	@OnClick(R.id.add_city_ok)
	public void onValidate() {
		if (mCity != null) {
			(new SaverThread(this, mCity)).start();
			dismiss(Activity.RESULT_OK);
		}
	}

	@Override
	public void onBackPressed() {
		dismiss();
	}

	private static class SaverThread extends Thread {

		private final City mCity;
		private final Context mContext;

		public SaverThread(Context context, City city) {
			mContext = context.getApplicationContext();
			mCity = city;
		}

		@Override
		public void run() {
			ContentResolver resolver = mContext.getContentResolver();
			Uri row = resolver.insert(PassportContentProvider.Cities.CONTENT_URI, mCity.toContentValues());
			Dog.d("Inserted : " + row);
		}
	}
}