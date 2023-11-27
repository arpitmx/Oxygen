package com.ncs.o2.UI.Assigned

import TaskListAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.WorkspaceTaskItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.FragmentWorkspaceBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class WorkspaceFragment(var sectionName: String) : Fragment(), TaskListAdapter.OnClickListener {


    @Inject
    lateinit var util: GlobalUtils.EasyElements

    private lateinit var viewModel: AssignedViewModel
    private lateinit var binding: FragmentWorkspaceBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskListAdapter: TaskListAdapter
    private lateinit var taskList: ArrayList<TaskItem>
    private var taskIdsList:MutableList<WorkspaceTaskItem> = mutableListOf()
    private lateinit var projectName: String
    val state = arrayOf(1)
    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWorkspaceBinding.inflate(inflater, container, false)
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
        viewModel = ViewModelProvider(this).get(AssignedViewModel::class.java)
        PrefManager.initialize(requireContext())
        projectName = PrefManager.getcurrentProject()
        getTaskIdsList()
    }



    private fun getTaskIdsList(){
        viewModel.getUserTasksId(sectionName) { result ->
            when (result) {
                is ServerResult.Success -> {
                    binding.lottieProgressInclude.progressbarBlock.gone()
                    val task = result.data
                    if (task!!.isEmpty()){
                        binding.layout.gone()
                        binding.placeholder.visible()
                    }
                    else {
                        taskIdsList = task!!.toMutableList()
                        getTasks()
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
    }

    private fun getTasks() {
        taskList = ArrayList()
        binding.recyclerView.gone()
        CoroutineScope(Dispatchers.IO).launch {
            for (i in 0 until taskIdsList.size) {
                viewModel.getTasksItembyId(
                    id = taskIdsList[i].ID,
                    projectName = projectName
                ) { result ->
                    when (result) {
                        is ServerResult.Success -> {
                            binding.lottieProgressInclude.progressbarBlock.gone()

                            taskList.add(result.data)

                            if (taskList.isEmpty()) {
                                binding.layout.gone()
                                binding.placeholder.visible()

                            } else {
                                binding.layout.visible()
                                binding.lottieProgressInclude.progressbarBlock.gone()
                                binding.placeholder.gone()
                                recyclerView = binding.recyclerView
                                taskListAdapter = TaskListAdapter(firestoreRepository,requireContext())
                                taskListAdapter.setTaskList(taskList)
                                taskListAdapter.notifyDataSetChanged()
                                taskListAdapter.setOnClickListener(this@WorkspaceFragment)
                                val layoutManager =
                                    LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                                layoutManager.reverseLayout = false
                                with(recyclerView) {
                                    this.layoutManager = layoutManager
                                    adapter = taskListAdapter
                                    edgeEffectFactory = BounceEdgeEffectFactory()
                                    visibility = View.VISIBLE
                                }

                                recyclerView.addOnScrollListener(object :
                                    RecyclerView.OnScrollListener() {
                                    override fun onScrollStateChanged(
                                        recyclerView: RecyclerView,
                                        newState: Int
                                    ) {
                                        super.onScrollStateChanged(recyclerView, newState)
                                        state[0] = newState
                                    }

                                    override fun onScrolled(
                                        recyclerView: RecyclerView,
                                        dx: Int,
                                        dy: Int
                                    ) {
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
                            util.singleBtnDialog(
                                "Failure",
                                "Failure in loading tasks, try again", "Reload"
                            ) {
                                setupRecyclerView()
                            }

                        }

                        is ServerResult.Progress -> {
                            binding.lottieProgressInclude.progressbarBlock.visible()
                        }

                    }

                }
            }
        }
    }
    private fun setupRecyclerView(){

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