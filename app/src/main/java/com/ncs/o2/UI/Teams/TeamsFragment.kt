package com.ncs.o2.UI.Teams

import TaskListAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Channel
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.load
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotate180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.set180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.UIComponents.Adapters.BottomSheetAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.AddTagsBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.CreateNewChannelBottomSheet
import com.ncs.o2.databinding.FragmentTeamsBinding
import com.ncs.o2.databinding.FragmentTeamsChatBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class TeamsFragment : Fragment(),ChannelsAdapter.OnClick,CreateNewChannelBottomSheet.OnChannelAdded {

    lateinit var binding:FragmentTeamsBinding
    private val activityBinding: MainActivity by lazy {
        (requireActivity() as MainActivity)
    }

    private var isVisible=false
    @Inject
    @FirebaseRepository
    lateinit var repository: Repository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeamsBinding.inflate(inflater, container, false)
        manageviews()
        if (PrefManager.getcurrentUserdetails().ROLE>=3){
            binding.addChannels.visible()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.channels.setOnClickListener {
            binding.arrow.set180(requireContext())
            if (!isVisible){
                isVisible=true
                binding.extendedChannels.visible()
                if (PrefManager.getLastChannelTimeStamp(PrefManager.getcurrentProject()).seconds.toInt()==0){
                    fetchChannels()
                }
                else{
                    Log.d("channelFetch","fetch from cache")
                    val oldList = PrefManager.getProjectChannels(PrefManager.getcurrentProject())
                    val newList = oldList.toMutableList().sortedByDescending { it.timestamp }
                    Log.d("channelFetch","fetch from cache new list: \n ${newList.toString()}")
                    if (newList.isNotEmpty()){
                        binding.placeholder.gone()
                        binding.channelsRv.visible()
                        setRecyclerView(newList.distinctBy { it.channel_id })
                    }
                }
                fetchNewChannels()

            }
            else{
                isVisible=false
                binding.extendedChannels.gone()
            }
        }

        binding.addChannels.setOnClickThrottleBounceListener {
            val newChannelsBottomShet =
                CreateNewChannelBottomSheet(this)
            newChannelsBottomShet.show(requireFragmentManager(), "Channels")
        }

    }

    fun fetchNewChannels(){
        repository.getNewChannels(
            PrefManager.getcurrentProject()
        ) { result ->
            when (result) {
                is ServerResult.Success -> {
                    binding.progressBar.gone()
                    if (result.data.isNotEmpty()){
                        Log.d("channelFetch","fetch new from firebase")
                        val data=result.data.sortedByDescending { it.timestamp }
                        val oldList = PrefManager.getProjectChannels(PrefManager.getcurrentProject())
                        val newList = (oldList.toMutableList() + result.data).sortedByDescending { it.timestamp }
                        Log.d("channelFetch","fetch new from firebase old list: \n ${oldList.toString()}")
                        Log.d("channelFetch","fetch new from firebase new list: \n ${newList.toString()}")
                        PrefManager.saveProjectChannels(PrefManager.getcurrentProject(),newList)
                        PrefManager.setLastChannelTimeStamp(PrefManager.getcurrentProject(),newList[0].timestamp!!)
                        setRecyclerView(newList.distinctBy { it.channel_id })
                    }
                }

                is ServerResult.Failure -> {
                    toast("Failure in loading Channels")
                    binding.progressBar.gone()
                }

                is ServerResult.Progress -> {
                    binding.progressBar.visible()
                }
            }
        }
    }

    fun fetchChannels(){
        repository.getChannels(
            PrefManager.getcurrentProject()
        ) { result ->
            when (result) {
                is ServerResult.Success -> {
                    binding.progressBar.gone()
                    if (result.data.isEmpty()){
                        binding.channelsRv.gone()
                        binding.placeholder.visible()
                    }
                    else{
                        Log.d("channelFetch","fetch from firebase")
                        binding.placeholder.gone()
                        binding.channelsRv.visible()
                        val data=result.data.sortedByDescending { it.timestamp }
                        val oldList = PrefManager.getProjectChannels(PrefManager.getcurrentProject())
                        val newList = (oldList.toMutableList() + result.data).sortedByDescending { it.timestamp }
                        Log.d("channelFetch","fetch from firebase old list: \n ${oldList.toString()}")
                        Log.d("channelFetch","fetch from firebase new list: \n ${newList.toString()}")

                        PrefManager.saveProjectChannels(PrefManager.getcurrentProject(),newList)
                        PrefManager.setLastChannelTimeStamp(PrefManager.getcurrentProject(),data[0].timestamp!!)
                        setRecyclerView(newList.distinctBy { it.channel_id })

                    }

                }

                is ServerResult.Failure -> {
                    toast("Failure in loading Channels")
                    binding.progressBar.gone()
                }

                is ServerResult.Progress -> {
                    binding.progressBar.visible()
                }
            }
        }
    }

    fun setRecyclerView(dataList: List<Channel>){
        val recyclerView=binding.channelsRv
        val adapter = ChannelsAdapter(dataList.toMutableList(),this)
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

    override fun onChannelClick(channel: Channel) {
        val intent = Intent(requireContext(), TeamsActivity::class.java)
        intent.putExtra("channel_id", channel.channel_id)
        intent.putExtra("channel_name", channel.channel_name)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }

    override fun onChannelAdd(channel: Channel) {
        val oldList = PrefManager.getProjectChannels(PrefManager.getcurrentProject())
        val newList = (oldList.toMutableList() + channel).sortedByDescending { it.timestamp }
        PrefManager.saveProjectChannels(PrefManager.getcurrentProject(),newList)
        PrefManager.setLastChannelTimeStamp(PrefManager.getcurrentProject(),channel.timestamp!!)
        setRecyclerView(newList.distinctBy { it.channel_id })
    }

}

