package com.ncs.o2.UI.Auth.ChooserScreen

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotate180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotateInfinity
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotateInfinityReverse
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.LoginScreen.LoginScreenFragment
import com.ncs.o2.databinding.FragmentChooserBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class ChooserFragment : Fragment() {


    companion object {
        fun newInstance() = ChooserFragment()
    }

    lateinit var binding: FragmentChooserBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChooserBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViews()
    }

    private fun setUpViews() {
        binding.splashBg.rotateInfinity(requireActivity())

        binding.title.animFadein(requireContext(),2000)
        binding.btnLogin.setOnClickThrottleBounceListener{
            findNavController().navigate(R.id.action_chooserFragment_to_loginScreenFragment)
        }

        binding.btnGetStarted.setOnClickThrottleBounceListener {
            findNavController().navigate(R.id.action_chooserFragment_to_signUpScreenFragment)
        }

    }


}
