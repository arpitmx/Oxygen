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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ncs.o2.Constants.Pref
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.CreateTask.CreateTaskActivity
import com.ncs.o2.databinding.CreateTagBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
//import net.datafaker.Faker
import javax.inject.Inject

@AndroidEntryPoint
class CreateTagsBottomSheet (private var selectedTagsList: MutableList<Tag>,private var TagsList: MutableList<Tag>,private var callback: AddTagsBottomSheet.getSelectedTagsCallback): BottomSheetDialogFragment(){


    @Inject
    @FirebaseRepository
    lateinit var repository: Repository
    lateinit var binding: CreateTagBottomSheetBinding

    private var TagListfromFireStore: MutableList<Tag> = mutableListOf()


//    private val faker: Faker by lazy { Faker() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = CreateTagBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }
    var initialbgcolor="#FFFFFF"
    var initialtextcolor="#000000"


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

            val tag=Tag(tagText = binding.tagTitle.text.toString(), bgColor = initialbgcolor, textColor = initialtextcolor, tagID = RandomIDGenerator.generateRandomTaskId(6) , projectName = PrefManager.getcurrentProject())

            if (tag.tagText.isNotEmpty()) {
                binding.doneButton.animate().alpha(0f).setDuration(300).withEndAction {
                    binding.doneButton.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.alpha = 0f
                    binding.progressBar.animate().alpha(1f).setDuration(300).start()
                }
                CoroutineScope(Dispatchers.Main).launch {
                    repository.postTags(tag,PrefManager.getcurrentProject()) { result ->

                        when (result) {

                            is ServerResult.Failure -> {
                                binding.progressBar.gone()
                            }

                            ServerResult.Progress -> {
                                binding.progressBar.visible()
                            }

                            is ServerResult.Success -> {
                                binding.progressBar.gone()
                                dismiss()
                                val addTagsBottomSheet = AddTagsBottomSheet(TagsList,callback,selectedTagsList,"create")
                                addTagsBottomSheet.show(requireActivity().supportFragmentManager,"this")
                            }

                        }
                    }
                }
            }
            else{
                Toast.makeText(requireContext(),"Tag Title can't be empty",Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun setViews() {
        setBottomSheetConfig()
        binding.tagTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                binding.previewChip.text=binding.tagTitle.text.toString()
                if (s.isNullOrBlank()){
                    binding.tagPreview.gone()
                }
                else{
                    binding.tagPreview.visible()
                }
            }
        })
        binding.tagbg.setOnClickThrottleBounceListener {
            ColorPickerDialog
                .Builder(requireContext())
                .setTitle("Set Tag Color")
                .setColorShape(ColorShape.SQAURE)
                .setDefaultColor(initialbgcolor)
                .setColorListener { color, colorHex ->
                    initialbgcolor=colorHex
                    binding.tagPreview.visible()
                    binding.tagbg.background=Color.parseColor(initialbgcolor).toDrawable()
                    binding.previewChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(initialbgcolor)))

                }
                .show()
        }
        binding.textbg.setOnClickThrottleBounceListener {
            ColorPickerDialog
                .Builder(requireContext())
                .setTitle("Set Text Color")
                .setColorShape(ColorShape.SQAURE)
                .setDefaultColor(initialtextcolor)
                .setColorListener { color, colorHex ->
                    initialtextcolor=colorHex
                    binding.tagPreview.visible()
                    binding.textbg.setBackgroundColor(Color.parseColor(initialtextcolor))
                    binding.previewChip.setTextColor(Color.parseColor(initialtextcolor))

                }
                .setPositiveButton("Set")
                .show()
        }
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = false
    }




}







