package com.ncs.o2.UI.UIComponents.BottomSheets.CreateSegment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Utility.Codes
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.invisible
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.showKeyboard
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.ServerExceptions
import com.ncs.o2.R
import com.ncs.o2.databinding.CreateSegmentBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint


/*
File : ProfileBottomSheet.kt -> com.ncs.o2.BottomSheets
Description : Profile bottom sheet  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 3:14 am on 05/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/
@AndroidEntryPoint
class CreateSegmentBottomSheet :BottomSheetDialogFragment() {

    private val viewModel : CreateSegmentViewModel by viewModels()
    lateinit var binding: CreateSegmentBottomSheetBinding

    override fun onCreateView(
         inflater: LayoutInflater,
        container: ViewGroup?,
       savedInstanceState: Bundle?
    ): View {
        binding = CreateSegmentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated( view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViews()

        binding.doneButton.setOnClickThrottleBounceListener{

            binding.validationsTxt.gone()

            val title = binding.segmetTitleTv.text.toString().trim()
            val desc = binding.segmentDescriptionTv.text.toString().trim()
            if (title.isNotEmpty() and (desc.isNotEmpty()) and(title.length<=15) ){
                val segment = Segment(
                    SEGMENT_NAME = title,
                    DESCRIPTION = desc,
                    SEGMENT_ID = "",
                    PROJECT_ID = "Versa",
                )

                viewModel.createSegment(segment = segment)
            }else {
                binding.segmetTitleTv.error = "Invalid Input"
            }
        }


    }

    private fun setUpViews() {


        binding.segmetTitleTv.requestFocus()

        binding.closeBottmSheet.setOnClickThrottleBounceListener {
            this.dismiss()
        }

        viewModel.showprogressLD.observe(this){
            if (it){
                binding.progressbar.visible()
            }else {
                binding.progressbar.invisible()
            }
        }


        viewModel.segmentValidityLiveData.observe(this){ state ->

            binding.validationsTxt.visible()

            when (state){
                ServerExceptions.duplicateNameException.exceptionDescription -> {
                    binding.validationsTxt.text = getString(R.string.ommphs_this_segment_name_already_exists_choose_another_one)
                }
                ServerExceptions.projectDoesNotExists.exceptionDescription -> {
                    binding.validationsTxt.text = getString(R.string.this_project_doesn_t_exist)
                }
                Codes.Status.VALID_INPUT -> {
                    binding.validationsTxt.text = getString(R.string.creating_your_segment)
                }
                else -> {
                    binding.validationsTxt.text = "*"+state
                }
            }


            }
        }
    }
