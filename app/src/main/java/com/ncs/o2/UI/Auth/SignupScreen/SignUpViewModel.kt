package com.ncs.o2.UI.Auth.SignupScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private val _emailError = MutableLiveData<String?>(null)
    val emailError: LiveData<String?> get() = _emailError

    private val _passwordError = MutableLiveData<String?>(null)
    val passwordError: LiveData<String?> get() = _passwordError

    private val _repeatpasswordError = MutableLiveData<String?>(null)
    val repeatpasswordError: LiveData<String?> get() = _repeatpasswordError

    private val validationEventChannel = Channel<ValidationEvent>()
    val validationEvents = validationEventChannel.receiveAsFlow()


    fun validateInput(email: String, password: String, repeatedPassword : String) {
        val isEmailValid = emailValidator.execute(email)
        val isPasswordValid = passwordValidator.execute(password)
        val isRepeatedPasswordValid = repeatedPasswordValidator.
                execute(password = password, repeatedPassword = repeatedPassword)

        if (!isEmailValid.successful) {
            _emailError.value = isEmailValid.errorMessage

        }else {
            _emailError.value = null
        }

        if (!isPasswordValid.successful) {
            _passwordError.value = isPasswordValid.errorMessage

        }else {
            _passwordError.value = null
        }

        if (!isRepeatedPasswordValid.successful) {
            _repeatpasswordError.value = isRepeatedPasswordValid.errorMessage

        }else {
            _repeatpasswordError.value = null
        }

        val hasError = listOf(
            isEmailValid,
            isPasswordValid,
            isRepeatedPasswordValid).any{ !it.successful }

        if (hasError) return

        viewModelScope.launch {
            validationEventChannel.send(ValidationEvent.Success)
        }


    }


    sealed class ValidationEvent {
        object Success: ValidationEvent ()
    }
}