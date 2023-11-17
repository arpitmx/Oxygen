package com.ncs.o2.UI.Tasks

import android.icu.lang.UCharacter.VerticalOrientation
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2.Orientation
import com.google.android.material.tabs.TabLayoutMediator
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.HelperClasses.Transformers
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.Sections.TaskSectionFragment
import com.ncs.o2.UI.UIComponents.Adapters.TaskSectionViewPagerAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.SegmentSelectionBottomSheet
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.FragmentTasksHolderBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private var sectionsList: MutableList<String> = mutableListOf()
    private val viewModel : TasksHolderViewModel by viewModels()


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
        }

        setUpViewPager(sectionsList)
        activityBinding.gioActionbar.constraintLayout2.visible()
        activityBinding.gioActionbar.constraintLayoutworkspace.gone()

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
        setUpTabsLayout(list)
    }

    private fun setUpTabsLayout(list: MutableList<String>) {
        TabLayoutMediator(
            activityBinding.gioActionbar.tabLayout, binding.viewPager2
        ) { tab, position ->
            if (position < list.size) {
                tab.text = list[position]
            }
        }.attach()
    }

    override fun sendSectionsList(list: MutableList<String>) {
        sectionsList.clear()
        sectionsList=list
    }


}