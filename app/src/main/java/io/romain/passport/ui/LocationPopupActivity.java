package io.romain.passport.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import io.romain.passport.R;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.GoogleApiUtils;

public abstract class LocationPopupActivity extends BaseActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final long LOCATION_REQUEST_TIMEOUT = 5L * 60L * 1000L; // request for at most 5 minutes
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10L * 1000L; // 10s
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final LocationRequest sLocationRequest = new LocationRequest()
            // Sets the desired interval for active location updates. This interval is
            // inexact. You may not receive updates at all if no location sources are available, or
            // you may receive them slower than requested. You may also receive updates faster than
            // requested if other applications are requesting location at a faster interval.
            .setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
            .setExpirationDuration(LOCATION_REQUEST_TIMEOUT)

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates faster than this value.
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
            .setNumUpdates(5)
            .setPriority(LocationRequest.PRIORITY_LOW_POWER);

    protected LocationRequest getLocationRequest() {
        return sLocationRequest;
    }

    private static final int REQUEST_PERMISSION = 22;
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final int REQUEST_CHECK_SETTINGS = 1222;

    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private boolean mResolvingError = false;


    private static final String STATE_POPUP_DISPLAYED = "state_popup_showing";
    private boolean mPopupDisplayed = false;

    private GoogleApiClient mGoogleApiClient;

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
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
            mPopupDisplayed = savedInstanceState.getBoolean(STATE_POPUP_DISPLAYED, false);
        }

        if (!mPopupDisplayed && !mResolvingError) {
            setLocationPermission();
        }
    }

    private void isLocationSettingsNeedMediation() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(sLocationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(getGoogleApiClient(), builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    Dog.i("All location settings are satisfied.");
                    // Our work is done here
                    onLocationServiceAvailable();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied. But could be fixed by showing the user
                    // a dialog.
                    Dog.i("Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(
                                LocationPopupActivity.this,
                                REQUEST_CHECK_SETTINGS
                        );

                        mPopupDisplayed = true;
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                        Dog.i("PendingIntent unable to execute request.");
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way to fix the
                    // settings so we won't show the dialog.
                    Dog.i("Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                    onLocationServiceUnavailable(false);
                    break;
            }
        });
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
            case REQUEST_CHECK_SETTINGS:
                mPopupDisplayed = false;
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Dog.i("User agreed to make required location settings changes.");
                        onLocationServiceAvailable();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not
                        Dog.i("User chose not to make required location settings changes.");
                        onLocationServiceUnavailable(true);
                    default:
                        break;
                }
                break;
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
        outState.putBoolean(STATE_POPUP_DISPLAYED, mPopupDisplayed);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }


    protected void onLocationServiceAvailable() {

    }

    protected void onLocationServiceUnavailable(boolean isRecoverable) {

    }


    // ## Permission Management
    private void setLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            isLocationSettingsNeedMediation();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showRationale();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        REQUEST_PERMISSION
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                setLocationPermission();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    showRationale();
                } else {
                    // The user doesn't want us helping him :'( #me-sad
                    onLocationServiceUnavailable(false);
                }
            }
        }
    }

    private void showRationale() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_city_location_dialog_title)
                .setMessage(R.string.add_city_location_dialog_message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    ActivityCompat.requestPermissions(LocationPopupActivity.this,
                            new String[]{
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            },
                            REQUEST_PERMISSION
                    );
                })
                .show();
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
            ((LocationPopupActivity) getActivity()).onErrorDialogDismissed();
        }
    }

}
