package com.ncs.o2.UI.UIComponents.BottomSheets

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Utility.Colors
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.UI.CreateTask.CreateTaskActivity
import com.ncs.o2.UI.UIComponents.BottomSheets.CreateSegment.CreateSegmentBottomSheet
import com.ncs.o2.databinding.AddTagBottomSheetBinding
import net.datafaker.Faker

class AddTagsBottomSheet (private var TagsList: MutableList<Tag>, private val callback : getSelectedTagsCallback?=null): BottomSheetDialogFragment(){
    private var tagList: MutableList<Tag> = mutableListOf()




    lateinit var binding: AddTagBottomSheetBinding

    init {

        tagList = TagsList.toMutableList()

    }

    private val faker: Faker by lazy { Faker() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = AddTagBottomSheetBinding.inflate(inflater, container, false)
        if (TagsList.isNotEmpty()){
            binding.progressbar.gone()
            binding.chipGroup.visible()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActionbar()
        setViews()

    }
    private fun setActionbar() {
        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }

        binding.createSegmentBtn.setOnClickThrottleBounceListener {

            dismiss()
            val createTagsBottomSheet = CreateTagsBottomSheet(TagsList)
            createTagsBottomSheet.show(requireActivity().supportFragmentManager,"this")

        }
    }

    private fun setViews() {
        setBottomSheetConfig()
        val chipGroup = binding.chipGroup

        for (i in 0 until TagsList.size) {
            val tag = TagsList[i]
            val chip = Chip(requireContext())
            chip.text = tag.tagText
            chip.isCheckable = true
            chip.isClickable = true
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(tag.bgColor)))
            chip.setTextColor(ColorStateList.valueOf(Color.parseColor(tag.textColor)))
            chip.isChecked=tag.isChecked
            chipGroup.addView(chip)
            chip.setOnCheckedChangeListener { buttonView, isCheckedT ->
                val tag = TagsList[i]
                tag.isChecked = isCheckedT
                callback!!.onSelectedTags(tag, isCheckedT)
            }
        }
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = false
    }
    interface getSelectedTagsCallback{
        fun onSelectedTags(tag : Tag,isChecked:Boolean)
        fun onTagListUpdated(tagList: MutableList<Tag>)

    }

}







