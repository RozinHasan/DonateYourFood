package com.rozin.donateyourfood;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.rozin.donateyourfood.models.Message;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;


public class FoodDonationApplication extends Application {

    // Enable Local Datastore.
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Message.class);

        Parse.enableLocalDatastore(this);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                // if defined
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url)).enableLocalDataStore()
                .build()
        );

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", getString(R.string.gcm_sender_id));
        installation.saveInBackground();


        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        // Can be Level.BASIC, Level.HEADERS, or Level.BODY
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);





        // Back4App's Parse setup
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .clientBuilder(builder)
                .server(getString(R.string.back4app_server_url))
                .build()
        );
    }
}
