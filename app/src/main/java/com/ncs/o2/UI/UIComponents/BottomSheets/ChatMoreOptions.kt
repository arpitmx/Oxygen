package com.ncs.o2.UI.UIComponents.BottomSheets


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Domain.Models.CheckList
import com.ncs.o2.Domain.Models.Message
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.NotificationsUtils
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.databinding.MsgMoreOptionsBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ChatMoreOptions(private val message: com.ncs.o2.Domain.Models.Message, private val openBy:String, private val onReplyClick: OnReplyClick, private val senderName:String,
                      val segmentName: String?=null, val sectionName: String?=null) : BottomSheetDialogFragment(){
    @Inject
    lateinit var utils : GlobalUtils.EasyElements
    lateinit var binding:MsgMoreOptionsBinding
    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    private val activityBinding: TaskDetailActivity by lazy {
        (requireActivity() as TaskDetailActivity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MsgMoreOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
    }

    private fun setViews() {
        setBottomSheetConfig()
        setActionbar()
        setUpViews()
    }

    private fun setActionbar() {
        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }
    }
    private fun setUpViews(){

        if (PrefManager.getcurrentUserdetails().ROLE>=3){
            if (openBy=="Task Chat"){
                binding.addtoCheckList.visible()
            }
            else{
                binding.addtoCheckList.gone()
            }
        }
        else{
            binding.addtoCheckList.gone()
        }

        binding.addtoCheckList.setOnClickThrottleBounceListener {
            if (PrefManager.getAppMode()== Endpoints.ONLINE_MODE) {
                val checkList=CheckList(
                    id=RandomIDGenerator.generateRandomTaskId(5),
                    title = "CheckPoint",
                    desc = message.content,
                    done = false,
                    index = -100)
                addCheckList(checkList)
            }
            else{
                toast("CheckList can't be created while offline")
            }

        }

        binding.sendEmail.setOnClickThrottleBounceListener {
            if (PrefManager.getAppMode()== Endpoints.ONLINE_MODE) {
                dismiss()
                val sendCopyToMailBottomSheet = SendCopyToMailBottomSheet(message)
                sendCopyToMailBottomSheet.show(requireFragmentManager(), "Send Copy")
            }
            else{
                toast("Can't send email in offline mode")
            }

        }

        binding.createTask.setOnClickThrottleBounceListener {
            if (PrefManager.getAppMode()== Endpoints.ONLINE_MODE) {
                dismiss()
                val addQuickTaskBottomSheet = AddQuickTaskBottomSheet(message,segmentName,sectionName)
                addQuickTaskBottomSheet.show(requireFragmentManager(), "Quick Task")
            }
            else{
                toast("Task can't be created while offline")
            }

        }

        binding.copy.setOnClickThrottleBounceListener {
            dismiss()
            val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Copied Text", message.content)
            clipboardManager.setPrimaryClip(clipData)
            toast("Message copied")
        }

        binding.reply.setOnClickThrottleBounceListener {
            dismiss()
            onReplyClick.onReplyClicked(message,senderName)
        }


    }

    private fun addCheckList(checkList: CheckList){
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    firestoreRepository.createNewCheckList(
                        taskId = activityBinding.taskId!!,
                        projectName = PrefManager.getcurrentProject(),
                        checkList = checkList)

                }
                when (result) {
                    is ServerResult.Failure -> {

                        utils.singleBtnDialog(
                            "Failure",
                            "Failure in creating a new checklist: ${result.exception.message}",
                            "Okay"
                        ) {
                            requireActivity().finish()
                        }
                        binding.buttons.visible()
                        binding.progressBar.gone()

                    }

                    is ServerResult.Progress -> {
                        binding.buttons.gone()
                        binding.progressBar.visible()
                    }

                    is ServerResult.Success -> {
                        binding.buttons.visible()
                        binding.progressBar.gone()
                        toast("Added message as a CheckList")
                        dismiss()
                        val notification = composeNotification(
                            NotificationType.TASK_CHECKPOINT_NOTIFICATION,
                            message = "${PrefManager.getcurrentUserdetails().USERNAME} : ${checkList.desc}"
                        )
                        val filteredList = {
                            val list = activityBinding.sharedViewModel.getList()
                            if (list.contains(PrefManager.getUserFCMToken())) {
                                list.remove(PrefManager.getUserFCMToken())
                            }
                            list

                        }
                        notification?.let {
                            sendNotification(
                                filteredList.invoke(), notification
                            )
                        }
                    }

                }

            } catch (e: Exception) {

                Timber.tag(TaskDetailsFragment.TAG).e(e)
                binding.progressBar.gone()
                binding.buttons.visible()

            }
        }
    }

    private fun sendNotification(receiverList: MutableList<String>, notification: Notification) {

        try {
            CoroutineScope(Dispatchers.IO).launch {
                for (receiverToken in receiverList) {
                    NotificationsUtils.sendFCMNotification(
                        receiverToken, notification = notification
                    )
                }

            }

        } catch (exception: Exception) {
            Timber.tag("")
            utils.showSnackbar(binding.root, "Failure in sending notifications", 5000)
        }

    }

    private fun composeNotification(type: NotificationType, message: String): Notification? {

        if (type == NotificationType.TASK_CHECKPOINT_NOTIFICATION) {

            return Notification(
                notificationID = RandomIDGenerator.generateRandomTaskId(6),
                notificationType = NotificationType.TASK_CHECKPOINT_NOTIFICATION.name,
                taskID = activityBinding.taskId!!,
                message = message,
                title = "${PrefManager.getcurrentProject()} | ${activityBinding.taskId} | ${activityBinding.taskTitle} | New Checkpoint",
                fromUser = PrefManager.getcurrentUserdetails().EMAIL,
                toUser = "None",
                timeStamp = Timestamp.now().seconds,
                projectID = PrefManager.getcurrentProject(),
            )
        }
        return null
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }

    interface OnReplyClick{
        fun onReplyClicked(message: Message,senderName: String)
    }
}