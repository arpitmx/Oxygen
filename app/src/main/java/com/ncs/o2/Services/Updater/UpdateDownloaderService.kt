package com.ncs.o2.Services.Updater

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.StartScreen.StartScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class UpdateDownloaderService : Service() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "UC912D"
        private const val NOTIFICATION_ID = 1
        private const val APK_URL = "https://github.com/arpitmx/O2/releases/download/testing/app-de.apk"
        //private const val APK_URL = "https://github.com/arpitmx/O2/releases/download/testing/Facebook.Lite_391.0.0.0.4_Apkpure.apk"
        private const val TAG = "UPDATER SERVICE 1"
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationLayout: RemoteViews

    override fun onCreate() {
        super.onCreate()

        Timber.tag(TAG).d("Updater running...")

        CoroutineScope(Dispatchers.IO).launch {
            downloadApkwithDownloadManager()
        }

    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //createNotificationChannel()
        //startForeground(NOTIFICATION_ID, createNotification())



        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Updater Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): android.app.Notification {


        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)


        notificationLayout = RemoteViews(this.packageName, R.layout.updater_notification)
        notificationLayout.setProgressBar(R.id.progressBar, 100, 0, false)

        notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Downloading APK")
            .setContentText("Download in progress...")
            .setSmallIcon(R.drawable.notifoxygen)
            .setContentIntent(pendingIntent)
            .setCustomContentView(notificationLayout)
            .setAutoCancel(true)

        return notificationBuilder.build()
    }

    private suspend fun downloadApk() {
        withContext(Dispatchers.IO) {
            try {

                Timber.tag(TAG).d("Updater downloading")

                val url = URL(APK_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                Timber.tag(TAG).d("URL : ${url}")


                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    Timber.tag(TAG).d("Server returned HTTP ${connection.responseCode}")
                    return@withContext
                }

                val apkFile =
                    File(cacheDir, "app.apk")
                val output = FileOutputStream(apkFile)
                val input: InputStream = connection.inputStream
                val buffer = ByteArray(4096)
                val totalSize = connection.contentLength.toLong()
                var downloadedSize: Long = 0

                Timber.tag(TAG).d("Total size ${totalSize}")


                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)

                    downloadedSize += bytesRead

                    val progress = ((downloadedSize * 100) / totalSize).toInt()
                    updateProgress(progress)

                    Timber.tag(TAG).d("Written ${bytesRead}")
                }

                output.close()
                input.close()

                Timber.tag(TAG).d("File downloaded ${apkFile.totalSpace}")

                stopForeground(true)
                notificationManager.cancel(NOTIFICATION_ID)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e)
            }
        }
    }

    private fun updateProgress(progress: Int) {
        notificationLayout.setProgressBar(R.id.progressBar, 100, progress, false)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }



    private suspend fun downloadApkwithDownloadManager() {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(APK_URL)

        val request = DownloadManager.Request(uri)
            .setTitle("O2 Update")
            .setDescription("Download in progress...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)
            .setDestinationInExternalFilesDir(this,Environment.DIRECTORY_DOWNLOADS , "O2app.apk")

       val downloadId =  downloadManager.enqueue(request)

        var finishDownload = false
        var progress = 0

        while (!finishDownload) {
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))

            if (cursor.moveToFirst()) {

                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))

                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        finishDownload = true
                        Timber.tag("Download Manager").d("Download failed")
                    }
                    DownloadManager.STATUS_PAUSED -> {

                    }
                    DownloadManager.STATUS_PENDING -> {

                    }
                    DownloadManager.STATUS_RUNNING -> {
                        val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (total >= 0) {
                            val downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            progress = ((downloaded * 100L) / total).toInt()
                            Timber.tag("Download Manager").d("Progress : ${progress}")

                        }
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        Timber.tag("Download Manager").d("Download finished")
                        progress = 100
                        finishDownload = true

                        installApk(applicationContext, downloadManager.getUriForDownloadedFile(downloadId))
                    }
                }
            }
            cursor.close()
        }

    }

    private suspend fun installApk(context: Context?, uri: Uri) {

        Timber.tag("Download Manager").d("Installing...")


        withContext(Dispatchers.Main){
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val packageManager: PackageManager = context!!.packageManager
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent)
            } else {
                Timber.tag("Download Manager").d("No application found to install the app")
            }

        }


    }


}
