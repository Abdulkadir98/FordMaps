package com.example.admin.ford_maps;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private ArrayList<LatLng> markers = new ArrayList<>();
    private ArrayList<Marker> friendsMarkers = new ArrayList<>();
    private Map<String, FriendInfo> friendLatLngMap = new HashMap<>();
    private Map<Integer, Marker> userMarkerMap = new HashMap<>();
    private FloatingActionButton mic;

    private Button log_out;
    private CheckBox isFacebookfriendsChecked;
    private CheckBox isRestaurantsChecked;

    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng coordinates;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    //Firebase instance variables
    private DatabaseReference mFirebaseDatabase;

    private Fragment place_autocomplete_fragment;
    private EditText enter_destination;



    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private ArrayList<String> friendsID;
    private String profilePictureUrl;

    private Toast mToast;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
            return;

        }

        log_out = findViewById(R.id.log_out_button);
        isFacebookfriendsChecked = findViewById(R.id.fb_friends);
        isRestaurantsChecked = findViewById(R.id.restraunts_near_you);
        mic = findViewById(R.id.mic);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(400);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    coordinates = new LatLng(location.getLatitude(),location.getLongitude());

                    Log.i(TAG, "Location: "+coordinates);
                    mFirebaseDatabase.child("users").child(Utils.getAccessTokenUserID(MapsActivity.this)).child("latitude")
                            .setValue(coordinates.latitude);
                    mFirebaseDatabase.child("users").child(Utils.getAccessTokenUserID(MapsActivity.this)).child("longitude")
                            .setValue(coordinates.longitude);


                        if (markers.size()>0)
                        {
                            markers.set(0,coordinates);
                            createMarkers(false);
                        }
                        else
                        {
                            markers.add(coordinates);
                            createMarkers(true);
                        }
                        Log.d("current Location",markers.toString());



//                    LatLng latLng= new LatLng(location.getLatitude(),location.getLongitude());
//                    mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Sydney"));
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));


                }
            };
        };

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        getString(R.string.speech_prompt));
                try {
                    startActivityForResult(intent, 100);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.speech_not_supported),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                Intent loginIntent = new Intent(MapsActivity.this, FacebookLoginActivity.class);
                startActivity(loginIntent);
            }
        });


        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        final FragmentManager fm = getFragmentManager();
//        fm.beginTransaction()
//                .hide(autocompleteFragment)
//                .commit();

//        enter_destination.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if(b || !b)
//                {
//                    fm.beginTransaction()
//                            .show(autocompleteFragment)
//                            .commit();
//                }
//            }
//        });


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("blah", "Place: " + place.getName());
                LatLng placeCoordinates = place.getLatLng();

                markers.add(placeCoordinates);

                if(markers.size()> 0){
                   markers.clear();

                   markers.add(coordinates);
                   markers.add(placeCoordinates);
               }

               createMarkers(false);

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("blah", "An error occurred: " + status);
            }
        });



        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

            return;
        }

        else{
            changeLocationSettings();
        }






        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);



    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION:
                if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    changeLocationSettings();
                } else {
                    Log.i("denied", "sdssd");
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    void showLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
            return;

        }
        else{
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            Log.i("maps", "Coordinates: " + location.getLatitude() + " " + location.getLongitude());
                            // Logic to handle location object
                            coordinates = new LatLng(location.getLatitude(), location.getLongitude());
//                            mMap.addMarker(new MarkerOptions().position(coordinates).title("Marker in Sydney"));
//                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
                            Log.i("maps", "called");

                             friendsID = new ArrayList<>();
                            final ArrayList<Friend> friends = new ArrayList<>();



                            ValueEventListener friendsListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Get Post object and use the values to update the UI
                                    for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                                        friendsID.add(postSnapshot.getValue(String.class));

                                        Log.d(TAG, "Friend id:"+friendsID.toString());
                                    }


                                    ValueEventListener friendObjectListener = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            Friend friend = dataSnapshot.getValue(Friend.class);
                                            friends.add(friend);
                                            double latitude = friend.getLatitude();
                                            double longitutde = friend.getLongitude();

                                            FriendInfo friendInfo = new FriendInfo();
                                            friendInfo.setLocation(new LatLng(latitude, longitutde));
                                            getFacebookProfilePicture(dataSnapshot.getKey(), friendInfo);


                                            friendLatLngMap.put(friend.getUsername(), friendInfo);
                                            //friendsMarkersPosition.add(new LatLng(latitude, longitutde));

                                            createMarkers(false);
                                            Log.d(TAG, "Friend: "+friends.get(0).toString());
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.v(TAG, "loadPost:onCancelled", databaseError.toException());
                                        }
                                    };

                                    //Getting Friends' objects and storing in "friends" ArrayList

                                    for (String friend : friendsID){
                                        mFirebaseDatabase.child("users").child(friend).addValueEventListener(friendObjectListener);
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Getting Post failed, log a message
                                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                                    // ...
                                }
                            };
                            String userID =  Utils.getAccessTokenUserID(MapsActivity.this);

                            mFirebaseDatabase.child("users").child(userID).child("friends").addValueEventListener(friendsListener);

                            if(markers.size() == 0){
                                markers.add(coordinates);
                                createMarkers(false);
                            }


                            mFirebaseDatabase.child("users").child(userID).child("latitude")
                                    .setValue(location.getLatitude());
                            mFirebaseDatabase.child("users").child(userID).child("longitude")
                                    .setValue(location.getLongitude());

                        }
                    }
                });}

    }

    protected void changeLocationSettings() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                showLocation();


            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this,
                                REQUEST_CHECK_SETTINGS);


                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();


            startLocationUpdates();
            showLocation();


    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.toolbar_options_menu, menu);
