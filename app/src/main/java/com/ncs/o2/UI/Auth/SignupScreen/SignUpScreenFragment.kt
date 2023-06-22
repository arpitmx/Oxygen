package com.ncs.o2.UI.Auth.SignupScreen

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ncs.o2.Domain.Models.state.RegistrationFormEvent
import com.ncs.o2.Domain.Models.state.RegistrationFormState
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.R
import com.ncs.o2.databinding.FragmentSignUpBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpScreenFragment : Fragment() {

    companion object {
        fun newInstance() = SignUpScreenFragment()
    }

    private val viewModel: SignUpViewModel by viewModels()
    lateinit var binding: FragmentSignUpBinding
    lateinit var state : RegistrationFormState

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpValidation()
        setUpViews()
    }

    private fun setUpValidation() {
        state = viewModel.state
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.validationEvents.collect{ event->
                when(event){
                    SignUpViewModel.ValidationEvent.Success -> {
                        Toast.makeText(activity, "Registration success", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }




    private fun setUpViews() {
        binding.login.setOnClickListener {
            findNavController().navigate(R.id.action_signUpScreenFragment_to_loginScreenFragment)
        }

        binding.toolbar.btnBack.setOnClickThrottleBounceListener{
            findNavController().navigate(R.id.action_signUpScreenFragment_to_chooserFragment)
        }


        with(binding.etEmail){
            setText(state.email)

            addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(email: Editable?) {
                    viewModel.onEvent(RegistrationFormEvent.EmailChanged(email = email.toString().trim()))
                }

            })
        }

        with(binding.etPass){
            setText(state.password)
            if (state.passwordError!=null){
                error = state.passwordError
            }

            addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(pass: Editable?) {
                    viewModel.onEvent(RegistrationFormEvent.PasswordChanged(password = pass.toString().trim()))
                }

            })

        }

        with(binding.etConfirmationPass){
            setText(state.repeatedPassword)
            if (state.repeatedPasswordError==null){
                error = state.repeatedPasswordError
            }

            addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(confirmation_pass: Editable?) {
                    viewModel.onEvent(RegistrationFormEvent.RepeatedPasswordChanged(repeatedPassword = confirmation_pass.toString().trim()))
                }
            })
        }

        binding.btnSignup.setOnClickThrottleBounceListener{
            viewModel.onEvent(RegistrationFormEvent.Submit)
            if (state.emailError!=null){
                binding.etEmail.error = state.emailError
            }
        }


    }


}