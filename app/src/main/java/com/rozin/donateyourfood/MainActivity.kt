package com.rozin.donateyourfood

import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.GsonBuilder
import com.parse.ParseInstallation
import com.parse.ParseObject
import com.parse.ParseUser
import com.rozin.donateyourfood.models.DeviceListModel
import com.rozin.donateyourfood.networks.ApiService
import com.rozin.donateyourfood.networks.RetrofitBuilder
import com.rozin.donateyourfood.utils.MyPreference
import mumayank.com.airlocationlibrary.AirLocation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var mSignUpTextView: Button
    lateinit var mLoginTextView: Button
    lateinit var mHomeTextView: Button
    private var airLocation: AirLocation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()


    }

    private fun getRegionName(location: Location): String {
        var regioName = "";

        val gcd = Geocoder(this, Locale.getDefault())
        try {
            val addresses = gcd.getFromLocation(location.latitude, location.longitude, 1);
            if (addresses.size > 0) {
                regioName = addresses.get(0).getLocality();
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        return regioName

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        airLocation?.onActivityResult(requestCode, resultCode, data);
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        airLocation?.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private fun init() {
        val isCityAlreadySelected = MyPreference.with(this@MainActivity).getBoolean("isCityfetched", false)

//        callGetTokesApi()
        FirebaseApp.initializeApp(this)


        // Save the current Installation to Back4App

        val installation = ParseInstallation.getCurrentInstallation()
        installation.put("GCMSenderId", getString(R.string.gcm_sender_id))
        installation.saveInBackground()
        getAndSaveDeviceTokenID()

        FirebaseMessaging.getInstance().subscribeToTopic("global")

        mSignUpTextView = findViewById<Button>(R.id.mainsignup)
        mSignUpTextView.setOnClickListener {
            val intent = Intent(this@MainActivity, Signup::class.java)
            startActivity(intent)
        }

        mLoginTextView = findViewById<Button>(R.id.mainlogin)
        mLoginTextView.setOnClickListener {
            val intent = Intent(this@MainActivity, Login::class.java)
            startActivity(intent)
        }

        mHomeTextView = findViewById(R.id.mainhome)
        mHomeTextView.setOnClickListener {
            if (isCityAlreadySelected) {
                val intent = Intent(this@MainActivity, Home::class.java)
                startActivity(intent)
            } else {
                callAirLocation()
            }

        }

        val currentUser = ParseUser.getCurrentUser()
        if (currentUser == null) {
            mLoginTextView.visibility = View.VISIBLE
            mSignUpTextView.visibility = View.VISIBLE
            mHomeTextView.visibility = View.INVISIBLE
        } else {
            Log.i(TAG, currentUser.username)
            mLoginTextView.visibility = View.INVISIBLE
            mSignUpTextView.visibility = View.INVISIBLE
        }
    }

    private fun callAirLocation(){
        airLocation = AirLocation(this , true , true , object : AirLocation.Callbacks{
            override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                MyPreference.with(this@MainActivity).addString("city" , "Dhaka").save()
                MyPreference.with(this@MainActivity).addBoolean("isCityfetched" , true).save()

                val intent = Intent(this@MainActivity, Home::class.java)
                startActivity(intent)
            }

            override fun onSuccess(location: Location) {
                val city = getRegionName(location)
                MyPreference.with(this@MainActivity).addString("city" , city).save()
                MyPreference.with(this@MainActivity).addBoolean("isCityfetched" , true).save()

                val intent = Intent(this@MainActivity, Home::class.java)
                startActivity(intent)
            }
        })

    }

    private fun getAndSaveDeviceTokenID() {
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e("FoodDonationApplication", "getInstanceId failed", task.exception)
                    } else {
                        val token = task.result!!.token
                        MyPreference.with(this).addString("myToken", token).save()
                        val isTokenSaved = MyPreference.with(this).getBoolean("isTokenSaved", false)

                        if (!isTokenSaved) {
                            sendTokenToServer(token)
                        }

                        Log.e("__________token", token)
                    }

                }
    }

    private fun sendTokenToServer(token: String) {
        val postingdata = ParseObject("UserToken")
        postingdata.put("getTokens", "tokens")
        postingdata.put("token", token) //string
        postingdata.saveInBackground { status ->
            if (status == null) {
                Log.e("__________token", "send to server sucessfully")
                MyPreference.with(this).addBoolean("isTokenSaved", true).save()
            } else {
                MyPreference.with(this).addBoolean("isTokenSaved", false).save()
                Log.e("__________token", "failed to send")
            }
        }
    }


    private fun callGetTokesApi() {
        val service = RetrofitBuilder.buildRetrofit().create(ApiService::class.java)
        val call = service.allDevices


        call.enqueue(object : Callback<DeviceListModel> {
            @RequiresApi(api = Build.VERSION_CODES.O)
            override fun onResponse(call: Call<DeviceListModel>, response: Response<DeviceListModel>) {
                Log.e("token", "tokenResponse --> $response")

                if (response.code() == 200) {
                    val dataList = response.body()
                    Log.e("response :", GsonBuilder().setPrettyPrinting().create().toJson(response))
                    for (i in 0 until dataList!!.devices.size) {
//                        beaconList.add(dataList.resource[i])
                        Log.e("===> Email :", dataList.devices[i].email)
                        Log.e("===> token :", dataList.devices[i].token)

                    }

                } else {
                    Toast.makeText(this@MainActivity, "failed to load tokens", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeviceListModel>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                Log.e("OnBeacon Failure :", t.message)
            }
        })

    }

    companion object {
        val TAG = MainActivity::class.java.getSimpleName()
    }
}
