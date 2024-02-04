package com.ncs.o2.HelperClasses.CrashReporting

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.ncs.o2.Domain.Models.CrashReport
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.Domain.Utility.RandomIDGenerator.generateTaskID
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.Notifications.NotificationsActivity
import com.ncs.versa.Constants.Endpoints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Calendar
import kotlin.system.exitProcess


class CrashMan(val context: Context) : Thread.UncaughtExceptionHandler {

    private val newLine = "\n"
    private val errorMessage = StringBuilder()
    private val softwareInfo = StringBuilder()
    private val activityInfo = StringBuilder()
    private val userInfo = StringBuilder()
    private val dateInfo = StringBuilder()
    var myContext: Context = context

    private val TAG = "CRASHMAN"

    override fun uncaughtException(t: Thread, exception: Throwable) {


        val stackTrace = StringWriter()
        exception.printStackTrace(PrintWriter(stackTrace))

        errorMessage.append(stackTrace.toString())

        softwareInfo.append(getVersionName(context))

        userInfo.append("Email: ${PrefManager.getCurrentUserEmail()}")

        activityInfo.append("Project-ID: ${PrefManager.getcurrentProject()}")
        activityInfo.append("Segment-ID: ${PrefManager.getcurrentsegment()}")

        dateInfo.append(Calendar.getInstance().time)


        val report : CrashReport = CrashReport(
            stackTrace = errorMessage.toString(),
            softwareVersion= softwareInfo.toString(),
            userInfo = userInfo.toString(),
            SDKVersion = Build.VERSION.SDK_INT.toString(),
            activity_ProjectID = PrefManager.getcurrentProject(),
            activity_SegmentID = PrefManager.getcurrentsegment(),
            crashTimeStamp = dateInfo.toString()
        )

        Timber.d("Error : $errorMessage")
        Timber.d("Software : $softwareInfo")
        Timber.d("Date : $dateInfo")

        val summary = composeSummary(report)

        CoroutineScope(Dispatchers.IO).launch {
            pushIssue(summary, errorMessage.toString())
        }

    }

    private fun showDialogue() {
        Handler(Looper.getMainLooper()).post {
            val globalUtil : GlobalUtils.EasyElements = GlobalUtils.EasyElements(context)
            globalUtil.singleBtnDialog("Unknown issue","CrashMan sensed an issue in o2, we have sent it to the developers for inspection.", "Okay") {

            }
        }


    }


    fun composeSummary(report: CrashReport): String {



        val crashReport = """
    ```python
    Crash Report :
    
    Time :
    - ${report.crashTimeStamp}
    
    User Info :
    - ${report.userInfo}
    
    Activity Specifics : 
    - Project-ID: ${report.activity_ProjectID}
    - Segment-ID: ${report.activity_SegmentID}
    
    App info :
     ${report.softwareVersion}
    
    Stacktrace : 
     ${report.stackTrace}
    ```
    
    """.trimIndent()


        return crashReport
    }


    fun getVersionName(context: Context): String {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            // Handle the exception or return a default value
            return "Unknown"
        }
    }

    suspend fun pushIssue(summary: String, error: String) {

        val repository = FirestoreRepository(FirebaseFirestore.getInstance())

        var title: String = "Crash : " + extractFirstNWords(error, 3)
        var description: String = summary

        var difficulty: Int = 1
        var priority: Int = 1
        var status: Int = 1

        var assignee: String = "None"
        val assigner: String = "help.oxygen.ncs@gmail.com"
        var moderators: List<String> = listOf()

        var time_STAMP: Timestamp? = Timestamp.now()
        var duration: String = "2 Hours"
        var tags: List<String> = listOf()
        var project_ID: String = "Ncso2v1"
        var segment: String = "Crashman"
        var section: String = "Issues Found"
        var type: Int = 1
        val last_updated: Timestamp = Timestamp.now()
        generateUniqueTaskID(PrefManager.getcurrentProject()) { id ->
            val task = Task(
                id = id,
                title = title,
                description = description,
                difficulty = difficulty,
                priority = priority,
                status = status,
                assignee = assignee,
                assigner = assigner,
                moderators = moderators,
                time_STAMP = time_STAMP,
                duration = duration,
                tags = tags,
                project_ID = project_ID,
                segment = segment,
                last_updated = last_updated,
                type = type,
                section = section
            )

            CoroutineScope(Dispatchers.Main).launch {
                repository.postTask(task, mutableListOf()) { serverResult ->
                    when (serverResult) {
                        is ServerResult.Success -> {

                            Toast.makeText(context, "Crash log sent..", Toast.LENGTH_SHORT).show()
                            Timber.tag(TAG).d("Error Reported.")
                            terminateApp()
                        }

                        is ServerResult.Progress -> {

                        }

                        is ServerResult.Failure -> {
                            Toast.makeText(context, "Failed to send logs..", Toast.LENGTH_SHORT).show()
                            Timber.tag(TAG).d("Failure in reporting error.")

                            Timber.tag(TAG).d("Error : $errorMessage")

                            terminateApp()
                        }
                    }
                }
            }
        }

    }

    fun generateUniqueTaskID(currentProject: String,result : (String) -> Unit) {
        var generatedID: String
        do {
            generatedID = generateTaskID(currentProject)
            val taskExists = checkIfTaskExists(generatedID)

        } while (taskExists)

        result(generatedID)
    }

    fun checkIfTaskExists(taskID: String): Boolean {
        val firestore = FirebaseFirestore.getInstance()
        val tasksCollection: CollectionReference = firestore
            .collection(Endpoints.PROJECTS)
            .document(PrefManager.getcurrentProject())
            .collection(Endpoints.Project.TASKS)
        val query = tasksCollection.whereEqualTo("id", taskID)
        return try {
            val querySnapshot: QuerySnapshot = query.get().result
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }



    fun terminateApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        exitProcess(2);
    }

    fun extractFirstNWords(input: String?, n: Int): String {
        if (input.isNullOrEmpty()) {
            return ""
        }
        val words = input.split("\\s+".toRegex())
        return words.take(n).joinToString(" ")
    }
}