@file:Suppress("NOTHING_TO_INLINE")
@file:JvmMultifileClass
@file:JvmName("Extensions")

package com.ncs.o2.Domain.Utility

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.os.SystemClock
import android.text.Editable
import android.text.SpannableStringBuilder
import android.util.DisplayMetrics
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.EdgeEffect
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRectF
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.MainActivityViewModel
import kotlin.math.max
import kotlin.math.min

//region View

inline operator fun <reified V : View?> V?.invoke(block: V.() -> Unit) = this?.apply(block)

inline fun View.shortSnackBar(string: String) = Snackbar.make(this, string, Snackbar.LENGTH_SHORT).show()

inline fun View.longSnackBar(string: String) = Snackbar.make(this, string, Snackbar.LENGTH_LONG).show()

inline fun View.infSnackBar(string: String) = Snackbar.make(this, string, Snackbar.LENGTH_INDEFINITE).show()

inline val View.mainActivity: MainActivity
    get() = this.context as MainActivity

inline val View.locationOnScreen: Point
    get() {
        val point = IntArray(2).also {
            this.getLocationOnScreen(it)
        }
        return Point(point[0], point[1])
    }

inline fun View.fadeIn(durationMillis: Long = 250) {
    this.startAnimation(AlphaAnimation(0F, 1F).apply {
        duration = durationMillis
        fillAfter = true
    })
}

inline fun View.fadeOut(durationMillis: Long = 250) {
    this.startAnimation(AlphaAnimation(1F, 0F).apply {
        duration = durationMillis
        fillAfter = true
    })
}

inline val View.isTransparent: Boolean
    get() = alpha == 0F

inline val View.isClear: Boolean
    get() = alpha == 1F

inline fun View.removeOnClickListener() = this.setOnClickListener(null)

val View.parents: List<ViewGroup>
    get() {
        val result = ArrayList<ViewGroup>()
        var current = parent
        while (current != null && current is ViewGroup) {
            result.add(current)
            current = current.parent
        }
        return result
    }

inline val View.parentView: View?
    get() = parent as? View?

inline val View.parentViewGroup: ViewGroup?
    get() = parent as? ViewGroup?

inline val View.rootViewGroup: ViewGroup?
    get() = this.rootView as? ViewGroup

inline val View.globalVisibleRect: Rect
    get() = Rect().also { this.getGlobalVisibleRect(it) }

inline val View.globalVisibleRectF: RectF
    get() = Rect().also { this.getGlobalVisibleRect(it) }.toRectF()

inline fun View.bottomHorizontalRect(top: Float): RectF {
    return this.globalVisibleRectF.also { it.top = top }
}

inline fun View.topHorizontalRect(bottom: Float): RectF {
    return this.globalVisibleRectF.also { it.bottom = bottom }
}

inline fun View.leftVerticalRect(right: Float): RectF {
    return this.globalVisibleRectF.also { it.right = right }
}

inline fun View.rightVerticalRect(left: Float): RectF {
    return this.globalVisibleRectF.also { it.left = left }
}

//endregion View

//region RecyclerView

inline fun RecyclerView.scrollToEnd() {
    if (this.adapter != null) {
        this.scrollToPosition(adapter!!.lastPosition)
    }
}

inline fun RecyclerView.smoothScrollToEnd() {
    if (this.adapter != null) {
        this.smoothScrollToPosition(adapter!!.lastPosition)
    }
}

inline fun RecyclerView.scrollToStart() {
    if (this.adapter != null) {
        this.scrollToPosition(0)
    }
}

inline fun RecyclerView.smoothScrollToStart() {
    if (this.adapter != null) {
        this.smoothScrollToPosition(0)
    }
}

inline fun RecyclerView.addOnScrollListener(
    crossinline onScrolled: (dx: Int, dy: Int) -> Unit = { _, _ -> },
    crossinline onScrollStateChanged: (newState: Int) -> Unit = { _ -> }) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            onScrolled(dx, dy)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            onScrollStateChanged(newState)
        }
    })
}


inline fun recycledViewPool(maxCount: Int) =
    object : RecyclerView.RecycledViewPool() {
        override fun setMaxRecycledViews(viewType: Int, max: Int) =
            super.setMaxRecycledViews(viewType, maxCount)
    }

inline val RecyclerView.Adapter<*>.lastPosition: Int
    get() = this.itemCount - 1

inline fun RecyclerView.Adapter<*>.notifySwapped(fromPosition: Int, toPosition: Int) {
    notifyItemRemoved(fromPosition)
    notifyItemInserted(fromPosition)
    notifyItemRemoved(toPosition)
    notifyItemInserted(toPosition)
}

//endregion RecyclerView



inline fun Activity.checkWritePermission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 1)
    }
}



inline fun ComponentActivity.addOnBackPressedCallback(crossinline onBackPressed: () -> Unit) {
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() = onBackPressed()
    })
}




inline fun String.toEditable() = SpannableStringBuilder(this)

inline val Editable?.isValid: Boolean get() = this != null && this.isNotBlank()

inline val MotionEvent.actionString: String get() = MotionEvent.actionToString(this.action)

inline fun MotionEvent.obtainCopy(): MotionEvent = MotionEvent.obtain(this)


inline fun MotionEvent.cancel(): MotionEvent =
    this.also { it.action = MotionEvent.ACTION_CANCEL }


/**
 * This method converts device specific pixels to density independent pixels.
 *
 * @param px A value in px (pixels) unit. Which we need to convert into db
 * @return A float value to represent dp equivalent to px value
 */

inline fun RectF.verticalPercent(pointF: PointF): Float {
    val min = min(bottom, top)
    val max = max(bottom, top)
    return (((pointF.y - min) / (max - min)) * 100F)
}

inline fun RectF.verticalPercentInverted(pointF: PointF): Float {
    val min = min(bottom, top)
    val max = max(bottom, top)
    return (((pointF.y - max) / (min - max)) * 100F)
}

inline fun RectF.horizontalPercent(pointF: PointF): Float {
    val min = min(bottom, top)
    val max = max(bottom, top)
    return (((pointF.x - min) / (max - min)) * 100F)
}

inline fun RectF.horizontalPercentInverted(pointF: PointF): Float {
    val min = min(bottom, top)
    val max = max(bottom, top)
    return (((pointF.y - max) / (min - max)) * 100F)
}
