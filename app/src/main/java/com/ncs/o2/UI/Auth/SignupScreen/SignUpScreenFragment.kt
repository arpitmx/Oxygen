package com.ncs.o2.UI.Auth.SignupScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.R
import com.ncs.o2.databinding.FragmentSignUpBinding

class SignUpScreenFragment : Fragment() {

    companion object {
        fun newInstance() = SignUpScreenFragment()
    }

    private lateinit var viewModel: SignUpViewModel
    lateinit var binding: FragmentSignUpBinding

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
    }

    private fun setUpViews() {
        binding.login.setOnClickListener {
            findNavController().navigate(R.id.action_signUpScreenFragment_to_loginScreenFragment)
        }

        binding.toolbar.btnBack.setOnClickThrottleBounceListener{
            findNavController().navigate(R.id.action_signUpScreenFragment_to_chooserFragment)
        }
    }

}