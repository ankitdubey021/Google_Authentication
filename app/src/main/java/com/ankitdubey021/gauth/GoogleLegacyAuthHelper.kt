package com.ankitdubey021.gauth

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleLegacyAuthHelper(private val activity: Activity) {

    private var googleSignInClient: GoogleSignInClient? = null

    fun setup(launcher: ActivityResultLauncher<Intent>) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)

        signOut {
            // After signing out, initiate sign-in
            val signInIntent = googleSignInClient!!.signInIntent
            launcher.launch(signInIntent)
        }
    }

    fun handleSignInResult(data: Intent?, onSuccess: (String?) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            onSuccess(account.email)  // Pass the email to the callback
        } catch (e: ApiException) {
            Log.w("GoogleAuthHelper", "signInResult:failed code=" + e.statusCode)
            onSuccess(null)  // Handle sign-in failure
        }
    }

    private fun signOut(onComplete: () -> Unit) {
        googleSignInClient?.signOut()?.addOnCompleteListener { onComplete() }
    }

    companion object {
        private const val WEB_CLIENT =
            "705507059964-o9vftoq3m5vs3lfpo72hltvib0rd26h7.apps.googleusercontent.com"
    }
}