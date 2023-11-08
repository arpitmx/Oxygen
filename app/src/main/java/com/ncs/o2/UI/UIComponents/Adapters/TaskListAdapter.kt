import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickFadeInListener
import com.ncs.o2.R
import com.ncs.o2.databinding.TaskItemBinding
import java.util.Date

class TaskListAdapter(val repository: FirestoreRepository) : RecyclerView.Adapter<TaskListAdapter.TaskItemViewHolder>() {

    private var onClickListener: OnClickListener? = null
    private var taskList: ArrayList<TaskItem> = ArrayList()
    inner class TaskItemViewHolder(private val binding: TaskItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: TaskItem,user: User) {
            Glide.with(binding.root)
                .load(user.profileDPUrl)
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.gone()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.gone()
                        return false
                    }
                })
                .encodeQuality(80)
                .override(40,40)
                .apply(
                    RequestOptions().
                    diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .error(R.drawable.profile_pic_placeholder)
                .into(binding.asigneeDp)

            if (task.completed!!){
                binding.taskId.paintFlags=binding.taskId.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.taskTitle.paintFlags=binding.taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            val timeDifference = Date().time - task.timestamp!!.toDate().time
            val minutes = (timeDifference / (1000 * 60)).toInt()
            val hours = minutes / 60
            val days = hours / 24

            val timeAgo: String = when {
                days > 0 -> "about $days days ago"
                hours > 0 -> "about $hours hours ago"
                minutes > 0 -> "about $minutes minutes ago"
                else -> "just now"
            }

            binding.taskDuration.text = "$timeAgo"
            binding.taskId.text = task.id
            binding.taskTitle.text = task.title
            binding.difficulty.text = task.getDifficultyString()
            binding.difficulty.setBackgroundColor(task.getDifficultyColor())
        }
    }

    fun setTaskList(newTaskList: List<TaskItem>) {
        val diffCallback = TaskDiffCallback(taskList, newTaskList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        taskList.clear()
        taskList.addAll(newTaskList)
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
                is ServerResult.Progress->{

                }
            }
        }
    }

}
