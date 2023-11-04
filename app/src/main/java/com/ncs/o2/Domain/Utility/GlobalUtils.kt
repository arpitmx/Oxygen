package com.ncs.o2.Domain.Utility

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.ncs.o2.R
import timber.log.Timber
import java.util.TimeZone


object GlobalUtils {


    class EasyElements(val context: Context) {
        fun dialog(title: String, msg: String, postiveText : String, negativeText : String ,positive: () -> Unit, negative: () -> Unit) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(msg)
            builder.setPositiveButton(postiveText) { dialog, which ->
                positive()
            }
            builder.setNegativeButton(negativeText) { dialog, which ->
                negative()
            }
            val dialog = builder.create()
            dialog.show()
        }



        fun singleBtnDialog(title: String, msg: String, btnText: String, positive: () -> Unit) {
            val builder = android.app.AlertDialog.Builder(context)
            builder.setIcon(R.drawable.logogradhd)
            builder.setTitle(title)
            builder.setMessage(msg)
            builder.setPositiveButton(btnText) { dialog, which ->
                positive()
            }
            val dialog = builder.create()
            dialog.show()
        }

        fun singleBtnDialog_InputError(title: String, msg: String, btnText: String, positive: () -> Unit) {
            val builder = android.app.AlertDialog.Builder(context)
            builder.setIcon(R.drawable.logogradhd)
            builder.setTitle(title)
            builder.setMessage(msg)
            builder.setPositiveButton(btnText) { dialog, which ->
                positive()
            }
            val dialog = builder.create()
            dialog.show()
        }

        fun singleBtnDialog_ErrorConnection(title: String, msg: String, btnText: String, positive: () -> Unit) {
            val builder = android.app.AlertDialog.Builder(context)
            builder.setIcon(R.drawable.baseline_signal_wifi_connected_no_internet_4_24)
            builder.setTitle(title)
            builder.setMessage(msg)
            builder.setPositiveButton(btnText) { dialog, which ->
                positive()
            }
            val dialog = builder.create()
            dialog.show()
        }

        fun showActionSnackbar(rootView: View, msg: String, duration: Int,actionText : String, action: () -> Unit) {
            Snackbar.make(rootView, msg, duration)
                .setAction(actionText) {
                    action()
                }
                .show()
        }

        fun showSnackbar(rootView: View, msg: String, duration: Int) {


            val snackbar = Snackbar.make(rootView, msg, duration)
            snackbar.setTextColor(Color.BLACK)
            snackbar.setBackgroundTint(Color.WHITE)
            snackbar.show()



        }


        fun loader(visible: Int) {
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Loading...")
            progressDialog.setMessage("Please wait.")
            progressDialog.isIndeterminate = true
            progressDialog.setCancelable(false)
            when (visible) {
                1 -> progressDialog.show()
                0 -> progressDialog.dismiss()
            }

        }
    }


    class EasyChecking {

        fun checkTimeZoneValid(timeZone: TimeZone): Boolean {
            Timber.tag("GlobalUtils-Debug101").d(timeZone.displayName)
            if (timeZone.displayName != Codes.STRINGS.TIME_ZONE_INDIA) {
                return false
            }

            return true
        }
    }

    class EasyLogging(val filename: String, val context: Context) {


        //Debug101 = Debug
        val extension = "-Debug101"


        fun lgd(msg: String) {
            Timber.tag(filename + extension).d(msg)
        }

        fun lge(msg: String) {
            Timber.tag(filename + extension).e(msg)
        }

        fun tst(msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }


    }


}