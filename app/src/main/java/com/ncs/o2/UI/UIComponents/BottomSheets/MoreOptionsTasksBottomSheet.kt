package com.ncs.o2.UI.UIComponents.BottomSheets

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.TodayTasks
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailViewModel
import com.ncs.o2.databinding.MoreOptionsTaskPageBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MoreOptionsTasksBottomSheet(private val taskItem: TaskItem,private val onArchive: OnArchive) : BottomSheetDialogFragment() {

    private val utils: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(requireActivity())
    }
    lateinit var binding: MoreOptionsTaskPageBinding

    @Inject
    lateinit var db: TasksDatabase
    private val viewModel: TaskDetailViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MoreOptionsTaskPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
    }

    private fun setViews() {
        setBottomSheetConfig()
        setUpViews()
    }

    private fun setUpViews() {
        binding.closeBtn.setOnClickThrottleBounceListener {
            dismiss()
        }
        if (PrefManager.getcurrentUserdetails().ROLE >= 3) {
            binding.btnArchiveTask.visible()
        } else {
            binding.btnArchiveTask.gone()
        }
        binding.btnArchiveTask.setOnClickThrottleBounceListener {
            utils.twoBtn(title = "Archive Task",
                msg = "Do you want to archive this Task ?",
                positiveBtnText = "Archive",
                negativeBtnText = "Cancel",
                positive = {
                    handleTaskArchive(taskItem)
                },
                negative = {
                })
        }
        binding.today.setOnClickThrottleBounceListener {
            val todays = PrefManager.getProjectTodayTasks(PrefManager.getcurrentProject()).toMutableList()

            val isTaskAlreadyPresent = todays.any { it.taskID == taskItem.id }

            if (isTaskAlreadyPresent) {
                toast("This task is already present in your today")
            } else {
                todays.add(
                    TodayTasks(
                    taskID = taskItem.id,
                    isCompleted = false
                )
                )
                PrefManager.saveProjectTodayTasks(PrefManager.getcurrentProject(), todays)
                toast("Task added to your today")
            }

            dismiss()
        }
    }

    private fun handleTaskArchive(_task: TaskItem) {
        val taskID = _task.id
        CoroutineScope(Dispatchers.IO).launch {
            val task = db.tasksDao()
                .getTasksbyId(tasksId = taskID, projectId = PrefManager.getcurrentProject())
            task?.archived = true
            db.tasksDao().update(task!!)
            withContext(Dispatchers.Main) {
                updateTaskArchive(taskItem = _task, newArchive = true)
            }

        }

    }

    private fun updateTaskArchive(taskItem: TaskItem,newArchive:Boolean) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Archiving the task")
        progressDialog.setCancelable(false)
        progressDialog.show()
        viewLifecycleOwner.lifecycleScope.launch {

            try {
                val result = withContext(Dispatchers.IO) {
                    viewModel.updateArchive(
                        taskID = taskItem.id,
                        archive = newArchive,
                        projectName = PrefManager.getcurrentProject()
                    )
                }
                when (result) {

                    is ServerResult.Failure -> {
                        progressDialog.dismiss()

                        utils.singleBtnDialog(
                            "Failure",
                            "Failure in Updating: ${result.exception.message}",
                            "Okay"
                        ) {
                            requireActivity().finish()
                        }

                    }

                    is ServerResult.Progress -> {
                        progressDialog.show()
                    }

                    is ServerResult.Success -> {
                        progressDialog.dismiss()
                        toast("Task Archived")
                        dismiss()
                        onArchive.onTaskArchive(taskItem)

                    }

                }

            } catch (e: Exception) {

                Timber.tag(TaskDetailsFragment.TAG).e(e)

            }

        }
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }

    interface OnArchive{
        fun onTaskArchive(taskItem: TaskItem)
    }

}