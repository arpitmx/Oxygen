package com.ncs.o2.UI.Auth.SignupScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncs.o2.Domain.Interfaces.AuthRepository
import com.ncs.o2.Domain.Models.state.RegistrationFormEvent
import com.ncs.o2.Domain.Models.state.RegistrationFormState
import com.ncs.o2.Domain.Utility.FirebaseAuthorizationRepository
import com.ncs.o2.UI.Auth.usecases.ValidationEmail
import com.ncs.o2.UI.Auth.usecases.ValidationPassword
import com.ncs.o2.UI.Auth.usecases.ValidationRepeatedPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel
@Inject constructor(
    val emailValidator : ValidationEmail,
    val passwordValidator : ValidationPassword,
    val repeatedPasswordValidator: ValidationRepeatedPassword,
    @FirebaseAuthorizationRepository val authRepository: AuthRepository
) : ViewModel() {

    var state by mutableStateOf(RegistrationFormState())
    private val validationEventChannel = Channel<ValidationEvent>()
    val validationEvents = validationEventChannel.receiveAsFlow()

    fun onEvent(event: RegistrationFormEvent){
        when (event){
            is RegistrationFormEvent.EmailChanged -> {
                state = state.copy(email = event.email)
            }
            is RegistrationFormEvent.PasswordChanged -> {
                state = state.copy(password = event.password)
            }
            is RegistrationFormEvent.RepeatedPasswordChanged -> {
                state = state.copy(repeatedPassword = event.repeatedPassword)
            }

            RegistrationFormEvent.Submit -> {
                submitDetails()
            }
        }
    }

    private fun submitDetails() {
        val emailResult = emailValidator.execute(state.email)
        val passwordResult = passwordValidator.execute(state.password)
        val repeatedPasswordResult = repeatedPasswordValidator.execute(state.password,state.repeatedPassword)

        val hasError = listOf(emailResult,passwordResult,repeatedPasswordResult).any{
            !it.successful
        }

        if (hasError){
            state = state.copy(
                emailError = emailResult.errorMessage,
                passwordError = passwordResult.errorMessage,
                repeatedPasswordError = repeatedPasswordResult.errorMessage
            )
            return
        }

        viewModelScope.launch {
            validationEventChannel.send(ValidationEvent.Success)
        }

    }

    sealed class ValidationEvent {
        object Success: ValidationEvent ()
    }

}