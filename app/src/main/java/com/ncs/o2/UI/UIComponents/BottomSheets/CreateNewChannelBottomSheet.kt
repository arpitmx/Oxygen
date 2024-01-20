package com.ncs.o2.UI.UIComponents.BottomSheets
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.ChipDrawable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ncs.o2.Constants.Pref
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Channel
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.CreateTask.CreateTaskActivity
import com.ncs.o2.databinding.CreateNewChannelBottomsheetBinding
import com.ncs.o2.databinding.CreateTagBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.datafaker.Faker
import javax.inject.Inject

@AndroidEntryPoint
class CreateNewChannelBottomSheet (private val callback:OnChannelAdded): BottomSheetDialogFragment(){


    @Inject
    @FirebaseRepository
    lateinit var repository: Repository
    lateinit var binding: CreateNewChannelBottomsheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = CreateNewChannelBottomsheetBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActionbar()
        setViews()

    }
    private fun setActionbar() {

        binding.closeBottmSheet.setOnClickThrottleBounceListener{
            dismiss()
        }

        binding.doneButton.setOnClickThrottleBounceListener {
            if (binding.channelName.text.isNullOrEmpty()){
                toast("Channel name is required")
            }
            else{
                val title=binding.channelName.text.toString()
                val id="$title${RandomIDGenerator.generateRandomTaskId(5)}"
                val channel=Channel(
                    channel_name = title,
                    channel_desc = binding.channelDesc.text.toString(),
                    channel_id = id,
                    timestamp = Timestamp.now(),
                    creator = PrefManager.getCurrentUserEmail()
                )

                CoroutineScope(Dispatchers.Main).launch {
                    repository.postChannel(channel,PrefManager.getcurrentProject()) { result ->

                        when (result) {

                            is ServerResult.Failure -> {
                                toast("Something went wrong")
                                binding.progressBar.gone()
                            }

                            ServerResult.Progress -> {
                                binding.progressBar.visible()
                            }

                            is ServerResult.Success -> {
                                toast("Channel Created Successfully")
                                binding.progressBar.gone()
                                dismiss()
                                callback.onChannelAdd(channel)

                            }

                        }
                    }
                }
            }
        }
    }

    private fun setViews() {
        setBottomSheetConfig()
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = false
    }

    interface OnChannelAdded{
        fun onChannelAdd(channel: Channel)
    }



}







