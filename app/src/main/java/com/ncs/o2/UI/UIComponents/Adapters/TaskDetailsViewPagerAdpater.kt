package com.ncs.o2.UI.UIComponents.Adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ncs.o2.UI.Tasks.TaskPage.Chat.TaskChatFragment
import com.ncs.o2.UI.Tasks.TaskPage.Checklist.TaskCheckListFragment
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment

class TaskDetailsViewPagerAdpater(fragmentActivity: Fragment, private var totalCount: Int) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return totalCount
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0-> TaskDetailsFragment()
            1-> TaskChatFragment()
            2-> TaskCheckListFragment()
            else -> TaskDetailsFragment()
        }
    }
}