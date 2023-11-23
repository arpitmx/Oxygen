package com.ncs.o2.UI.UIComponents.BottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.UI.UIComponents.Adapters.BottomSheetAdapter
import com.ncs.o2.databinding.LayoutPriorityBottomSheetBinding

class BottomSheet(private val dataList: List<String>,private val type:String,private val sendText: SendText):BottomSheetDialogFragment (),BottomSheetAdapter.onClickString{
    lateinit var binding: LayoutPriorityBottomSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutPriorityBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }
        when(type){
            "PRIORITY" -> binding.priority.visible()
            "STATE" -> binding.state.visible()
            "TYPE" -> binding.type.visible()
            "DIFFICULTY" -> binding.difficulty.visible()
        }
        setRecyclerView(dataList,type)
    }

    fun setRecyclerView(dataList: List<String>, type: String){
        val recyclerView=binding.rvPriority
        val adapter = BottomSheetAdapter(dataList.toMutableList(),type,requireContext(),this@BottomSheet)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.visible()
    }

    override fun sendString(text: String,type: String) {
        sendText.stringtext(text,type)
        dismiss()
    }
    interface SendText {
        fun stringtext(text: String,type: String)
    }

}