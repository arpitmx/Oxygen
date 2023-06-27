package com.ncs.o2.UI.Tasks.Sections

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Layout
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.progressGone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.progressGoneSlide
import com.ncs.o2.Domain.Utility.ExtensionsUtil.progressVisible
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskDetails.TaskDetailActivity
import com.ncs.o2.UI.Tasks.TasksHolderViewModel
import com.ncs.o2.UI.UIComponents.Adapters.TaskListAdapter
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.FragmentTaskSectionBinding
import com.ncs.o2.databinding.FragmentTasksHolderBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory


class TaskSectionFragment : Fragment(), TaskListAdapter.OnClickListener {



    private val viewModel: TaskSectionViewModel by viewModels()
    private lateinit var binding: FragmentTaskSectionBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskListAdapter : TaskListAdapter
    private lateinit var taskList: ArrayList<Task>


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        setupRecyclerView()
    }

    private fun setupRecyclerView() {


        val task1 = Task("Appbar not working in the new implementation Appbar not working in the new implementation",
            "Have to implement that.","#1234",2, listOf("link1,link2"),3,1, ASSIGNEE = listOf("mod1"),
            "Assigner1","31/2/23", DURATION = "3", PROJECT_ID = "Versa123", SEGMENT = "SEG1", ASSIGNEE_DP_URL = "https://picsum.photos/200", isCompleted = true
        )

        val task2 = Task("Window navigation not working in Versa 2.0",
            "Have to implement that.","#1364",1, listOf("link1,link2"),2,3, listOf("mod1"),
            "Assigner1","31/2/22",DURATION = "3", PROJECT_ID = "Versa123", SEGMENT = "SEG1", ASSIGNEE_DP_URL = "https://picsum.photos/300"
        )
        val task3 = Task("Window navigation not working in Versa 2.0",
            "Have to implement that.","#1364",3, listOf("link1,link2"),2,3, listOf("mod1"),
            "Assigner1","31/2/22",DURATION = "3", PROJECT_ID = "Versa123", SEGMENT = "SEG1", ASSIGNEE_DP_URL = "https://picsum.photos/300"
        )
        taskList = arrayListOf(task1,task2,task3,task2,task1,task2,task1,task2,task1)
        taskList.add(task1)
        taskList.add(task2)

        recyclerView = binding.recyclerView
        taskListAdapter = TaskListAdapter()
        taskListAdapter.setTaskList(taskList)
        taskListAdapter.setOnClickListener(this)

        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        with(recyclerView){
            this.layoutManager = layoutManager
            adapter = taskListAdapter
            edgeEffectFactory = BounceEdgeEffectFactory()
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

    override fun onCLick(position: Int, task: Task) {
        startActivity(Intent(requireContext(), TaskDetailActivity::class.java))
    }



}