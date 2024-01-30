package com.ncs.o2.UI.UIComponents.BottomSheets.Userlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailViewModel
import com.ncs.o2.databinding.AddQuickTaskBottomSheetBinding
import com.ncs.o2.databinding.EditTitleBottomsheetBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class EditTitleBottomSheet(private val title: String,private val onTitleUpdate: OnTitleUpdate) : BottomSheetDialogFragment(){
    @Inject
    lateinit var utils : GlobalUtils.EasyElements
    lateinit var binding:EditTitleBottomsheetBinding
    @Inject
    lateinit var firestoreRepository: FirestoreRepository
    private val viewModel: TaskDetailViewModel by viewModels()
    private val activityBinding: TaskDetailActivity by lazy {
        (requireActivity() as TaskDetailActivity)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EditTitleBottomsheetBinding.inflate(inflater, container, false)
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

        binding.title.setText(title)

        binding.submit.setOnClickThrottleBounceListener {
            if (binding.title.text.toString().trim().isBlank()){
                toast("Title can't be empty")
            }
            else{
                updateTitle(binding.title.text.toString().trim())
            }
        }


    }

    private fun updateTitle(newTitle:String){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    viewModel.updateTitle(
                        taskID = activityBinding.taskId,
                        projectName = PrefManager.getcurrentProject(),
                        newTitle = newTitle
                    )

                }

                when (result) {

                    is ServerResult.Failure -> {

                        utils.singleBtnDialog(
                            "Failure",
                            "Failure in Updating: ${result.exception.message}",
                            "Okay"
                        ) {
                            requireActivity().finish()
                        }
                        binding.progressBar.gone()
                        binding.body.visible()

                    }

                    is ServerResult.Progress -> {
                        binding.body.gone()
                        binding.progressBar.visible()
                    }

                    is ServerResult.Success -> {
                        binding.progressBar.gone()
                        binding.body.visible()
                        toast("Title Updated")
                        dismiss()
                        onTitleUpdate.ontitleUpdated(newTitle)
                    }

                }

            } catch (e: Exception) {

                Timber.tag(TaskDetailsFragment.TAG).e(e)
                binding.progressBar.gone()

            }
        }
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }

    interface OnTitleUpdate{
        fun ontitleUpdated(title: String)
    }
}