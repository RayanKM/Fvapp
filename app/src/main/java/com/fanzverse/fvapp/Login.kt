package com.fanzverse.fvapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.amplifyframework.core.Amplify
import com.fanzverse.fvapp.databinding.ActivityLoginBinding
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding // declare a lateinit var of type ActivityLoginBinding named binding

    override fun onCreate(savedInstanceState: Bundle?) { // override the onCreate() function
        super.onCreate(savedInstanceState) // call the super class's onCreate() function
        binding =
            ActivityLoginBinding.inflate(layoutInflater) // inflate the view using the ActivityLoginBinding class and assign it to the binding variable
        setContentView(binding.root) // set the content view of the activity to the root view of the inflated layout

        binding.button.setOnClickListener {
            val username = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            // Check if email and password are not empty
            if (username.isNotEmpty() && pass.isNotEmpty()) {
                Amplify.Auth.signIn(username, pass,
                    { result ->
                        if (result.isSignedIn) {
                            runOnUiThread {
                                MotionToast.createColorToast(
                                    this,
                                    "Login Successful",
                                    "Joining..",
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(
                                        this,
                                        www.sanju.motiontoast.R.font.helvetica_regular
                                    )
                                )
                                // Wait for 2 seconds and go to Register activity
                                Handler().postDelayed({
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }, 2000)
                            }
                        } else {
                            runOnUiThread {
                                showToast("Sign in not complete")
                            }
                        }
                    },
                    {runOnUiThread {
                        Log.d("sqdqdsq", it.localizedMessage!!)
                        showToast("Failed to sign in: $it")
                    } }
                )
                // Sign in the user using the entered email and password
            } else {
                // If email or password is empty, show an error message using a Toast
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.textView.setOnClickListener { // set an onClickListener for the textView in the layout
            val intent = Intent(
                this,
                Register::class.java
            ) // create an Intent to navigate to the Register activity
            startActivity(intent) // start the activity using the created Intent
        }
    }
}