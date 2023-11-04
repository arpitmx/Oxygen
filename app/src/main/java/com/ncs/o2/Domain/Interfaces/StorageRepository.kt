package com.ncs.o2.Domain.Interfaces

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.google.firebase.storage.StorageReference

/*
File : StorageRepository -> com.ncs.o2.Domain.Interfaces
Description : Interface for Storage Repository 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 12:07 pm on 04/11/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/

interface StorageRepository {

    fun uploadDisplayPicture(bitmap: Bitmap): LiveData<StorageReference>

}