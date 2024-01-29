package com.ncs.o2.UI.UIComponents.BottomSheets


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncs.o2.Domain.Models.CheckList
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.databinding.MsgMoreOptionsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MessageMoreOptions(private val message: com.ncs.o2.Domain.Models.Message,private val openBy:String) : BottomSheetDialogFragment(){
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
            val checkList=CheckList(
                id=RandomIDGenerator.generateRandomTaskId(5),
                title = "CheckPoint",
                desc = message.content,
                done = false,
                index = -100)
            addCheckList(checkList)
        }

        binding.createTask.setOnClickThrottleBounceListener {
            dismiss()
            val addQuickTaskBottomSheet = AddQuickTaskBottomSheet(message)
            addQuickTaskBottomSheet.show(requireFragmentManager(), "Quick Task")
        }


    }

    private fun addCheckList(checkList: CheckList){
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    firestoreRepository.createNewCheckList(
                        taskId = activityBinding.taskId,
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
                    }

                }

            } catch (e: Exception) {

                Timber.tag(TaskDetailsFragment.TAG).e(e)
                binding.progressBar.gone()
                binding.buttons.visible()

            }
        }
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }
}