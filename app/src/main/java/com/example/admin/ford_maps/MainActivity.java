package com.example.admin.ford_maps;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    String name;

    public static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        mDatabase = FirebaseDatabase.getInstance().getReference();


        if (!isNetworkAvailable()){
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Alert");
            alertDialog.setCancelable(false);
            alertDialog.setMessage("No Internet Connection");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Refresh",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(intent);
                            dialog.dismiss();
                            finish();
                        }
                    });
            alertDialog.show();
        }


        else{
            // If MainActivity is reached without the user being logged in, redirect to the Login
            // Activity
            if (AccessToken.getCurrentAccessToken() == null) {

                Intent loginIntent = new Intent(MainActivity.this, FacebookLoginActivity.class);
                startActivityForResult(loginIntent, 200);

            }
            else
            {
                makeFacebookRequest();
            }
        }



    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void makeFacebookRequest(){
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();

        //Storing access token
        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("ACCESS_TOKEN_USERID", accessToken.getUserId()).apply();

        Log.i(TAG, "user id: "+accessToken.getUserId().toString());

        GraphRequest request =  GraphRequest.newGraphPathRequest(accessToken, "/"+accessToken.getUserId().toString()+"/friends",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {

                        try {
                            JSONObject responseObject = new JSONObject(response.getRawResponse());
                            JSONArray friendsArray = responseObject.getJSONArray("data");



                            ArrayList<String> friendsList = new ArrayList<String>();
                            if (friendsArray != null) {
                                for (int i=0;i<friendsArray.length();i++){
                                    friendsList.add(friendsArray.getJSONObject(i).getString("id"));
                                }
                            }



                            mDatabase.child("users").child(accessToken.getUserId().toString()).child("friends").setValue(friendsList);

                            GraphRequest request_for_name =  GraphRequest.newGraphPathRequest(accessToken, "/"+accessToken.getUserId().toString()+"?fields=name",
                                    new GraphRequest.Callback() {
                                        @Override
                                        public void onCompleted(GraphResponse response) {
                                            Log.i(TAG, "Response: "+response.toString());

                                            try {
                                                JSONObject responseObject = new JSONObject(response.getRawResponse());
                                                name = responseObject.getString("name");
                                                mDatabase.child("users").child(accessToken.getUserId().toString()).child("username").setValue(name);

                                                Log.i(TAG, "Name: "+name);

                                                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                                                startActivity(intent);

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            Log.i(TAG, "Response: "+response.toString());
                                        }
                                    });

                            request_for_name.executeAsync();



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                        Log.i(TAG, "Response: "+response.getRawResponse());
                    }
                });

        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 200 && resultCode == RESULT_OK){
            makeFacebookRequest();
        }
        else{
            Toast.makeText(getApplicationContext(), "Error logging in.", Toast.LENGTH_SHORT).show();
        }
    }
}
