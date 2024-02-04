import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.DateTimeUtils
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.loadProfileImg
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickFadeInListener
import com.ncs.o2.R
import com.ncs.o2.UI.UIComponents.Adapters.TagAdapterHomeScreen
import com.ncs.o2.databinding.TaskItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class TaskListAdapter(val repository: FirestoreRepository,val context: Context,val taskList:MutableList<TaskItem>,val db:TasksDatabase) : RecyclerView.Adapter<TaskListAdapter.TaskItemViewHolder>(){


    private val selectedTags = mutableListOf<Tag>()
    private var onClickListener: OnClickListener? = null
    inner class TaskItemViewHolder(private val binding: TaskItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: TaskItem,user: User) {
            if (user.profileDPUrl!=null &&(context as? Activity)?.isDestroyed != true) {

                binding.asigneeDp.loadProfileImg(user.profileDPUrl.toString())
            }else{
                binding.asigneeDp.setImageDrawable(context.getDrawable(R.drawable.profile_pic_placeholder))
            }


            if (task.completed){
                binding.taskId.paintFlags=binding.taskId.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.taskTitle.paintFlags=binding.taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }

            binding.taskDuration.text = DateTimeUtils.getTimeAgo(task.timestamp!!.seconds)
            binding.taskId.text = task.id
            binding.taskTitle.text = task.title
            binding.difficulty.text = task.getDifficultyString()

            when (task.difficulty){
                1 -> binding.difficulty.background= ResourcesCompat.getDrawable(context.resources,R.drawable.label_cardview_green,null)
                2 -> binding.difficulty.background= ResourcesCompat.getDrawable(context.resources,R.drawable.label_cardview_yellow,null)
                3 -> binding.difficulty.background= ResourcesCompat.getDrawable(context.resources,R.drawable.label_cardview_red,null)
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

    fun setTaskList(newTaskList: List<TaskItem>) {
        val diffCallback = TaskDiffCallback(taskList, newTaskList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        taskList.clear()
        taskList.addAll(newTaskList)
        diffResult.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }

    fun setTasks(newTaskList: List<Task>) {
        val taskItems: List<TaskItem> = newTaskList.map { task ->
            TaskItem(
                title = task.title!!,
                id = task.id,
                assignee_id = task.assignee!!,
                difficulty = task.difficulty!!,
                timestamp = task.time_STAMP,
                completed = if (SwitchFunctions.getStringStateFromNumState(task.status!!)=="Completed") true else false,
                tagList = task.tags
            )
        }
        val diffCallback = TaskDiffCallback(taskList, taskItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        taskList.clear()
        taskList.addAll(taskItems)
        diffResult.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskItemViewHolder {
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: TaskItemViewHolder, position: Int) {

        fetchAssigneeDetails(taskList[position].assignee_id) { user ->
            holder.bind(taskList[position], user)
        }

        holder.itemView.setOnClickFadeInListener {
            if (onClickListener != null) {
                onClickListener!!.onCLick(position, taskList[position])
            }
        }


    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onCLick(position: Int, task: TaskItem)
    }

    private class TaskDiffCallback(private val oldList: List<TaskItem>, private val newList: List<TaskItem>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldTask = oldList[oldItemPosition]
            val newTask = newList[newItemPosition]
            return oldTask.id == newTask.id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldTask = oldList[oldItemPosition]
            val newTask = newList[newItemPosition]
            return oldTask == newTask
        }
    }
    private fun fetchAssigneeDetails(assigneeId: String, onUserFetched: (User) -> Unit) {
        if (assigneeId!="None" && assigneeId!="") {
            repository.getUserInfobyId(assigneeId) { result ->
                when (result) {
                    is ServerResult.Success -> {
                        val user = result.data
                        if (user != null) {
                            onUserFetched(user)
                        }
                    }

                    is ServerResult.Failure -> {

                    }

                    is ServerResult.Progress -> {

                    }

                    else -> {}
                }
            }
        }else{
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

}
