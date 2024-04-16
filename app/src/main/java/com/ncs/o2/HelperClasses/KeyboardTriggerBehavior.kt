package com.ncs.o2.HelperClasses

import android.app.Activity
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import timber.log.Timber

open class KeyboardTriggerBehavior(parent: View, val minKeyboardHeight: Int = 200) : LiveData<KeyboardTriggerBehavior.Status>() {
    enum class Status {
        OPEN, CLOSED
    }

//    val contentView: View = activity.findViewById(android.R.id.content)
    val contentView = parent

    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val displayRect = Rect().apply { contentView.getWindowVisibleDisplayFrame(this) }
        val keypadHeight = contentView.rootView.height - displayRect.height()

        if (keypadHeight > 0.25*parent.rootView.height) {
            setDistinctValue(Status.OPEN)
        } else {
            setDistinctValue(Status.CLOSED)
        }
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in Status>) {
        super.observe(owner, observer)
        observersUpdated()
    }

    override fun observeForever(observer: Observer<in Status>) {
        super.observeForever(observer)
        observersUpdated()
    }

    override fun removeObservers(owner: LifecycleOwner) {
        super.removeObservers(owner)
        observersUpdated()
    }

    override fun removeObserver(observer: Observer<in Status>) {
        super.removeObserver(observer)
        observersUpdated()
    }

    private fun setDistinctValue(newValue: Status) {
        if (value != newValue) {
            value = newValue
        }
    }

    private fun observersUpdated() {
        if (hasObservers()) {
            contentView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        } else {
            contentView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        }
    }
}