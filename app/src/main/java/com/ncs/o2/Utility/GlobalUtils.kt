package com.ncs.o2.Utility

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.TimeZone

object GlobalUtils {


    class EasyElements(val context: Context) {
        fun dialog(title: String, msg: String, positive: () -> Unit, negative: () -> Unit) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(msg)
            builder.setPositiveButton("OK") { dialog, which ->
                positive()
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                negative()
            }
            val dialog = builder.create()
            dialog.show()
        }


        fun singleBtnDialog(title: String, msg: String, btnText: String, positive: () -> Unit) {
            val builder = android.app.AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(msg)
            builder.setPositiveButton(btnText) { dialog, which ->
                positive()
            }
            val dialog = builder.create()
            dialog.show()
        }

        fun showSnackbar(rootView: View, msg: String, duration: Int, action: () -> Unit) {
            Snackbar.make(rootView, msg, duration)
                .setAction("Retry") {
                    action()
                }
                .show()

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
            if (timeZone.displayName != codes.STRINGS.TIME_ZONE_INDIA) {
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