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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.datafaker.Faker

class AddTagsBottomSheet (private var TagsList: MutableList<Tag>, private val callback : getSelectedTagsCallback?=null,private var selectedTagsList: MutableList<Tag>): BottomSheetDialogFragment(){
    private var tagList: MutableList<Tag> = mutableListOf()
    private var TagListfromFireStore: MutableList<Tag> = mutableListOf()

    lateinit var binding: AddTagBottomSheetBinding
    private val faker: Faker by lazy { Faker() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = AddTagBottomSheetBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressbar.visible()
        binding.chipGroup.gone()
        val job =  CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                val firestore = FirebaseFirestore.getInstance()
                val projectDocRef = firestore.collection("Projects").document("Versa") // Replace with actual project name

                projectDocRef.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val tags = documentSnapshot.get("TAGS") as List<HashMap<String, Any>>

                            for (tagData in tags) {
                                val tag = Tag(
                                    tagData["tagText"].toString(),
                                    tagData["bgColor"].toString(),
                                    tagData["textColor"].toString(),
                                    tagData["tagID"].toString()
                                )
                                TagListfromFireStore.add(tag)

                            }
                        } else {
                        }

                        TagsList = (selectedTagsList+TagsList+TagListfromFireStore).distinctBy {it.tagID}.toMutableList()
                        tagList = TagsList.toMutableList()

                        binding.progressbar.gone()
                        binding.chipGroup.visible()
                        setViews()
                    }
                    .addOnFailureListener { e ->
                        // Handle the failure
                    }
            }
        }
        job.start()
        setActionbar()
    }
    private fun setActionbar() {
        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }

        binding.createSegmentBtn.setOnClickThrottleBounceListener {

            dismiss()
            val createTagsBottomSheet = CreateTagsBottomSheet(selectedTagsList,TagsList,callback!!)
            createTagsBottomSheet.show(requireActivity().supportFragmentManager,"this")

        }
    }

    private fun setViews() {
        setBottomSheetConfig()

        val chipGroup = binding.chipGroup
        Log.d("Tag",TagsList.toString())

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







