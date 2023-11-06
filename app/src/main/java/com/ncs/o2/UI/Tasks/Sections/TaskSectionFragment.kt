package com.ncs.o2.UI.Tasks.Sections

import TaskListAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskDetails.TaskDetailActivity
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.FragmentTaskSectionBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class TaskSectionFragment(var sectionName: String) : Fragment(), TaskListAdapter.OnClickListener {


    @Inject
    lateinit var util: GlobalUtils.EasyElements

    private lateinit var viewModel: TaskSectionViewModel
    private lateinit var binding: FragmentTaskSectionBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskListAdapter: TaskListAdapter
    private lateinit var taskList: ArrayList<TaskItem>
    private lateinit var taskList2: ArrayList<Task>
    private lateinit var projectName: String
    private lateinit var segmentName: String


    val state = arrayOf(1)

    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskSectionBinding.inflate(inflater, container, false)
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
        segmentName = PrefManager.getcurrentsegment()
        setupViews()

    }

    private fun setupViews() {

        if (segmentName == "Select Segment") {
            binding.placeholderText.visible()
            binding.layout.gone()
            activityBinding.gioActionbar.tabLayout.gone()
            activityBinding.gioActionbar.searchCont.gone()
            activityBinding.gioActionbar.line.gone()
        } else {
            binding.placeholderText.gone()
            binding.layout.visible()
            activityBinding.gioActionbar.tabLayout.visible()
            activityBinding.gioActionbar.searchCont.visible()
            activityBinding.gioActionbar.line.visible()
            setupRecyclerView()
        }
    }

    private fun setupRecyclerView() {

        taskList = ArrayList<TaskItem>()

        viewModel.getTasksItemsForSegment(projectName, segmentName, sectionName) { result ->
            when (result) {
                is ServerResult.Success -> {

                    binding.lottieProgressInclude.progressbarBlock.gone()

                    val task = result.data
                    taskList.clear()

                    for (element in task) {
                        taskList.add(element)
                    }

                    if (taskList.size == 0) {
                        binding.layout.gone()
                        binding.placeholder.visible()

                    } else {
                        binding.layout.visible()
                        binding.placeholder.gone()

                        recyclerView = binding.recyclerView
                        taskListAdapter = TaskListAdapter()
                        taskListAdapter.setTaskList(taskList)
                        taskListAdapter.notifyDataSetChanged()

                        taskListAdapter.setOnClickListener(this)
                        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                        layoutManager.reverseLayout = false
                        with(recyclerView) {
                            this.layoutManager = layoutManager
                            adapter = taskListAdapter
                            edgeEffectFactory = BounceEdgeEffectFactory()
                        }

                        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                            override fun onScrollStateChanged(
                                recyclerView: RecyclerView,
                                newState: Int
                            ) {
                                super.onScrollStateChanged(recyclerView, newState)
                                state[0] = newState
                            }

                            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                super.onScrolled(recyclerView, dx, dy)
                                if (dy > 0 && (state[0] == 0 || state[0] == 2)) {
                                    hideSearch()
                                } else if (dy < -10) {
                                    showSearch()
                                }
                            }
                        })
                    }




                }

                is ServerResult.Failure -> {
                    val errorMessage = result.exception.message
                    binding.lottieProgressInclude.progressbarBlock.gone()
                    util.singleBtnDialog("Failure",
                        "Failure in loading tasks, try again","Reload"
                    ) {
                        setupRecyclerView()
                    }
                }

                is ServerResult.Progress -> {
                    binding.lottieProgressInclude.progressbarBlock.visible()
                }

            }

        }


        activityBinding.gioActionbar.filterBtn.setOnClickListener {
//            val newTask=Task("New Task Added",
//                "Have to implement that.","#0987",3, listOf("link1,link2"),2,3, listOf("mod1"),
//                "Assigner1","31/2/22",DURATION = "3", PROJECT_ID = "Versa123", SEGMENT = "SEG1", ASSIGNEE_DP_URL = "https://picsum.photos/300", isCompleted = false
//            )

//            taskList.add(newTask)
//            Toast.makeText(requireContext(),"New Task Added",Toast.LENGTH_SHORT).show()

//            taskList.remove(task2)
//            Toast.makeText(requireContext(),"task2 removed",Toast.LENGTH_SHORT).show()

//            val update=Task("New Task Updated for id #1364",
//                "Have to implement that.","#1364",3, listOf("link1,link2"),2,3, listOf("mod1"),
//                "Assigner1","31/2/22",
//                duration = "3", project_ID = "Versa123", segment = "SEG1", assignee_DP_URL = "https://picsum.photos/300", completed = false
//            )

//            val id=update.id
//            for(i in taskList.indices){
//                if(taskList[i].id==id){
//                    taskList[i]=update
//                }
//            }

//            Toast.makeText(requireContext(),"task updated",Toast.LENGTH_SHORT).show()
//            taskListAdapter.setTaskList(taskList)

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

    override fun onCLick(position: Int, task: TaskItem) {
        val intent = Intent(requireContext(), TaskDetailActivity::class.java)
        intent.putExtra("task_id", task.id)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }


}