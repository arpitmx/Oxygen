package com.ncs.o2.UI.Tasks.Sections

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import kotlin.math.abs

class DragHelper (private val tabChangeListener: TabChangeListener): ItemTouchHelper.Callback(){
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        return makeMovementFlags(dragFlags, 0)
    }


    var changed = false
    var dx = 0F
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        val threshold = 500

        Timber.tag("Drag").i("dX: $dX, dY: $dY" )
        Timber.tag("Drag").i("Diff : ${recyclerView.width - threshold}")

        tabChangeListener.smoothScrollby(dX,dY)
        if (abs(dX) > recyclerView.width - threshold) {
            if (dX > 0) {
                //tabChangeListener.switchToNextTab()

            } else if (dX < 0) {
               // tabChangeListener.switchToPreviousTab()
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {

      return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    interface TabChangeListener {
        fun switchToNextTab()
        fun switchToPreviousTab()

        fun smoothScrollby(dx : Float, dy :Float)

    }
}