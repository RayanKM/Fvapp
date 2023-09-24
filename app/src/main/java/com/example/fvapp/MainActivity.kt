package com.example.fvapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.core.Amplify
import com.example.fvapp.databinding.ActivityMainBinding

// Import other query classes as needed

class MainActivity : AppCompatActivity(), Communicator {
    companion object {
        var userN: String? = null?:"" // Change YourType to the actual type of your global variable
    }
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val options = AuthSignOutOptions.builder()
            .globalSignOut(true)
            .build()
        Amplify.Auth.fetchAuthSession(
            { authSession ->
                val isSignedIn = authSession.isSignedIn
                if (isSignedIn) {
                    Log.e("MyAmplifyApp", "yes")
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.mn, Home())
                        commit()
                    }
                    Amplify.Auth.getCurrentUser(
                        { user ->
                            userN = user.username
                            Log.e("MyAmplifyApp", "Logged in as: $userN")
                        },
                        { error ->
                            // Handle the error when fetching the current user
                            Log.e("MyAmplifyApp", "Error fetching current user: $error")
                        }
                    )
                }
                else {
                    Log.e("MyAmplifyApp", "Not logged in")
                    startActivity(Intent(this, Login::class.java))
                }
            },
            { error ->
                // Handle the error when fetching the auth session
                Log.e("MyAmplifyApp", "Error fetching auth session: $error")
            }
        )
        binding.Navbt.setOnItemSelectedListener {
            when (it) {
                R.id.Home -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.mn, Home())
                        commit()
                    }
                }
                R.id.Profile -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.mn, Profile())
                        commit()
                    }
                }
                R.id.Search -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.mn, SearchProfile())
                        commit()
                    }
                }
                R.id.Notif -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.mn, Notifications())
                        commit()
                    }
                }
            }
        }
    }
    override fun passdata(post: PosDataModel) {
        val bundle = Bundle()
        bundle.putParcelable("post", post) // Replace "post" with the key you want to use

        // Perform the fragment transaction
        val transaction = this.supportFragmentManager.beginTransaction()
        val frg = PostDetail()
        frg.arguments = bundle

        transaction.replace(R.id.mn, frg)
        transaction.addToBackStack(null)
        transaction.commit()
    }
    override fun passid(id: String) {
        val bundle = Bundle()
        bundle.putString("id", id) // Replace "post" with the key you want to use

        // Perform the fragment transaction
        val transaction = this.supportFragmentManager.beginTransaction()
        val frg = Profile()
        frg.arguments = bundle
        transaction.replace(R.id.mn, frg)
        transaction.addToBackStack(null)
        transaction.commit()
    }
    fun setSelectedItem(itemId: Int) {
        binding.Navbt.setItemSelected(itemId)
    }
    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            fragmentManager.addOnBackStackChangedListener {
                val currentFragment = fragmentManager.findFragmentById(R.id.mn)
                if (currentFragment is Home){
                    binding.Navbt.setItemSelected(R.id.Home)
                }else if (currentFragment is SearchProfile){
                    binding.Navbt.setItemSelected(R.id.Search)
                }else if (currentFragment is Notifications){
                binding.Navbt.setItemSelected(R.id.Notif)
                }else if (currentFragment is Profile){
                    binding.Navbt.setItemSelected(R.id.Profile)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.e("qsdqzdfqsdqsd", "RESTARTED!!!!!!!!")
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(R.id.mn)
        Log.e("qsdqzdfqsdqsd", "$currentFragment")
    }
}
