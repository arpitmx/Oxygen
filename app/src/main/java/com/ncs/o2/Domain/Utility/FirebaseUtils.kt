package com.ncs.o2.Domain.Utility

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

/*
File : FirebaseUtils.kt -> com.ncs.o2.Domain.Utility
Description : Utils for firebase  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 3:08 pm on 21/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/
object FirebaseUtils {




    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun <T> Task<T>.awaitt(): T {
        return suspendCancellableCoroutine { cont ->
            addOnCompleteListener {
                if (it.exception != null) {
                    cont.resumeWithException(it.exception!!)
                } else {
                    cont.resume(it.result, null)
                }
            }
        }
    }
}