package com.ncs.o2.UI.UIComponents.BottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.UI.UIComponents.Adapters.SegmentListAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.CreateSegment.CreateSegmentBottomSheet
import com.ncs.o2.databinding.SegmetSelectionBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.internal.lifecycle.HiltViewModelMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.datafaker.Faker

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

    lateinit var binding: SegmetSelectionBottomSheetBinding
    private val recyclerView: RecyclerView by lazy {
        binding.recyclerViewSegments
    }

    private val faker: Faker by lazy { Faker() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SegmetSelectionBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews()

    }

    private fun setViews() {


        setBottomSheetConfig()
        setActionbar()

        CoroutineScope(Dispatchers.IO).launch {
            val segList = mutableListOf<Segment>()
            repeat(4) {
                val segment = Segment(
                    SEGMENT_ID = faker.idNumber().toString(),
                    SEGMENT_NAME = faker.clashOfClans().defensiveBuilding().toString()
                )
                synchronized(segList) {
                    segList.add(segment)
                }
            }

           withContext(Dispatchers.Main){
               setRecyclerView(segList)
           }

        }
    }

    private fun setActionbar() {
        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }

        binding.createSegmentBtn.setOnClickThrottleBounceListener {

            dismiss()
            val createSegmentBottomSheet = CreateSegmentBottomSheet()
            createSegmentBottomSheet.show(requireActivity().supportFragmentManager,"this")


        }
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }

    private fun setRecyclerView(segments: MutableList<Segment>) {

        val adapter = SegmentListAdapter(segments, this@SegmentSelectionBottomSheet)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.visible()
        binding.progressbar.gone()


    }

    override fun onClick(segment: Segment, position: Int) {
        Toast.makeText(requireContext(), segment.SEGMENT_NAME, Toast.LENGTH_SHORT).show()
        dismiss()
    }
}