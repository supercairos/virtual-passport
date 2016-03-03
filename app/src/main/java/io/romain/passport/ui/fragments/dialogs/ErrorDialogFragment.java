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
package io.romain.passport.ui.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import io.romain.passport.R;

public class ErrorDialogFragment extends BaseDialogFragment {

	private String mMessage;

	public ErrorDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	private static final String ARG_MESSAGE_DIALOG = "title";

	public static ErrorDialogFragment newInstance(Context c, int string) {
		return newInstance(c.getString(string));
	}

	public static ErrorDialogFragment newInstance(String title) {
		ErrorDialogFragment frag = new ErrorDialogFragment();
		Bundle args = new Bundle();
		args.putString(ARG_MESSAGE_DIALOG, title);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMessage = getArguments().getString("title");
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.error_dialog_title)
				.setMessage(mMessage)
				.setPositiveButton(android.R.string.ok, (dialog, which) -> {
					dialog.dismiss();
				})
				.create();
	}


}
