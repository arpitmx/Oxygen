package com.ncs.o2.UI.Tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayoutMediator
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.Sections.TaskSectionFragment
import com.ncs.o2.UI.UIComponents.Adapters.TaskSectionViewPagerAdapter
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.FragmentTasksHolderBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksHolderFragment : Fragment() {


    private lateinit var binding: FragmentTasksHolderBinding
    private val activityBinding: ActivityMainBinding by lazy {
        (requireActivity() as MainActivity).binding
    }
    private lateinit var segmentName:String
    private val mainActivity: MainActivity by lazy {
        requireActivity() as MainActivity
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksHolderBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PrefManager.initialize(requireContext())
        segmentName=PrefManager.getcurrentsegment()
        PrefManager.selectedPosition.observe(viewLifecycleOwner, Observer { newPosition ->
            setUpViewPager()
        })
        mainActivity.segmentText.observe(viewLifecycleOwner, Observer { newSegmentText ->
            segmentName = newSegmentText
            setUpViewPager()
        })
        setUpViewPager()

    }

    private fun setUpViewPager() {
        val adapter = TaskSectionViewPagerAdapter(this, 4)
        binding.viewPager2.adapter = adapter
        setUpTabsLayout()

    }

    private fun setUpTabsLayout() {

        TabLayoutMediator(
            activityBinding.gioActionbar.tabLayout, binding.viewPager2
        ) { tab, position ->
            when (position){
                0-> tab.text = "Ongoing Progress"
                1-> tab.text = "Ready for Test"
                2-> tab.text = "Testing"
                3-> tab.text = "Completed"
            }

        }.attach()

    }



}