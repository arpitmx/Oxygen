package com.ncs.o2.UI.UIComponents.BottomSheets

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.BuildConfig
import com.ncs.o2.Domain.Models.TodayTasks
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.databinding.MoreOptionBottomSheetBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MoreOptionsBottomSheet : BottomSheetDialogFragment(){
    @Inject
    lateinit var utils : GlobalUtils.EasyElements
    lateinit var binding:MoreOptionBottomSheetBinding

    private val activityBinding: TaskDetailActivity by lazy {
        (requireActivity() as TaskDetailActivity)
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
                .setLink(Uri.parse("${BuildConfig.DYNAMIC_LINK_HOST}/${taskId.substringAfter('-', taskId)}/${PrefManager.getcurrentProject()}/${taskId.substringBefore('-').substring(1)}"))
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