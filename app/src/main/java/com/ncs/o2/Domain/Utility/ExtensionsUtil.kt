package com.ncs.o2.Domain.Utility

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.ncs.o2.R
import timber.log.Timber
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        animFadeOut(context, duration)
        visibility = View.GONE

    }

    fun View.progressVisible(context: Context, duration: Long = 1500L) = run {
        visibility = View.VISIBLE
        animFadein(context, duration)
    }


    fun View.progressGoneSlide(context: Context, duration: Long = 1500L) = run {
        animSlideUp(context, duration)
        visibility = View.GONE

    }

    fun View.progressVisibleSlide(context: Context, duration: Long = 1500L) = run {
        visibility = View.VISIBLE
        animSlideDown(context, duration)
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

    fun Fragment.showKeyboard(editBox: EditText) {
        activity?.apply {
            val imm: InputMethodManager =
                getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInputFromInputMethod(editBox.windowToken, InputMethodManager.SHOW_FORCED)
        }
    }

    fun EditText.showKeyboardB() {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        requestFocus()
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
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

    /**
     * Set Drawable to the left of EditText
     * @param icon - Drawable to set
     */
    fun EditText.setDrawable(icon: Drawable) {
        this.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
    }


    /**
     * Function to run a delayed function
     * @param millis - Time to delay
     * @param function - Function to execute
     */
    fun runDelayed(millis: Long, function: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(function, millis)
    }

    /**
     * Show multiple views
     */
    fun showViews(vararg views: View) {
        views.forEach { view -> view.visible() }
    }


    /**
     * Hide multiple views
     */
    fun hideViews(vararg views: View) {
        views.forEach { view -> view.gone() }
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


    fun ImageView.load(url: Any, placeholder: Drawable) {
        Glide.with(context)
            .setDefaultRequestOptions(RequestOptions().placeholder(placeholder))
            .load(url)
            .thumbnail(0.05f)
            .error(placeholder)
            .into(this)
    }


    fun isValidContext(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        if (context is Activity) {
            val activity = context as Activity
            if (activity.isDestroyed || activity.isFinishing) {
                return false
            }
        }
        return true
    }


    fun ImageView.loadProfileImg(url: Any) {
        if (isValidContext(context)) {

            Glide.with(context)
                .load(url)
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .encodeQuality(80)
                .override(40, 40)
                .apply(
                    RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .error(R.drawable.profile_pic_placeholder)
                .into(this)
        }
    }


    /**
     * Load image to ImageView
     * @param url - Url of the image, can be Int, drawable or String
     * @param placeholder - Placeholder to show when loading image
     * @param thumbnail - Image thumbnail url
     */
    fun ImageView.load(url: Any, placeholder: Int, thumbnail: String) {
        Glide.with(context)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(placeholder)
            )
            .load(url)
            .thumbnail(Glide.with(context).asDrawable().load(thumbnail).thumbnail(0.1f))
            .into(this)
    }

    /**
     * Load image to ImageView
     * @param url - Url of the image, can be Int, drawable or String
     * @param placeholder - Placeholder to show when loading image
     * @param thumbnail - Image thumbnail url
     */
    fun ImageView.load(url: Any, placeholder: Drawable, thumbnail: String) {
        Glide.with(context)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(placeholder)
            )
            .load(url)
            .thumbnail(Glide.with(context).asDrawable().load(thumbnail).thumbnail(0.1f))
            .into(this)
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
        val animation =
            AnimationUtils.loadAnimation(context, me.shouheng.utils.R.anim.slide_top_to_bottom)
                .apply {
                    duration = animDuration
                }
        this.startAnimation(animation)
        this.gone()
    }


    fun View.animSlideUpVisible(context: Context, animDuration: Long = 1500L) = run {
        this.clearAnimation()
        val animation = AnimationUtils.loadAnimation(context, R.anim.slide_bottom_to_up)
            .apply {
                duration = animDuration
            }
        this.startAnimation(animation)
        this.visible()
    }

    fun View.slideDownAndGone(
        duration: Long = 300L,
        onEndAction: (() -> Unit)? = null
    ): ViewPropertyAnimator {
        return animate()
            .translationYBy(height.toFloat())
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                visibility = View.GONE
                onEndAction?.invoke()
            }
    }

    fun View.slideUpAndVisible(
        duration: Long = 300L,
        onEndAction: (() -> Unit)? = null
    ): ViewPropertyAnimator {
        visibility = View.VISIBLE
        return animate()
            .translationYBy(-height.toFloat())
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                onEndAction?.invoke()
            }
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

    fun View.set180(context: Context, animDuration: Long = 200L) {
        clearAnimation()
        val currentRotation = tag as? Float ?: 0f
        val targetRotation = if (currentRotation == 0f) 180f else 0f
        val rotationProperty =
            PropertyValuesHolder.ofFloat(View.ROTATION, currentRotation, targetRotation)
        val animator = ObjectAnimator.ofPropertyValuesHolder(this, rotationProperty)
            .apply {
                duration = animDuration
            }
        tag = targetRotation

        animator.start()
    }


    fun View.rotateInfinity(context: Context) = run {
        this.clearAnimation()
        val animation = AnimationUtils.loadAnimation(context, R.anim.rotateinfi)
        this.startAnimation(animation)
    }


    fun View.popInfinity(context: Context) = run {
        this.clearAnimation()
        val animation = AnimationUtils.loadAnimation(context, R.anim.popinfi)
        this.startAnimation(animation)
    }


    fun View.blink(context: Context) = run {
        this.clearAnimation()
        val animation = AnimationUtils.loadAnimation(context, R.anim.blink)
        this.startAnimation(animation)
    }

    fun View.rotateInfinityReverse(context: Context) = run {
        this.clearAnimation()
        val animation = AnimationUtils.loadAnimation(context, R.anim.rotateinfirev)
        this.startAnimation(animation)
    }

    fun View.animFadeOut(context: Context, animDuration: Long = 1500L) = run {
        this.clearAnimation()
        val animation =
            AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_out)
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
        val vibrationEffect = VibrationEffect.createOneShot(5L, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
    }


    fun TextInputEditText.appendTextAtCursor(textToAppend: String) {
        val start = selectionStart
        val end = selectionEnd

        val editable = text

        editable?.replace(start, end, textToAppend)
        setSelection(start + textToAppend.length)
    }


    fun View.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
        if (this.layoutParams is ViewGroup.MarginLayoutParams) {
            val params = this.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(left, top, right, bottom);
        }
    }

    fun TextInputEditText.appendTextAtCursorMiddleCursor(textToAppend: String, type: Int) {
        val position = this.selectionStart
        val text = this.text
        val newText = StringBuilder(text).insert(position, textToAppend).toString()
        this.setText(newText)
        if (type == 2) this.setSelection(position + 1)
        else this.setSelection(position + 2)
    }


    fun View.setOnClickThrottleBounceListener(throttleTime: Long = 600L, onClick: () -> Unit) {

        this.setOnClickListener(object : View.OnClickListener {

            private var lastClickTime: Long = 0
            override fun onClick(v: View) {
                context?.let {
                    v.bounce(context)
                    if (SystemClock.elapsedRealtime() - lastClickTime < throttleTime) return
                    else onClick()
                    lastClickTime = SystemClock.elapsedRealtime()
                }

            }
        })
    }

    fun View.setOnDoubleClickListener(listener: () -> Unit) {
        val doubleClickInterval = 500 // Adjust this value as needed (in milliseconds)
        var lastClickTime: Long = 0

        this.setOnClickListener { view ->
            view.bounce(context)
            val clickTime = SystemClock.uptimeMillis()
            if (clickTime - lastClickTime < doubleClickInterval) {
                // Double click detected
                context?.let {
                    context.performHapticFeedback()
                    listener.invoke()
                }

            }

            lastClickTime = clickTime
        }
    }

    fun deleteDownloadedFile(downloadID : Long, context: Context) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.remove(downloadID)
    }

    fun getVersionName(context: Context): String? {
        return try {
            val packageInfo: PackageInfo =
                context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    fun String.isGreaterThanVersion(otherVersion: String): Boolean {
        val thisParts = this.split(".").map { it.toInt() }
        val otherParts = otherVersion.split(".").map { it.toInt() }

        for (i in 0 until maxOf(thisParts.size, otherParts.size)) {
            val thisPart = thisParts.getOrNull(i) ?: 0
            val otherPart = otherParts.getOrNull(i) ?: 0

            if (thisPart != otherPart) {
                return thisPart > otherPart
            }
        }

        return false
    }


    fun View.setOnClickSingleTimeBounceListener(onClick: () -> Unit) {

        this.setOnClickListener(object : View.OnClickListener {
            private var clicked: Boolean = false
            override fun onClick(v: View) {
                //context.performHapticFeedback()
                v.bounce(context)
                if (clicked) return
                else onClick()
                clicked = true
            }
        })
    }

    inline fun View.setOnClickFadeInListener(crossinline onClick: () -> Unit) {
        setOnClickListener {
            // context.performHapticFeedback()
            it.animFadein(context, 100)
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


    fun View.setBackgroundColorRes(colorResId: Int) {
        val color = context.resources.getColor(colorResId)
        setBackgroundColor(color)
    }

    fun View.setBackgroundColor(color: Int) {
        background = ColorDrawable(color)
    }

    fun View.setBackgroundDrawable(drawable: Drawable) {
        background = drawable
    }
}

