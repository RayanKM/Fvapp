package com.example.fvapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class Splash : AppCompatActivity() {
    // A constant value "splash_time" is declared with the type "Long".
    private val splash_time : Long = 1000

    // This method is called when the activity is starting.
    override fun onCreate(savedInstanceState: Bundle?) {

        // Sets the activity to full-screen.
        requestWindowFeature(1)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.statusBarColor = Color.TRANSPARENT

        // Calls the superclass onCreate method, sets the layout to be displayed.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean("dark_mode_enabled", false)
        ThemeManager.applyTheme(isDarkModeEnabled, this)
        // A handler is created to delay the start of the "MainActivity" activity for "splash_time" duration.
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, splash_time)
    }
}
