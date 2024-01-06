package com.ncs.o2.UI.Assigned

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ncs.o2.UI.Tasks.Sections.TaskSectionFragment

class WorkspaceViewPagerAdapter(fragmentActivity: Fragment, private var totalCount: Int) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return totalCount
    }

    override fun createFragment(position: Int): Fragment {

        return when(position){

            0-> WorkspaceFragment.newInstance("Assigned")
            1-> WorkspaceFragment.newInstance("Working On")
            2-> WorkspaceFragment.newInstance("Reviewing")
            else -> WorkspaceFragment.newInstance("Completed")
        }
    }
}