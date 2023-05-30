package com.ncs.o2.UI

import android.util.Log
import androidx.lifecycle.ViewModel
import timber.log.Timber

/*
File : MainActivityViewModel.kt -> com.ncs.o2
Description : Viewmodel for Mainactivity 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 7:28 pm on 30/05/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/

class MainActivityViewModel : ViewModel() {

    fun greet() {
        Timber.tag("Hello").d("greet: hi")
    }

}