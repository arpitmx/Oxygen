package com.ncs.o2.UI.UIComponents.BottomSheets

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.mifmif.common.regex.Main
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.state.SegmentItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.MainActivity
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
import net.datafaker.Faker
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
    var segmentSelectionListener: SegmentSelectionListener? = null
    var sectionSelectionListener:sendSectionsListListner?=null
    lateinit var sectionList:MutableList<String>
    private val faker: Faker by lazy { Faker() }
    private lateinit var segmentName:String
    @Inject
    lateinit var utils: GlobalUtils.EasyElements
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


    }

    private fun setActionbar() {
        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }

        binding.createSegmentBtn.setOnClickThrottleBounceListener {
            if (PrefManager.getAppMode()==Endpoints.ONLINE_MODE){
                dismiss()
                val createSegmentBottomSheet = CreateSegmentBottomSheet()
                createSegmentBottomSheet.show(requireActivity().supportFragmentManager,"this")

            }
            else{
                toast("Segments creation is not allowed")
            }
        }


    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }

    private fun setRecyclerView(segments: List<SegmentItem>) {
        Log.d("segments",segments.toString())
        val adapter = SegmentListAdapter(segments, this@SegmentSelectionBottomSheet)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.visible()
        binding.progressbar.gone()


    }

    override fun onClick(segment: SegmentItem, position: Int) {
        Toast.makeText(requireContext(), segment.segment_NAME, Toast.LENGTH_SHORT).show()
        if (type!="Search"){
            PrefManager.setcurrentsegment(segment.segment_NAME)
            segmentName=segment.segment_NAME
            sendsectionList(PrefManager.getcurrentProject())
            PrefManager.putsectionsList(sectionList)
        }
        segmentName=segment.segment_NAME
        segmentSelectionListener?.onSegmentSelected(segment.segment_NAME)
        sectionSelectionListener?.sendSectionsList(sectionList)
        dismiss()
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
                                                creation_DATETIME = segment.creation_DATETIME
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
                                            segments[0].creation_DATETIME!!
                                        )

                                        withContext(Dispatchers.Main) {
                                            setRecyclerView(PrefManager.getProjectSegments(projectName).distinctBy { it.segment_ID })
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
                    PrefManager.getProjectSegments(projectName).distinctBy { it.segment_NAME })
            }
            getNewSegments(projectName)
        }else{
            Log.d("segmentsFetchCheck","Fetching segments from cache")

            setRecyclerView(
                PrefManager.getProjectSegments(projectName).distinctBy { it.segment_NAME })
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
                                    creation_DATETIME = segment.creation_DATETIME
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
                                    segments[0].creation_DATETIME!!
                                )

                                withContext(Dispatchers.Main) {
                                    setRecyclerView(PrefManager.getProjectSegments(projectName).distinctBy { it.segment_ID })
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