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
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.DateTimeUtils
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.loadProfileImg
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickFadeInListener
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.databinding.TaskItemBinding
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
    recyclerView: RecyclerView,
    val viewModel: TaskSectionViewModel
) : RecyclerView.Adapter<TodayTasksAdpater.TaskItemViewHolder>() {

    private val selectedTags = mutableListOf<Tag>()
    private var onClickListener: OnClickListener? = null

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(
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

                val position=viewHolder.adapterPosition



                if (dX > 0) {
                    background.color = Color.RED
                    icon = ContextCompat.getDrawable(context, R.drawable.baseline_delete_24)
                } else {
                    if (taskList[position].isCompleted){
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

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

        }

        ItemTouchHelper(simpleItemTouchCallback).apply {
            attachToRecyclerView(recyclerView)
        }
    }

    init {

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    inner class TaskItemViewHolder(private val binding: TaskItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnLongClickListener {
                startDrag()
                true
            }
        }

        private fun startDrag() {
            itemTouchHelper.startDrag(this)
        }

        fun bind(task1: TodayTasks, user: User) {
            fetchTasksForID(task1.taskID){
                Log.d("Todays",it.toString())

                val task=TaskItem(
                    title = it.title,
                    id = it.id,
                    assignee_id = it.assignee,
                    difficulty = it.difficulty,
                    timestamp = it.time_STAMP,
                    completed = if (SwitchFunctions.getStringStateFromNumState(it.status) == "Completed") true else false ,
                    tagList = it.tags,
                    last_updated = it.last_updated
                )

                if (user.profileDPUrl != null && (context as? Activity)?.isDestroyed != true) {
                    binding.asigneeDp.loadProfileImg(user.profileDPUrl.toString())
                } else {
                    binding.asigneeDp.setImageDrawable(context.getDrawable(R.drawable.profile_pic_placeholder))
                }

                if (!task1.isCompleted) {
                    binding.taskId.paintFlags = binding.taskId.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    binding.taskTitle.paintFlags = binding.taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                } else {
                    binding.taskId.paintFlags = binding.taskId.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    binding.taskTitle.paintFlags = binding.taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }



                binding.taskDuration.text = DateTimeUtils.getTimeAgo(task.timestamp!!.seconds)
                binding.taskId.text = task.id
                binding.taskTitle.text = task.title
                binding.difficulty.text = task.getDifficultyString()

                when (task.difficulty) {
                    1 -> binding.difficulty.background =
                        ResourcesCompat.getDrawable(context.resources, R.drawable.label_cardview_green, null)
                    2 -> binding.difficulty.background =
                        ResourcesCompat.getDrawable(context.resources, R.drawable.label_cardview_yellow, null)
                    3 -> binding.difficulty.background =
                        ResourcesCompat.getDrawable(context.resources, R.drawable.label_cardview_red, null)
                }

                CoroutineScope(Dispatchers.IO).launch {

                    val iterator = task.tagList.iterator()
                    while (iterator.hasNext()) {
                        val tagId = iterator.next()
                        val tag = db.tagsDao().getTagbyId(tagId)

                        if (!tag.isNull) {
                            tag?.checked = true

                            synchronized(selectedTags) {
                                selectedTags.add(tag!!)
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        setTagsView(selectedTags.toList().toMutableList(), binding, task)
                    }
                }

            }
        }
    }

    private fun setTagsView(list: MutableList<Tag>, binding: TaskItemBinding, task: TaskItem) {
        val tagIdSet = HashSet(task.tagList)

        synchronized(list) {
            val iterator = list.iterator()

            while (iterator.hasNext()) {
                val tag = iterator.next()
                if (!tagIdSet.contains(tag?.tagID)) {
                    iterator.remove()
                }
            }
        }

        val finalList = list.distinctBy { it.tagID }
        val tagsRecyclerView = binding.tagRecyclerView
        val layoutManager = FlexboxLayoutManager(context)

        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        tagsRecyclerView.layoutManager = layoutManager
        val adapter = TagAdapterHomeScreen(finalList)
        tagsRecyclerView.adapter = adapter
    }



    fun setTasks(newTaskList: List<TodayTasks>) {
        val diffCallback = TaskDiffCallback(taskList, newTaskList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        taskList.clear()
        taskList.addAll(newTaskList.sortedBy { it.isCompleted }.distinctBy { it.taskID }.toMutableList())
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskItemViewHolder {
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: TaskItemViewHolder, position: Int) {
        fetchTasksForID(taskList[position].taskID){
            Log.d("Todays",it.toString())

            fetchAssigneeDetails(it.assignee) { user ->
                holder.bind(taskList[position], user)
            }

            holder.itemView.setOnClickFadeInListener {
                if (onClickListener != null) {
                    onClickListener!!.onCLick(position, taskList[position])
                }
            }
        }

    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onCLick(position: Int, task: TodayTasks)
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

    private fun fetchAssigneeDetails(assigneeId: String, onUserFetched: (User) -> Unit) {
        if (assigneeId != "None" && assigneeId != "") {
            repository.getUserInfobyId(assigneeId) { result ->
                when (result) {
                    is ServerResult.Success -> {
                        val user = result.data
                        if (user != null) {
                            onUserFetched(user)
                        }
                    }

                    is ServerResult.Failure -> {
                        // Handle failure
                    }

                    is ServerResult.Progress -> {
                        // Handle progress
                    }

                    else -> {
                        // Handle other cases
                    }
                }
            }
        } else {
            onUserFetched(User(
                firebaseID = null,
                profileDPUrl = null,
                profileIDUrl = null,
                post = null,
                username = null,
                role = null,
                timestamp = null,
                designation = null,
                fcmToken = null,
                isChecked = false
            ))
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
