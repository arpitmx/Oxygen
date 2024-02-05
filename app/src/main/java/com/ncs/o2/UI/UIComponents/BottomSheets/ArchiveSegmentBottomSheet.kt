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
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.UIComponents.Adapters.SegmentListAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.CreateSegment.CreateSegmentBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.CreateSegment.CreateSegmentViewModel
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.ArchiveSegmentsBottomsheetBinding
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


@AndroidEntryPoint
class ArchiveSegmentBottomSheet(private val type:String,val segmentSelectionListener:SegmentSelectionListener,val sectionSelectionListener:sendSectionsListListner) : BottomSheetDialogFragment(),
    SegmentListAdapter.OnClickCallback {
    @Inject lateinit var firestoreRepository:FirestoreRepository
    private var segments:List<Segment> = emptyList()
    lateinit var binding: ArchiveSegmentsBottomsheetBinding
    private val recyclerView: RecyclerView by lazy {
        binding.recyclerViewSegments
    }

    lateinit var sectionList:MutableList<String>
    private val faker: Faker by lazy { Faker() }
    private lateinit var segmentName:String
    private val utils: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(requireActivity())
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ArchiveSegmentsBottomsheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()

    }

    private fun setViews() {


        setBottomSheetConfig()
        setActionbar()
        setRecyclerView(PrefManager.getArchivedProjectSegments(PrefManager.getcurrentProject()).distinctBy { it.segment_ID })
    }

    private fun setActionbar() {
        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }

    private fun setRecyclerView(segments: List<SegmentItem>) {
        Log.d("segments",segments.toString())
        val adapter = SegmentListAdapter(segments, this@ArchiveSegmentBottomSheet)
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

        if (type!="Search" && type!="Quick Task" && type!="Create Task"){
            segmentName=segment.segment_NAME
        }
        if (type=="MainActivity" || type=="Create Task" ){
            PrefManager.setcurrentsegment(segment.segment_NAME)
            PrefManager.setcurrentsection(sectionList[0])
            segmentName=segment.segment_NAME
            sendsectionList(PrefManager.getcurrentProject())
            PrefManager.putsectionsList(sectionList)
        }
        segmentName=segment.segment_NAME
        segmentSelectionListener.onSegmentSelected(segment.segment_NAME)
        sectionSelectionListener.sendSectionsList(sectionList)
        dismiss()
    }

    override fun onLongClick(segment: SegmentItem, position: Int) {
        if (PrefManager.getcurrentUserdetails().ROLE>=3){
            requireContext().performHapticFeedback()
            utils.twoBtn(title = "Unarchive Segment",
                msg = "Do you want to unarchive this segment ?", positiveBtnText = "Unarchive", negativeBtnText = "Cancel", positive = {
                    val segments=PrefManager.getProjectSegments(PrefManager.getcurrentProject()).distinctBy { it.segment_ID }.toMutableList()
                    Log.d("segmentsArchive",segments.toString())
                    for (i in 0 until segments.size){
                        if (segments[i].segment_ID==segment.segment_ID){
                            segments[i].archived=false
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
                    toast("Segment Unarchived")
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

    private fun sendsectionList(projectName: String) {
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