package com.ncs.o2.UI.UIComponents.BottomSheets
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.UI.CreateProject
import com.ncs.o2.UI.UIComponents.Adapters.SegmentListAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.CreateSegment.CreateSegmentBottomSheet
import com.ncs.o2.databinding.ProjectAddBottomSheetBinding

class AddProjectBottomSheet : BottomSheetDialogFragment(){

    lateinit var binding:ProjectAddBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ProjectAddBottomSheetBinding.inflate(inflater, container, false)
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

        binding.addProject.setOnClickThrottleBounceListener {
            startActivity(Intent(requireContext(),CreateProject::class.java))
            dismiss()
        }
        val link=binding.projectLink.text
        binding.submitLink.setOnClickThrottleBounceListener {

        }
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }

}