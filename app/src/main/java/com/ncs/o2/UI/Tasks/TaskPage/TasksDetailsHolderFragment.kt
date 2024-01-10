package com.ncs.o2.UI.Tasks.TaskPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.ncs.o2.UI.UIComponents.Adapters.TaskDetailsViewPagerAdpater
import com.ncs.o2.databinding.FragmentTasksDetailsHolderBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksDetailsHolderFragment : Fragment() {


    lateinit var binding: FragmentTasksDetailsHolderBinding


    private val activityBinding: TaskDetailActivity by lazy {
        (requireActivity() as TaskDetailActivity)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksDetailsHolderBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewPager()

    }

    private fun setUpViewPager() {

        val adapter = TaskDetailsViewPagerAdpater(this, 3)
        binding.viewPager2.adapter = adapter
        setUpTabsLayout()
    }

    private fun setUpTabsLayout() {


        TabLayoutMediator(
            activityBinding.binding.tabLayout, binding.viewPager2
        ) { tab, position ->
            when(position){
                0-> tab.text="Details"
                1-> tab.text="Activity"
                2-> tab.text="Checklist"
            }
        }.attach()
    }

}