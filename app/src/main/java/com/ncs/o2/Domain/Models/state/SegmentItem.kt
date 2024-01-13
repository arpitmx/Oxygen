package com.ncs.o2.Domain.Models.state

import com.google.firebase.Timestamp

data class SegmentItem (
    val segment_NAME : String="",
    var sections: MutableList<String> = mutableListOf()
)