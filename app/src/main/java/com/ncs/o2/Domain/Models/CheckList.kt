package com.ncs.o2.Domain.Models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckList(
    val id:String = "",
    val title:String = "",
    val desc:String = "",
    var done:Boolean = false,
    var index:Int=0,
):Parcelable