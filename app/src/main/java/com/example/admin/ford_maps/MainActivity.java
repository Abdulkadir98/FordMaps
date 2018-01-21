package com.example.admin.ford_maps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference();


        // If MainActivity is reached without the user being logged in, redirect to the Login
        // Activity
        if (AccessToken.getCurrentAccessToken() == null) {
            Intent loginIntent = new Intent(MainActivity.this, FacebookLoginActivity.class);
            startActivity(loginIntent);
        }
        else
        {
            final AccessToken accessToken = AccessToken.getCurrentAccessToken();

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



                           } catch (JSONException e) {
                               e.printStackTrace();
                           }



                           Log.i(TAG, "Response: "+response.getRawResponse());
                       }
                   });

            request.executeAsync();

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
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Log.i(TAG, "Response: "+response.toString());
                        }
                    });

            request_for_name.executeAsync();
        }

        Button logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                Intent loginIntent = new Intent(MainActivity.this, FacebookLoginActivity.class);
                startActivity(loginIntent);
            }
        });
        Button goToMaps = findViewById(R.id.goToMaps);
        goToMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(loginIntent);
            }
        });


    }
}
