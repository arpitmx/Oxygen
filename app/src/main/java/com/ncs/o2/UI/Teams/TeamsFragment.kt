package com.ncs.o2.UI.Teams

import TaskListAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.load
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotate180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.set180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.UIComponents.Adapters.BottomSheetAdapter
import com.ncs.o2.databinding.FragmentTeamsBinding
import com.ncs.o2.databinding.FragmentTeamsChatBinding
import com.ncs.versa.Constants.Endpoints


class TeamsFragment : Fragment() {

    lateinit var binding:FragmentTeamsBinding
    private val activityBinding: MainActivity by lazy {
        (requireActivity() as MainActivity)
    }

    private var isVisible=false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeamsBinding.inflate(inflater, container, false)
        manageviews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.channels.setOnClickListener {
            binding.arrow.set180(requireContext())
            if (!isVisible){
                isVisible=true
                binding.extendedChannels.visible()
                setRecyclerView(listOf("general","random","gossip","memes"))
            }
            else{
                isVisible=false
                binding.extendedChannels.gone()
            }
        }

    }

    fun setRecyclerView(dataList: List<String>){
        val recyclerView=binding.channelsRv
        val adapter = ChannelsAdapter(dataList.toMutableList())
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.visible()
    }

    private fun manageviews(){
        val drawerLayout = activityBinding.binding.drawer
        activityBinding.binding.gioActionbar.btnHamTeams.setOnClickThrottleBounceListener {
            val gravity = if (!drawerLayout.isDrawerOpen(GravityCompat.START)) GravityCompat.START else GravityCompat.END
            drawerLayout.openDrawer(gravity)
        }
        activityBinding.binding.gioActionbar.tabLayout.gone()
        activityBinding.binding.gioActionbar.searchCont.visible()
        activityBinding.binding.gioActionbar.actionbar.visible()
        activityBinding.binding.gioActionbar.constraintLayout2.gone()
        activityBinding.binding.gioActionbar.constraintLayoutsearch.gone()
        activityBinding.binding.gioActionbar.constraintLayoutworkspace.gone()
        activityBinding.binding.gioActionbar.constraintLayoutTeams.visible()
        activityBinding.binding.gioActionbar.projectIcon.load(
            PrefManager.getProjectIconUrl(
                PrefManager.getcurrentProject()),resources.getDrawable(R.drawable.placeholder_image))
    }

}

