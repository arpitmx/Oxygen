package com.ncs.o2.UI.Auth.LoginScreen

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.showKeyboard
import com.ncs.o2.Domain.Utility.ExtensionsUtil.showKeyboardB
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.SignupScreen.SignUpViewModel
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.databinding.FragmentLoginScreenBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class LoginScreenFragment @Inject constructor(): Fragment() {

    companion object {
        fun newInstance() = LoginScreenFragment()
    }

    private val viewModel: LoginScreenViewModel by viewModels()
    lateinit var binding: FragmentLoginScreenBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginScreenBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        setUpValidation()
    }


    private fun setUpValidation() {
        handleValidation()
// This code launches a coroutine on the main thread and collects validation events from a ViewModel. If the event is a success, it displays a short toast message.
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.validationEvents.collect{ event->
                when(event){
                    LoginScreenViewModel.ValidationEvent.Success -> {
                        Toast.makeText(activity, "Checking...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun handleValidation() {
//        binding.btnLogin.setOnClickThrottleBounceListener {
//            val email = binding.etEmail.text.toString()
//            val pass = binding.etPass.text.toString()
//            viewModel.validateInput(
//                email = email,
//                password = pass)
//        }
    }


    private fun setUpViews() {
        binding.signUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginScreenFragment_to_signUpScreenFragment)

            viewModel.emailError.observe(requireActivity()){error ->
                binding.etEmail.error = error
            }

            viewModel.passwordError.observe(requireActivity()){error->
                binding.etPass.error = error
            }

        }


        binding.toolbar.btnBack.setOnClickThrottleBounceListener{
            findNavController().navigate(R.id.action_loginScreenFragment_to_chooserFragment)
        }

//        binding.btnLogin.setOnClickThrottleBounceListener {
//            val email = binding.etEmail.text.toString()
//            val pass = binding.etPass.text.toString()
//            viewModel.validateInput(email = email, password = pass)
//        }

        binding.btnLogin.setOnClickThrottleBounceListener {
            requireActivity().startActivity(Intent(requireContext(),MainActivity::class.java))
        }

    }

    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus
        val methodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        assert(view != null)
        methodManager.hideSoftInputFromWindow(view!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun showKeyboardThis(activity: Activity) {
        val view = activity.currentFocus
        val methodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        assert(view != null)
        methodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

}