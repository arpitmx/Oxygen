package com.ncs.o2.UI.UIComponents.BottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.Codes.STRINGS.segmentText
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.UIComponents.Adapters.SegmentListAdapter
import com.ncs.o2.UI.UIComponents.Adapters.sectionListAdapter
import com.ncs.o2.databinding.ActivitySectionDisplayBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.datafaker.Faker
import javax.inject.Inject

@AndroidEntryPoint
class sectionDisplayBottomSheet() : BottomSheetDialogFragment(), sectionListAdapter.OnClickCallback {
    @Inject lateinit var firestoreRepository: FirestoreRepository
    lateinit var binding: ActivitySectionDisplayBottomSheetBinding
    private val recyclerView: RecyclerView by lazy {
        binding.recyclerViewSegments
    }

    private var sectionName = ""
    var sectionSelectionListener: SectionSelectionListener? = null

    lateinit var sectionList:MutableList<String>
    private val faker: Faker by lazy { Faker() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivitySectionDisplayBottomSheetBinding.inflate(inflater, container, false)

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
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }

    private fun setRecyclerView(sections_list: List<*>) {

        val adapter = sectionListAdapter(sections_list, this@sectionDisplayBottomSheet)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.visible()
        binding.progressbar.gone()
    }

    override fun onClick(sectionName: String) {
        Toast.makeText(requireContext(),sectionName,Toast.LENGTH_SHORT).show()
        sectionSelectionListener?.onSectionSelected(sectionName)
        dismiss()
    }

    interface sendSectionsListListner{
        fun sendSectionsList(list:MutableList<String>)

    }

    interface SectionSelectionListener {
        fun onSectionSelected(sectionName: String)
    }

    private fun fetchSegments(projectName: String) {
        firestoreRepository.getSection(projectName, segmentText) { serverResult ->
            when (serverResult) {
                is ServerResult.Success -> {
                    binding.progressbar.gone()
                    CoroutineScope(Dispatchers.IO).launch {
                        val secList = serverResult.data
                        if (secList.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                setRecyclerView(secList)
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


//    private fun sendsectionList(projectName: String) {
//        firestoreRepository.getSegments(projectName) { serverResult ->
//            when (serverResult) {
//                is ServerResult.Success -> {
//                    binding.progressbar.gone()
//                    CoroutineScope(Dispatchers.IO).launch {
//                        val segList = serverResult.data
//                        if (segList.isNotEmpty()) {
//                            for (i in 0 until segList.size){
//                                if (segList[i].segment_NAME == segmentName){
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
//    }


}