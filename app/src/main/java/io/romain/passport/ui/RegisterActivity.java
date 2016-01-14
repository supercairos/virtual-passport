package io.romain.passport.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import io.romain.passport.R;
import io.romain.passport.logic.helpers.UserHelper;
import io.romain.passport.model.User;
import io.romain.passport.ui.fragments.dialogs.ErrorDialogFragment;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.PathUtils;
import io.romain.passport.utils.SimpleTextWatcher;
import io.romain.passport.utils.UriRequestBody;
import io.romain.passport.utils.glide.CircleTransform;
import io.romain.passport.utils.loaders.ProfileLoader;
import io.romain.passport.utils.validators.EmailValidator;
import io.romain.passport.utils.validators.PasswordValidator;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RegisterActivity extends BaseActivity {

	private static final int SELECT_PROFILE_REQUEST_CODE = 1337;
	private static final int LOADER_ID = 1338;

	@Bind(R.id.profile_picture)
	ImageView mProfilePicture;

	@Bind(R.id.edit_name)
	EditText mName;
	@Bind(R.id.edit_name_layout)
	TextInputLayout mNameLayout;

	@Bind(R.id.edit_email)
	EditText mEmail;
	@Bind(R.id.edit_email_layout)
	TextInputLayout mEmailLayout;

	@Bind(R.id.edit_password)
	EditText mPassword;
	@Bind(R.id.edit_password_layout)
	TextInputLayout mPasswordLayout;

	@Bind(R.id.action_bar)
	Toolbar mActionBar;

	@Bind(R.id.register_button_register)
	Button mRegisterButton;

	ProgressDialog mDialog;

	private Uri mProfilePictureUri;
	private Uri mOutputFileUri;

	private BitmapTransformation mCircleTransform;
	private Subscription mSubscriber;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		setSupportActionBar(mActionBar);
		//noinspection ConstantConditions
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mCircleTransform = new CircleTransform(this);
		mName.addTextChangedListener(mRegisterFieldWatcher);
		mEmail.addTextChangedListener(mRegisterFieldWatcher);
		mPassword.addTextChangedListener(mRegisterFieldWatcher);
		mPassword.setOnEditorActionListener((v, actionId, event) -> {
			switch (actionId) {
				case EditorInfo.IME_ACTION_DONE:
					onValidateClicked();
					return true;
			}

			return false;
		});

		getSupportLoaderManager().initLoader(LOADER_ID, null, new ProfileLoader(this) {
			@Override
			protected void onProfileLoaded(UserProfile profile) {
				mEmail.setText(profile.email);
				mName.setText(getString(R.string.register_display_name, profile.givenName, profile.familyName));

				if (mProfilePictureUri == null) {
					mProfilePictureUri = profile.photo;
					if (profile.photo != null) {
						Glide.with(RegisterActivity.this)
								.load(mProfilePictureUri)
								.centerCrop()
								.transform(mCircleTransform)
								.into(mProfilePicture);
					}
				}

				mPassword.requestFocus();
				mRegisterButton.setEnabled(isRegisterValid());
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@OnClick(R.id.register_button_register)
	public void onValidateClicked() {
		String name = mName.getEditableText().toString();
		String email = mEmail.getEditableText().toString();
		String password = mPassword.getEditableText().toString();

		boolean isEverythingOk = true;
		if (!EmailValidator.getInstance().validate(email)) {
			mEmailLayout.setError(getString(R.string.error_email));
			mEmail.requestFocus();
			isEverythingOk = false;
		} else {
			mEmailLayout.setError(null);
		}

		if (!PasswordValidator.isAcceptablePassword(password)) {
			mPasswordLayout.setError(getString(R.string.error_password));
			mPassword.requestFocus();
			isEverythingOk = false;
		} else {
			mPasswordLayout.setError(null);
		}

		if (TextUtils.isEmpty(name)) {
			mNameLayout.setError(getString(R.string.error_name));
			mName.requestFocus();
			isEverythingOk = false;
		} else {
			mNameLayout.setError(null);
		}

		if (isEverythingOk) {
			// Register
			mDialog = ProgressDialog.show(this, getString(R.string.register), getString(R.string.please_wait), true);
			mDialog.show();
			User.UserService service = mRetrofit.create(User.UserService.class);

			mSubscriber = service.register(new User(name, email, password, mProfilePictureUri))
					.flatMap(user -> service.upload("Bearer " + user.token, new UriRequestBody(getContentResolver(), mProfilePictureUri)))
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.doOnError(throwable -> Dog.e(throwable, "Error... :'("))
					.subscribe(
							// onNext();
							user -> UserHelper.save(RegisterActivity.this, user),
							// onError();
							user -> ErrorDialogFragment.newInstance(getString(R.string.unknown_error)).show(getSupportFragmentManager(), "Dialog")
					);
		}
	}

	@OnClick(R.id.profile_picture)
	public void onProfilePictureClicked() {
		startImageIntent(SELECT_PROFILE_REQUEST_CODE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mDialog != null) {
			mDialog.dismiss();
		}

		if (mSubscriber != null) {
			mSubscriber.unsubscribe();
		}
	}

	private void startImageIntent(int requestCode) {
		try {
			mOutputFileUri = Uri.fromFile(createImageFile());

			// Camera.
			final List<Intent> cameraIntents = new ArrayList<>();
			final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			final PackageManager packageManager = getPackageManager();
			final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, PackageManager.MATCH_DEFAULT_ONLY);
			for (ResolveInfo res : listCam) {
				final String packageName = res.activityInfo.packageName;
				final Intent intent = new Intent(captureIntent);
				intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
				intent.setPackage(packageName);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutputFileUri);
				cameraIntents.add(intent);
			}

			// Filesystem.
			final Intent galleryIntent = new Intent();
			galleryIntent.setType("image/*");
			galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				galleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
				galleryIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
				galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
			} else {
				galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
			}
			galleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
			galleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

			// Chooser of filesystem options.
			final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.picker_select_source));

			// Add the camera options.
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
			startActivityForResult(chooserIntent, requestCode);
		} catch (IOException e) {
			Toast.makeText(this, "Error creating picture", Toast.LENGTH_LONG).show();
			Dog.e(e, "IOException");
		}
	}

	@SuppressLint("SimpleDateFormat")
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "renter_" + timeStamp;
		File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

		return File.createTempFile(
				imageFileName,  /* prefix */
				".jpg",         /* suffix */
				storageDir     /* directory */
		);
	}

	// http://stackoverflow.com/questions/19834842/android-gallery-on-kitkat-returns-different-uri-for-intent-action-get-content
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_PROFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			if (data == null || (data.getAction() != null && data.getAction().equals(MediaStore.ACTION_IMAGE_CAPTURE))) {
				mProfilePictureUri = mOutputFileUri;
			} else {
				Uri uri = data.getData();
				if (!mOutputFileUri.equals(uri)) {
					new File(mOutputFileUri.getPath()).delete();
				}

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					int takeFlags = data.getFlags();
					takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
					// Check for the freshest data.
					getContentResolver().takePersistableUriPermission(uri, takeFlags);
				}

				String path = PathUtils.getPath(this, data.getData());
				if (path != null) {
					mProfilePictureUri = Uri.fromFile(new File(path));
				}
			}

			Glide.with(this)
					.load(mProfilePictureUri)
					.centerCrop()
					.transform(mCircleTransform)
					.into(mProfilePicture);

			Dog.d("Found picture : %s", mProfilePictureUri);
		}
	}

	private TextWatcher mRegisterFieldWatcher = new SimpleTextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			mRegisterButton.setEnabled(isRegisterValid());
		}
	};

	private boolean isRegisterValid() {
		return mEmail.length() > 0 && mName.length() > 0 && mPassword.length() > 0;
	}
}
