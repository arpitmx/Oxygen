package com.ncs.o2.UI.CreateTask

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.contentColorFor
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.R
import com.ncs.o2.UI.UIComponents.Adapters.ContributorAdapter
import com.ncs.o2.UI.UIComponents.Adapters.UserListAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.UserlistBottomSheet
import com.ncs.o2.databinding.ActivityCreateTaskBinding
import dagger.hilt.android.AndroidEntryPoint
import net.datafaker.Faker
import net.datafaker.providers.base.Bool
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class CreateTaskActivity : AppCompatActivity(), ContributorAdapter.OnProfileClickCallback, UserlistBottomSheet.getContributorsListCallback {

    private val binding: ActivityCreateTaskBinding by lazy {
        ActivityCreateTaskBinding.inflate(layoutInflater)
    }

    private val viewmodel: CreateTaskViewModel by viewModels()
    private val easyElements : GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this)
    }

    private val contriRecyclerView : RecyclerView by lazy {
        binding.contributorsRecyclerView
    }

    lateinit var contriAdapter : ContributorAdapter
    private var localContributorList : MutableList<User> = mutableListOf()
    lateinit var dataList : MutableList<User>

    @Inject lateinit var calendar : Calendar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val testTask = Task(
            Faker().animal().scientificName().toString(),
            Faker().code().asin(), ID= "", 1, emptyList(), 1, 1, emptyList(),
            "userid1", "01/04/2023", DURATION = "3Hr+", PROJECT_ID =  "Versa", SEGMENT = "Development", SECTION = "TaskSection4",
            )

       // Activity -> Viewmodel -> PostUsecase + GetUsecase -> Repository(DB)-> Firestore db

        binding.duration.setOnClickThrottleBounceListener {
            viewmodel.createTask(testTask)
        }


        dataList = mutableListOf(
            User(
                "https://yt3.googleusercontent.com/xIPexCvioEFPIq_nuEOOsv129614S3K-AblTK2P1L9GvVIZ6wmhz7VyCT-aENMZfCzXU-qUpaA=s900-c-k-c0x00ffffff-no-rj",
                "armax",
                "android",
                "url1"
            ),
            User(
                "https://hips.hearstapps.com/hmg-prod/images/apple-ceo-steve-jobs-speaks-during-an-apple-special-event-news-photo-1683661736.jpg?crop=0.800xw:0.563xh;0.0657xw,0.0147xh&resize=1200:*",
                "abhishek", "android", "url2",
            ),
            User("https://picsum.photos/200", "vivek", "design", "url3"),
            User("https://picsum.photos/300", "lalit", "web", "url4"),
            User("https://picsum.photos/350", "yogita", "design", "url5"),
            User("https://picsum.photos/450", "aditi", "design", "url6"),
        )


        binding.addContributorsBtn.setOnClickThrottleBounceListener {
            val userListBottomSheet = UserlistBottomSheet(this, dataList)
            userListBottomSheet.show(supportFragmentManager, "userlist")
        }

        setUpViews()
        setUpLiveData()
    }

    private fun setUpLiveData() {
        //Progress listener
        viewmodel.progressLiveData.observe(this){ visibility ->
            if (visibility){
                binding.progressCircularInclude.root.visible()
            }else {
                binding.progressCircularInclude.root.gone()
            }
        }

        viewmodel.successLiveData.observe(this){
            if (it){
                toast("Task Published...")
                finish()
            }
        }

    }

    private fun setUpViews() {
        setUpActionBar()
        setUpDatePicker()
        setUpClickListeners()
        setUpCallbacks()
        setupSelectedMembersRecyclerView()
    }

    private fun setupSelectedMembersRecyclerView() {

        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP

        contriRecyclerView.layoutManager = layoutManager
        contriAdapter = ContributorAdapter(localContributorList,this)
        contriRecyclerView.adapter = contriAdapter
        contriRecyclerView.visible()

    }

    private fun setUpCallbacks() {
        viewmodel.serverExceptionLiveData.observe(this){exceptionMsg->
            easyElements.dialog("Server error",exceptionMsg,{},{})
        }
    }

    private fun setUpClickListeners() {
        binding.startDate.setOnClickThrottleBounceListener {
            startdatePickerDialog.show()
        }

        binding.endDate.setOnClickThrottleBounceListener{
            enddatePickerDialog.show()
        }
    }


    lateinit var startdatePickerDialog : DatePickerDialog
    lateinit var enddatePickerDialog : DatePickerDialog
    private fun setUpDatePicker() {

        startdatePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                // Update the calendar with the selected date
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Update the text on the button with the selected date
                val selectedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
                binding.startDate.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        enddatePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                // Update the calendar with the selected date
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Update the text on the button with the selected date
                val selectedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
                binding.endDate.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        startdatePickerDialog.datePicker.minDate = calendar.timeInMillis
        enddatePickerDialog.datePicker.minDate = calendar.timeInMillis

    }

    private fun setUpActionBar() {

        binding.gioActionbar.titleTv.visible()
        binding.gioActionbar.titleTv.text = getString(R.string.new_task)

        binding.gioActionbar.btnBack.setOnClickThrottleBounceListener {
            onBackPressedDispatcher.onBackPressed()
            finish()
        }
    }

    override fun onProfileClick(user: User, position: Int) {
    }

    override fun onGetContributorsList(selectedContributorsList: MutableList<User>) {
        this.localContributorList = selectedContributorsList
        contriAdapter.updateList(selectedContributorsList)
    }


}