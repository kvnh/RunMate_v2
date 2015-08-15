package com.khackett.runmate;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.khackett.runmate.utils.DirectionsJSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivityDirectionsMultiple extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    double mLatitude = 0;
    double mLongitude = 0;
    ArrayList<LatLng> markerPoints;
    ArrayList<Double> distanceCount;
    List<List<HashMap<String, String>>> allPoints;
    List<Polyline> polylines;

    // member variable for the send route button and distance counter display
    protected Button mButtonSend;
    protected Button mButtonUndo;
    protected TextView mDistanceCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_activity_directions_multiple);

        // Initializing array lists
        markerPoints = new ArrayList<LatLng>();
        distanceCount = new ArrayList<Double>();
        polylines = new ArrayList<Polyline>();

        // Getting reference to SupportMapFragment of the activity_maps
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Getting Map for the SupportMapFragment
        mMap = fm.getMap();

        if (mMap != null) {

            // Enable MyLocation Button in the Map
            mMap.setMyLocationEnabled(true);

            // set the zoom controls to visible
            mMap.getUiSettings().setZoomControlsEnabled(true);

            // centre the camera to the users current location
            // centreCamera();

            // Setting onclick event listener for the map
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                @Override
                public void onMapClick(LatLng point) {

                    // animate camera to centre on touched position
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(point));

                    // Adding new latlng point to the array list
                    markerPoints.add(point);

                    // Creating MarkerOptions object
                    MarkerOptions marker = new MarkerOptions();

                    // Sets the location for the marker to the touched point
                    marker.position(point);

                    /**
                     * For the start location, the colour of the marker is GREEN and
                     * for the end location, the colour of the marker is RED.
                     */
                    if (markerPoints.size() == 1) {
                        // place a green marker for the start position
                        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }

                    if (markerPoints.size() >= 2) {
                        LatLng point1 = markerPoints.get(markerPoints.size() - 2);
                        LatLng point2 = markerPoints.get(markerPoints.size() - 1);

                        // marker.position(point2).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        // marker.position(point2).visible(true);
                        marker.position(point1).visible(false);

                        // Getting URL to the Google Directions API
                        // send these values to the getDirectionsUrl() method and assign returned value to string variable url
                        String url = getDirectionsUrl(point1, point2);
                        // create a DownloadTask object - see nested class below
                        DownloadTask downloadTask = new DownloadTask();
                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);
                    }

                    // Add a new marker to the map
                    mMap.addMarker(marker);

                    System.out.println("Enhanced for");
                    for (LatLng line : markerPoints) {
                        System.out.println(line);
                    }

                }
            });

            // The map will be cleared on long click
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng point) {
                    // Removes all the points from Google Map
                    mMap.clear();
                    // Removes all the points in the ArrayList
                    markerPoints.clear();
                }
            });

        }

        // set up member variables for each UI component
        mButtonSend = (Button) findViewById(R.id.btn_send);
        mButtonUndo = (Button) findViewById(R.id.btn_undo);
        mDistanceCount = (TextView) findViewById(R.id.distanceCount);

        // add an onClickListener for the send route button
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // first ensure that there are at least 2 points in the ArrayList
                if (markerPoints.size() <= 1) {
                    // if not, display a message to the user - use a dialog so that some user interaction is required before it disappears
                    // use Builder to build and configure the alert
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivityDirectionsMultiple.this);
                    // set the message title and text for the button - use String resources for all of these values
                    // chain the methods together as they are all referencing the builder object
                    builder.setMessage(R.string.route_creation_error_message)
                            .setTitle(R.string.route_creation_error_title)
                                    // button to dismiss the dialog.  Set the listener to null as we only want to dismiss the dialog
                                    // ok is gotten from android resources
                            .setPositiveButton(android.R.string.ok, null);
                    // we need to create a dialog and show it
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    // declare intent to capture a route using whatever camera app is available
                    Intent createRouteIntent = new Intent(MapsActivityDirectionsMultiple.this, RouteRecipientsActivity.class);

                    // using android.location to extend Parcelable in order to create and store the LatLng values in an arrayList
                    createRouteIntent.putParcelableArrayListExtra("markerPoints", markerPoints);
                    // start RouteRecipientsActivity in order to choose recipients
                    startActivity(createRouteIntent);
                }
            }
        });

        mButtonUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // create variable for the 2nd last point clicked and assign value form markerPoints array list
                LatLng lastPoint;
                lastPoint = markerPoints.get(markerPoints.size() - 2);

                // animate camera to centre on the previously touched position
                System.out.println("Centering camera to previous position at " + lastPoint.toString());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(lastPoint));


                // remove polyline object from the map
                for (Polyline line : polylines) {
                    if (polylines.get(polylines.size() - 1).equals(line)) {
                        line.remove();
                        polylines.remove(line);
                    }
                }

                // remove last value from the markerPoints array list
                markerPoints.remove(markerPoints.size() - 1);

                // update the distance text
                double routeDistance = 0;
                distanceCount.remove(distanceCount.size() - 1);
                for (Double step : distanceCount) {
                    routeDistance += step;
                }

                System.out.println("Total Distance calculated in undo in m = " + routeDistance);
                // output new value to ui
                mDistanceCount.setText(routeDistance / 1000 + "km");
            }
        });
    }

    /**
     * Creates a url containing the origin and destination points and other parameters
     * which can then be sent as a HTTP request to the Google Directions API to create data in JSON format
     *
     * @param origin
     * @param dest
     * @return
     */
    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String stringOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String stringDestination = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = stringOrigin + "&" + stringDestination;
        // Output format
        String output = "json";
        // transport mode
        String transMode = "&mode=walking";
        // Building the url to the web service
        // see https://developers.google.com/maps/documentation/directions/#DirectionsRequests
        // eg. https://maps.googleapis.com/maps/api/directions/json?origin=40.722543,-73.998585&destination=40.7577,-73.9857&mode=walking
        // ... would give the points between lower_manhattan and times_square and the directions in between in JSON format
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + transMode;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Problem downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // create a new ParserTask object and invoke thread for parsing JSON data
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
            // create a new ParseDistanceTask object and invoke thread for parsing JSON data
            ParseDistanceTask parseDistanceTask = new ParseDistanceTask();
            parseDistanceTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                // Start parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            PolylineOptions lineOptions = null;
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    // Adding all the points in the route to LineOptions
                    lineOptions.add(position).width(6).color(Color.BLUE);
                    System.out.println("Printing test data");
                }
                // add Polyline to list and draw on map
                polylines.add(mMap.addPolyline(lineOptions));
            }
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParseDistanceTask extends AsyncTask<String, Integer, Double> {

        // Parsing the data in non-ui thread
        @Override
        protected Double doInBackground(String... jsonData) {
            JSONObject jsonObject;
            Double distance = null;
            try {
                jsonObject = new JSONObject(jsonData[0]);
                // get all routes from the routes array
                JSONArray array = jsonObject.getJSONArray("routes");
                // get the first route in the JSON object
                JSONObject routes = array.getJSONObject(0);
                // get all of the legs from the route and add to legs array
                JSONArray legs = routes.getJSONArray("legs");
                // get the first leg in the JSON object
                JSONObject steps = legs.getJSONObject(0);
                // get the distance element
                JSONObject distanceJSON = steps.getJSONObject("distance");
                // get the value from the distance element and assign to distance
                distance = Double.parseDouble(distanceJSON.getString("value"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return distance;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(Double distance) {
            double routeDistance = 0;
            distanceCount.add(distance);
            for (Double step : distanceCount) {
                routeDistance += step;
            }
            System.out.println("Total Distance calculated in AsyncTask in m = " + routeDistance);
            mDistanceCount.setText(routeDistance / 1000 + "km");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

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

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
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
