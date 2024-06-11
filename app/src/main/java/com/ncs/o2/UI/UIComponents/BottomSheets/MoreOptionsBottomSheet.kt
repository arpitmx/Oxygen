package com.ncs.o2.UI.UIComponents.BottomSheets

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.BuildConfig
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.TodayTasks
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailViewModel
import com.ncs.o2.databinding.MoreOptionBottomSheetBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MoreOptionsBottomSheet() : BottomSheetDialogFragment(){
    lateinit var binding:MoreOptionBottomSheetBinding

    private val activityBinding: TaskDetailActivity by lazy {
        (requireActivity() as TaskDetailActivity)
    }
    @Inject
    lateinit var db: TasksDatabase
    private val viewModel: TaskDetailViewModel by viewModels()
    var task:Task ? = null
    private val utils: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(requireActivity())
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MoreOptionBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
    }

    private fun setViews() {
        setBottomSheetConfig()
        setActionbar()
    }

    private fun setActionbar() {

        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }
        binding.shareButton.setOnClickThrottleBounceListener {
            if (PrefManager.getAppMode()== Endpoints.ONLINE_MODE){
                binding.progressBar.visible()
                binding.buttons.gone()
                createTaskLink(activityBinding.taskId!!)
            }
            else{
                toast("Error Creating task link, you are offline")
            }

        }

        binding.today.setOnClickThrottleBounceListener {
            val todays = PrefManager.getProjectTodayTasks(PrefManager.getcurrentProject()).toMutableList()

            val isTaskAlreadyPresent = todays.any { it.taskID == activityBinding.taskId }

            if (isTaskAlreadyPresent) {
                toast("This task is already present in your today")
            } else {
                todays.add(TodayTasks(
                    taskID = activityBinding.taskId!!,
                    isCompleted = false
                ))
                PrefManager.saveProjectTodayTasks(PrefManager.getcurrentProject(), todays)
                toast("Task added to your today")
            }

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
                    handleTaskArchive(activityBinding.taskId!!)
                },
                negative = {
                })

        }

    }
    private fun handleTaskArchive(taskID: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val task = db.tasksDao()
                .getTasksbyId(tasksId = taskID, projectId = PrefManager.getcurrentProject())
            task?.archived = true
            db.tasksDao().update(task!!)
            withContext(Dispatchers.Main) {
                updateTaskArchive(taskID = taskID, newArchive = true)
            }

        }

    }

    private fun updateTaskArchive(taskID: String,newArchive:Boolean) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Archiving the task")
        progressDialog.setCancelable(false)
        progressDialog.show()
        viewLifecycleOwner.lifecycleScope.launch {

            try {
                val result = withContext(Dispatchers.IO) {
                    viewModel.updateArchive(
                        taskID = taskID,
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

                    }

                }

            } catch (e: Exception) {

                Timber.tag(TaskDetailsFragment.TAG).e(e)

            }

        }
    }

    private fun shareTaskLink(link:String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, link)
        startActivity(Intent.createChooser(intent, "Share task link using"))
    }

    private fun createTaskLink(taskId: String){
        if (taskId[0]=='#' && taskId[1]=='T' && taskId.split(" ").size==1 && !taskId.contains('-')) {
            val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("${BuildConfig.DYNAMIC_LINK_HOST}/share/${taskId.substring(2)}/${PrefManager.getcurrentProject()}"))
                .setDomainUriPrefix(BuildConfig.DYNAMIC_LINK_HOST)
                .setAndroidParameters(
                    DynamicLink.AndroidParameters.Builder("com.ncs.o2")
                        .setMinimumVersion(1)
                        .build()
                )
                .buildDynamicLink()
            FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(dynamicLink.uri)
                .buildShortDynamicLink()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.progressBar.gone()
                        binding.buttons.visible()
                        val shortLink = task.result?.shortLink
                        dismiss()
                        shareTaskLink(shortLink.toString())
                    } else {
                        binding.progressBar.gone()
                        binding.buttons.visible()
                        utils.showSnackbar(requireView(), "Error Creating task link", 2000)
                    }
                }
        }
        else{

            val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("${BuildConfig.DYNAMIC_LINK_HOST}/share/${taskId.substringAfter('-', taskId)}/${PrefManager.getcurrentProject()}/${taskId.substringBefore('-').substring(1)}"))
                .setDomainUriPrefix(BuildConfig.DYNAMIC_LINK_HOST)
                .setAndroidParameters(
                    DynamicLink.AndroidParameters.Builder("com.ncs.o2")
                        .setMinimumVersion(1)
                        .build()
                )
                .buildDynamicLink()

            FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(dynamicLink.uri)
                .buildShortDynamicLink()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.progressBar.gone()
                        binding.buttons.visible()
                        val shortLink = task.result?.shortLink
                        dismiss()
                        shareTaskLink(shortLink.toString())
                    } else {
                        binding.progressBar.gone()
                        binding.buttons.visible()
                        utils.showSnackbar(requireView(), "Error Creating task link", 2000)
                    }
                }
        }
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }
}