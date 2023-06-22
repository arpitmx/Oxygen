package com.ncs.o2.UI.Auth.usecases

import android.util.Patterns
import com.ncs.o2.Domain.Models.ValidationResult

/*
File : ValidationPassword.kt -> com.ncs.o2.UI.Auth.usecases
Description : Validation class for password 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 6:24 pm on 21/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*//*
File : ValidationPassword.kt -> com.ncs.o2.UI.Auth.usecases
Description : Validation class for password 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 6:24 pm on 21/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/
class ValidationPassword {

    fun execute(password:String) : ValidationResult {

        if (password.isBlank()){
            return ValidationResult(
                false,
                "Password cannot be blank."
            )
        }

        if (password.length < 8){
            return ValidationResult(
                false,
                "Password should atleast be of 8 characters."
            )
        }
        return ValidationResult(true)
    }


}