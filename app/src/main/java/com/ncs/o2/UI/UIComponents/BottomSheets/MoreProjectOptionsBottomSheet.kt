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
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.databinding.MoreOptionBottomSheetBinding
import com.ncs.o2.databinding.MoreProjectOptionBottomsheetBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MoreProjectOptionsBottomSheet : BottomSheetDialogFragment(){
    @Inject
    lateinit var utils : GlobalUtils.EasyElements
    @Inject
    @FirebaseRepository
    lateinit var repository: Repository
    lateinit var binding:MoreProjectOptionBottomsheetBinding

    private val activityBinding: TaskDetailActivity by lazy {
        (requireActivity() as TaskDetailActivity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MoreProjectOptionBottomsheetBinding.inflate(inflater, container, false)
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
            if (PrefManager.getAppMode()== Endpoints.ONLINE_MODE) {
                getProjectLink()
            }
            else{
               toast("Error getting project link,you are offline")
            }
        }
    }

    private fun shareProjectLink(link:String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, link)
        startActivity(Intent.createChooser(intent, "Share Project link using"))
    }

    private fun getProjectLink(){
        repository.getProjectLink(PrefManager.getcurrentProject()) { result ->

            when (result) {

                is ServerResult.Failure -> {
                    binding.progressBar.gone()
                    binding.buttons.visible()
                    utils.showSnackbar(requireView(),"Error getting project link",2000)
                }

                ServerResult.Progress -> {
                    binding.progressBar.visible()
                    binding.buttons.gone()
                }

                is ServerResult.Success -> {
                    binding.progressBar.gone()
                    binding.buttons.visible()
                    Log.d("projectLink",result.data)
                    shareProjectLink(result.data)
                    dismiss()
                }

            }
        }
    }



    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }
}