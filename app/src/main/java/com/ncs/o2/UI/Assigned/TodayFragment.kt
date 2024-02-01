package com.ncs.o2.UI.Assigned

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.TodayTasks
import com.ncs.o2.Domain.Models.UserNote
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.UIComponents.BottomSheets.AddUserNotesBottomSheet
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.ActivityTodayBinding
import com.ncs.o2.databinding.FragmentAssignedBinding
import com.ncs.o2.databinding.FragmentTodayBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class TodayFragment : Fragment(),TodayTasksAdpater.OnClickListener,TodayTasksAdpater.SwipeListener,AddUserNotesBottomSheet.OnNoteCreated {

    lateinit var binding: FragmentTodayBinding
    private val viewModel: TaskSectionViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    var taskItems: MutableList<TaskItem> = mutableListOf()
    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())
    val state = arrayOf(1)
    @Inject
    lateinit var db: TasksDatabase
    lateinit var taskadapter:TodayTasksAdpater
    var todays:MutableList<TodayTasks> = mutableListOf()
    private val activityBinding: ActivityTodayBinding by lazy {
        (requireActivity() as TodayActivity).binding
    }
    @Inject
    lateinit var util: GlobalUtils.EasyElements
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swiperefresh.setOnRefreshListener {
            syncCache(PrefManager.getcurrentProject())
        }
        setTasks()

        activityBinding.btnAddNotes.setOnClickThrottleBounceListener {
            val addUserNotesBottomSheet= AddUserNotesBottomSheet(this, UserNote(id = "", desc = ""))
            addUserNotesBottomSheet.show(requireFragmentManager(),"Add Notes")
        }
    }

    private fun setTasks(){
        if (PrefManager.getProjectTodayTasks(PrefManager.getcurrentProject()).isEmpty()) {
            binding.layout.gone()
            binding.recyclerView.gone()
            binding.progressbarBlock.gone()
            binding.placeholder.visible()
        } else {
            todays.clear()
            todays=PrefManager.getProjectTodayTasks(PrefManager.getcurrentProject()).toMutableList()
            setUpOnSuccessRV(todays)
        }
    }

    private fun setUpOnSuccessRV(list: MutableList<TodayTasks>){
        Log.d("Todays",list.toString())
        if (list.isEmpty()){
            binding.layout.gone()
            binding.recyclerView.gone()
            binding.progressbarBlock.gone()
            binding.placeholder.visible()
        }
        else{
            binding.layout.visible()
            binding.recyclerView.visible()
            binding.progressbarBlock.gone()
            binding.placeholder.gone()
            recyclerView = binding.recyclerView
            taskadapter = TodayTasksAdpater(
                firestoreRepository,
                requireContext(),
                mutableListOf(),
                db,
                this,
                recyclerView,
                viewModel,
                this,
            )
            taskadapter.setTasks(list.sortedBy { it.isCompleted }.distinctBy { it.taskID}.toMutableList())
            val layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            layoutManager.reverseLayout = false
            with(recyclerView) {
                this.layoutManager = layoutManager
                adapter = taskadapter
                edgeEffectFactory = BounceEdgeEffectFactory()
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
                    } else if (dy < -10) {
                    }
                }
            })

        }
    }

    private fun syncCache(projectName:String){
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Please wait, Syncing Tasks")
        progressDialog.setCancelable(false)
        progressDialog.show()
        CoroutineScope(Dispatchers.Main).launch {
            try {

                val taskResult = withContext(Dispatchers.IO) {
                    firestoreRepository.getTasksinProjectAccordingtoTimeStamp(projectName)
                }


                when (taskResult) {

                    is ServerResult.Failure -> {
                        progressDialog.dismiss()
                        binding.swiperefresh.isRefreshing=false

                    }

                    is ServerResult.Progress -> {
                        progressDialog.show()
                        progressDialog.setMessage("Please wait, Syncing Tasks")

                    }

                    is ServerResult.Success -> {

                        val tasks = taskResult.data
                        if (tasks.isNotEmpty()){
                            val newList=taskResult.data.toMutableList().sortedByDescending { it.last_updated }
                            PrefManager.setLastTaskTimeStamp(projectName,newList[0].last_updated!!)
                            for (task in tasks) {
                                db.tasksDao().insert(task)
                            }
                        }
                        progressDialog.dismiss()
                        requireActivity().recreate()
                        binding.swiperefresh.isRefreshing=false

                    }

                }

            } catch (e: java.lang.Exception) {
                Timber.tag(TaskDetailsFragment.TAG).e(e)
                progressDialog.dismiss()


            }

        }
    }
    override fun onCLick(task: TodayTasks) {
        if (task.taskID[0]!='#'){
            val userNotes=PrefManager.getProjectUserNotes(PrefManager.getcurrentProject())
            var note:UserNote?=null

            for (n in userNotes){
                if (n.id==task.taskID){
                    note=n
                }
            }
            val addUserNotesBottomSheet=AddUserNotesBottomSheet(this,note!!)
            addUserNotesBottomSheet.show(requireFragmentManager(),"Add Notes")
        }
        else{
            val intent =Intent(requireContext(), TaskDetailActivity::class.java)
            intent.putExtra("task_id", task.taskID)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        }
    }

    fun startAnimation() {
        activityBinding.anim.visible()
        if (activityBinding.anim.isAnimating) {
            activityBinding.anim.cancelAnimation()
        }
        activityBinding.anim.playAnimation()
        ExtensionsUtil.runDelayed(4800) {
            activityBinding.anim.gone()
        }

    }



    override fun onleftSwipe(task: TodayTasks, position: Int) {
        requireContext().performHapticFeedback()
        if (!task.isCompleted){
            val _todays = PrefManager.getProjectTodayTasks(PrefManager.getcurrentProject()).toMutableList()

            for (_today in _todays) {
                if (_today.taskID == task.taskID) {
                    _today.isCompleted = true
                    break
                }
            }

            for (today in todays) {
                if (today.taskID == task.taskID) {
                    today.isCompleted = true
                    break
                }
            }


//            val toUpdate = todays[position]
//            toUpdate.isCompleted = true
//            todays[position]=toUpdate

            startAnimation()

            PrefManager.saveProjectTodayTasks(PrefManager.getcurrentProject(),_todays)
            taskadapter.notifyDataSetChanged()
            taskadapter.setTasks(_todays.sortedBy { it.isCompleted }.distinctBy { it.taskID }.toMutableList())
            util.showSnackbar(binding.root,"Marked as Completed for Today",2000)
        }
        else{
            val _todays = PrefManager.getProjectTodayTasks(PrefManager.getcurrentProject()).toMutableList()

            for (today in _todays) {
                if (today.taskID == task.taskID) {
                    today.isCompleted = false
                    break
                }
            }

            for (today in todays) {
                if (today.taskID == task.taskID) {
                    today.isCompleted = false
                    break
                }
            }

//            val toUpdate = todays[position]
//            toUpdate.isCompleted = false
//            todays[position]=toUpdate
            PrefManager.saveProjectTodayTasks(PrefManager.getcurrentProject(),_todays)
            taskadapter.notifyDataSetChanged()
            taskadapter.setTasks(_todays.sortedBy { it.isCompleted }.distinctBy { it.taskID }.toMutableList())
//            setTasks()
            util.showSnackbar(binding.root,"Marked as not Completed ",2000)
        }


    }


    override fun onrightSwipe(task: TodayTasks,position: Int) {
        requireContext().performHapticFeedback()
        val _todays=PrefManager.getProjectTodayTasks(PrefManager.getcurrentProject()).toMutableList()

        val iterator = _todays.iterator()
        while (iterator.hasNext()) {
            val today = iterator.next()
            if (task.taskID == today.taskID) {
                iterator.remove()
                break
            }
        }
        PrefManager.saveProjectTodayTasks(PrefManager.getcurrentProject(),_todays)

        for (i in 0 until todays.size) {
            if (todays[i].taskID==task.taskID){
                todays.removeAt(i)
                break
            }
        }

//        todays.removeAt(position)
//        taskadapter.notifyItemRemoved(position)
        taskadapter.setTasks(_todays.sortedBy { it.isCompleted }.distinctBy { it.taskID }.toMutableList())
        if (_todays.isEmpty()){
            setTasks()
        }
        util.showSnackbar(binding.root,"Removed from Today",2000)

    }

    override fun noteCreated(userNote: UserNote) {
        val _todays=PrefManager.getProjectTodayTasks(PrefManager.getcurrentProject()).toMutableList()
        requireActivity().recreate()

    }

}
