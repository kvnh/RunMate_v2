package com.khackett.runmate;

import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivityManualPolyline extends FragmentActivity implements GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    double mLatitude = 0;
    double mLongitude = 0;

    // create an array list to contain all of the points tapped on the map.
    // These points will contain latitude and longitude points
    private ArrayList<LatLng> arrayPoints = null;

    // A Polyline object consists of a set of LatLng locations,
    // and creates a series of line segments that connect those locations in an ordered sequence.
    // create a PolylineOptions object first and add points to it
    // instantiate a new polyline object
    PolylineOptions polylineOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_activity_manual_polyline);

        setUpMapIfNeeded();

        // instantiate arrayPoints object
        arrayPoints = new ArrayList<LatLng>();

        // Getting reference to the SupportMapFragment of activity_maps.xml
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // setting the GoogleMap object to the fragment
        mMap = fm.getMap();

        // Enabling MyLocation Layer of Google Map - this will set a blue marker to the users location
        mMap.setMyLocationEnabled(true);

        // centres the camera on the users current location when the map loads
        centreCamera();

        mMap.getUiSettings().setZoomControlsEnabled(true);

        // setting click event handlers for the map
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

//    /**
//     * method created to get the search entry from a user and turn this into a geo location
//     *
//     * @param view
//     */
//    public void onSearch(View view) {
//        EditText location_tf = (EditText) findViewById(R.id.TFAddress);
//        // convert this to a String
//        String location = location_tf.getText().toString();
//
//        List<Address> addressList = null;
//
//        if (location != null || !location.equals("")) {
//            // create an object of geocoder class
//            Geocoder geocoder = new Geocoder(this);
//            try {
//                addressList = geocoder.getFromLocationName(location, 1);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            // get the first entry from the address that was entered
//            Address address = addressList.get(0);
//
//            // get the latitude and longitude of the inputted address and assign to latLng
//            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
//
//            // set a marker on the location the entered location
//            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
//
//            // centre the camera on the users current location
//            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//        }
//    }

    /**
     * method to change the type of the map between normal view and satellite view
     *
     * @param view
     */
    public void changeType(View view) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    /**
     * checks if a map fragment has already been created
     * if not, then it creates one and calls the setUpMap() method
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                // addLines();
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        // set the app to locate the users current location
        mMap.setMyLocationEnabled(true);
    }

    /**
     * Called when the user makes a tap gesture on the map, but only if none of the overlays of the map handled the gesture.
     * Implementations of this method are always invoked on the main thread.
     *
     * @param point
     */
    @Override
    public void onMapClick(LatLng point) {

        // some help from
        // http://www.androidhub4you.com/2013/07/draw-polyline-in-google-map-version-2.html
        // http://wptrafficanalyzer.in/blog/adding-marker-on-touched-location-of-google-maps-using-android-api-v2-with-supportmapfragment/
        // http://wptrafficanalyzer.in/blog/drawing-polyline-and-markers-along-the-tapped-positions-in-google-map-android-api-v2-using-arraylist/
        // to create some of these functions

        // instantiate a marker object
        MarkerOptions marker = new MarkerOptions();

        // set the position and title to display co-ordinates
        // this will be displayed upon tapping the marker
        marker.position(point).title(point.latitude + " : " + point.longitude).snippet("blabla");

        // Setting the content of the infowindow of the marker
        // Again, this will be displayed upon tapping the marker
        marker.snippet("Latitude:" + point.latitude + "," + "Longitude:" + point.longitude);

        // Clears the marker of the previously touched position
        // This will only be seen on the 2nd click and thereafter
        mMap.clear();

        // animates the camera to centre on the touched position
        mMap.animateCamera(CameraUpdateFactory.newLatLng(point));

        // place a marker on the touched position
        mMap.addMarker(marker);

        // initialising the polyline in the map and setting some values
        polylineOptions = new PolylineOptions()
                .color(Color.RED)
                .width(5);

        // these next 2 lines can be deleted
        // polylineOptions.color(Color.RED);
        // polylineOptions.width(5);

        // Adding the tapped point to the ArrayList
        arrayPoints.add(point);

        // Setting points of polyline
        polylineOptions.addAll(arrayPoints);

        // Adding the polyline to the map
        mMap.addPolyline(polylineOptions);
    }

    /**
     * after long clicking the map, all markers are cleared
     *
     * @param point
     */
    @Override
    public void onMapLongClick(LatLng point) {
        // Removes all markers, polylines, polygons, overlays, etc from the map.
        mMap.clear();
        // Clears all values from the arraylist
        arrayPoints.clear();
    }


    // @Override
    public void onLocationChanged(Location location) {
        // Draw the marker, if destination location is not set
        if (arrayPoints.size() < 2) {

            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            LatLng point = new LatLng(mLatitude, mLongitude);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

            // drawMarker(point);
        }
    }


    /**
     * set the camera to centre on the users current location
     */
    public void centreCamera() {

        // http://stackoverflow.com/questions/23226056/to-use-or-not-to-use-getmylocation-in-google-maps-api-v2-for-android

        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location From GPS
        Location location = locationManager.getLastKnownLocation(provider);

        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        LatLng point = new LatLng(mLatitude, mLongitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

    }

}