//        return true;
//    }
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if(item.getItemId()==R.id.change_language)
//        {
//            return true;
//        }
//        else
//            return super.onOptionsItemSelected(item);
//    }

    protected void createMarkers(boolean first) {
        //mMap.clear();

        if(!first){
            if(userMarkerMap.size() > 0){
                Marker marker = userMarkerMap.get(0);
                marker.remove();
                userMarkerMap.remove(1);
            }
        }

        for(int i=0; i<markers.size(); i++){

            //Marker marker = new MarkerOptions().position(markers.get(i)).anchor(0.5f, 0.5f);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(markers.get(i))
                    .anchor(0.5f, 0.5f));


            userMarkerMap.put(i, marker);

        }
        if (first)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(0), 15));

        // Checks, whether start and end locations are captured
        if (markers.size() >= 2) {
            LatLng origin = markers.get(0);
            LatLng dest = markers.get(1);

            // Getting URL to the Google Directions API
            String url = getUrl(origin, dest);
            Log.d("onMapClick", url.toString());
            FetchUrl FetchUrl = new FetchUrl();

            // Start downloading json data from Google Directions API
            FetchUrl.execute(url);
            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }


    }
    void createFriendsMarkers(){
        for(Map.Entry<String, FriendInfo> entry: friendLatLngMap.entrySet()){

//                    Log.i(TAG, "url: "+value.getProfilePictureUrl());
//                    Bitmap bmImg = Ion.with(this)
//                            .load(value.getProfilePictureUrl()).asBitmap().get();
            String username = entry.getKey();
            LatLng location = entry.getValue().getLocation();
            friendsMarkers.add(mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(username)
                    .icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(getResources()
                            .getDrawable(R.drawable.ic_person_map))))
                    .anchor(0.5f, 0.5f)));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            }
    }
    void createRestaurantsMarkers()
    {
        String url = Utils.getNearbyRestaurantUrl(markers.get(0), 500);
        Object[] DataTransfer = new Object[2];
        DataTransfer[0] = mMap;
        DataTransfer[1] = url;
        Log.d("onClick", url);
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData(MapsActivity.this);
        getNearbyPlacesData.execute(DataTransfer);

        if(mToast != null){
            mToast.cancel();
        }

        mToast =  Toast.makeText(MapsActivity.this,"Nearby Restaurants", Toast.LENGTH_LONG);
        mToast.show();
    }
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.fb_friends:
                if (checked){
                    createFriendsMarkers();
                }
            else {
                    mMap.clear();
                    createMarkers(false);
                }
                break;
            case R.id.restraunts_near_you:
                if (checked){
                    createRestaurantsMarkers();
                }
            else {
                    mMap.clear();
                    createMarkers(false);
                    break;
                }

        }
    }
    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


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
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

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
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }
    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 100: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Toast.makeText(MapsActivity.this, result.get(0),Toast.LENGTH_LONG).show();
                    if (result.get(0).equals("log out")||result.get(0).equals("logout")||result.get(0).equals("log out from facebook"))
                    {
                        LoginManager.getInstance().logOut();
                        Intent loginIntent = new Intent(MapsActivity.this, FacebookLoginActivity.class);
                        startActivity(loginIntent);
                    }
                    else if (result.get(0).equals("show Facebook friends")){
                        isFacebookfriendsChecked.setChecked(true);
                    }
                    else if (result.get(0).equals("show nearby restaurants")){
                        isRestaurantsChecked.setChecked(true);
                    }
                    else Toast.makeText(getApplicationContext(), "Command not found.",Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_CHECK_SETTINGS : {
                if(resultCode == RESULT_OK && null != data){
                   //simulateLongRunningWork();
                    Toast.makeText(getApplicationContext(),"Refresh app", Toast.LENGTH_SHORT).show();

                }
            }

        }
    }
    private void simulateLongRunningWork(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                finish();
            }
        }, 4000);
    }

    private void getFacebookProfilePicture(final String friendID, final FriendInfo friendInfo){

//        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
//        GraphRequest request = GraphRequest.newGraphPathRequest(accessToken, "/" + friendID + "/picture?type=normal&redirect=false",
//                new GraphRequest.Callback() {
//                    @Override
//                    public void onCompleted(GraphResponse response) {
//                        Log.i(TAG, "Response: "+response.getRawResponse());
//
//                        try {
//                            JSONObject jsonObject = new JSONObject(response.getRawResponse());
//                            JSONObject dataObject = jsonObject.getJSONObject("data");
//
//                             profilePictureUrl = dataObject.getString("url");
//                             profilePictureUrl.replaceAll("\\\\", "");
//                            Log.i(TAG, "profile pic url: "+ profilePictureUrl);
//
//                            friendInfo.setProfilePicture(profilePictureUrl);
//
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//        request.executeAsync();

    }

}
