package com.ncs.o2.UI.Tasks.Sections

import TaskListAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskDetails.TaskDetailActivity
import com.ncs.o2.UI.Tasks.TasksHolderFragment
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.FragmentTaskSectionBinding
import com.ncs.o2.databinding.FragmentTasksHolderBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.shouheng.utils.app.ActivityUtils.overridePendingTransition


@AndroidEntryPoint
class TaskSectionFragment(var sectionName: String) : Fragment(), TaskListAdapter.OnClickListener {



    private lateinit var viewModel: TaskSectionViewModel
    private lateinit var binding: FragmentTaskSectionBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskListAdapter : TaskListAdapter
    private lateinit var taskList: ArrayList<Task>
    private lateinit var taskList2: ArrayList<Task>
    private lateinit var projectName:String
    private lateinit var segmentName:String


    val state = arrayOf(1)

    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentTaskSectionBinding.inflate(inflater,container,false)
        return binding.root
        }
    private val activityBinding: ActivityMainBinding by lazy {
        (requireActivity() as MainActivity).binding
    }




    private val searchCont by lazy {
        activityBinding.gioActionbar.searchCont
    }
    private val segmentText by lazy {
        activityBinding.gioActionbar.titleTv
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(TaskSectionViewModel::class.java)
        PrefManager.initialize(requireContext())
        projectName = PrefManager.getcurrentProject()
        segmentName=PrefManager.getcurrentsegment()
        setupViews()

    }

    private fun setupViews() {

        if (segmentName=="Select Segment"){
            binding.placeholderText.visible()
            binding.layout.gone()
            activityBinding.gioActionbar.tabLayout.gone()
            activityBinding.gioActionbar.searchCont.gone()
            activityBinding.gioActionbar.line.gone()
        }
        else {
            binding.placeholderText.gone()
            binding.layout.visible()
            activityBinding.gioActionbar.tabLayout.visible()
            activityBinding.gioActionbar.searchCont.visible()
            activityBinding.gioActionbar.line.visible()
            setupRecyclerView()
        }
    }

    private fun setupRecyclerView() {
        taskList= ArrayList<Task>()
        val task1 = Task("Appbar not working in the new implementation Appbar not working in the new implementation",
            "Have to implement that.","#1234",2, listOf("link1,link2"),3,1, assignee = listOf("mod1"),
            "Assigner1","31/2/23", duration = "3", project_ID = "Versa123", segment = "SEG1", assignee_DP_URL = "https://picsum.photos/200", completed = true
        )

        val task2 = Task("Window navigation not working in Versa 2.0",
            "Have to implement that.","#1364",1, listOf("link1,link2"),2,3, listOf("mod1"),
            "Assigner1","31/2/22",
            duration = "3", project_ID = "Versa123", segment = "SEG1", assignee_DP_URL = "https://picsum.photos/300"
        )
        val task3 = Task("Window navigation not working in Versa 2.0",
            "Have to implement that.","#1364",3, listOf("link1,link2"),2,3, listOf("mod1"),
            "Assigner1","31/2/22",
            duration = "3", project_ID = "Versa123", segment = "SEG1", assignee_DP_URL = "https://picsum.photos/300"
        )
//        taskList = arrayListOf(task1,task2,task3,task2,task1,task2,task1,task2,task1)
//        taskList.add(task1)
        viewModel.getTasksForSegment(projectName, segmentName, sectionName) { result ->
            when (result) {
                is ServerResult.Success -> {
                    val task = result.data
                    taskList.clear()
                    for (i in 0 until task.size){
                        taskList.add(task[i])
                    }
                    if (taskList.isEmpty()) {
                        binding.layout.gone()
                        binding.placeholder.visible()
                    } else {
                        binding.layout.visible()
                        binding.placeholder.gone()
                    }
                    recyclerView = binding.recyclerView
                    taskListAdapter = TaskListAdapter()
                    taskListAdapter.setTaskList(taskList)
                    taskListAdapter.notifyDataSetChanged()

                    taskListAdapter.setOnClickListener(this)
                    val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    layoutManager.reverseLayout = false
                    with(recyclerView){
                        this.layoutManager = layoutManager
                        adapter = taskListAdapter
                        edgeEffectFactory = BounceEdgeEffectFactory()
                    }
                    recyclerView.addOnScrollListener(object :RecyclerView.OnScrollListener(){
                        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                            super.onScrollStateChanged(recyclerView, newState)
                            state[0]=newState
                        }

                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            if(dy>0&&(state[0]==0 || state[0]==2 )){
                                hideSearch()
                            }else if(dy<-10){
                                showSearch()
                            }
                        }
                    })

                    binding.lottieProgressInclude.progressLayout.gone()
                }
                is ServerResult.Failure -> {
                    val errorMessage = result.exception.message
                    binding.lottieProgressInclude.progressLayout.gone()
                }
                is ServerResult.Progress->{
                    binding.lottieProgressInclude.progressLayout.visible()
                }

            }

        }






