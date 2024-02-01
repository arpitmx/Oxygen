package com.ncs.o2.UI.Assigned

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.R
import com.ncs.o2.UI.UIComponents.Adapters.TagAdapterHomeScreen
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.TodayTasks
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Models.UserNote
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.DateTimeUtils
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.loadProfileImg
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickFadeInListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.databinding.ChatMessageItemBinding
import com.ncs.o2.databinding.TaskItemBinding
import com.ncs.o2.databinding.TodayTaskItemBinding
import com.ncs.o2.databinding.UserTaskLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

class TodayTasksAdpater(
    val repository: FirestoreRepository,
    val context: Context,
    val taskList: MutableList<TodayTasks>,
    val db: TasksDatabase,
    val callback:SwipeListener,
    val recyclerView: RecyclerView,
    val viewModel: TaskSectionViewModel,
    val onClickListener: OnClickListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemTouchHelper: ItemTouchHelper? = null

    private fun createItemTouchHelper(updatedTaskList: List<TodayTasks>) {
        val simpleItemTouchCallback = SimpleItemTouchCallback(updatedTaskList)

        itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper?.attachToRecyclerView(recyclerView)
    }

    inner class SimpleItemTouchCallback(
        private val updatedTaskList: List<TodayTasks>
    ) : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {

        override fun isLongPressDragEnabled(): Boolean {
            context.performHapticFeedback()
            return true
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(taskList, i, i + 1)
                    PrefManager.saveProjectTodayTasks(PrefManager.getcurrentProject(),taskList)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(taskList, i, i - 1)
                    PrefManager.saveProjectTodayTasks(PrefManager.getcurrentProject(),taskList)

                }
            }

            notifyItemMoved(fromPosition, toPosition)
            return true
        }


        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            when (direction) {
                ItemTouchHelper.LEFT -> {
                    callback.onleftSwipe(taskList[position],position)
                }
                ItemTouchHelper.RIGHT -> {
                    callback.onrightSwipe(taskList[position],position)
                }
            }
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val background = ColorDrawable(ContextCompat.getColor(context, R.color.primary))
            var icon = ContextCompat.getDrawable(context, R.drawable.baseline_check)

            val position=viewHolder.bindingAdapterPosition

            val taskListCopy = ArrayList(updatedTaskList)
            Log.d("position",position.toString())
            Log.d("position_up",updatedTaskList.toString())

            if (position in 0 until taskListCopy.size) {
                val currentTask = taskListCopy[position]
                if (dX > 0) {
                    background.color = Color.RED
                    icon = ContextCompat.getDrawable(context, R.drawable.baseline_delete_24)
                } else {
                    if (currentTask.isCompleted){
                        background.color = ContextCompat.getColor(context, R.color.account)
                        icon = ContextCompat.getDrawable(context, R.drawable.baseline_reply_24)
                    }
                    else{
                        background.color = ContextCompat.getColor(context, R.color.primary)
                        icon = ContextCompat.getDrawable(context, R.drawable.baseline_check)
                    }

                }

                val leftBound: Int
                val topBound = itemView.top
                val rightBound: Int
                val bottomBound = itemView.bottom

                if (dX > 0) {
                    leftBound = itemView.left
                    rightBound = (itemView.left + dX).toInt()
                } else {
                    leftBound = (itemView.right + dX).toInt() - icon!!.intrinsicWidth
                    rightBound = itemView.right
                }

                background.setBounds(leftBound, topBound, rightBound, bottomBound)
                background.draw(c)

                val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
                val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                val iconBottom = iconTop + icon.intrinsicHeight
                val iconLeft: Int
                val iconRight: Int

                if (dX > 0) {
                    iconLeft = itemView.left + iconMargin
                    iconRight = iconLeft + icon.intrinsicWidth
                } else {
                    iconLeft = rightBound - iconMargin - icon.intrinsicWidth
                    iconRight = rightBound - iconMargin
                }

                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                icon.draw(c)

            }



            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }





    inner class TaskItemViewHolder(private val binding: TodayTaskItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnLongClickListener {
                startDrag()
                true
            }

        }

        private fun startDrag() {
            itemTouchHelper?.startDrag(this)
        }

        fun bind(task1: TodayTasks) {
            if (task1.taskID[0]=='#') {
                fetchTasksForID(task1.taskID) {
                    Log.d("Todays", it.toString())
                    binding.root.setOnClickThrottleBounceListener{
                        onClickListener.onCLick(task1)
                    }
                    val task = TaskItem(
                        title = it.title,
                        id = it.id,
                        assignee_id = it.assignee,
                        difficulty = it.difficulty,
                        timestamp = it.time_STAMP,
                        completed = if (SwitchFunctions.getStringStateFromNumState(it.status) == "Completed") true else false,
                        tagList = it.tags,
                        last_updated = it.last_updated
                    )

                    if (!task1.isCompleted) {
                        binding.taskId.paintFlags =
                            binding.taskId.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                        binding.taskTitle.paintFlags =
                            binding.taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    } else {
                        binding.taskId.paintFlags =
                            binding.taskId.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        binding.taskTitle.paintFlags =
                            binding.taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }
                    binding.taskId.visible()
                    binding.difficulty.visible()
                    binding.difficulty2.gone()

                    binding.taskId.text = task.id
                    binding.taskTitle.text = task.title
                    binding.difficulty.text = task.getDifficultyString()

                    when (task.difficulty) {
                        1 -> binding.difficulty.background =
                            ResourcesCompat.getDrawable(
                                context.resources,
                                R.drawable.label_cardview_green,
                                null
                            )

                        2 -> binding.difficulty.background =
                            ResourcesCompat.getDrawable(
                                context.resources,
                                R.drawable.label_cardview_yellow,
                                null
                            )

                        3 -> binding.difficulty.background =
                            ResourcesCompat.getDrawable(
                                context.resources,
                                R.drawable.label_cardview_red,
                                null
                            )
                    }

                }
            }
            else{
                val userNotes=PrefManager.getProjectUserNotes(PrefManager.getcurrentProject())
                var note:UserNote?=null

                for (n in userNotes){
                    if (n.id==task1.taskID){
                        note=n
                    }
                }
                binding.taskId.gone()
                binding.difficulty.gone()
                binding.difficulty2.visible()

                if (!note.isNull){
                    if (!task1.isCompleted) {
                        binding.taskId.paintFlags = binding.taskId.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                        binding.taskTitle.paintFlags = binding.taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    } else {
                        binding.taskId.paintFlags = binding.taskId.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        binding.taskTitle.paintFlags = binding.taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }
                    binding.taskTitle.text = note?.desc
                }
                binding.root.setOnClickThrottleBounceListener{
                    onClickListener.onCLick(task1)
                }
            }
        }
    }

    inner class UserTaskItemViewHolder(private val binding: UserTaskLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnLongClickListener {
                startDrag()
                true
            }
        }

        private fun startDrag() {
            itemTouchHelper?.startDrag(this)
        }

        fun bind(task1: TodayTasks) {

            val userNotes=PrefManager.getProjectUserNotes(PrefManager.getcurrentProject())
            var note:UserNote?=null

            for (n in userNotes){
                if (n.id==task1.taskID){
                    note=n
                }
            }

            if (!note.isNull){
                if (!task1.isCompleted) {
                    binding.taskId.paintFlags = binding.taskId.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    binding.taskTitle.paintFlags = binding.taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                } else {
                    binding.taskId.paintFlags = binding.taskId.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    binding.taskTitle.paintFlags = binding.taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                binding.taskId.text = note?.id
                binding.taskTitle.text = note?.desc
            }
        }
    }

    fun setTasks(newTaskList: List<TodayTasks>) {
        val diffCallback = TaskDiffCallback(taskList, newTaskList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        taskList.clear()
        taskList.addAll(newTaskList.sortedBy { it.isCompleted }.distinctBy { it.taskID }.toMutableList())
        notifyDataSetChanged()
        createItemTouchHelper(taskList)
        diffResult.dispatchUpdatesTo(this)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0->{
                TaskItemViewHolder(
                    TodayTaskItemBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                ))
            }
            else ->{
                throw IllegalArgumentException("Invalid view type")
            }
        }

    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TaskItemViewHolder).bind(taskList[position])
    }


    interface OnClickListener {
        fun onCLick( task: TodayTasks)
    }

    private class TaskDiffCallback(private val oldList: List<TodayTasks>, private val newList: List<TodayTasks>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldTask = oldList[oldItemPosition]
            val newTask = newList[newItemPosition]
            return oldTask.taskID == newTask.taskID
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldTask = oldList[oldItemPosition]
            val newTask = newList[newItemPosition]
            return oldTask == newTask
        }
    }

    interface SwipeListener{
        fun onleftSwipe(task: TodayTasks,position: Int)
        fun onrightSwipe(task: TodayTasks,position: Int)
    }
    private fun fetchTasksForID(
        taskID: String,
        onSuccess: (Task) -> Unit
    ) {
        viewModel.getTasksForID(PrefManager.getcurrentProject(), taskID) { result ->
            when (result) {
                is DBResult.Success -> {
                    onSuccess(result.data)
                }

                is DBResult.Failure -> {
                }

                is DBResult.Progress -> {
                }
            }
        }
    }
}
