package com.ncs.o2.UI.UIComponents.Adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.Tasks.Sections.TaskSectionFragment

/*
File : TaskSectionAdapter.kt -> com.ncs.o2.UI.UIComponents.Adapters
Description : Adapter for ViewPager for Sections  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 12:09 am on 14/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/
class TaskSectionViewPagerAdapter(fragmentActivity: Fragment, private var totalCount: Int,private var sectionsList:MutableList<String>) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
            return totalCount
    }

    override fun createFragment(position: Int): Fragment {
        if (position < sectionsList.size) {
            return TaskSectionFragment.newInstance(sectionsList[position])
        }
        return TaskSectionFragment.newInstance("")
    }


}