package com.ncs.o2.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickFadeInListener
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

            binding.taskDuration.text= task.DURATION
            binding.taskId.text = task.ID
            binding.taskStatus.text = task.getStatusString()
            binding.taskTitle.text = task.TITLE

            binding.difficulty.text = task.getDifficultyString()
            binding.difficulty.setTextColor(task.getDifficultyColor())

            binding.priority.text = task.PRIORITY.toString()
            binding.priority.setBackgroundColor(task.getPriorityColor())

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