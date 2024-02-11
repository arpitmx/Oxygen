package com.ncs.o2.UI.Teams

import TaskListAdapter
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.Data.Room.MessageRepository.MessageDatabase
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Channel
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.load
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotate180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.set180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.CreateTask.CreateTaskActivity
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.UIComponents.Adapters.BottomSheetAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.AddTagsBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.CreateNewChannelBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.MoreProjectOptionsBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.TeamsPagemoreOptions
import com.ncs.o2.databinding.FragmentTeamsBinding
import com.ncs.o2.databinding.FragmentTeamsChatBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import me.shouheng.utils.app.ActivityUtils.overridePendingTransition
import javax.inject.Inject


@AndroidEntryPoint
class TeamsFragment : Fragment(), ChannelsAdapter.OnClick, TeamsPagemoreOptions.OnChannelAdded {

    @Inject
    @FirebaseRepository
    lateinit var repository: Repository

    @Inject
    lateinit var db: TasksDatabase
    lateinit var binding: FragmentTeamsBinding
    private val activityBinding: MainActivity by lazy {
        (requireActivity() as MainActivity)
    }
    @Inject
    lateinit var msgDB:MessageDatabase
    private var isVisible = true
    private var isStatsVisible = true
    private var isTeamsVisible = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeamsBinding.inflate(inflater, container, false)

        manageviews()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val channels=PrefManager.getProjectChannels(PrefManager.getcurrentProject())

