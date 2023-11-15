package com.ncs.o2.UI.StartScreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.Room.NotificationRepository.NotificationDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StartScreenViewModel @Inject
constructor(
    @FirebaseRepository val repository: Repository,
    val notificationDatabase: NotificationDatabase, application: Application
) : AndroidViewModel(application) {


    companion object {
        val TAG = "StartScreenViewModel"
    }

    private val _serverResultLiveData = MutableLiveData<ServerResult<List<Notification>>>()
    val serverResultLiveData: LiveData<ServerResult<List<Notification>>> get() = _serverResultLiveData

    fun logCatOutput() = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        Runtime.getRuntime().exec("logcat -c")
        Runtime.getRuntime().exec("logcat")
            .inputStream
            .bufferedReader()
            .useLines { lines ->
                lines.forEach { line -> emit(line) }
            }
    }


    fun pushNewNotificationsToRoom(
        notifications: List<Notification>,
        serverResult: (ServerResult<Int>) -> Unit
    ) {

        val dao = notificationDatabase.notificationDao()
        serverResult(ServerResult.Progress)

        if (notifications.isEmpty()) {
            serverResult(ServerResult.Success(notifications.size))
            return
        }


        CoroutineScope(Dispatchers.IO).launch {
            try {
                notificationDatabase.runInTransaction {

                    for (notification in notifications) {
                        launch {
                            dao.insert(notification)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    serverResult(ServerResult.Success(notifications.size))
                }

            } catch (e: Exception) {
                Timber.e(e, "Error while inserting notifications")

                withContext(Dispatchers.Main) {
                    serverResult(ServerResult.Failure(e))
                }

            }
        }

    }


    fun setUpNewNotifications() {

        val latestNotificationTimeStamp = PrefManager.getLatestNotificationTimeStamp()

        CoroutineScope(Dispatchers.Main).launch {

            repository.getNewNotifications(latestNotificationTimeStamp) { notificationListResult ->

                when (notificationListResult) {
                    is ServerResult.Failure -> {
                        Timber.tag(TAG)
                            .d("Notification list fetching Failure ${notificationListResult.exception.printStackTrace()}")
                        _serverResultLiveData.postValue(
                            ServerResult.Failure(
                                notificationListResult.exception
                            )
                        )
                    }

                    ServerResult.Progress -> {
                        Timber.tag(TAG).d("Fetching notification list")
                        _serverResultLiveData.postValue(ServerResult.Progress)
                    }

                    is ServerResult.Success -> {

                        val notificationList = notificationListResult.data
                        Timber.tag(TAG).d("New notification list fetching is successful : ${notificationListResult.data}")

                        if (notificationList.isNotEmpty()) {
                            val latestNotificationTimestamp = notificationList[0].timeStamp //Getting the latest notification timestamp
                            PrefManager.setLatestNotificationTimeStamp(latestNotificationTimestamp)

                            CoroutineScope(Dispatchers.IO).launch {
                                val newNotificationCount = getNewNotificationCount(notificationList)

                                withContext(Dispatchers.Main) {
                                    PrefManager.setNotificationCount(PrefManager.getNotificationCount()+newNotificationCount)
                                    _serverResultLiveData.postValue(
                                        ServerResult.Success(
                                            notificationList
                                        )
                                    )
                                }
                            }

                        } else {
                            _serverResultLiveData.postValue(
                                ServerResult.Success(
                                    notificationList
                                )
                            )
                        }
                    }
                }
            }
        }
    }


    fun getNewNotificationCount(notificationList: List<Notification>): Int {
        var count = 0
        val lastSeenTimeStamp = PrefManager.getLastSeenTimeStamp()

        for (notification in notificationList) {
            if (notification.timeStamp > lastSeenTimeStamp) {
                count++
            }
        }

        return count
    }


    fun checkMaintenanceThroughRepository(): LiveData<maintainceCheck> {
        return repository.maintenanceCheck()
    }
}