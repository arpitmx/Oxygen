package com.ncs.o2.UI.Tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.UIComponents.Adapters.TaskSectionViewPagerAdapter
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.FragmentTasksHolderBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksHolderFragment : Fragment() {


    private val viewModel: TasksHolderViewModel by viewModels()
    private lateinit var binding: FragmentTasksHolderBinding
    private val activityBinding: ActivityMainBinding by lazy {
        (requireActivity() as MainActivity).binding
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

        setUpViewPager()
        setUpTabsLayout()

    }

    private fun setUpViewPager() {
        val adapter = TaskSectionViewPagerAdapter(this, 4)
        binding.viewPager2.adapter = adapter
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