        for (ch in channels){
            getNewMessages(ch.channel_name)
        }
    }

    override fun onResume() {
        super.onResume()

        manageviews()
        setUpProjectStats()
        val channels=PrefManager.getProjectChannels(PrefManager.getcurrentProject())
        for (ch in channels){
            getNewMessages(ch.channel_name)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        OverScrollDecoratorHelper.setUpOverScroll(binding.parent)
        OverScrollDecoratorHelper.setUpOverScroll(binding.extendedStats)

        binding.parent.gone()
        binding.parent.animFadein(requireContext(), 300)
        binding.parent.visible()

        binding.extendedChannels.visible()

        activityBinding.binding.gioActionbar.btnMoreTeams.setOnClickThrottleBounceListener {
            val moreTeamsOptionBottomSheet =
                TeamsPagemoreOptions(this)
            moreTeamsOptionBottomSheet.show(requireFragmentManager(), "more")
        }

        if (PrefManager.getLastChannelTimeStamp(PrefManager.getcurrentProject()).seconds.toInt() == 0) {
            fetchChannels()
        } else {
            Log.d("channelFetch", "fetch from cache")
            val oldList = PrefManager.getProjectChannels(PrefManager.getcurrentProject())
            val newList = oldList.toMutableList().sortedByDescending { it.timestamp }
            Log.d("channelFetch", "fetch from cache new list: \n ${newList.toString()}")
            if (newList.isNotEmpty()) {
                binding.placeholder.gone()
                binding.channelsRv.visible()
                setRecyclerView(newList.distinctBy { it.channel_id })
            }
        }


        fetchNewChannels()

        binding.stats.setOnClickListener {
            binding.arrowStats.set180(requireContext())
            if (!isStatsVisible) {
                isStatsVisible = true
                binding.extendedStats.visible()
            } else {
                isStatsVisible = false
                binding.extendedStats.gone()
            }
        }

        binding.teams.setOnClickListener {
            binding.arrowTeam.set180(requireContext())
            if (!isTeamsVisible) {
                isTeamsVisible = true
                binding.extendedTeam.visible()
            } else {
                isTeamsVisible = false
                binding.extendedTeam.gone()
            }
        }

        binding.channels.setOnClickListener {
            binding.arrow.set180(requireContext())
            if (!isVisible) {
                isVisible = true
                binding.extendedChannels.visible()
                if (PrefManager.getLastChannelTimeStamp(PrefManager.getcurrentProject()).seconds.toInt() == 0) {
                    fetchChannels()
                } else {
                    Log.d("channelFetch", "fetch from cache")
                    val oldList = PrefManager.getProjectChannels(PrefManager.getcurrentProject())
                    val newList = oldList.toMutableList().sortedByDescending { it.timestamp }
                    Log.d("channelFetch", "fetch from cache new list: \n ${newList.toString()}")
                    if (newList.isNotEmpty()) {
                        binding.placeholder.gone()
                        binding.channelsRv.visible()
                        setRecyclerView(newList.distinctBy { it.channel_id })
                    }
                }
                fetchNewChannels()

            } else {
                isVisible = false
                binding.extendedChannels.gone()
            }
        }


        binding.pending.statParent.setOnClickThrottleBounceListener {
            val intent = Intent(requireContext(), TasksHolderActivity::class.java)
            intent.putExtra("type", "Pending")
            intent.putExtra("index", "2")
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        }
        binding.ongoing.statParent.setOnClickThrottleBounceListener {
            val intent = Intent(requireContext(), TasksHolderActivity::class.java)
            intent.putExtra("type", "Ongoing")
            intent.putExtra("index", "2")
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)

        }
        binding.review.statParent.setOnClickThrottleBounceListener {
            val intent = Intent(requireContext(), TasksHolderActivity::class.java)
            intent.putExtra("type", "Review")
            intent.putExtra("index", "2")
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)

        }
        binding.completed.statParent.setOnClickThrottleBounceListener {
            val intent = Intent(requireContext(), TasksHolderActivity::class.java)
            intent.putExtra("type", "Completed")
            intent.putExtra("index", "2")
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)

        }

        binding.viewTeamMember.setOnClickThrottleBounceListener {
            val intent = Intent(requireContext(), TeamsViewerActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        }

        binding.addTeamMembers.setOnClickThrottleBounceListener {

            val link = PrefManager.getProjectDeepLink(PrefManager.getcurrentProject())
            Log.d("deeplink", link)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, link)
            startActivity(Intent.createChooser(intent, "Share Project link using"))
        }

        binding.swiperefresh.setOnRefreshListener {
            syncCache(PrefManager.getcurrentProject())
        }

        activityBinding.binding.gioActionbar.searchBar.setOnClickThrottleBounceListener {
            activityBinding.navController.navigate(R.id.action_teams_to_search, bundleOf())
        }

    }

    private fun syncCache(projectName: String) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Please wait, Syncing Tasks")
        progressDialog.setCancelable(false)
        progressDialog.show()
        CoroutineScope(Dispatchers.Main).launch {
            try {

                val taskResult = withContext(Dispatchers.IO) {
                    repository.getTasksinProjectAccordingtoTimeStamp(projectName)
                }


                when (taskResult) {

                    is ServerResult.Failure -> {
                        progressDialog.dismiss()
                        binding.swiperefresh.isRefreshing = false
                        requireActivity().recreate()

                    }

                    is ServerResult.Progress -> {
                        progressDialog.show()
                        progressDialog.setMessage("Please wait, Syncing Tasks")

                    }

                    is ServerResult.Success -> {

                        val tasks = taskResult.data
                        if (tasks.isNotEmpty()) {
                            val newList = taskResult.data.toMutableList()
                                .sortedByDescending { it.last_updated }
                            PrefManager.setLastTaskTimeStamp(projectName, newList[0].last_updated!!)
                            for (task in tasks) {
                                db.tasksDao().insert(task)
                            }
                        }
                        progressDialog.dismiss()
                        requireActivity().recreate()
                        binding.swiperefresh.isRefreshing = false

                    }

                }

            } catch (e: java.lang.Exception) {
                progressDialog.dismiss()
            }

        }
    }



    fun fetchNewChannels() {
        repository.getNewChannels(
            PrefManager.getcurrentProject()
        ) { result ->
            when (result) {
                is ServerResult.Success -> {
                    binding.progressBar.gone()
                    if (result.data.isNotEmpty()) {
                        Log.d("channelFetch", "fetch new from firebase")
                        val data = result.data.sortedByDescending { it.timestamp }
                        val oldList =
                            PrefManager.getProjectChannels(PrefManager.getcurrentProject())
                        val newList =
                            (oldList.toMutableList() + result.data).sortedByDescending { it.timestamp }
                        Log.d(
                            "channelFetch",
                            "fetch new from firebase old list: \n ${oldList.toString()}"
                        )
                        Log.d(
                            "channelFetch",
                            "fetch new from firebase new list: \n ${newList.toString()}"
                        )
                        PrefManager.saveProjectChannels(PrefManager.getcurrentProject(), newList)
                        PrefManager.setLastChannelTimeStamp(
                            PrefManager.getcurrentProject(),
                            newList[0].timestamp!!
                        )
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

    fun fetchChannels() {
        repository.getChannels(
            PrefManager.getcurrentProject()
        ) { result ->
            when (result) {
                is ServerResult.Success -> {
                    binding.progressBar.gone()
                    if (result.data.isEmpty()) {
                        postDefaultChannel()
                    } else {
                        Log.d("channelFetch", "fetch from firebase")
                        binding.placeholder.gone()
                        binding.channelsRv.visible()
                        val data = result.data.sortedByDescending { it.timestamp }
                        val oldList =
                            PrefManager.getProjectChannels(PrefManager.getcurrentProject())
                        val newList =
                            (oldList.toMutableList() + result.data).sortedByDescending { it.timestamp }
                        Log.d(
                            "channelFetch",
                            "fetch from firebase old list: \n ${oldList.toString()}"
                        )
                        Log.d(
                            "channelFetch",
                            "fetch from firebase new list: \n ${newList.toString()}"
                        )

                        PrefManager.saveProjectChannels(PrefManager.getcurrentProject(), newList)
                        PrefManager.setLastChannelTimeStamp(
                            PrefManager.getcurrentProject(),
                            data[0].timestamp!!
                        )
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

    private fun postDefaultChannel() {
        if (PrefManager.getcurrentProject()!="None") {
            val channel = Channel(
                channel_name = "General",
                channel_desc = "General Description",
                channel_id = "General${RandomIDGenerator.generateRandomTaskId(4)}",
                timestamp = Timestamp.now(),
                creator = PrefManager.getCurrentUserEmail()
            )

            CoroutineScope(Dispatchers.Main).launch {
                repository.postChannel(channel, PrefManager.getcurrentProject()) { result ->

                    when (result) {

                        is ServerResult.Failure -> {
                            binding.progressBar.gone()
                        }

                        ServerResult.Progress -> {
                            binding.progressBar.visible()
                        }

                        is ServerResult.Success -> {
                            binding.progressBar.gone()
                            val oldList =
                                PrefManager.getProjectChannels(PrefManager.getcurrentProject())
                            val newList =
                                (oldList.toMutableList() + channel).sortedByDescending { it.timestamp }
                            PrefManager.saveProjectChannels(
                                PrefManager.getcurrentProject(),
                                newList
                            )
                            PrefManager.setLastChannelTimeStamp(
                                PrefManager.getcurrentProject(),
                                channel.timestamp!!
                            )
                            setRecyclerView(newList.distinctBy { it.channel_id })
                        }
                    }
                }
            }
        }
    }

    fun setUpProjectStats() {
        CoroutineScope(Dispatchers.IO).launch {
            val currentProject = PrefManager.getcurrentProject()
            val submittedTasks = getFilteredTasksBySegmentState(currentProject, 1)
            val openTasks = getFilteredTasksBySegmentState(currentProject, 2)
            val ongoingTasks = getFilteredTasksBySegmentState(currentProject, 3)
            val reviewTasks = getFilteredTasksBySegmentState(currentProject, 4)
            val completedTasks = getFilteredTasksBySegmentState(currentProject, 5)

            Log.d("listsizeTeams",submittedTasks.size.toString())
            Log.d("listsizeTeams",openTasks.size.toString())

            withContext(Dispatchers.Main) {

                binding.pending.statIcon.setImageDrawable(resources.getDrawable(R.drawable.baseline_access_time_filled_24))
                binding.pending.statTitle.text = "Pending"
                binding.pending.statCount.text = "${submittedTasks!!.size + openTasks!!.size} tasks"

                binding.ongoing.statIcon.setImageDrawable(resources.getDrawable(R.drawable.baseline_ongoing_24))
                binding.ongoing.statTitle.text = "Ongoing"
                binding.ongoing.statCount.text = "${ongoingTasks!!.size} tasks"

                binding.review.statIcon.setImageDrawable(resources.getDrawable(R.drawable.baseline_review_24))
                binding.review.statTitle.text = "Review"
                binding.review.statCount.text = "${reviewTasks!!.size} tasks"

                binding.completed.statIcon.setImageDrawable(resources.getDrawable(R.drawable.round_task_alt_24))
                binding.completed.statTitle.text = "Completed"
                binding.completed.statCount.text = "${completedTasks!!.size} tasks"
            }

        }


    }

    suspend fun getFilteredTasksBySegmentState(
        projectName: String,
        segmentState: Int
    ): List<Task> = withContext(Dispatchers.IO) {
        val tasks = db.tasksDao().getTasksInProjectforState(projectName, segmentState)
        val segments = PrefManager.getProjectSegments(projectName)
        tasks.filter { task ->
            val segmentName = task.segment
            val segment = segments.find { it.segment_NAME == segmentName }
            segment?.archived != true
        }
    }




    fun getNewMessages(channelName:String){

            repository.getNewTeamsMessages(PrefManager.getcurrentProject(), channelName) { result ->
                when (result) {
                    is ServerResult.Success -> {
                        if (result.data.isNotEmpty()) {
                            val messagedata =
                                result.data.toMutableList().sortedByDescending { it.timestamp }
                            Log.d("messageCount", messagedata.toString())
                            PrefManager.setChannelTimestamp(
                                PrefManager.getcurrentProject(),
                                channelName,
                                messagedata[0].timestamp!!
                            )
                            setRecyclerView(
                                PrefManager.getProjectChannels(PrefManager.getcurrentProject()).distinctBy { it.channel_name })
                        }

                    }

                    is ServerResult.Failure -> {
                        val errorMessage = result.exception.message
                    }

                    is ServerResult.Progress -> {

                    }
                }

        }
    }


    fun setRecyclerView(dataList: List<Channel>) {
        val recyclerView = binding.channelsRv
        val adapter = ChannelsAdapter(dataList.toMutableList(), this,msgDB)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.visible()
    }

    private fun manageviews() {
        val drawerLayout = activityBinding.binding.drawer
        activityBinding.binding.gioActionbar.btnHamTeams.setOnClickThrottleBounceListener {
            val gravity =
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) GravityCompat.START else GravityCompat.END
            drawerLayout.openDrawer(gravity)
        }

        activityBinding.binding.gioActionbar.tabLayout.gone()
        activityBinding.binding.gioActionbar.line.visible()
        activityBinding.binding.gioActionbar.refresh.visible()
        activityBinding.binding.gioActionbar.searchCont.visible()
        activityBinding.binding.gioActionbar.actionbar.visible()
        activityBinding.binding.gioActionbar.constraintLayout2.gone()
        activityBinding.binding.gioActionbar.constraintLayoutsearch.gone()
        activityBinding.binding.gioActionbar.constraintLayoutworkspace.gone()
        activityBinding.binding.gioActionbar.constraintLayoutTeams.visible()
        activityBinding.binding.gioActionbar.btnMoreTeams.visible()
        activityBinding.binding.gioActionbar.projectName.text = PrefManager.getcurrentProject()
        activityBinding.binding.gioActionbar.projectIcon.load(
            PrefManager.getProjectIconUrl(
                PrefManager.getcurrentProject()
            ), resources.getDrawable(R.drawable.placeholder_image)
        )
        if (PrefManager.getcurrentProject()=="None"){
            activityBinding.binding.gioActionbar.btnMoreTeams.gone()
            activityBinding.binding.gioActionbar.searchCont.gone()
        }
        else{
            activityBinding.binding.gioActionbar.btnMoreTeams.visible()
            activityBinding.binding.gioActionbar.searchCont.visible()
        }
    }


    override fun onChannelClick(channel: Channel) {
        val intent = Intent(requireContext(), TeamsActivity::class.java)
        intent.putExtra("channel_name", channel.channel_name)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }

    override fun onChannel(channel: Channel) {
        val oldList = PrefManager.getProjectChannels(PrefManager.getcurrentProject())
        val newList = (oldList.toMutableList() + channel).sortedByDescending { it.timestamp }
        PrefManager.saveProjectChannels(PrefManager.getcurrentProject(), newList)
        PrefManager.setLastChannelTimeStamp(PrefManager.getcurrentProject(), channel.timestamp!!)
        setRecyclerView(newList.distinctBy { it.channel_id })
    }


}

