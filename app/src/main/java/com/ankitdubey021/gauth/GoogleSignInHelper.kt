package com.ankitdubey021.gauth

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleSignInHelper(private val activity: MainActivity) {
    fun signInViaGoogle(callback: (response: GoogleSignInResponse) -> Unit) {
        //Initialise credentialManager
        val credentialManager = CredentialManager.create(activity)

        //Setting up credential option for google sign-in
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) //to filter out already signed up users
            .setServerClientId(WEB_CLIENT) //project's OAuth 2.0 web client id
            .setAutoSelectEnabled(true) //if there is just a single account logged in in a device that will be auto selected
            .build()

        //building the request body
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        //handling sign up in a background thread
        CoroutineScope(Dispatchers.Default).launch {
            try {
                //trying to get the credential
                val result = credentialManager.getCredential(
                    request = request,
                    context = activity
                )
                withContext(Dispatchers.Main.immediate) {
                    //If it is handled properly, we will return the profile details
                    callback(handleSignIn(result))
                }
            } catch (e: GetCredentialException) {
                //catching and returning exceptions
                withContext(Dispatchers.Main.immediate) {
                    callback(GoogleSignInFailed(e.message ?: "Unknown error"))
                }
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse): GoogleSignInResponse {
        //When we set credentialOption as GetGoogleIdOption, the result.credential should be of CustomCredential type only
        when (val credential = result.credential) {
            is CustomCredential -> {
                //the type of credential should of GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL only
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        //googleIdTokenCredential.idToken can be used in backend to further verify the account details
                        return GoogleSignInSuccess(
                            (googleIdTokenCredential.displayName ?: ""),
                            googleIdTokenCredential.id,
                            googleIdTokenCredential.idToken
                        )
                    } catch (e: GoogleIdTokenParsingException) {
                        return GoogleSignInFailed(e.message ?: "Google Id token parsing error")
                    }
                } else {
                    return GoogleSignInFailed("Invalid credential type")
                }
            }

            else -> {
                return GoogleSignInFailed("Invalid credential option")
            }
        }
    }

    companion object {
        private const val ANDROID_CLIENT_ID = "705507059964-6meolrfr2u0kuul8ip88oujjvtvlfmgr.apps.googleusercontent.com"
        private const val WEB_CLIENT = "705507059964-o9vftoq3m5vs3lfpo72hltvib0rd26h7.apps.googleusercontent.com"
    }
}