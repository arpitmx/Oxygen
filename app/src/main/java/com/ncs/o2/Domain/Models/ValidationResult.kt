package com.ncs.o2.Domain.Models

/*
File : ValidationResult.kt -> com.ncs.o2.Domain.Models
Description : Data class for validation result 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 6:14 pm on 21/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/

data class ValidationResult (
    val successful: Boolean,
    val errorMessage: String? = null
)
