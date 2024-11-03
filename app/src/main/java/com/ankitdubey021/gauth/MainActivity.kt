package com.ankitdubey021.gauth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var googleAuthHelper: GoogleLegacyAuthHelper
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        googleAuthHelper = GoogleLegacyAuthHelper(this)

        // Register the launcher for Google sign-in
        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // If the result was successful, handle the sign-in result
                googleAuthHelper.handleSignInResult(result.data) { email ->
                    email?.let {
                        findViewById<TextView>(R.id.tv).text = it
                    } ?: run {
                        // Handle failure (e.g., show a toast or log)
                    }
                }
            }
        }

        /*findViewById<Button>(R.id.sign_in_button).setOnClickListener {
            signIn()
        }*/

        findViewById<Button>(R.id.sign_in_button).setOnClickListener {
            googleAuthHelper.setup(signInLauncher)  // Pass the launcher to the helper
        }
    }

    private fun signIn() {
        GoogleSignInHelper(this).signInViaGoogle {
            when (it) {
                is GoogleSignInSuccess -> {
                    Log.d("GoogleSignIn", "Success: ${it.displayName} ${it.email}")
                }

                is GoogleSignInFailed -> {
                    Log.d("GoogleSignIn", "Failed: ${it.message}")
                }
            }
        }
    }
}