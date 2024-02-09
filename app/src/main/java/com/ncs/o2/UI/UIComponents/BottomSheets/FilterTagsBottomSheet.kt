package com.ncs.o2.UI.UIComponents.BottomSheets

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.databinding.AddTagBottomSheetBinding
import com.ncs.o2.databinding.FilterTagBottomSheetBinding
import com.ncs.versa.Constants.Endpoints
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import net.datafaker.Faker
import javax.inject.Inject

@AndroidEntryPoint
class FilterTagsBottomSheet (private var TagsList: MutableList<Tag>, private val callback : getSelectedTagsCallback?=null,private var selectedTagsList: MutableList<Tag>): BottomSheetDialogFragment(){
    private var tagList: MutableList<Tag> = mutableListOf()
    private var TagListfromFireStore: MutableList<Tag> = mutableListOf()
    private val viewmodel: TaskSectionViewModel by viewModels()
    lateinit var binding: FilterTagBottomSheetBinding
//    private val faker: Faker by lazy { Faker() }
    @Inject
    @FirebaseRepository
    lateinit var repository: Repository
    @Inject
    lateinit var db:TasksDatabase


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FilterTagBottomSheetBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressbar.visible()
        binding.chipGroup.gone()
        if (db.tagsDao().isNull){
            val job =  CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    FirebaseFirestore.getInstance().collection(Endpoints.PROJECTS).document(PrefManager.getcurrentProject()).collection(Endpoints.Project.TAGS).get()
                        .addOnSuccessListener { documentSnapshot ->
                            for (document in documentSnapshot.documents) {
                                val tagText = document.getString("tagText")
                                val tagID = document.getString("tagID")
                                val textColor = document.getString("textColor")!!
                                val bgColor = document.getString("bgColor")
                                val projectName=document.getString("projectName")
                                val tagItem = Tag(
                                    tagText = tagText!!,
                                    tagID = tagID!!,
                                    textColor = textColor,
                                    bgColor = bgColor!!,
                                    projectName = projectName!!
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
        }
        else{
            fetchfromdb()
        }


        setActionbar()
    }
    private fun setActionbar() {
        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }
    }

    private fun setViews() {
        setBottomSheetConfig()

        val chipGroup = binding.chipGroup
        Log.d("Tag", TagsList.toString())

        val checkedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isCheckedT ->
            if (isCheckedT) {
                for (i in 0 until chipGroup.childCount) {
                    val chip = chipGroup.getChildAt(i) as Chip
                    if (chip != buttonView && chip.isChecked) {
                        chip.isChecked = false
                        val tag = chip.tag as Tag
                        tag.checked = false
                        callback?.onSelectedTags(tag, false)
                    }
                }
            }

            val position = chipGroup.indexOfChild(buttonView)
            val tag = TagsList[position]
            tag.checked = isCheckedT
            callback?.onSelectedTags(tag, isCheckedT)
        }

        for (i in 0 until TagsList.size) {
            val tag = TagsList[i]
            val chip = Chip(requireContext())
            chip.text = tag.tagText
            chip.isCheckable = true
            chip.isClickable = true
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(tag.bgColor)))
            chip.setTextColor(ColorStateList.valueOf(Color.parseColor(tag.textColor)))
            chip.isChecked = tag.checked
            chip.tag = tag
            chip.setOnCheckedChangeListener(checkedChangeListener)
            chipGroup.addView(chip)
        }
    }


    private fun setBottomSheetConfig() {
        this.isCancelable = false
    }
    interface getSelectedTagsCallback{
        fun onSelectedTags(tag : Tag,isChecked:Boolean)
        fun onTagListUpdated(tagList: MutableList<Tag>)

    }
    fun fetchfromdb(){
        viewmodel.getTagsInProject(PrefManager.getcurrentProject()) { result ->
            when (result) {
                is DBResult.Success -> {
                    binding.progressbar.gone()
                    binding.chipGroup.visible()
                    TagListfromFireStore=result.data.toMutableList()
                    TagsList = (selectedTagsList+TagsList+TagListfromFireStore).distinctBy {it.tagID}.toMutableList()
                    tagList = TagsList.toMutableList()
                    setViews()
                }

                is DBResult.Failure -> {
                    binding.progressbar.gone()
                    binding.chipGroup.gone()
                    dismiss()
                }

                is DBResult.Progress -> {
                    binding.progressbar.visible()
                    binding.chipGroup.gone()
                }

            }
        }
    }

}







