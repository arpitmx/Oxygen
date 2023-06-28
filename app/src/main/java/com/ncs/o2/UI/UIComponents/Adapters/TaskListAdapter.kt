package com.ncs.o2.UI.UIComponents.Adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
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

/*
File : TaskListAdapter.kt -> com.ncs.o2.UI.Tasks.TaskList
Description : Adapter class for Task page 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 1:40 pm on 31/05/23

Todo >
Tasks CLEAN CODE :
Tasks BUG FIXES :
Tasks FEATURE MUST HAVE :
Tasks FUTURE ADDITION :

*/
class TaskListAdapter(
) : RecyclerView.Adapter<ViewHolder>() {

    private var onClickListener: OnClickListener? = null
    private var taskList: ArrayList<Task>? = null


    inner class TaskItemViewHolder(private val binding: TaskItemBinding) :
        ViewHolder(binding.root) {

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

            binding.taskDuration.text= "about "+task.DURATION+" hours ago"
            binding.taskId.text = task.ID
            binding.taskTitle.text = task.TITLE
            binding.difficulty.text = task.getDifficultyString()
            binding.difficulty.setBackgroundColor(task.getDifficultyColor())

        }


    }


    fun setTaskList(newTaskList: ArrayList<Task>){
        this.taskList = newTaskList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return taskList!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is TaskItemViewHolder -> {
                holder.bind(taskList!![position])

                holder.itemView.setOnClickFadeInListener {
                    if (onClickListener != null) {
                        onClickListener!!.onCLick(position, taskList!![position])
                    }
                }
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onCLick(position: Int, task: Task)
    }

}