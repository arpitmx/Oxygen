package com.ncs.o2.Domain.Models.state

/*
File : RegistrationFormEvent.kt -> com.ncs.o2.Domain.Models.state
Description : Class for events 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 7:30 pm on 21/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/

sealed class RegistrationFormEvent {
    data class EmailChanged (val email: String) : com.ncs.o2.Domain.Models.state.RegistrationFormEvent()
    data class PasswordChanged (val password: String) : com.ncs.o2.Domain.Models.state.RegistrationFormEvent( )
    data class RepeatedPasswordChanged ( val repeatedPassword: String) : com.ncs.o2.Domain.Models.state.RegistrationFormEvent()
    object Submit: com.ncs.o2.Domain.Models.state.RegistrationFormEvent()
}