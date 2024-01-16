package com.ncs.o2.HelperClasses

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.ncs.o2.Domain.Utility.GlobalUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NetworkChangeReceiver(
    context: Context?,
    private val callback: NetworkChangeCallback
) : BroadcastReceiver() {

    private val easyElements: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(context!!)
    }

    private var isNetworkConnected = true
    private var isOfflineDialogShown = false

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            val connectivityManager =
                context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo

            if (networkInfo != null && networkInfo.isConnected && !isNetworkConnected) {
                isNetworkConnected = true
                showOnlineDialog()
                PrefManager.setOfflineDialogShown(false)

            } else if (networkInfo == null || !networkInfo.isConnected) {
                isNetworkConnected = false

                if (!PrefManager.hasOfflineDialogBeenShown()) {
                    showOfflineDialog()
                    PrefManager.setOfflineDialogShown(true)
                }
            }
        }
    }

    private fun showOnlineDialog() {
        isOfflineDialogShown = false
        easyElements.twoBtn(
            "Network is Available",
            "Continue with Online mode",
            "Restart",
            "Cancel",
            {
                callback.onOnlineModePositiveSelected()
            },
            {
            }
        )
    }

    private fun showOfflineDialog() {
        easyElements.twoBtn(
            "Network is down",
            "Continue with offline mode",
            "Offline Mode",
            "Retry",
            {
                callback.onOfflineModePositiveSelected()
            },
            {
                retryNetworkCheck()
            }
        )
    }

    fun retryNetworkCheck() {
        val connectivityManager =
            easyElements.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            isNetworkConnected = true
            showOnlineDialog()
        } else {
            isNetworkConnected = false
            showOfflineDialog()
        }
    }

    interface NetworkChangeCallback {
        fun onOnlineModePositiveSelected()
        fun onOfflineModePositiveSelected()
        fun onOfflineModeNegativeSelected()
    }
}
