package com.ncs.o2.UI.Auth.LoginScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.ncs.o2.Domain.Interfaces.AuthRepository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.FirebaseAuthorizationRepository
import com.ncs.o2.Domain.Utility.Issue
import com.ncs.o2.UI.Auth.SignupScreen.SignUpViewModel
import com.ncs.o2.UI.Auth.usecases.ValidationEmail
import com.ncs.o2.UI.Auth.usecases.ValidationPassword
import com.ncs.o2.UI.Auth.usecases.ValidationRepeatedPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    val emailValidator : ValidationEmail,
    val passwordValidator : ValidationPassword,
    @FirebaseAuthorizationRepository val authRepository: AuthRepository
) : ViewModel() {

    private val _emailError = MutableLiveData<String?>(null)
    val emailError: LiveData<String?> get() = _emailError

    private val _passwordError = MutableLiveData<String?>(null)
    val passwordError: LiveData<String?> get() = _passwordError

    private val validationEventChannel = Channel<ValidationEvent>()
    val validationEvents = validationEventChannel.receiveAsFlow()

    private val _loginLiveData = MutableLiveData<ServerResult<FirebaseUser>?>(null)
    val loginLiveData: LiveData<ServerResult<FirebaseUser>?> = _loginLiveData


    @Issue("Running multiple times on wrong input...")
    fun validateInput(email: String, password: String) {
        val isEmailValid = emailValidator.execute(email)
        val isPasswordValid = passwordValidator.execute(password)

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

        val hasError = listOf(
            isEmailValid,
            isPasswordValid).any{ !it.successful }

        if (hasError) return

        viewModelScope.launch {
            validationEventChannel.send(ValidationEvent.Success)
            loginUser(email, password = password)
        }

    }

    suspend fun loginUser(email: String, password: String) {
        _loginLiveData.postValue(ServerResult.Progress)
        val result = authRepository.login(email, password)
        _loginLiveData.postValue(result)

    }



    sealed class ValidationEvent {
        object Success: ValidationEvent ()
    }

}