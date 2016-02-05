package io.romain.passport.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import io.romain.passport.R;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.GoogleApiUtils;

public abstract class GoogleApiActivity extends BaseActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    protected static final int REQUEST_PERMISSION = 22;
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private boolean mResolvingError = false;

    private GoogleApiClient mGoogleApiClient;

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public boolean isResolvingError() {
        return mResolvingError;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!GoogleApiUtils.isGooglePlayServicesAvailable(this)) {
            Toast.makeText(this, getString(R.string.common_google_play_services_install_text_phone, getString(R.string.app_name)), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (savedInstanceState != null) {
            mResolvingError = savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
        }
    }

    private void connect() {
        if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    private void disconnect() {
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_RESOLVE_ERROR:
                mResolvingError = false;
                if (resultCode == RESULT_OK) {
                    // Make sure the app is not already connected or attempting to connect
                    connect();
                }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Dog.i("Connection succeeded");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Dog.i("Connection suspended");
        connect();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }


    /* ##### ERROR HANDLING ##### */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Dog.e("Connection to play services failed...");
        if (!mResolvingError) {
            if (result.hasResolution()) {
                try {
                    mResolvingError = true;
                    result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    // There was an error with the resolution intent. Try again.
                    mGoogleApiClient.connect();
                }
            } else {
                // Show dialog using GoogleApiAvailability.getErrorDialog()
                showErrorDialog(result.getErrorCode());
                mResolvingError = true;
            }
        }
    }

    /* Creates a dialog for an error message */
    private void showErrorDialog(int code) {
        //noinspection SpellCheckingInspection
        ErrorDialogFragment.newInstance(code).show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    private void onErrorDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {

        // Unique tag for the error dialog fragment
        private static final String DIALOG_ERROR = "dialog_error";

        public ErrorDialogFragment() {
        }

        public static ErrorDialogFragment newInstance(int code) {

            Bundle args = new Bundle();
            args.putInt(DIALOG_ERROR, code);

            ErrorDialogFragment fragment = new ErrorDialogFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    getActivity(),
                    errorCode,
                    REQUEST_RESOLVE_ERROR
            );
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((GoogleApiActivity) getActivity()).onErrorDialogDismissed();
        }
    }

}
