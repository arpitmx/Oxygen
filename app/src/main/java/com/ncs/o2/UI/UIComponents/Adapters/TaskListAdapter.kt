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
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickFadeInListener
import com.ncs.o2.R
import com.ncs.o2.databinding.TaskItemBinding

class TaskListAdapter : RecyclerView.Adapter<TaskListAdapter.TaskItemViewHolder>() {

    private var onClickListener: OnClickListener? = null
    private var taskList: ArrayList<Task> = ArrayList()

    inner class TaskItemViewHolder(private val binding: TaskItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            Glide.with(binding.root)
                .load(task.ASSIGNEE_DP_URL)
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

            if (task.isCompleted){
                binding.taskId.paintFlags=binding.taskId.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.taskTitle.paintFlags=binding.taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }

            binding.taskDuration.text = "about ${task.DURATION} hours ago"
            binding.taskId.text = task.ID
            binding.taskTitle.text = task.TITLE
            binding.difficulty.text = task.getDifficultyString()
            binding.difficulty.setBackgroundColor(task.getDifficultyColor())
        }
    }

    fun setTaskList(newTaskList: ArrayList<Task>) {
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
        holder.bind(taskList[position])

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
        fun onCLick(position: Int, task: Task)
    }

    private class TaskDiffCallback(private val oldList: List<Task>, private val newList: List<Task>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldTask = oldList[oldItemPosition]
            val newTask = newList[newItemPosition]
            return oldTask.ID == newTask.ID
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldTask = oldList[oldItemPosition]
            val newTask = newList[newItemPosition]
            return oldTask == newTask
        }
    }
}
