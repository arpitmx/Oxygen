package com.ncs.o2.UI.Tasks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.Sections.TaskSectionFragment
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.UI.UIComponents.Adapters.TaskSectionViewPagerAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.SegmentSelectionBottomSheet
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.FragmentTasksHolderBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class TasksHolderFragment : Fragment(),SegmentSelectionBottomSheet.sendSectionsListListner {


    lateinit var binding: FragmentTasksHolderBinding
    private val activityBinding: ActivityMainBinding by lazy {
        (requireActivity() as MainActivity).binding
    }
    private lateinit var segmentName:String
    private val mainActivity: MainActivity by lazy {
        requireActivity() as MainActivity
    }
    @Inject
    lateinit var firestoreRepository: FirestoreRepository
    @Inject
    lateinit var db:TasksDatabase
    private var sectionsList: MutableList<String> = mutableListOf()
    private val viewModel: TaskSectionViewModel by viewModels()
    val processedPositions = mutableSetOf<Int>()
    val lock = Mutex()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksHolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        segmentName=PrefManager.getcurrentsegment()

        if (segmentName=="Select Segment"){
            activityBinding.gioActionbar.tabLayout.gone()
            activityBinding.gioActionbar.searchCont.gone()
        }
        else{
            activityBinding.gioActionbar.tabLayout.visible()
            activityBinding.gioActionbar.searchCont.visible()
        }
        val sectionsList = PrefManager.getsectionsList().toMutableList()

        PrefManager.list.observe(viewLifecycleOwner) { newList ->
            setUpViewPager(newList.toMutableList())
        }

        PrefManager.selectedPosition.observe(viewLifecycleOwner) { newPosition ->
            setUpViewPager(sectionsList)
        }
        mainActivity.segmentText.observe(viewLifecycleOwner) { newSegmentText ->
            segmentName = newSegmentText
            setUpViewPager(sectionsList)
            if (segmentName=="Select Segment"){
                activityBinding.gioActionbar.tabLayout.gone()
                activityBinding.gioActionbar.searchCont.gone()
            }
            else{
                activityBinding.gioActionbar.tabLayout.visible()
                activityBinding.gioActionbar.searchCont.visible()
            }
        }

        setUpViewPager(sectionsList)
        activityBinding.gioActionbar.constraintLayout2.visible()
        activityBinding.gioActionbar.constraintLayoutworkspace.gone()
        activityBinding.gioActionbar.actionbar.visible()
        activityBinding.gioActionbar.constraintLayoutsearch.gone()

        setUpBackPress()
    }

    private fun setUpBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.viewPager2.currentItem ==0){
                    requireActivity().finishAffinity()
                }else {
                    binding.viewPager2.currentItem = binding.viewPager2.currentItem -1
                }
            }
        })
    }

    private fun setUpViewPager(list:MutableList<String>) {

        val adapter = TaskSectionViewPagerAdapter(this, list.size,list)
        binding.viewPager2.adapter = adapter
        binding.viewPager2.offscreenPageLimit = 4
        setUpTabsLayout(list)
    }

    private fun setUpTabsLayout(list: MutableList<String>) {
        Log.d("sectionslist", list.toString())
        TabLayoutMediator(
            activityBinding.gioActionbar.tabLayout, binding.viewPager2
        ) { tab, position ->
            if (position < list.size && position !in processedPositions) {
                Log.d("sectionslist", "current position is $position")

                CoroutineScope(Dispatchers.IO).launch {
                    lock.withLock {
                        position.let { currentPosition ->
                            val tasks = db.tasksDao().getTasks(
                                PrefManager.getcurrentProject(),
                                segmentName,
                                list[currentPosition]
                            )

                            Log.d("sectionslist", "position while updating is $currentPosition")

                            withContext(Dispatchers.Main) {
                                if (tasks.isEmpty()) {
                                    tab.text = list[currentPosition]
                                } else {
                                    tab.text = "${list[currentPosition]} (${tasks.size})"
                                }
                            }

                            processedPositions.add(currentPosition)
                        }
                    }
                }
            }
        }.attach()
    }



    override fun sendSectionsList(list: MutableList<String>) {
        sectionsList.clear()
        sectionsList=list
    }




}