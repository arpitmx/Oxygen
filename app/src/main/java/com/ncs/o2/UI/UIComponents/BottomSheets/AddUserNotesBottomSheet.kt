package com.ncs.o2.UI.UIComponents.BottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncs.o2.Domain.Models.TodayTasks
import com.ncs.o2.Domain.Models.UserNote
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.databinding.AddUserNotesBottomsheetBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddUserNotesBottomSheet(val callback:OnNoteCreated,val u_note: UserNote) : BottomSheetDialogFragment(){
    @Inject
    lateinit var utils : GlobalUtils.EasyElements
    lateinit var binding:AddUserNotesBottomsheetBinding
    @Inject
    lateinit var firestoreRepository: FirestoreRepository


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AddUserNotesBottomsheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
    }

    private fun setViews() {
        setBottomSheetConfig()
        setActionbar()
        setUpViews()
    }

    private fun setActionbar() {
        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }
    }
    private fun setUpViews(){

        binding.desc.setText(u_note.desc)

        binding.submit.setOnClickThrottleBounceListener {
            if (binding.desc.text?.trim().toString().isBlank()){
                toast("Description is required")
            }
            else{
                val id="USER-${RandomIDGenerator.generateRandomTaskId(5)}"
                val userNote=UserNote(
                    id = id,
                    desc = binding.desc.text?.trim().toString()
                )
                val today=TodayTasks(
                    taskID = id,
                    isCompleted = false
                )
                val oldNotes=PrefManager.getProjectUserNotes(PrefManager.getcurrentProject()).toMutableList()
                val todays=PrefManager.getProjectTodayTasks(PrefManager.getcurrentProject()).toMutableList()
                if (oldNotes.contains(u_note)){
                    oldNotes.remove(u_note)
                    userNote.id=u_note.id
                    oldNotes.add(userNote)
                    PrefManager.saveProjectUserNotes(PrefManager.getcurrentProject(),oldNotes)
                    toast("Note Updated")
                    callback.noteCreated(userNote)
                    dismiss()
                }
                else{
                    oldNotes.add(userNote)
                    todays.add(today)

                    PrefManager.saveProjectUserNotes(PrefManager.getcurrentProject(),oldNotes)
                    PrefManager.saveProjectTodayTasks(PrefManager.getcurrentProject(),todays)
                    toast("Note Added")
                    callback.noteCreated(userNote)
                    dismiss()
                }
            }
        }
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }

    interface OnNoteCreated{
        fun noteCreated(userNote: UserNote)
    }

}