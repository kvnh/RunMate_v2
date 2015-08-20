package com.khackett.runmate;

import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivityTrackRun extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /**
     * Request code to send to Google Play Services in case of connection failure
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /**
     * The desired interval for location updates.
     */
    public static final long MIN_UPDATE_INTERVAL_MILLISECONDS = 1000 * 5;

    /**
     * The fastest rate for location updates - updates will never be more frequent than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_MILLISECONDS = 1000 * 1;

    /**
     * The minimum distance from previous update to accept new update (in meters).
     */
    private static int DISPLACEMENT_METRES = 1;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    // Provides the entry point to Google Play services.
    private GoogleApiClient mGoogleApiClient;
    // Used to request a quality of service for location updates and store parameters for requests to the FusedLocationProviderApi
    private LocationRequest mLocationRequest;
    // The current location of the device
    private Location mCurrentLocation;

    // TAg for current Activity
    public static final String TAG = MapsActivityTrackRun.class.getSimpleName();

    // member variables for the UI buttons and text outputs
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;

    /**
     * Boolean to to track whether the location updates have been turned on or off by the user.
     * Value changes when the user presses the Start Run and Stop Run buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    private double mLat;
    private double mLng;

    // Declare array list for location points
    private List<LatLng> latLngPointsArray;
    private Polyline line;

    private List<Polyline> polylines;

//    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_activity_track_run);
        setUpMapIfNeeded();

        // set up member variables for each UI component
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);

        // set the location update request to false to start the activity
        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Initializing array lists
        latLngPointsArray = new ArrayList<LatLng>();
        polylines = new ArrayList<Polyline>();

        // make the devices location visible
        mMap.setMyLocationEnabled(true);
        // set the zoom controls to visible
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // create the Google API client and request the location services API
        createGoogleApiClient();


//        provider = LocationManager.GPS_PROVIDER;
//        // Acquire a reference to the system Location Manager to return a new LocationManager instance
//        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//
//        // Register the listener with the Location Manager to receive location updates
//        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
//        // Getting reference to SupportMapFragment of the activity_maps
//        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        // Getting Map for the SupportMapFragment
//        mMap = fm.getMap();
    }

    @Override
    public void onResume() {
        super.onResume();

        // setUpMapIfNeeded();

        // Resume receiving location updates if requested
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            // remove location updates, but do not disconnect the GoogleApiClient object
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        // Disconnect the GoogleApiClient object
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Creates the Google API client used to access Google Play services
     */
    protected synchronized void createGoogleApiClient() {
        // create a new GoogleApiClient object using the builder pattern
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // let the client know that this class will handle connection management
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                        // add the LocationServices API from Google Play Services
                .addApi(LocationServices.API)
                        // build the client
                .build();

        // create the location request
        createLocationRequest();
    }

    /**
     * Creates the location request and sets the accuracy of the current location
     */
    protected void createLocationRequest() {
        // initialise the mLocationRequest object with desired settings
        mLocationRequest = LocationRequest.create()
                // request the most precise location possible
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        // set update interval for active location updates
                .setInterval(MIN_UPDATE_INTERVAL_MILLISECONDS)
                        // set fastest rate for active location updates
                        // app will never receive updates faster than this setting
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MILLISECONDS)
                .setSmallestDisplacement(DISPLACEMENT_METRES);
    }

    /**
     * Handles the Start Run button and requests start of location updates.
     * Does nothing if updates have already been requested.
     */
    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }

    /**
     * Handles the Stop Run button, and requests removal of location updates.
     * Does nothing if updates were not previously requested.
     */
    public void stopUpdatesButtonHandler(View view) {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
    }

    /**
     * Called when a successful connection is made to the Google API client
     */
    @Override
    public void onConnected(Bundle bundle) {

        // log that a connection has been made
        Log.i(TAG, "Location services are now connected");

        // if current location is null, get the last known location of device
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        // If Start Run button is pressed before GoogleApiClient connects,
        // mRequestingLocationUpdates is set to true in startUpdatesButtonHandler()
        // Then start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    /**
     * Requests location updates from the FusedLocationApi
     */
    protected void startLocationUpdates() {
        // Calls this LocationListener when location has changed
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Removes location updates from the FusedLocationApi when activity is paused or stopped
     */
    protected void stopLocationUpdates() {
        // Calls this LocationListener when location has changed
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    //    @Override
//    public void onLocationChanged(Location location) {
//        handleNewLocation(location);
//    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Here's me :)");
        mMap.addMarker(options);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        mMap.animateCamera(cameraUpdate);
    }

    /**
     * Callback from requestLocationUpdates() that is called when the location changes
     */
    @Override
    public void onLocationChanged(Location location) {

        if (!location.hasAccuracy()) {
            return;
        }
        if (location.getAccuracy() > 40) {
            return;
        }

        double latitude, longitude;
        LatLng latLngPoint;

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        latLngPoint = new LatLng(latitude, longitude);

        PolylineOptions polylineOptions = new PolylineOptions()
                .width(6)
                .color(Color.BLUE);
        // .geodesic(true);
        for (int i = 0; i < latLngPointsArray.size(); i++) {
            polylineOptions.add(latLngPointsArray.get(i));
        }
        line = mMap.addPolyline(polylineOptions);
        latLngPointsArray.add(latLngPoint);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLngPoint, 20);
        mMap.animateCamera(cameraUpdate);

        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        Toast.makeText(this, getResources().getString(R.string.location_updated_message), Toast.LENGTH_SHORT).show();

//        mCurrentLocation = location;
//
//        mLat = mCurrentLocation.getLatitude();
//        mLng = mCurrentLocation.getLongitude();
//        // LatLng newLatLngPoint = new LatLng((int) (mLat * 1e6), (int) (mLng * 1e6));
//        LatLng newLatLngPoint = new LatLng(mLat, mLng);
//
//        // add each new point detected to the latlng array and print to console
//        latLngPointsArray.add(newLatLngPoint);
//        System.out.println("Printing lat & lng points array accurate");
//        for (LatLng point : latLngPointsArray) {
//            System.out.println(point);
//        }
//
////        PolylineOptions lineOptions = new PolylineOptions();
////        lineOptions.add(newLatLngPoint).width(6).color(Color.BLUE);
////        // add Polyline to list and draw on map
////        polylines.add(mMap.addPolyline(lineOptions));

    }


    //    public void onLocationChanged(Location location) {
//        String msg = "Location:" + location.getLatitude() + "," + location.getLongitude();
//        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
//        double new_lat = location.getLatitude();
//        double new_long = location.getLongitude();
//        double previous_lat = new_lat;
//        double previous_long = new_long;
//        drawTrack(new_lat, new_long, previous_lat, previous_long);
//    }
//
//    private void drawTrack(double new_lat, double new_long, double previous_lat, double previous_long) {
//        PolylineOptions options = new PolylineOptions();
//        options.add(new LatLng(previous_lat, previous_long));
//        options.add(new LatLng(new_lat, new_long));
//        options.width(10);
//        options.color(Color.RED);
//        mMap.addPolyline(options);
//
//
//        googleMap.addPolyline(new PolylineOptions()
//                .add(new LatLng(Double.parseDouble(YOUR PREVIOUS LATITUDE VALUE),
//                        Double.parseDouble(YOUR PREVIOUS LONGITUDE VALUE),
//                        new LatLng(Double.parseDouble(YOUR LATEST LATITUDE VALUE),
//                                Double.parseDouble(YOUR LATEST LONGITUDE VALUE)
//                                        .width(5).color(getResources()
//                                        .getColor(R.color.BLACK))
//                                        .geodesic(true));
//    }


    /**
     * Updates the latitude, longitude, and last location time in the UI.
     */
    private void updateUI() {
        if (mCurrentLocation != null) {
            mLatitudeTextView.setText(String.valueOf(mCurrentLocation.getLatitude()));
            mLongitudeTextView.setText(String.valueOf(mCurrentLocation.getLongitude()));
            mLastUpdateTimeTextView.setText(mLastUpdateTime);
        }
    }

    /**
     * Ensures  only one button is enabled at a time.
     * Stop Run button is enabled if the user is requesting location updates.
     * Start Run button is enabled if the user is not requesting location updates.
     */
    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // log that the connection is suspended and try to reconnect
        Log.i(TAG, "Connection to Location services have been suspended. Attempting to reconnect");
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // If connection failed, start a Google Play services activity to resolve the error
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                // Thrown if Google Play services canceled the original PendingIntent
            } catch (IntentSender.SendIntentException e) {
                // Log error
                e.printStackTrace();
            }
        } else {
            // Display a dialog to the user with the error code if no resolution available
            Log.i(TAG, "Location services connection failed. Code = " + connectionResult.getErrorCode());
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    private void setUpMap() {
        // mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }


}
