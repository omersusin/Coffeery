package co.coffeery.app.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import co.coffeery.app.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Collections
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CloudBackupManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("cloud", Context.MODE_PRIVATE)
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val transport = NetHttpTransport()

    fun isSignedIn(): Boolean = prefs.getBoolean("signed_in", false)

    fun getAccountEmail(): String? = prefs.getString("account_email", null)

    fun getServerClientId(): String = context.getString(R.string.google_server_client_id)

    fun getProfilePhotoUrl(): android.net.Uri? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account?.photoUrl
    }

    fun getSignInClient(): GoogleSignInClient {
        val serverClientId = context.getString(R.string.google_server_client_id)
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
        if (!serverClientId.startsWith("YOUR_")) {
            builder.requestIdToken(serverClientId)
        }
        return GoogleSignIn.getClient(context, builder.build())
    }

    fun getSignInIntent(client: GoogleSignInClient): Intent = client.signInIntent

    fun handleSignInResult(data: Intent?, onResult: (Boolean) -> Unit) {
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java)
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
        client.signOut()
    }

    suspend fun backupToDrive(activity: Activity, dbFile: java.io.File): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val credential = buildCredential()
                val drive = Drive.Builder(transport, jsonFactory, credential)
                    .setApplicationName("Coffeery")
                    .build()

                val timestamp = java.text.SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    java.util.Locale.US
                ).format(java.util.Date())
                val zipName = "coffeery_backup_$timestamp.db.zip"

                val zipBytes = zipFile(dbFile, "coffeery_backup.db")

                val metadata = File()
                    .setName(zipName)
                    .setParents(Collections.singletonList("appDataFolder"))

                val mediaContent = com.google.api.client.http.ByteArrayContent(
                    "application/zip", zipBytes
                )

                drive.files().create(metadata, mediaContent).execute()
                Result.success(zipName)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun restoreFromDrive(activity: Activity, dbFile: java.io.File): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val credential = buildCredential()
                val drive = Drive.Builder(transport, jsonFactory, credential)
                    .setApplicationName("Coffeery")
                    .build()

                val files = drive.files().list()
                    .setSpaces("appDataFolder")
                    .setOrderBy("modifiedTime desc")
                    .setPageSize(10)
                    .execute()
                    .files ?: emptyList()

                if (files.isEmpty()) return@withContext Result.failure(
                    Exception("No backup found in Drive")
                )

                val latestFile = files.first()
                val outputStream = ByteArrayOutputStream()
                drive.files().get(latestFile.id).executeMediaAndDownloadTo(outputStream)

                unzipFile(outputStream.toByteArray(), dbFile)
                Result.success(latestFile.name)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun buildCredential(): GoogleAccountCredential {
        val account = GoogleSignIn.getLastSignedInAccount(context)
            ?: throw IllegalStateException("Not signed in")
        return GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singleton(DriveScopes.DRIVE_APPDATA)
        ).setSelectedAccount(account.account!!)
    }

    private fun zipFile(sourceFile: java.io.File, entryName: String): ByteArray {
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { zos ->
            zos.putNextEntry(ZipEntry(entryName))
            FileInputStream(sourceFile).use { it.copyTo(zos) }
            zos.closeEntry()
        }
        return bos.toByteArray()
    }

    private fun unzipFile(zipBytes: ByteArray, targetFile: java.io.File) {
        ZipInputStream(zipBytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    FileOutputStream(targetFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }
}
