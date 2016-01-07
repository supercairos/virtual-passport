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
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.OnClick;
import io.romain.passport.R;
import io.romain.passport.ui.transitions.FabDialogMorphSetup;
import io.romain.passport.ui.views.LocationAutocompleteTextView;

public class AddCityActivity extends BaseActivity {

	@Bind(R.id.dialog_container)
	ViewGroup mContainer;

	@Bind(R.id.add_city_city)
	LocationAutocompleteTextView mEditText;

	boolean isDismissing = false;

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
	}

	@OnClick(R.id.dialog_outer_frame)
	public void dismiss() {
		isDismissing = true;
		setResult(Activity.RESULT_CANCELED);
		finishAfterTransition();
	}

	@Override
	public void onBackPressed() {
		dismiss();
	}
}
