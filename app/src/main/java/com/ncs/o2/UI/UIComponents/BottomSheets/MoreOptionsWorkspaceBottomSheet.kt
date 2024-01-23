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
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.Teams.TasksHolderActivity
import com.ncs.o2.databinding.MoreOptionBottomSheetBinding
import com.ncs.o2.databinding.MoreOptionsWorkspaceBottomsheetBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MoreOptionsWorkspaceBottomSheet : BottomSheetDialogFragment(){
    @Inject
    lateinit var utils : GlobalUtils.EasyElements
    lateinit var binding:MoreOptionsWorkspaceBottomsheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MoreOptionsWorkspaceBottomsheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
    }

    private fun setViews() {
        binding.assigned.setOnClickThrottleBounceListener {
            dismiss()
            requireActivity().recreate()
        }
        binding.moderating.setOnClickThrottleBounceListener {
            dismiss()
            startActivity("moderating")
        }
        binding.opened.setOnClickThrottleBounceListener {
            dismiss()
            startActivity("opened")
        }
    }

    private fun startActivity(type:String){
        val intent = Intent(requireContext(), TasksHolderActivity::class.java)
        intent.putExtra("type", type)
        intent.putExtra("index", "1")
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }

}