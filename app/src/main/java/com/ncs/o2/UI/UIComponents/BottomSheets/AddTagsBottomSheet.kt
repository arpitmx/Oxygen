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
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.databinding.AddTagBottomSheetBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.datafaker.Faker
import javax.inject.Inject

@AndroidEntryPoint
class AddTagsBottomSheet (private var TagsList: MutableList<Tag>, private val callback : getSelectedTagsCallback?=null,private var selectedTagsList: MutableList<Tag>): BottomSheetDialogFragment(){
    private var tagList: MutableList<Tag> = mutableListOf()
    private var TagListfromFireStore: MutableList<Tag> = mutableListOf()

    lateinit var binding: AddTagBottomSheetBinding
    private val faker: Faker by lazy { Faker() }
    @Inject
    @FirebaseRepository
    lateinit var repository: Repository
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
        PrefManager.initialize(requireContext())
        val job =  CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                FirebaseFirestore.getInstance().collection(Endpoints.PROJECTS).document(PrefManager.getcurrentProject()).collection(Endpoints.Project.TAGS).get()
                    .addOnSuccessListener { documentSnapshot ->
                        for (document in documentSnapshot.documents) {
                            val tagText = document.getString("tagText")
                            val tagID = document.getString("tagID")
                            val textColor = document.getString("textColor")!!
                            val bgColor = document.getString("bgColor")
                            val tagItem = Tag(
                                tagText = tagText!!,
                                tagID = tagID,
                                textColor = textColor,
                                bgColor = bgColor!!,
                            )
                            TagListfromFireStore.add(tagItem)
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
            chip.isChecked=tag.checked
            chipGroup.addView(chip)
            chip.setOnCheckedChangeListener { buttonView, isCheckedT ->
                val tag = TagsList[i]
                tag.checked = isCheckedT
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







