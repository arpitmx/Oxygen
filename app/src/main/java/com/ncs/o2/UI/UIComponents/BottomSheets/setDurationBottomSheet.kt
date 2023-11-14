package com.ncs.o2.UI.UIComponents.BottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.databinding.DurationSetBottomSheetBinding

class setDurationBottomSheet: BottomSheetDialogFragment() {

    lateinit var binding: DurationSetBottomSheetBinding
    var durationAddedListener: DurationAddedListener? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DurationSetBottomSheetBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val radioGroup: RadioGroup = view.findViewById(R.id.mcqGroup)

        binding.doneBtn.setOnClickListener {

            if (binding.projectLink.text!!.isEmpty()){
                Toast.makeText(requireContext(), "Pls set some duration", Toast.LENGTH_SHORT).show()
            }
            else{
                val selectedRadioButtonId = radioGroup.checkedRadioButtonId
                if (selectedRadioButtonId != -1) {
                    val selectedRadioButton: RadioButton = view.findViewById(selectedRadioButtonId)
                    val selectedOption = selectedRadioButton.text.toString()

                    durationAddedListener?.onDurationAdded("${binding.projectLink.text} $selectedOption")
                    Toast.makeText(requireContext(), "${binding.projectLink.text} $selectedOption", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }

        setViews()

    }

    fun setViews(){

        binding.closeBtn.setOnClickThrottleBounceListener {
            dismiss()
        }

    }

    interface DurationAddedListener {
        fun onDurationAdded(segmentName: String)
    }
}