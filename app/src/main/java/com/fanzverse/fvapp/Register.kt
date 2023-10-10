package com.fanzverse.fvapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Usr
import com.fanzverse.fvapp.databinding.ActivityRegisterBinding
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Handle click on Login TextView
        binding.textView.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()
            val username = binding.nameEt.text.toString()
            val name = binding.name.text.toString()

            // Check if all fields are not empty
            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty() && username.isNotEmpty()) {
                // Check if passwords match
                if (pass == confirmPass) {
                    val options = AuthSignUpOptions.builder()
                        .userAttribute(AuthUserAttributeKey.email(), email)
                        .build()
                    Amplify.Auth.signUp(username, pass, options,
                        {
                            createUser(name,username,email,pass)
                        },
                        { error ->
                            runOnUiThread {
                                showToast(error.localizedMessage!!)
                            }
                        }
                    )
                } else {
                    // If passwords don't match, display error message
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                // If any field is empty, display error message
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun createUser(name:String, username:String, email:String,password:String){
        Amplify.Auth.signIn(username, password,
            { result ->
                if (result.isSignedIn) {
                    val user = Usr.builder()
                        .username(username)
                        .fullname(name)
                        .email(email)
                        .pfp("")
                        .build()
                    Amplify.API.mutate(
                        ModelMutation.create(user),
                        { response ->
                            runOnUiThread {
                                // Display success toast message using MotionToast library
                                MotionToast.createColorToast(
                                    this,
                                    "SignUp Successful",
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
                            // This block is executed when the mutation is successful
                            Log.i("MyAmplifyApp", "Todo with id: ${response.data.id}")
                            // Handle any other logic you need here for a successful mutation
                        },
                        { error ->
                            // This block is executed when there's an error during the mutation
                            Log.e("MyAmplifyApp", "Create failed", error)
                            // Handle the error appropriately
                        }
                    )
                } else {
                    Log.e("MyAmplifyApp", "non")
                }
            },
            {runOnUiThread {
                Log.e("MyAmplifyApp", "log failed", it)
            }
            }
        )
    }

}