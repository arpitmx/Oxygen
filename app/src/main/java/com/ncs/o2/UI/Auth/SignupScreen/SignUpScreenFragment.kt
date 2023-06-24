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
import androidx.lifecycle.lifecycleScope
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

        setUpViews()
        setUpValidation()

    }

    private fun setUpValidation() {
        handleValidation()
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main){
            viewModel.validationEvents.collect{ event ->
            when(event){
                SignUpViewModel.ValidationEvent.Success -> {
                    Toast.makeText(activity, "Registration success", Toast.LENGTH_SHORT).show()

                }
            }
         }
        }
    }

    private fun handleValidation() {
        binding.btnSignup.setOnClickThrottleBounceListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPass.text.toString()
            val confirmPass = binding.etConfirmationPass.text.toString()
            viewModel.validateInput(
                email = email,
                password = pass,
                repeatedPassword = confirmPass)
            }
    }


    private fun setUpViews() {
        binding.login.setOnClickListener {
            findNavController().navigate(R.id.action_signUpScreenFragment_to_loginScreenFragment)
        }

        binding.toolbar.btnBack.setOnClickThrottleBounceListener{
            findNavController().navigate(R.id.action_signUpScreenFragment_to_chooserFragment)
        }

        viewModel.emailError.observe(requireActivity()){error ->
            binding.etEmail.error = error
        }

        viewModel.passwordError.observe(requireActivity()){error->
            binding.etPass.error = error
        }

        viewModel.repeatpasswordError.observe(requireActivity()){error->
            binding.etConfirmationPass.error = error
        }

    }


}