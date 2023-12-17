package com.ncs.o2.UI.Assigned

import TaskListAdapter
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotate180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.set180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.UIComponents.Adapters.TaskSectionViewPagerAdapter
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.FragmentAssignedBinding
import com.ncs.o2.databinding.FragmentTasksHolderBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AssignedFragment : Fragment() , TaskListAdapter.OnClickListener {

    companion object {
        fun newInstance() = AssignedFragment()
    }
    lateinit var binding: FragmentAssignedBinding
    private lateinit var viewModel: AssignedViewModel
    private lateinit var taskListAdapter: TaskListAdapter
    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())
    var taskList: MutableList<TaskItem> = mutableListOf()

    var assigned_rv:Boolean=false
    var working_rv=false
    var reviewing_rv=false
    var completed_rv=false
    private val activityBinding: ActivityMainBinding by lazy {
        (requireActivity() as MainActivity).binding
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAssignedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpViews()
        setUpViewPager()
        manageviews()

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AssignedViewModel::class.java)

    }


    override fun onCLick(position: Int, task: TaskItem) {
        val intent = Intent(requireContext(), TaskDetailActivity::class.java)
        intent.putExtra("task_id", task.id)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }

    private fun setUpViews(){

    }
    private fun manageviews(){
        val drawerLayout = activityBinding.drawer
        activityBinding.gioActionbar.btnHamWorkspace.setOnClickThrottleBounceListener {
            val gravity = if (!drawerLayout.isDrawerOpen(GravityCompat.START)) GravityCompat.START else GravityCompat.END
            drawerLayout.openDrawer(gravity)
        }
        activityBinding.gioActionbar.tabLayout.visible()
        activityBinding.gioActionbar.searchCont.visible()
        activityBinding.gioActionbar.actionbar.visible()
        activityBinding.gioActionbar.constraintLayout2.gone()
        activityBinding.gioActionbar.constraintLayoutsearch.gone()
        activityBinding.gioActionbar.constraintLayoutworkspace.visible()
    }
    private fun setUpViewPager() {

        PrefManager.initialize(requireContext())
        val adapter = WorkspaceViewPagerAdapter(this,4)
        binding.viewPager2.adapter = adapter
        setUpTabsLayout()
    }

    private fun setUpTabsLayout() {
        TabLayoutMediator(
            activityBinding.gioActionbar.tabLayout, binding.viewPager2
        ) { tab, position ->
            when(position){
                0-> tab.text="Assigned"
                1-> tab.text="Working On"
                2-> tab.text="Reviewing"
                3-> tab.text="Completed"
            }
        }.attach()
    }

//    private fun setupViews(){
//        activityBinding.gioActionbar.actionbar.gone()
//        activityBinding.gioActionbarWorkspace.actionbarWorkspace.visible()
//        val drawerLayout = activityBinding.drawer
//        activityBinding.gioActionbarWorkspace.btnHamWorkspace.setOnClickThrottleBounceListener {
//            val gravity = if (!drawerLayout.isDrawerOpen(GravityCompat.START)) GravityCompat.START else GravityCompat.END
//            drawerLayout.openDrawer(gravity)
//        }
//
//    }
//    private fun setAnimationOnRecyclerView(recyclerView: RecyclerView) {
//        val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fall_down)
//        recyclerView.layoutAnimation = controller
//        recyclerView.scheduleLayoutAnimation()
//        postponeEnterTransition()
//        view?.viewTreeObserver?.addOnPreDrawListener {
//            startPostponedEnterTransition()
//            true
//        }
//    }


