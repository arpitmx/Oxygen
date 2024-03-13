package com.ncs.o2.UI.Tasks.TaskPage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.ncs.o2.Data.Room.MessageRepository.MessageDatabase
import com.ncs.o2.Domain.Models.CheckList
import com.ncs.o2.Domain.Models.Message
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.UIComponents.Adapters.TaskDetailsViewPagerAdpater
import com.ncs.o2.UI.UIComponents.Adapters.TaskSectionViewPagerAdapter
import com.ncs.o2.databinding.FragmentTasksDetailsHolderBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class TasksDetailsHolderFragment : Fragment() {


    lateinit var binding: FragmentTasksDetailsHolderBinding
    @Inject
    lateinit var db:MessageDatabase

    private val activityBinding: TaskDetailActivity by lazy {
        (requireActivity() as TaskDetailActivity)
    }
    @Inject
    lateinit var firestoreRepository: FirestoreRepository
    val checkList:MutableList<CheckList> = mutableListOf()

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


        val viewPager2 = binding.viewPager2
        val adapter = TaskDetailsViewPagerAdpater(this, 3)
        viewPager2.adapter = adapter

        val tabLayout = activityBinding.binding.tabLayout
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
            when(position){
                0-> {
                    countParent.visible()
                    tabText.text = "Details"
                }
                1-> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val messages=db.messagesDao().getMessagesForTask(PrefManager.getcurrentProject(),activityBinding.taskId!!)
                        withContext(Dispatchers.Main){
                            if (messages.isEmpty()){
                                countParent.gone()
                                tabText.text = "Activity"
                            }
                            else{
                                countParent.visible()
                                tabText.text = "Activity"
                                iconCount.text=messages.size.toString()
                            }
                        }
                    }

                }
                2-> {
                    CoroutineScope(Dispatchers.IO).launch {
                        getCheckList{
                            if (it.isEmpty()){
                                countParent.gone()
                                tabText.text = "Checklist"
                            }
                            else{
                                activityBinding.checkLists.clear()
                                activityBinding.checkLists.addAll(it)
                                countParent.visible()
                                tabText.text = "Checklist"
                                iconCount.text=it.size.toString()
                            }
                        }
                    }

                }
            }
            tab.customView = customTabView
        }

        tabLayoutMediator.attach()

    }

    private fun getCheckList(returnList: (List<CheckList>) -> Unit){


        CoroutineScope(Dispatchers.Main).launch {

            firestoreRepository.getCheckList(
                projectName = PrefManager.getcurrentProject(), taskId = activityBinding.taskId!!) { result ->

                when (result) {

                    is ServerResult.Failure -> {
                    }

                    ServerResult.Progress -> {
                    }

                    is ServerResult.Success -> {
                        returnList(result.data)
                    }

                }
            }
        }
    }
    fun updateTabColors(selectedPosition: Int) {
        val selectedColor = ContextCompat.getColor(activityBinding.binding.tabLayout.context, R.color.selected_tab_color)
        val unselectedColor = ContextCompat.getColor(activityBinding.binding.tabLayout.context, R.color.unselected_tab_color)

        for (i in 0 until activityBinding.binding.tabLayout.tabCount) {
            val tab = activityBinding.binding.tabLayout.getTabAt(i)
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

}