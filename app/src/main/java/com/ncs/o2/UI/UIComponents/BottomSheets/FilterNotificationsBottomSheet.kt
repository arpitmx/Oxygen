package com.ncs.o2.UI.UIComponents.BottomSheets

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Models.TodayTasks
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.UIComponents.Adapters.NotificationFilterAdpater
import com.ncs.o2.UI.UIComponents.Adapters.UserListAdapter
import com.ncs.o2.databinding.MoreOptionBottomSheetBinding
import com.ncs.o2.databinding.NotificationsFilterBottomsheetBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FilterNotificationsBottomSheet(val callback: GetFiltersCallback,var OList:MutableList<FilterNotifications>, val selectedFilter :MutableList<FilterNotifications>) : BottomSheetDialogFragment(),NotificationFilterAdpater.OnFilterClick{

    lateinit var binding:NotificationsFilterBottomsheetBinding
    lateinit var adapter:NotificationFilterAdpater
    private var TList: MutableList<FilterNotifications> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NotificationsFilterBottomsheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
    }

    private fun setViews() {
        setBottomSheetConfig()
        setActionbar()
        initViews()
    }

    private fun setActionbar() {
        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }
    }

    private fun initViews(){
        OList=(selectedFilter+OList).distinctBy { it.index }.sortedBy { it.index }.toMutableList()
        TList = OList
        adapter = NotificationFilterAdpater(OList, this)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        val recyclerView=binding.filtersRv
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.visible()
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }

    data class FilterNotifications(
        val index:Int,
        val title:String,
        var isChecked:Boolean,
    )

    override fun onClick(filter: FilterNotifications, position: Int, isChecked: Boolean) {
        if (isChecked) {
            TList[position].isChecked = isChecked

        } else {
            TList[position].isChecked = isChecked
        }
        callback.sendFilter(filter,isChecked,position)
        dismiss()
    }
    interface GetFiltersCallback {

        fun sendFilter(filter:FilterNotifications,isChecked: Boolean,position: Int)
    }
}
