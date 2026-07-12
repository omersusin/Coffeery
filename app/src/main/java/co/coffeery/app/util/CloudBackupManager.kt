package co.coffeery.app.util

import android.content.Context
import android.content.Intent
import co.coffeery.app.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class CloudBackupManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("cloud", Context.MODE_PRIVATE)

    fun isSignedIn(): Boolean = prefs.getBoolean("signed_in", false)
    fun getAccountEmail(): String? = prefs.getString("account_email", null)

    fun getProfilePhotoUrl(): android.net.Uri? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account?.photoUrl
    }

    fun getSignInClient(): GoogleSignInClient {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestIdToken(context.getString(R.string.google_server_client_id))
        return GoogleSignIn.getClient(context, builder.build())
    }

    fun getSignInIntent(client: GoogleSignInClient): Intent = client.signInIntent

    fun handleSignInResult(data: Intent?, onResult: (Boolean) -> Unit) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                prefs.edit()
                    .putBoolean("signed_in", true)
                    .putString("account_email", account.email)
                    .apply()
                onResult(true)
            } else {
                onResult(false)
            }
        } catch (e: ApiException) {
            onResult(false)
        }
    }

    fun signOut(client: GoogleSignInClient) {
        prefs.edit().putBoolean("signed_in", false).remove("account_email").apply()
        client.signOut().addOnCompleteListener {
            client.revokeAccess()
        }
    }
}
