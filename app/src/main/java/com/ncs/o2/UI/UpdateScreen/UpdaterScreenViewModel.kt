package com.ncs.o2.UI.UpdateScreen

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat.getExternalFilesDirs
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncs.o2.HelperClasses.PrefManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class UpdaterScreenViewModel(val context: Application) : AndroidViewModel(context) {


    private val TAG = "UpdaterViewModel"
    private var progress = 0
    private var finishDownload = false
    private var downloadID: Long = -1
    lateinit var downloadURI: Uri
    private val DIRECTORY = "${Environment.DIRECTORY_DOWNLOADS}/Updates/"
    private var isFiredOnce = false


    private val _downloadStatus = MutableLiveData<DownloadStatus>()
    val downloadStatus: LiveData<DownloadStatus>
        get() = _downloadStatus

    fun deleteDownloadedFile(downloadID : Long) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.remove(downloadID)
    }

    fun updateDownloadStatus(progress: Int, status: Int) {
        val downloadStatus = DownloadStatus(progress, status)
        _downloadStatus.postValue(downloadStatus)
    }

    fun resetValues() {
        progress = 0
        downloadID = -1
        isFiredOnce = false
    }

    fun startUpdater(update_url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            downloadApkWithDownloadManager(update_url)
        }
    }

    private fun downloadApkWithDownloadManager(url: String) {

        if (!isFiredOnce) {

            isFiredOnce = true
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(url)

            Timber.tag(TAG).d("Directory : ${DIRECTORY}")
            val request = DownloadManager.Request(uri)
                .setTitle("O2 Update")
                .setDescription("Download in progress...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalFilesDir(context, DIRECTORY, "O2app.apk")


            downloadID = downloadManager.enqueue(request)
            progress = 0

            while (!finishDownload) {
                val cursor =
                    downloadManager.query(DownloadManager.Query().setFilterById(downloadID))

                if (cursor.moveToFirst()) {

                    val status =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))

                    when (status) {

                        DownloadManager.STATUS_FAILED -> {
                            finishDownload = true
                            Timber.tag("Download Manager").d("STATUS FAILED")
                            updateDownloadStatus(0, DownloadManager.STATUS_FAILED)
                            deleteDownloadedFile(downloadID)
                        }

                        DownloadManager.STATUS_PAUSED -> {
                            Timber.tag("Download Manager").d("STATUS PAUSED")
                            updateDownloadStatus(progress, DownloadManager.STATUS_PAUSED)
                        }

                        DownloadManager.STATUS_PENDING -> {
                            Timber.tag("Download Manager").d("STATUS PENDING")
                            updateDownloadStatus(progress, DownloadManager.STATUS_PENDING)
                        }

                        DownloadManager.STATUS_RUNNING -> {

                            val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            if (total > 0) {
                                val downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                progress = ((downloaded * 100L) / total).toInt()
                                Timber.tag(TAG).d("Progress : ${progress}")

                                updateDownloadStatus(progress, DownloadManager.STATUS_RUNNING)
                            }
                        }

                        DownloadManager.STATUS_SUCCESSFUL -> {

                            Timber.tag(TAG).d("Download finished")
                            progress = 100
                            finishDownload = true
                            downloadURI = downloadManager.getUriForDownloadedFile(downloadID)

                            PrefManager.setDownloadID(downloadID)
                            updateDownloadStatus(progress, DownloadManager.STATUS_SUCCESSFUL)

                        }
                    }
                }
                cursor.close()
            }
        } else {
            Timber.tag(TAG).d("Tried Downloading multiple times")
        }
    }
}