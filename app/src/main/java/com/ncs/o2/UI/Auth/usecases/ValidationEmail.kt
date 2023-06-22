package com.ncs.o2.UI.Auth.usecases

import android.util.Patterns
import com.ncs.o2.Domain.Models.ValidationResult

/*
File : ValidationEmail.kt -> com.ncs.o2.UI.Auth.usecases
Description : Class for validating email 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 6:15 pm on 21/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION :

*/
class ValidationEmail {

    fun execute(email:String) : ValidationResult{

        if (email.isBlank()){
            return ValidationResult(
                false,
                "Email can't be blank."
            )
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return ValidationResult(
                false,
                "Invalid email address."
            )
        }

        return ValidationResult(true)

    }

}