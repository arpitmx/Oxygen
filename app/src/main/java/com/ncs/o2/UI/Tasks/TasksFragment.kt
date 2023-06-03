package com.ncs.o2.UI.Tasks

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Models.Task
import com.ncs.o2.UI.Tasks.TaskDetails.TaskDetailActivity
import com.ncs.o2.UI.Tasks.TaskList.TaskListAdapter
import com.ncs.o2.databinding.FragmentTasksBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksFragment : Fragment(),TaskListAdapter.OnClickListener {


    private val viewModel: TasksViewModel by viewModels()
    private lateinit var binding: FragmentTasksBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskListAdapter : TaskListAdapter
    private lateinit var taskList: ArrayList<Task>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
    }

    private fun setupViews() {
        setupRecyclerView()
    }

    private fun setupRecyclerView() {



        val task1 = Task("Appbar not working in the new implementation",
        "Have to implement that.","#1234",2, listOf("link1,link2"),3,1, listOf("mod1"),
            "Assigner1","31/2/23", "3H+"
        )
        val task2 = Task("Window navigation not working in Versa 2.0",
            "Have to implement that.","#1364",1, listOf("link1,link2"),2,3, listOf("mod1"),
            "Assigner1","31/2/22", "4H+"
        )

        taskList = arrayListOf(task1,task2,task1,task2,task1,task2,task1,task2,task1)
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

    }

    override fun onCLick(position: Int, task: Task) {
        startActivity(Intent(requireContext(), TaskDetailActivity::class.java))
    }
}