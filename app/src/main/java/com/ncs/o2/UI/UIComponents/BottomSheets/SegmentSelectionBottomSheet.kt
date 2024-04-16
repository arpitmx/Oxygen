package com.ncs.o2.UI.UIComponents.BottomSheets

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
//import com.mifmif.common.regex.Main
import com.ncs.o2.Constants.Pref
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.state.SegmentItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.UIComponents.Adapters.SegmentListAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.CreateSegment.CreateSegmentBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.CreateSegment.CreateSegmentViewModel
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.SegmetSelectionBottomSheetBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
//import net.datafaker.Faker
import javax.inject.Inject

/*
File : SegmentSelectionBottomSheet.kt -> com.ncs.o2.UI.UIComponents.BottomSheets
Description : BottomSheet for segment selection 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 11:55 pm on 08/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/
@AndroidEntryPoint
class SegmentSelectionBottomSheet(private val type:String) : BottomSheetDialogFragment(),
    SegmentListAdapter.OnClickCallback {
    @Inject lateinit var firestoreRepository:FirestoreRepository
    private var segments:List<Segment> = emptyList()
    lateinit var binding: SegmetSelectionBottomSheetBinding
    private val recyclerView: RecyclerView by lazy {
        binding.recyclerViewSegments
    }
    private val activityBinding: ActivityMainBinding by lazy {
        (requireActivity() as MainActivity).binding
    }
    var segmentSelectionListener: SegmentSelectionListener? = null
    var sectionSelectionListener:sendSectionsListListner?=null
    lateinit var sectionList:MutableList<String>
//    private val faker: Faker by lazy { Faker() }
    private lateinit var segmentName:String
    private val utils: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(requireActivity())
    }
    private val viewModel: TaskSectionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SegmetSelectionBottomSheetBinding.inflate(inflater, container, false)

        fetchSegments(PrefManager.getcurrentProject())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sectionList = mutableListOf()
        setViews()

    }

    private fun setViews() {


        setBottomSheetConfig()
        setActionbar()

        if (PrefManager.getcurrentUserdetails().ROLE>=3){
            binding.closeBtn.gone()
            binding.archiveBtn.visible()
        }

    }

    private fun setActionbar() {
        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }

        binding.createSegmentBtn.setOnClickThrottleBounceListener {
            if (PrefManager.getAppMode()==Endpoints.ONLINE_MODE){

                if (PrefManager.getcurrentUserdetails().ROLE>=2){
                    dismiss()
                    val createSegmentBottomSheet = CreateSegmentBottomSheet()
                    createSegmentBottomSheet.show(requireActivity().supportFragmentManager,"this")

                }else{
                    toast(getString(R.string.insufficient_permission_for_your_role_to_create_segment))
                }

            }
            else{
                toast(getString(R.string.segments_creation_is_not_allowed_in_offline_mode))
            }
        }

        binding.archiveBtn.setOnClickThrottleBounceListener {
            dismiss()
            val archiveSegmentBottomSheet = ArchiveSegmentBottomSheet()
            archiveSegmentBottomSheet.show(requireActivity().supportFragmentManager,"this")
        }

    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }

    private fun setRecyclerView(segments: List<SegmentItem>) {
        Timber.tag("segments").d(segments.toString())
        val adapter = SegmentListAdapter(segments.sortedBy { it.creation_DATETIME }, this@SegmentSelectionBottomSheet)
        val linearLayoutManager = LinearLayoutManager(requireContext())

        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.visible()
        binding.progressbar.gone()

    }

    override fun onClick(segment: SegmentItem, position: Int) {
        Toast.makeText(requireContext(), segment.segment_NAME, Toast.LENGTH_SHORT).show()
        val segments=PrefManager.getProjectSegments(PrefManager.getcurrentProject())
        val sections:MutableList<String> = mutableListOf()
        for(seg in segments){
            if (seg.segment_NAME==segment.segment_NAME){
                sections.addAll(seg.sections.toSet().toMutableList())
            }
        }
        sectionList=sections
        Log.d("sectionsListsegment",segment.toString())
        Log.d("sectionsList",sectionList.toString())

        if (type!="Search" && type!="Quick Task" && type!="Create Task"){
//            PrefManager.setcurrentsegment(segment.segment_NAME)
            segmentName=segment.segment_NAME
//            sendsectionList(PrefManager.getcurrentProject())
//            PrefManager.putsectionsList(sectionList)
        }
        if (type=="MainActivity" || type=="Create Task" ){
            PrefManager.setcurrentsegment(segment.segment_NAME)
            PrefManager.setcurrentsection(sectionList[0])
            segmentName=segment.segment_NAME
            sendsectionList(PrefManager.getcurrentProject())
            PrefManager.putsectionsList(sectionList)
        }
        segmentName=segment.segment_NAME
        segmentSelectionListener?.onSegmentSelected(segment.segment_NAME)
        sectionSelectionListener?.sendSectionsList(sectionList)
        dismiss()
    }

    override fun onLongClick(segment: SegmentItem, position: Int) {
        if (PrefManager.getcurrentUserdetails().ROLE>=3){
            requireContext().performHapticFeedback()
            utils.twoBtn(title = "Archive Segment",
                msg = "Do you want to archive this segment ?", positiveBtnText = "Archive", negativeBtnText = "Cancel", positive = {
                    val segments=PrefManager.getProjectSegments(PrefManager.getcurrentProject()).distinctBy { it.segment_ID }.toMutableList()
                    Log.d("segmentsArchive",segments.toString())
                    for (i in 0 until segments.size){
                        if (segments[i].segment_ID==segment.segment_ID){
                            segments[i].archived=true
                            firestoreRepository.updateSegment(PrefManager.getcurrentProject(),segments[i]) { serverResult ->
                                when (serverResult) {
                                    is ServerResult.Success -> {
                                        binding.recyclerViewSegments.gone()
                                        binding.progressbar.visible()
                                    }
                                    is ServerResult.Failure -> {
                                        val exception = serverResult.exception
                                        binding.recyclerViewSegments.visible()
                                        binding.progressbar.gone()
                                    }
                                    is ServerResult.Progress -> {
                                        binding.recyclerViewSegments.gone()
                                        binding.progressbar.visible()
                                    }
                                }
                            }
                        }
                    }
                    PrefManager.setLastSegmentsTimeStamp(PrefManager.getcurrentProject(), Timestamp.now())
                    PrefManager.saveProjectSegments(PrefManager.getcurrentProject(),segments)
                    Log.d("segmentsArchive",PrefManager.getProjectSegments(PrefManager.getcurrentProject()).distinctBy { it.segment_ID }.toString())
                    toast("Segment Archived")
                    if (PrefManager.getcurrentsegment()==segment.segment_NAME){
                        PrefManager.setcurrentsegment("Select Segment")
                        findNavController().navigate(R.id.task_item)
                        viewModel.updateCurrentSegment("Select Segment")
                        activityBinding.gioActionbar.titleTv.text="Select Segment"
                    }
                    dismiss()

                }, negative = {})

        }
    }

    interface SegmentSelectionListener {
        fun onSegmentSelected(segmentName: String)
    }
    interface sendSectionsListListner{
        fun sendSectionsList(list:MutableList<String>)

    }

    private fun fetchSegments(projectName: String) {
        if (PrefManager.getAppMode()==Endpoints.ONLINE_MODE) {
            if (PrefManager.getLastSegmentsTimeStamp(projectName).seconds.toInt() == 0) {
                Log.d("segmentsFetchCheck","Fetching segments from firebase")
                firestoreRepository.getSegments(projectName) { serverResult ->
                    when (serverResult) {
                        is ServerResult.Success -> {
                            binding.progressbar.gone()
                            if (serverResult.data.isNotEmpty()) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val segList = serverResult.data
                                    var segments: List<SegmentItem> =
                                        serverResult.data.map { segment ->
                                            SegmentItem(
                                                segment_NAME = segment.segment_NAME,
                                                sections = segment.sections,
                                                segment_ID = segment.segment_ID,
                                                creation_DATETIME = segment.creation_DATETIME,
                                                archived = segment.archived,
                                                last_updated = segment.last_updated!!

                                            )
                                        }
                                    val list = PrefManager.getProjectSegments(projectName).toMutableList()
                                    Log.d("segmentsFetchCheck", "old segments : \n ${list.toString()}")
                                    segments = segments.distinctBy { it.segment_ID }.sortedByDescending { it.creation_DATETIME }
                                    Log.d("segmentsFetchCheck", "new segments : \n ${segments.toString()}")
                                    list.addAll(segments)
                                    Log.d("segmentsFetchCheck", "after addition : \n ${list.toString()}")
                                    PrefManager.saveProjectSegments(projectName, list)


                                    if (segments.isNotEmpty()) {
                                        PrefManager.setLastSegmentsTimeStamp(
                                            projectName,
                                            segments[0].last_updated!!
                                        )

                                        withContext(Dispatchers.Main) {
                                            setRecyclerView(PrefManager.getUnArchivedProjectSegments(projectName).distinctBy { it.segment_ID })
                                        }
                                    }

                                }
                            }
                        }

                        is ServerResult.Failure -> {
                            val exception = serverResult.exception
                        }

                        is ServerResult.Progress -> {
                            binding.progressbar.visible()
                        }
                    }
                }
            } else {
                Log.d("segmentsFetchCheck","Fetching segments from cache")

                setRecyclerView(
                    PrefManager.getUnArchivedProjectSegments(projectName).distinctBy { it.segment_NAME })
            }
            getNewSegments(projectName)
        }else{
            Log.d("segmentsFetchCheck","Fetching segments from cache")

            setRecyclerView(
                PrefManager.getUnArchivedProjectSegments(projectName).distinctBy { it.segment_NAME })
        }

    }

    private fun getNewSegments(projectName: String){
        firestoreRepository.getNewSegments(projectName) { serverResult ->
            when (serverResult) {
                is ServerResult.Success -> {
                    binding.progressbar.gone()
                    if (serverResult.data.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            Log.d(
                                "segmentsFetchCheck",
                                "Looking for new segments found: \n ${serverResult.data}"
                            )

                            val segList = serverResult.data
                            var segments: List<SegmentItem> = serverResult.data.map { segment ->
                                SegmentItem(
                                    segment_NAME = segment.segment_NAME,
                                    sections = segment.sections,
                                    segment_ID = segment.segment_ID,
                                    creation_DATETIME = segment.creation_DATETIME,
                                    archived =segment.archived,
                                    last_updated = segment.last_updated!!
                                )
                            }

                            val list = PrefManager.getProjectSegments(projectName).distinctBy { it.segment_ID }.toMutableList()
                            Log.d("segmentsFetchCheck", "old segments : \n ${list.toString()}")
                            segments = segments.distinctBy { it.segment_ID }.sortedByDescending { it.creation_DATETIME }
                            Log.d("segmentsFetchCheck", "new segments : \n ${segments.toString()}")

                            Log.d("segmentsFetchCheck", "after addition : \n ${list.toString()}")
                            val filteredList = list.filter { listItem ->
                                segments.none { it.segment_ID == listItem.segment_ID }
                            }
                            val combinedList = filteredList + segments
                            list.clear()
                            list.addAll(combinedList)
                            PrefManager.saveProjectSegments(projectName, list)

                            Log.d("newwk", "after addition : \n ${list.toString()}")

                            if (segments.isNotEmpty()) {
                                PrefManager.setLastSegmentsTimeStamp(
                                    projectName,
                                    segments[0].last_updated!!
                                )

                                withContext(Dispatchers.Main) {
                                    setRecyclerView(PrefManager.getUnArchivedProjectSegments(projectName).distinctBy { it.segment_ID })
                                }
                            }

                        }
                    }
                }
                is ServerResult.Failure -> {
                    val exception = serverResult.exception
                }
                is ServerResult.Progress -> {
                    binding.progressbar.visible()
                }
            }
        }
    }

    fun updateList(list1: List<SegmentItem>, list2: List<SegmentItem>): List<SegmentItem> {
        val updatedList = mutableListOf<SegmentItem>()
        val updatedIdsSet = mutableSetOf<String>()
        val mapList1 = list1.associateBy { it.segment_ID }
        for (item2 in list2) {
            val id = item2.segment_ID
            if (mapList1.containsKey(id)) {
                val item1 = mapList1[id]!!
                val updatedItem = item1.copy(
                    segment_NAME = item2.segment_NAME,
                    sections = item2.sections,
                    segment_ID = item2.segment_ID,
                    creation_DATETIME = item2.creation_DATETIME,
                    last_updated = item2.last_updated,
                    archived = item2.archived
                )
                updatedList.add(updatedItem)
                updatedIdsSet.add(id)
            } else {
                updatedList.add(item2)
            }
        }
        for (item1 in list1) {
            if (!updatedIdsSet.contains(item1.segment_ID)) {
                updatedList.add(item1)
            }
        }

        return updatedList
    }

    private fun sendsectionList(projectName: String) {
//        firestoreRepository.getSegments(projectName) { serverResult ->
//            when (serverResult) {
//                is ServerResult.Success -> {
//                    binding.progressbar.gone()
//                    CoroutineScope(Dispatchers.Main).launch {
//                        val segList = serverResult.data
//                        if (segList.isNotEmpty()) {
//                            for (i in 0 until segList.size) {
//                                if (segList[i].segment_NAME == segmentName) {
//                                    sectionList = segList[i].sections
//                                }
//                            }
//                            PrefManager.putsectionsList(sectionList)
//                            withContext(Dispatchers.Main) {
//                                PrefManager.list.value = sectionList
//                            }
//                        }
//                    }
//                }
//                is ServerResult.Failure -> {
//                    val exception = serverResult.exception
//                    // Handle the failure here
//                }
//                is ServerResult.Progress -> {
//                    binding.progressbar.visible()
//                }
//            }
//        }
        val segments=PrefManager.getProjectSegments(projectName)
        val newList=segments.distinctBy { it.segment_NAME }
        for(segment in newList){
            if (segment.segment_NAME==segmentName){
                sectionList=segment.sections
            }
        }
        PrefManager.putsectionsList(sectionList)
        PrefManager.list.value = sectionList

    }

}