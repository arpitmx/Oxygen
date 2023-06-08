package com.ncs.o2.Domain.Utility

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.integrity.internal.l
import com.ncs.o2.R
import com.ncs.o2.Domain.Utility.ExtensionsUtil.bounce
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setSingleClickListener
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/*
File : ExtensionsUtil.kt -> com.ncs.versa.Utility
Description : This file contains extension functions for different datatypes for easting out the development process 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : Versa Android)

Creation : 3:16 pm on 25/05/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/


object ExtensionsUtil {

    //Logging extensions

    fun Any?.printToLog(tag: String = "Debug Log") {
        Timber.tag(tag).d(toString())
    }


    // Visibililty Extensions

    fun View.gone() = run { visibility = View.GONE }
    fun View.visible() = run { visibility = View.VISIBLE }
    fun View.invisible() = run { visibility = View.INVISIBLE }

    infix fun View.visibleIf(condition: Boolean) =
        run { visibility = if (condition) View.VISIBLE else View.GONE }

    infix fun View.goneIf(condition: Boolean) =
        run { visibility = if (condition) View.GONE else View.VISIBLE }

    infix fun View.invisibleIf(condition: Boolean) =
        run { visibility = if (condition) View.INVISIBLE else View.VISIBLE }


    fun View.progressGone(context: Context, duration: Long = 1500L) = run {
        animFadeOut(context,duration )
        visibility = View.GONE

    }
    fun View.progressVisible(context: Context, duration: Long = 1500L) = run {
        visibility = View.VISIBLE
        animFadein(context, duration )
    }



    // Toasts

    fun Fragment.toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    fun Fragment.toast(@StringRes message: Int) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    fun Activity.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun Activity.toast(@StringRes message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


    //Snackbar

    fun View.snackbar(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(this, message, duration).setAnimationMode(ANIMATION_MODE_SLIDE).show()
    }

    fun View.snackbar(@StringRes message: Int, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(this, message, duration).show()
    }


    fun Activity.hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: View(this)
        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun Fragment.hideKeyboard() {
        activity?.apply {
            val imm: InputMethodManager =
                getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            val view = currentFocus ?: View(this)
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }


    // Convert px to dp
    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()

    //Convert dp to px
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()


    val String.isDigitOnly: Boolean
        get() = matches(Regex("^\\d*\$"))

    val String.isAlphabeticOnly: Boolean
        get() = matches(Regex("^[a-zA-Z]*\$"))

    val String.isAlphanumericOnly: Boolean
        get() = matches(Regex("^[a-zA-Z\\d]*\$"))


    //Null check
    val Any?.isNull get() = this == null

    fun Any?.ifNull(block: () -> Unit) = run {
        if (this == null) {
            block()
        }
    }

    //Date formatting
    fun String.toDate(format: String = "yyyy-MM-dd HH:mm:ss"): Date? {
        val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
        return dateFormatter.parse(this)
    }

    fun Date.toStringFormat(format: String = "yyyy-MM-dd HH:mm:ss"): String {
        val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
        return dateFormatter.format(this)
    }


    //Network check
    @RequiresApi(Build.VERSION_CODES.M)
    fun Context.isNetworkAvailable(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
        return if (capabilities != null) {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else false
    }


    //Permission
    fun Context.isPermissionGranted(permission: String) = run {
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }


    //Animation
    fun View.animSlideUp(context: Context, animDuration: Long = 1500L) = run {
        this.clearAnimation()
        val animation = AnimationUtils.loadAnimation(context, R.anim.slide_bottom_to_up)
            .apply {
                duration = animDuration
            }
        this.startAnimation(animation)
    }

    fun View.animSlideDown(context: Context, animDuration: Long = 1500L) = run {
        this.clearAnimation()
        val animation = AnimationUtils.loadAnimation(context, me.shouheng.utils.R.anim.slide_top_to_bottom)
            .apply {
                duration = animDuration
            }
        this.startAnimation(animation)
    }

    fun View.animSlideLeft(context: Context, animDuration: Long = 1500L) = run {
        this.clearAnimation()
        val animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
            .apply {
                duration = animDuration
            }
        this.startAnimation(animation)
    }

    fun View.animSlideRight(context: Context, animDuration: Long = 1500L) = run {
        this.clearAnimation()
        val animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right)
            .apply {
                duration = animDuration
            }
        this.startAnimation(animation)
    }

    fun View.animFadein(context: Context, animDuration: Long = 1500L) = run {
        this.clearAnimation()
        val animation = AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
            .apply {
                duration = animDuration
            }
        this.startAnimation(animation)
    }


    fun View.rotate180(context: Context, animDuration: Long = 500L) = run {
        this.clearAnimation()
        val animation = AnimationUtils.loadAnimation(context, R.anim.rotate180)
            .apply {
                duration = animDuration
            }
        this.startAnimation(animation)
    }

    fun View.animFadeOut(context: Context, animDuration: Long = 1500L) = run {
        this.clearAnimation()
        val animation = AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_out)
            .apply {
                duration = animDuration
            }
        this.startAnimation(animation)

    }

    fun View.bounce(context: Context, animDuration: Long = 500L) = run {
        this.clearAnimation()
        val animation = AnimationUtils.loadAnimation(context, R.anim.bounce)
            .apply {
                duration = animDuration
            }
        this.startAnimation(animation)
    }

    private const val SHORT_HAPTIC_FEEDBACK_DURATION = 5L
    fun Context.performHapticFeedback() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = VibrationEffect.createOneShot(100L, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
    }


    fun View.setOnClickThrottleBounceListener(throttleTime: Long = 600L,onClick : ()->Unit){

        this.setOnClickListener(object : View.OnClickListener {

            private var lastClickTime: Long = 0
            override fun onClick(v: View) {
                v.bounce(context)
                if (SystemClock.elapsedRealtime() - lastClickTime < throttleTime) return
                else onClick()
                lastClickTime = SystemClock.elapsedRealtime()
            }
        })
    }




    fun View.setOnClickSingleTimeBounceListener(onClick : ()->Unit){

        this.setOnClickListener(object : View.OnClickListener {
            private var clicked : Boolean = false
            override fun onClick(v: View) {
                context.performHapticFeedback()
                v.bounce(context)
                if (clicked) return
                else onClick()
                clicked = true
            }
        })
   }

    inline fun View.setOnClickFadeInListener(crossinline onClick : ()->Unit){
        setOnClickListener{
            context.performHapticFeedback()
            it.animFadein(context,100)
            onClick()
        }
    }




    fun View.setSingleClickListener(throttleTime: Long = 600L, action: () -> Unit) {
        this.setOnClickListener(object : View.OnClickListener {
            private var lastClickTime: Long = 0

            override fun onClick(v: View) {
                if (SystemClock.elapsedRealtime() - lastClickTime < throttleTime) return
                else action()
                lastClickTime = SystemClock.elapsedRealtime()
            }
        })
    }

}