//    private fun manageAssignedTasks(){
//        binding.assigned.setOnClickThrottleBounceListener {
//            binding.assignedArrow.set180(requireContext())
//            if (!assigned_rv) {
//                assigned_rv=true
//                taskList.clear()
//                val newTask= TaskItem(title = "Test Task",id="#T104657", assignee_id = "mohit@mail.com", difficulty = 3, duration = "1",timestamp = null,completed = false, assignee_DP_URL = "https://picsum.photos/200" )
//                taskList.add(newTask)
//                taskList.add(newTask)
//                taskList.add(newTask)
//                taskList.add(newTask)
//                taskList.add(newTask)
//                taskList.add(newTask)
//                if (taskList.isNotEmpty()) {
//                    binding.recyclerView.visible()
//                    val rv = binding.recyclerView
//                    taskListAdapter = TaskListAdapter(firestoreRepository)
//                    taskListAdapter.setTaskList(taskList)
//                    taskListAdapter.notifyDataSetChanged()
//                    taskListAdapter.setOnClickListener(this)
//                    val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//                    layoutManager.reverseLayout = false
//                    with(rv) {
//                        this.layoutManager = layoutManager
//                        adapter = AssignedAdapter(taskList)
//                    }
//                    setAnimationOnRecyclerView(binding.recyclerView)
//                }
//                if (taskList.isEmpty()){
//                    binding.assignedNull.visible()
//                }
//            }
//            else{
//                assigned_rv=false
//                binding.recyclerView.gone()
//                binding.assignedNull.gone()
//            }
//        }
//    }
//    private fun manageWorkingTasks(){
//        binding.workingOn.setOnClickThrottleBounceListener {
//            binding.workingOnArrow.set180(requireContext())
//            if (!working_rv) {
//                working_rv=true
//                taskList.clear()
//
//                if (taskList.isNotEmpty()) {
//                    binding.workingONRv.visible()
//                    val rv = binding.workingONRv
//                    taskListAdapter = TaskListAdapter(firestoreRepository)
//                    taskListAdapter.setTaskList(taskList)
//                    taskListAdapter.notifyDataSetChanged()
//                    taskListAdapter.setOnClickListener(this)
//                    val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//                    layoutManager.reverseLayout = false
//                    with(rv) {
//                        this.layoutManager = layoutManager
//                        adapter = AssignedAdapter(taskList)
//                    }
//                    setAnimationOnRecyclerView(binding.workingONRv)
//                }
//                if (taskList.isEmpty()){
//                    binding.workingOnNull.visible()
//                }
//            }
//            else{
//                working_rv=false
//                binding.workingONRv.gone()
//                binding.workingOnNull.gone()
//            }
//        }
//    }
//    private fun managereviewingTasks(){
//        binding.reviewing.setOnClickThrottleBounceListener {
//            binding.reviewingArrow.set180(requireContext())
//            if (!reviewing_rv) {
//                reviewing_rv=true
//                taskList.clear()
//                val newTask= TaskItem(title = "Test Task",id="#T104657", assignee_id = "mohit@mail.com", difficulty = 3, duration = "1",timestamp = null,completed = false, assignee_DP_URL = "https://picsum.photos/200" )
//                taskList.add(newTask)
//                taskList.add(newTask)
//                taskList.add(newTask)
//                taskList.add(newTask)
//                taskList.add(newTask)
//                taskList.add(newTask)
//                if (taskList.isNotEmpty()) {
//                    binding.reviewingRv.visible()
//                    val rv = binding.reviewingRv
//                    taskListAdapter = TaskListAdapter(firestoreRepository)
//                    taskListAdapter.setTaskList(taskList)
//                    taskListAdapter.notifyDataSetChanged()
//                    taskListAdapter.setOnClickListener(this)
//                    val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//                    layoutManager.reverseLayout = false
//                    with(rv) {
//                        this.layoutManager = layoutManager
//                        adapter = AssignedAdapter(taskList)
//                    }
//                    setAnimationOnRecyclerView(binding.reviewingRv)
//                }
//                if (taskList.isEmpty()){
//                    binding.reviewingNull.visible()
//                }
//            }
//            else{
//                reviewing_rv=false
//                binding.reviewingRv.gone()
//                binding.reviewingNull.gone()
//            }
//        }
//    }
//    private fun managecompletedTasks(){
//        binding.completed.setOnClickThrottleBounceListener {
//            binding.completedArrow.set180(requireContext())
//            if (!completed_rv) {
//                completed_rv=true
//                taskList.clear()
//                val newTask= TaskItem(title = "Test Task",id="#T104657", assignee_id = "mohit@mail.com", difficulty = 3, duration = "1",timestamp = null,completed = false, assignee_DP_URL = "https://picsum.photos/200" )
//                taskList.add(newTask)
//                taskList.add(newTask)
//
//                if (taskList.isNotEmpty()) {
//                    binding.completedRv.visible()
//                    val rv = binding.completedRv
//                    taskListAdapter = TaskListAdapter(firestoreRepository)
//                    taskListAdapter.setTaskList(taskList)
//                    taskListAdapter.notifyDataSetChanged()
//                    taskListAdapter.setOnClickListener(this)
//                    val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//                    layoutManager.reverseLayout = false
//                    with(rv) {
//                        this.layoutManager = layoutManager
//                        adapter = AssignedAdapter(taskList)
//                    }
//
//                    setAnimationOnRecyclerView(binding.completedRv)
//                }
//                if (taskList.isEmpty()){
//                    binding.completedNull.visible()
//                }
//            }
//            else{
//                completed_rv=false
//                binding.completedRv.gone()
//                binding.completedNull.gone()
//            }
//        }
//    }
}