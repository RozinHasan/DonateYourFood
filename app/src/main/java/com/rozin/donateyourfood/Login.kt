package com.rozin.donateyourfood

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.parse.ParseUser

class Login : AppCompatActivity() {
    lateinit var mUsername: EditText
    lateinit var mPassword: EditText
    lateinit var mLoginButton: Button
    lateinit var mSignUpTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(R.layout.activity_login)
        mSignUpTextView = findViewById<View>(R.id.signup_link) as TextView
        mSignUpTextView.setOnClickListener {
            val intent = Intent(this@Login, Signup::class.java)
            startActivity(intent)
        }
        mUsername = findViewById<View>(R.id.login_username) as EditText
        mPassword = findViewById<View>(R.id.login_password) as EditText
        mLoginButton = findViewById<View>(R.id.login_button) as Button
        mLoginButton.setOnClickListener {
            var username = mUsername.text.toString()
            var password = mPassword.text.toString()

            username = username.trim { it <= ' ' }
            password = password.trim { it <= ' ' }



            if (username.isEmpty() || password.isEmpty()) {
                val builder = AlertDialog.Builder(this@Login)
                builder.setMessage(R.string.login_error_message)
                        .setTitle(R.string.login_error_title)
                        .setPositiveButton(android.R.string.ok, null)
                val dialog = builder.create()
                dialog.show()
            } else {
                // Login
                setProgressBarIndeterminateVisibility(true)
                ParseUser.logInInBackground(username, password) { user, e ->
                    setProgressBarIndeterminateVisibility(false)

                    if (e == null) {
                        // Success!
                        val intent = Intent(this@Login, Home::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    } else {
                        val builder = AlertDialog.Builder(this@Login)
                        builder.setMessage(e.message)
                                .setTitle(R.string.login_error_title)
                                .setPositiveButton(android.R.string.ok, null)
                        val dialog = builder.create()
                        dialog.show()
                    }
                }
            }
        }
    }
}
