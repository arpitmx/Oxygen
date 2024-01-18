package com.ncs.o2.UI.UIComponents.BottomSheets

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.ncs.o2.Constants.Errors
import com.ncs.o2.Domain.Models.state.SegmentItem
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.invisible
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.HelperClasses.ServerExceptions
import com.ncs.o2.R
import com.ncs.o2.databinding.CreateSectionBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateSectionsBottomSheet : BottomSheetDialogFragment(){
    lateinit var binding: CreateSectionBottomSheetBinding
    private val editTextList = mutableListOf<TextInputLayout>()
    private val sectionNameList= mutableListOf<String>()
    var num=0
    private lateinit var parentLayout: LinearLayoutCompat
    private val viewModel : CreateSectionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CreateSectionBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated( view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()

        binding.submit.setOnClickThrottleBounceListener{
            val segment=PrefManager.getSegmentDetails()
            binding.validationsTxt.gone()
            for (i in 0 until editTextList.size){
                sectionNameList.add(editTextList[i].editText?.text.toString().trim())
            }

            if (areAllsecctionNamesUnique(sectionNameList) && sectionNameList.size==num && !sectionNameList.contains("")){
                segment.creation_DATETIME= Timestamp.now()
                segment.sections=sectionNameList
                viewModel.createSegment(segment)
            }
            else if (!areAllsecctionNamesUnique(sectionNameList)){
                toast("Section names should be unique")
                sectionNameList.clear()
            }

        }
        inflatelayouts()


    }

    private fun setupView(){

        binding.closeBottmSheet.setOnClickThrottleBounceListener {
            dismiss()
        }

        viewModel.showprogressLD.observe(this){
            if (it){
                binding.progressbar.visible()
            }else {
                binding.progressbar.invisible()
            }
        }

        viewModel.segmentcreationLiveData.observe(this){ state->
            when(state){
                ServerExceptions.segement_created.exceptionDescription->{
                    val segment=PrefManager.getSegmentDetails()
                    val oldSegments=PrefManager.getProjectSegments(PrefManager.getcurrentProject())
                    val new = oldSegments.toMutableList()
                    new.add(SegmentItem(segment_NAME = segment.segment_NAME, sections = sectionNameList, segment_ID = segment.segment_ID, creation_DATETIME = Timestamp.now()))
                    Log.d("segmentCreationCacheCheck",new.toString())
                    PrefManager.saveProjectSegments(projectName = PrefManager.getcurrentProject(),new)
                    Log.d("segmentCreationCacheCheck",PrefManager.getProjectSegments(PrefManager.getcurrentProject()).toString())
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
                }

                else -> {
                    binding.validationsTxt.text = "*"+state
                }
            }


        }
    }
    private fun inflatelayouts(){
        binding.doneButton.setOnClickThrottleBounceListener{
            val num1= binding.numberOfSections.text!!.toString()
            if (num1 !="" && num1.matches(Regex("^[0-9]+$")) && num1 !=".") {
                num=num1.toInt()
                parentLayout = binding.editTextsContainer
                val inflater = LayoutInflater.from(requireContext())
                binding.editTextslayout.visible()
                editTextList.clear()
                for (i in 0 until num) {
                    val editText = inflater.inflate(
                        R.layout.custom_edit_text,
                        parentLayout,
                        false
                    ) as TextInputLayout

                    parentLayout.addView(editText)
                    editText.editText?.hint = "Section ${i + 1}"
                    editTextList.add(editText)
                    binding.numberOfSectionsCont.gone()
                    binding.btnCont.visible()
                }
            }
            else{
                toast("Re-check the inputs")
            }

        }
        binding.reset.setOnClickThrottleBounceListener {
            num=0
            binding.numberOfSections.setText("")
            parentLayout.removeAllViews()
            editTextList.clear()
            binding.editTextslayout.gone()
            binding.numberOfSectionsCont.visible()
            binding.btnCont.gone()
        }

    }
    fun areAllsecctionNamesUnique(list: List<String>): Boolean {
        val set = list.toSet()
        return list.size == set.size
    }
}
