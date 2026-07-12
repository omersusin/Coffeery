package co.coffeery.app.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import co.coffeery.app.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CloudBackupManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("cloud", Context.MODE_PRIVATE)

    fun isSignedIn(): Boolean = prefs.getBoolean("signed_in", false)
    fun getAccountEmail(): String? = prefs.getString("account_email", null)

    fun getProfilePhotoUrl(): android.net.Uri? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account?.photoUrl
    }

    fun isPlayServicesAvailable(): Boolean {
        val result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        return result == ConnectionResult.SUCCESS
    }

    fun silentSignIn() {
        val client = getSignInClient()
        client.silentSignIn().addOnCompleteListener { task: Task<GoogleSignInAccount> ->
            if (task.isSuccessful && task.result != null) {
                val account = task.result
                prefs.edit()
                    .putBoolean("signed_in", true)
                    .putString("account_email", account.email)
                    .apply()
            }
        }
    }

    fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(Scope("https://www.googleapis.com/auth/drive.appdata"))
            .requestIdToken(context.getString(R.string.google_server_client_id))
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(client: GoogleSignInClient): Intent = client.signInIntent

    fun handleSignInResult(data: Intent?, onResult: (Boolean, String) -> Unit) {
        try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                prefs.edit()
                    .putBoolean("signed_in", true)
                    .putString("account_email", account.email)
                    .apply()
                Log.d("Coffeery", "Google Sign-In SUCCESS: ${account.email}")
                onResult(true, account.email ?: "")
            } else {
                onResult(false, "Account was null")
            }
        } catch (e: ApiException) {
            val code = e.statusCode
            val msg = when (code) {
                12500 -> "Error 12500: Check Support Email in GCC OAuth consent screen, and SHA-1 matches"
                12501 -> "Error 12501: Sign-in cancelled or Web Client ID mismatch"
                10 -> "Error 10: SHA-1 fingerprint mismatch. Run ./gradlew signingReport"
                8 -> "Error 8: Network error. Check internet connection."
                13 -> "Error 13: Play Services outdated. Update Google Play Services."
                15 -> "Error 15: Wrong package name or SHA-1 in GCC"
                else -> "Error $code: ${e.localizedMessage ?: "Unknown"}"
            }
            Log.e("Coffeery", "Google Sign-In FAILED: $msg", e)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            onResult(false, msg)
        } catch (e: Exception) {
            val msg = "Error: ${e.javaClass.simpleName} — ${e.localizedMessage ?: "Unknown"}"
            Log.e("Coffeery", "Google Sign-In CRASH: $msg", e)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            onResult(false, msg)
        }
    }

    suspend fun backupToDrive(activity: Activity, jsonData: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context) ?: return@withContext Result.failure(Exception("Not signed in"))
            val credential = GoogleAccountCredential.usingOAuth2(context, listOf("https://www.googleapis.com/auth/drive.appdata"))
            credential.selectedAccount = account.account

            val httpTransport = com.google.api.client.http.javanet.NetHttpTransport()
            val jsonFactory = com.google.api.client.json.gson.GsonFactory.getDefaultInstance()
            val drive = com.google.api.services.drive.Drive.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("Coffeery")
                .build()

            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
            val metadata = com.google.api.services.drive.model.File()
                .setName("coffeery_backup_$timestamp.json")
                .setParents(listOf("appDataFolder"))
                .setMimeType("application/json")

            val content = com.google.api.client.http.ByteArrayContent.fromString("application/json", jsonData)
            val file = drive.files().create(metadata, content)
                .setFields("id,name,modifiedTime")
                .execute()
            Result.success(file.name)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromDrive(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context) ?: return@withContext Result.failure(Exception("Not signed in"))
            val credential = GoogleAccountCredential.usingOAuth2(context, listOf("https://www.googleapis.com/auth/drive.appdata"))
            credential.selectedAccount = account.account

            val httpTransport = com.google.api.client.http.javanet.NetHttpTransport()
            val jsonFactory = com.google.api.client.json.gson.GsonFactory.getDefaultInstance()
            val drive = com.google.api.services.drive.Drive.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("Coffeery")
                .build()

            val files = drive.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id,name,modifiedTime)")
                .setOrderBy("modifiedTime desc")
                .setPageSize(1)
                .execute()
                .files ?: return@withContext Result.failure(Exception("No backup found"))

            val latestFile = files.first()
            val outputStream = java.io.ByteArrayOutputStream()
            drive.files().get(latestFile.id).executeMediaAndDownloadTo(outputStream)
            Result.success(outputStream.toString("UTF-8"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut(client: GoogleSignInClient) {
        prefs.edit().putBoolean("signed_in", false).remove("account_email").apply()
        client.signOut().addOnCompleteListener {
            client.revokeAccess().addOnCompleteListener {
                Log.d("Coffeery", "Google Sign-Out complete")
            }
        }
    }
}
