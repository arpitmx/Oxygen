package com.ncs.o2.Services.Updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import timber.log.Timber

class DownloadCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        if (downloadId != null && downloadId != -1L) {
            installDownloadedApk(context, downloadId)
            Timber.tag("DownloadCompleteReceiver : Recieved")
        }
    }
}

private fun installDownloadedApk(context: Context?, downloadId: Long) {
    val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val query = DownloadManager.Query().setFilterById(downloadId)
    val cursor = downloadManager.query(query)

    if (cursor.moveToFirst()) {
        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

        if (columnIndex != -1) {
            val downloadStatus = cursor.getInt(columnIndex)

            if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                val uriColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

                if (uriColumnIndex != -1) {
                    val uri = Uri.parse(cursor.getString(uriColumnIndex))
                    installApk(context, uri)
                } else {
                    // Handle the case where the URI column is not present
                }
            } else {
                // Handle the case where the download status is not successful
            }
        } else {
            // Handle the case where the status column is not present
        }
    } else {
        // Handle the case where the cursor is empty
    }

    cursor.close()
}

private fun installApk(context: Context?, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context?.startActivity(intent)
}