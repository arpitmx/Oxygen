package com.ncs.o2.UI.Report

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.databinding.FragmentShakePrefrencesBinding
import com.ncs.o2.databinding.FragmentTeamsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShakePrefrencesFragment() : Fragment() {

    lateinit var binding: FragmentShakePrefrencesBinding
    private val activityBinding: ShakeDetectedActivity by lazy {
        (requireActivity() as ShakeDetectedActivity)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShakePrefrencesBinding.inflate(inflater, container, false)
        setUpViews()
        return binding.root
    }



    private fun setUpViews(){

        activityBinding.binding.gioActionbar.titleTv.text="Shake to report"

        val pref=PrefManager.getShakePref()

        if (pref){
            binding.simpleSwitch.isChecked=true
            binding.shakeControls.visible()
            binding.message.visible()
            val sensi=PrefManager.getShakeSensitivity()
            when(sensi){
                1->{
                    setTick(listOf(binding.mediumIc,binding.heavyIc),binding.lightIc)
                }
                2->{
                    setTick(listOf(binding.lightIc,binding.heavyIc),binding.mediumIc)
                }
                3->{
                    setTick(listOf(binding.lightIc,binding.mediumIc),binding.heavyIc)
                }
            }
        }
        else{
            binding.simpleSwitch.isChecked=false
            binding.shakeControls.gone()
            binding.message.gone()
        }

        binding.simpleSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                PrefManager.setShakePref(true)
                binding.shakeControls.visible()
                binding.message.visible()

                binding.light.setOnClickThrottleBounceListener{
                    PrefManager.setShakeSensitivity(1)
                    setUpViews()
                }
                binding.medium.setOnClickThrottleBounceListener{
                    PrefManager.setShakeSensitivity(2)
                    setUpViews()

                }
                binding.heavy.setOnClickThrottleBounceListener{
                    PrefManager.setShakeSensitivity(3)
                    setUpViews()

                }

            } else {
                PrefManager.setShakePref(false)
                binding.shakeControls.gone()
                binding.message.gone()

            }
        }
        binding.light.setOnClickThrottleBounceListener{
            PrefManager.setShakeSensitivity(1)
            setUpViews()
        }
        binding.medium.setOnClickThrottleBounceListener{
            PrefManager.setShakeSensitivity(2)
            setUpViews()
        }
        binding.heavy.setOnClickThrottleBounceListener{
            PrefManager.setShakeSensitivity(3)
            setUpViews()
        }
    }

    private fun setTick(unchecked:List<ImageView>,checked:ImageView){
        unchecked[0].gone()
        unchecked[1].gone()
        checked.visible()
    }



}