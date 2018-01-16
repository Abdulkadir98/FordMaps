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

import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // If MainActivity is reached without the user being logged in, redirect to the Login
        // Activity
        if (AccessToken.getCurrentAccessToken() == null) {
            Intent loginIntent = new Intent(MainActivity.this, FacebookLoginActivity.class);
            startActivity(loginIntent);
        }
        else
        {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();

            Log.i(TAG, "user id: "+accessToken.getUserId().toString());

           GraphRequest request =  GraphRequest.newGraphPathRequest(accessToken, "/"+accessToken.getUserId().toString()+"/friends",
                   new GraphRequest.Callback() {
                       @Override
                       public void onCompleted(GraphResponse response) {
                           JSONArray friendsArray = new JSONArray();
                           friendsArray = response.getJSONArray();

                           Log.i(TAG, "Response: "+response.toString());
                       }
                   });

            request.executeAsync();
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


    }
}
