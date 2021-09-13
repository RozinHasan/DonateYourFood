package com.rozin.donateyourfood

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.SaveCallback

class Signup : AppCompatActivity() {
    lateinit var mUsername: EditText
    lateinit var mPassword: EditText
    lateinit var mConfirmPassword: EditText
    lateinit var mEmail: EditText
    lateinit var mSignUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(R.layout.activity_signup)



        mUsername = findViewById<View>(R.id.signup_username) as EditText
        mPassword = findViewById<View>(R.id.signup_password) as EditText
        mEmail = findViewById<View>(R.id.signup_email) as EditText
        mConfirmPassword = findViewById<View>(R.id.signup_confirm) as EditText
        mSignUpButton = findViewById<View>(R.id.signup_button) as Button
        mSignUpButton.setOnClickListener {
            var username = mUsername.text.toString()
            var password = mPassword.text.toString()
            var email = mEmail.text.toString()
            var confirmp = mConfirmPassword.text.toString()
            username = username.trim { it <= ' ' }
            password = password.trim { it <= ' ' }
            email = email.trim { it <= ' ' }
            confirmp = confirmp.trim { it <= ' ' }

            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || confirmp.isEmpty() ||
                    confirmp != password) {
                val builder = AlertDialog.Builder(this@Signup)
                builder.setMessage(R.string.signup_error_message)
                        .setPositiveButton(android.R.string.ok, null)
                val dialog = builder.create()
                dialog.show()
            } else {

                // this is create query of your parse table
                val query = ParseQuery<ParseObject>("User") //table name
                query.whereEqualTo("email", email)
                query.whereEqualTo("password", password)

                // this is for doing in background
                val finalUsername = username
                val finalPassword = password
                val finalEmail = email
                query.findInBackground { scoreList, e ->
                    if (e == null) {
                        if (scoreList.size == 0) {
                            // if there is no data like your email and password then it,s come here
                            // create the new user!
                            setProgressBarIndeterminateVisibility(true)

                            val newUser = ParseUser()
                            newUser.username = finalUsername
                            newUser.setPassword(finalPassword)
                            newUser.email = finalEmail

                            newUser.signUpInBackground { e ->
                                setProgressBarIndeterminateVisibility(false)

                                if (e == null) {
                                    // Success!
                                    createUserRatingsClass()
                                } else {
                                    val builder = AlertDialog.Builder(this@Signup)
                                    builder.setMessage(e.message)
                                            .setTitle(R.string.signup_error_title)
                                            .setPositiveButton(android.R.string.ok, null)
                                    val dialog = builder.create()
                                    dialog.show()
                                }
                            }
                        } else {
                            // if there is data like your email and password then it,s come here
                            Toast.makeText(applicationContext, "Username or password already exists", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.d("score", "Error: " + e.message)
                    }
                }
            }
        }
    }

    private fun createUserRatingsClass(){
        val rateObject = ParseObject("UserRatings")
        rateObject.put("userId", ParseUser.getCurrentUser().objectId)
        rateObject.put("username", ParseUser.getCurrentUser().username)
        rateObject.put("email", ParseUser.getCurrentUser().email)
        rateObject.put("onestar", 0)
        rateObject.put("twostar", 0)
        rateObject.put("threestar", 0)
        rateObject.put("fourstar", 0)
        rateObject.put("fivestar", 0)
        rateObject.put("numOfRatings", 0)

        rateObject.saveInBackground{ e->
            if (e == null){
                val intent = Intent(this@Signup, Home::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }else{
                val builder = AlertDialog.Builder(this@Signup)
                builder.setMessage(e.message)
                        .setTitle(R.string.signup_error_title)
                        .setPositiveButton(android.R.string.ok, null)
                val dialog = builder.create()
                dialog.show()
            }
        }
    }
}
