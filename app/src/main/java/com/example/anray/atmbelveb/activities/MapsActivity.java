package com.example.anray.atmbelveb.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anray.atmbelveb.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {


    private static final String TAG = "DEV";
    private static final int LOCATION_PERMISSIONS_REQUEST_FIRST = 555;
    private static final int LOCATION_PERMISSIONS_REQUEST_SECOND = 777;
    private static final int LOCATION_PERMISSIONS_REQUEST_THIRD = 888;
    private static int mRequestCode = LOCATION_PERMISSIONS_REQUEST_FIRST;
    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //.enableAutoManage(this, this)
                .build();
        Log.d(TAG, "onCreate");

    }


    @Override
    protected void onStart() {

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d(TAG, "onStart");
        super.onStart();

        //mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        Log.d(TAG, "onStop");

        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            checkPermissionsSinceApi23(mRequestCode);

        } else {

            setMapControls();

        }

    }

    private void setMapControls() {
        mMap.setMyLocationEnabled(true);
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "GPS");
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation != null) {
            LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
            Log.d(TAG, "GPS" + latLng.toString());
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void checkPermissionsSinceApi23(int requestCode) {


        if (hasGeoPermission()) {

            setMapControls();

        } else {

            if (wasPermissionDeniedBefore()) {

                showPurposeOfGeoPermission(requestCode);

            } else {

                requestGeoPermission(requestCode);

            }

        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST_FIRST:
                if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissionsSinceApi23(LOCATION_PERMISSIONS_REQUEST_FIRST);
                } else if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    checkPermissionsSinceApi23(LOCATION_PERMISSIONS_REQUEST_SECOND);
                }
                break;
            case LOCATION_PERMISSIONS_REQUEST_SECOND:
                if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissionsSinceApi23(LOCATION_PERMISSIONS_REQUEST_THIRD);
                } else if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showOpenAppSettingsMessage();
                }
                break;
            case LOCATION_PERMISSIONS_REQUEST_THIRD:
                if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissionsSinceApi23(LOCATION_PERMISSIONS_REQUEST_THIRD);
                } else if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.geo_denied_notification, Toast.LENGTH_LONG);
                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                    if (tv != null) {
                        tv.setGravity(Gravity.CENTER);
                    }
                    toast.show();
                }
                break;


        }

    }

    private void showPurposeOfGeoPermission(final int requestCode) {
        Snackbar.make(findViewById(R.id.map), R.string.explain_geo_permissions, 5000)
                .setAction(getString(R.string.snackbar_agree), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        requestGeoPermission(requestCode);

                    }
                })
                .show();
    }

    private void showOpenAppSettingsMessage() {

        final Snackbar snackbar = Snackbar.make(findViewById(R.id.map), R.string.open_app_settings_message, 5000);
        snackbar.setAction(getString(R.string.snackbar_agree), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openAppSettings();

                Toast toast = Toast.makeText(getApplicationContext(), R.string.advice_where_grant_geo, Toast.LENGTH_LONG);
                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                if (tv != null) {
                    tv.setGravity(Gravity.CENTER);
                }
                toast.show();

            }
        });
        snackbar.show();

    }


    private boolean wasPermissionDeniedBefore() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean hasGeoPermission() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestGeoPermission(int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                requestCode);
    }

    private void openAppSettings() {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, LOCATION_PERMISSIONS_REQUEST_THIRD);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_PERMISSIONS_REQUEST_THIRD) {
            Log.d(TAG, "onActivityResult");
            mRequestCode = LOCATION_PERMISSIONS_REQUEST_THIRD;
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
