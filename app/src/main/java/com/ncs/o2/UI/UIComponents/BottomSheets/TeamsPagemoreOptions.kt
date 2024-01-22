package com.ncs.o2.UI.UIComponents.BottomSheets

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Channel
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.databinding.MoreOptionBottomSheetBinding
import com.ncs.o2.databinding.MoreProjectOptionBottomsheetBinding
import com.ncs.o2.databinding.TeamsPageOptionsBottomsheetBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TeamsPagemoreOptions(private val callback:OnChannelAdded) : BottomSheetDialogFragment(),CreateNewChannelBottomSheet.OnChannelAdded{

    lateinit var binding:TeamsPageOptionsBottomsheetBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TeamsPageOptionsBottomsheetBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActionbar()
        if (PrefManager.getcurrentUserdetails().ROLE>=3 && PrefManager.getAppMode()==Endpoints.ONLINE_MODE){
            binding.addChannels.visible()
        }
        else{
            binding.addChannels.gone()

        }
        binding.addChannels.setOnClickThrottleBounceListener {
            dismiss()
            val newChannelsBottomShet =
                CreateNewChannelBottomSheet(this)
            newChannelsBottomShet.show(requireFragmentManager(), "Channels")
        }
    }

    private fun setActionbar() {
        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }

    }
    override fun onChannelAdd(channel: Channel) {
        callback.onChannel(channel)
    }
    interface OnChannelAdded{
        fun onChannel(channel: Channel)
    }

}