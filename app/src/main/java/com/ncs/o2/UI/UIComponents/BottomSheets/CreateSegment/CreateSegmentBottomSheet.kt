package com.ncs.o2.UI.UIComponents.BottomSheets.CreateSegment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.ncs.o2.Constants.Errors
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.invisible
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.HelperClasses.ServerExceptions
import com.ncs.o2.R
import com.ncs.o2.UI.UIComponents.BottomSheets.CreateSectionsBottomSheet
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
    var segment: Segment?=null
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

        val current_project=PrefManager.getcurrentProject()

        binding.doneButton.setOnClickThrottleBounceListener{
            binding.validationsTxt.gone()
            setUpViews()

            val title = binding.segmetTitleTv.text.toString().trim()
            val desc = binding.segmentDescriptionTv.text.toString().trim()
            if (title.isNotEmpty() and (desc.isNotEmpty()) and(title.length<=15) ){
                segment = Segment(
                    segment_NAME = title.toLowerCase().capitalize(),
                    description = desc,
                    segment_ID = "${title.toLowerCase()}_${System.currentTimeMillis().toString().substring(8,12)}",
                    project_ID = current_project,
                    creation_DATETIME = Timestamp.now(),
                    segment_CREATOR = PrefManager.getcurrentUserdetails().USERNAME,
                    segment_CREATOR_ID = FirebaseAuth.getInstance().currentUser?.uid!!
                )
                viewModel.createSegment(segment = segment!!)
                PrefManager.setSegmentdetails(segment!!)

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

        viewModel.segmentcreationLiveData.observe(this){ state->
            binding.validationsTxt.visible()
            when(state){
                ServerExceptions.segement_created.exceptionDescription->{
                    binding.validationsTxt.text="Segment created"
                    this.dismiss()
                    this.toast("Segment Created")
                }
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
                Errors.Status.VALID_INPUT -> {
                    binding.validationsTxt.text = getString(R.string.creating_your_segment)
                    this.dismiss()

                    val createSectionsBottomSheet = CreateSectionsBottomSheet()
                    createSectionsBottomSheet.show(requireActivity().supportFragmentManager,"this")
                }

                else -> {
                    binding.validationsTxt.text = "*"+state
                }
            }


            }
        }

    }

