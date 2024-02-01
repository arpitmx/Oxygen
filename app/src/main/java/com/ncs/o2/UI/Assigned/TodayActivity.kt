package com.ncs.o2.UI.Assigned

import android.animation.AnimatorListenerAdapter
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.animation.Animator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieListener
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.TodayTasks
import com.ncs.o2.Domain.Models.UserNote
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.runDelayed
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.HelperClasses.ShakeDetector
import com.ncs.o2.R
import com.ncs.o2.UI.Report.ShakePrefrencesFragment
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.UIComponents.BottomSheets.AddUserNotesBottomSheet
import com.ncs.o2.databinding.ActivityTasksHolderBinding
import com.ncs.o2.databinding.ActivityTodayBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class TodayActivity : AppCompatActivity() {

    val binding: ActivityTodayBinding by lazy {
        ActivityTodayBinding.inflate(layoutInflater)
    }

    private val util: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this@TodayActivity)
    }
    var type: String? =null
    var index: String? =null
    val tasks:MutableList<Task> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnBack.setOnClickThrottleBounceListener{
            onBackPressed()
        }
        binding.btnDelete.setOnClickThrottleBounceListener {
            util.twoBtnDialog(title = "Clear All", msg = "Are you sure you want to clear your Today section?",
                positiveBtnText = "Clear", negativeBtnText = "Cancel", positive = {
                    PrefManager.saveProjectTodayTasks(PrefManager.getcurrentProject(), emptyList())
                    todayFragment()
                }, negative = {})
        }

        todayFragment()
    }
    private fun todayFragment(){
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = TodayFragment()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.slide_out_right)
    }



}