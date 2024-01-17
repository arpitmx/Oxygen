package com.ncs.o2.UI.Tasks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setBackgroundDrawable
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
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

        val viewPager2 = binding.viewPager2
        val adapter = TaskSectionViewPagerAdapter(this, list.size, list)
        viewPager2.adapter = adapter

        val tabLayout = activityBinding.gioActionbar.tabLayout
        tabLayout.removeAllTabs()

        val tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            val customTabView = LayoutInflater.from(tabLayout.context).inflate(R.layout.tab_item, null)
            val tabText: TextView = customTabView.findViewById(R.id.tabText)
            val iconCount: TextView = customTabView.findViewById(R.id.iconCount)
            val countParent: ConstraintLayout = customTabView.findViewById(R.id.count)

            updateTabColors(viewPager2.currentItem)

            viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateTabColors(position)
                }
            })
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
                                    countParent.gone()
                                    tabText.text = list[position]
                                } else {
                                    countParent.visible()
                                    tabText.text = list[position]
                                    iconCount.text=tasks.size.toString()
                                }
                            }

                            processedPositions.add(currentPosition)
                        }
                    }
                }
            }
            tab.customView = customTabView
        }

        tabLayoutMediator.attach()


    }

    fun updateTabColors(selectedPosition: Int) {
        val selectedColor = ContextCompat.getColor(activityBinding.gioActionbar.tabLayout.context, R.color.selected_tab_color)
        val unselectedColor = ContextCompat.getColor(activityBinding.gioActionbar.tabLayout.context, R.color.unselected_tab_color)

        for (i in 0 until activityBinding.gioActionbar.tabLayout.tabCount) {
            val tab = activityBinding.gioActionbar.tabLayout.getTabAt(i)
            val tabText = tab?.customView?.findViewById<TextView>(R.id.tabText)
            val tabIconBg = tab?.customView?.findViewById<LinearLayout>(R.id.tabIcon)

            if (tab != null && tabText != null && tabIconBg != null) {
                val color = if (i == selectedPosition) selectedColor else unselectedColor
                val drawable=if (i == selectedPosition) R.drawable.tab_selected__icon_bg else R.drawable.tab_unselected_icon_bg
                tabText.setTextColor(color)
                tabIconBg.setBackgroundDrawable(resources.getDrawable(drawable))
            }
        }
    }

    override fun sendSectionsList(list: MutableList<String>) {
        sectionsList.clear()
        sectionsList=list
    }




}