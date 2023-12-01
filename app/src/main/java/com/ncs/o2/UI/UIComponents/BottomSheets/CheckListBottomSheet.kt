package com.ncs.o2.UI.UIComponents.BottomSheets

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncs.o2.Domain.Models.CheckList
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.databinding.CreateChecklistBottomsheetBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CheckListBottomSheet (private val count:Int,private val listener: checkListItemListener,private val checkList: CheckList= CheckList()):BottomSheetDialogFragment(){
    val binding: CreateChecklistBottomsheetBinding by lazy {
        CreateChecklistBottomsheetBinding.inflate(layoutInflater)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { it ->
                val behaviour = BottomSheetBehavior.from(it)
                setupFullHeight(it)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }


    private fun initViews(){
        binding.title.setText(checkList.title)
        binding.desc.setText(checkList.desc)
        binding.number.text=count.toString()
        binding.gioActionbar.btnCross.setOnClickThrottleBounceListener{
            dismiss()
        }
        binding.gioActionbar.btnNext.setOnClickListener {
            val title=binding.title.text.toString()
            val desc=binding.desc.text.toString()
            if (title.isBlank() || desc.isBlank()){
                toast("All fields are required")
            }
            else{
                if (checkList.id!=""){
                    listener.checkListItem(CheckList(id = checkList.id, title = title, desc = desc, done = false), isEdited = true)
                    toast("Updated Successfully")
                }
                else{
                    listener.checkListItem(CheckList(id = RandomIDGenerator.generateRandomTaskId(5), title = title, desc = desc, done = false))
                }
                binding.title.text?.clear()
                binding.desc.text?.clear()
                dismiss()
            }
        }
    }
    interface checkListItemListener{
        fun checkListItem(checkList: CheckList,isEdited:Boolean=false)
    }
}