//        Handler(Looper.getMainLooper()).postDelayed(Runnable {
//            val task1 = Task("Updated Previous task1",
//                "Have to implement that.","#1234",2, listOf("link1,link2"),3,1, ASSIGNEE = listOf("mod1"),
//                "Assigner1","31/2/23", DURATION = "3", PROJECT_ID = "Versa123", SEGMENT = "SEG1", ASSIGNEE_DP_URL = "https://picsum.photos/200", isCompleted = true
//            )
//
//            val task4 = Task("Versa 2.0",
//                "Have to implement that.","#4444",1, listOf("link1,link2"),2,3, listOf("mod1"),
//                "Assigner1","31/2/22",DURATION = "3", PROJECT_ID = "Versa123", SEGMENT = "SEG1", ASSIGNEE_DP_URL = "https://picsum.photos/300"
//            )
//            val task5 = Task("Task Title",
//                "Have to implement that.","#5555",3, listOf("link1,link2"),2,3, listOf("mod1"),
//                "Assigner1","31/2/22",DURATION = "3", PROJECT_ID = "Versa123", SEGMENT = "SEG1", ASSIGNEE_DP_URL = "https://picsum.photos/300"
//            )
//            taskList2= arrayListOf(task1,task5,task4,task2,task3,task5,task1,task1,task4,task4)
//            taskListAdapter.setTaskList(taskList2)
//        },4000)

        activityBinding.gioActionbar.filterBtn.setOnClickListener {
//            val newTask=Task("New Task Added",
//                "Have to implement that.","#0987",3, listOf("link1,link2"),2,3, listOf("mod1"),
//                "Assigner1","31/2/22",DURATION = "3", PROJECT_ID = "Versa123", SEGMENT = "SEG1", ASSIGNEE_DP_URL = "https://picsum.photos/300", isCompleted = false
//            )

//            taskList.add(newTask)
//            Toast.makeText(requireContext(),"New Task Added",Toast.LENGTH_SHORT).show()

//            taskList.remove(task2)
//            Toast.makeText(requireContext(),"task2 removed",Toast.LENGTH_SHORT).show()

            val update=Task("New Task Updated for id #1364",
                "Have to implement that.","#1364",3, listOf("link1,link2"),2,3, listOf("mod1"),
                "Assigner1","31/2/22",
                duration = "3", project_ID = "Versa123", segment = "SEG1", assignee_DP_URL = "https://picsum.photos/300", completed = false
            )

            val id=update.id
            for(i in taskList.indices){
                if(taskList[i].id==id){
                    taskList[i]=update
                }
            }

            Toast.makeText(requireContext(),"task updated",Toast.LENGTH_SHORT).show()
            taskListAdapter.setTaskList(taskList)

        }

//        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                if (dy > 100 && searchCont.visibility == View.VISIBLE) {
//                   Handler(Looper.getMainLooper()).postDelayed({
//                       searchCont.progressGoneSlide(requireContext(),200)
//                   },200)
//                } else if (dy < -20 && searchCont.visibility  != View.VISIBLE) {
//
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        searchCont.progressVisible(requireContext(),200)
//                    },200)
//                }
//            }
//
//        })


    }

    private fun showSearch() {
        searchCont.visible()
    }

    private fun hideSearch() {
        searchCont.gone()
    }

    override fun onCLick(position: Int, task: Task) {
        val intent = Intent(requireContext(), TaskDetailActivity::class.java)
        intent.putExtra("task_id", task.id)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }


}