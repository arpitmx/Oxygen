package com.ncs.o2.UI.UIComponents.Adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ncs.o2.UI.Tasks.TaskDetails.Chat.TaskChatFragment
import com.ncs.o2.UI.Tasks.TaskDetails.Details.TaskDetailsFragment

class TaskDetailsViewPagerAdpater(fragmentActivity: Fragment, private var totalCount: Int) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return totalCount
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0-> TaskDetailsFragment()
            else-> TaskChatFragment()
        }
    }
}