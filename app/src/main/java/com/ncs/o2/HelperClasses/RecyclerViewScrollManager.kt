package com.ncs.versa.HelperClasses

import android.os.Handler
import androidx.recyclerview.widget.RecyclerView


/**
 * Created by Alok Ranjan on 06-10-2022
 * Project : NCS Grab.io
 * Description :
 * Github : https://github.com/arpitmx
 **/

//Branch Check
//added by neev

class RecyclerViewScrollManager {

    abstract class FabScroll : RecyclerView.OnScrollListener() {
        var isVisible = false
        val MINIMUM = 300f
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (isVisible && scrollDist1 > MINIMUM) {
                hide()
                scrollDist1 = 0
                isVisible = false
            } else if (!isVisible && scrollDist1 < -MINIMUM) {
                show()
                scrollDist1 = 0
                isVisible = true
            }
            if (isVisible && dy > 0 || !isVisible && dy < 0) {
                scrollDist1 += dy
            }
        }


        abstract fun show()
        abstract fun hide()

        companion object {
            var scrollDist1 = 0
            fun setScrollDist() {
                scrollDist1 = 0
            }
        }
    }

    abstract class MiniplayerScroll : RecyclerView.OnScrollListener() {
        var isVisible = false
        val MINIMUM_SHOW = 15000f
        val MINIMUM_HIDE = 300f
        val HIDING_DELAY: Long = 8000
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (isVisible && scrollDist > MINIMUM_HIDE) {
                hide()
                scrollDist = 0
                isVisible = false
            } else if (!isVisible && scrollDist < -MINIMUM_SHOW) {
                show()
                scrollDist = 0
                isVisible = true
                Handler().postDelayed({
                    hide()
                    scrollDist = 0
                    isVisible = false
                }, HIDING_DELAY)
            }
            if (isVisible && dy > 0 || !isVisible && dy < 0) {
                scrollDist += dy
            }
        }

        abstract fun show()
        abstract fun hide()

        companion object {
            var scrollDist = 0
            fun setScrollDist() {
                scrollDist = 0
            }
        }
    }
}