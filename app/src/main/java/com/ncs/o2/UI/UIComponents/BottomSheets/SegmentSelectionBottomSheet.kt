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
import com.mifmif.common.regex.Main
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.UIComponents.Adapters.SegmentListAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.CreateSegment.CreateSegmentBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.CreateSegment.CreateSegmentViewModel
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.SegmetSelectionBottomSheetBinding
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
class SegmentSelectionBottomSheet : BottomSheetDialogFragment(),
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

            dismiss()
            val createSegmentBottomSheet = CreateSegmentBottomSheet()
            createSegmentBottomSheet.show(requireActivity().supportFragmentManager,"this")
//            val createSectionsBottomSheet = CreateSectionsBottomSheet()
//            createSectionsBottomSheet.show(requireActivity().supportFragmentManager,"this")

        }
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }

    private fun setRecyclerView(segments: List<Segment>) {

        val adapter = SegmentListAdapter(segments, this@SegmentSelectionBottomSheet)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.visible()
        binding.progressbar.gone()


    }

    override fun onClick(segment: Segment, position: Int) {
        Toast.makeText(requireContext(), segment.segment_NAME, Toast.LENGTH_SHORT).show()
        PrefManager.setcurrentsegment(segment.segment_NAME)
        segmentName=segment.segment_NAME
        sendsectionList(PrefManager.getcurrentProject())
        PrefManager.putsectionsList(sectionList)
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
        firestoreRepository.getSegments(projectName) { serverResult ->
            when (serverResult) {
                is ServerResult.Success -> {
                    binding.progressbar.gone()
                    CoroutineScope(Dispatchers.IO).launch {
                        val segList = serverResult.data
                        if (segList.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                setRecyclerView(segList)
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
        firestoreRepository.getSegments(projectName) { serverResult ->
            when (serverResult) {
                is ServerResult.Success -> {
                    binding.progressbar.gone()
                    CoroutineScope(Dispatchers.Main).launch {
                        val segList = serverResult.data
                        if (segList.isNotEmpty()) {
                            for (i in 0 until segList.size) {
                                if (segList[i].segment_NAME == segmentName) {
                                    sectionList = segList[i].sections
                                }
                            }
                            PrefManager.putsectionsList(sectionList)
                            withContext(Dispatchers.Main) {
                                PrefManager.list.value = sectionList
                            }
                        }
                    }
                }
                is ServerResult.Failure -> {
                    val exception = serverResult.exception
                    // Handle the failure here
                }
                is ServerResult.Progress -> {
                    binding.progressbar.visible()
                }
            }
        }
    }



}