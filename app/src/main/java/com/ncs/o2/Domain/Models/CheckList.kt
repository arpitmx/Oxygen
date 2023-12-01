package com.ncs.o2.Domain.Models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckList(
    val id:String = "",
    val title:String = "",
    val desc:String = "",
    val done:Boolean = false,
):Parcelable