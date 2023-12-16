package com.ncs.o2.Domain.Models

import java.lang.Exception

sealed class DBResult<out T> {
    data class Success<out T>(val data : T) : com.ncs.o2.Domain.Models.DBResult<T>()
    object Progress : com.ncs.o2.Domain.Models.DBResult<Nothing>()
    data class Failure(val exception: Exception) : com.ncs.o2.Domain.Models.DBResult<Nothing>()


}