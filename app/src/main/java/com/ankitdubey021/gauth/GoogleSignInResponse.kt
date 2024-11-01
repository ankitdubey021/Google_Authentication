package com.ankitdubey021.gauth

sealed class GoogleSignInResponse

data class GoogleSignInSuccess(
    val displayName: String, val email: String, val idToken: String
) : GoogleSignInResponse()

data class GoogleSignInFailed(
    val message: String
) : GoogleSignInResponse()