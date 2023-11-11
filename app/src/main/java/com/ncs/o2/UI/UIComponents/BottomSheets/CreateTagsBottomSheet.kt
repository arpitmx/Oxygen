package com.ncs.o2.UI.UIComponents.BottomSheets
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.UI.CreateTask.CreateTaskActivity
import com.ncs.o2.databinding.CreateTagBottomSheetBinding
import net.datafaker.Faker

class CreateTagsBottomSheet (private var selectedTagsList: MutableList<Tag>,private var TagsList: MutableList<Tag>,private var callback: AddTagsBottomSheet.getSelectedTagsCallback): BottomSheetDialogFragment(){



    lateinit var binding: CreateTagBottomSheetBinding

    private var TagListfromFireStore: MutableList<Tag> = mutableListOf()


    private val faker: Faker by lazy { Faker() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = CreateTagBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }
    var initialbgcolor="#FFFFFF"
    var initialtextcolor="#FFFFFF"


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

            val tag=Tag(tagText = binding.tagTitle.text.toString(), bgColor = initialbgcolor, textColor = initialtextcolor, tagID = System.currentTimeMillis().toString() )

            if (tag.tagText.isNotEmpty()) {
                binding.doneButton.animate().alpha(0f).setDuration(300).withEndAction {
                    binding.doneButton.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.alpha = 0f
                    binding.progressBar.animate().alpha(1f).setDuration(300).start()
                }
                FirebaseFirestore.getInstance().collection("Projects")
                    .document("Versa")  //Add the actual project name here
                    .update("TAGS", FieldValue.arrayUnion(tag))
                    .addOnSuccessListener {
                        binding.progressBar.visibility = View.GONE
                        dismiss()
                        val addTagsBottomSheet = AddTagsBottomSheet(TagsList,callback,selectedTagsList)
                        addTagsBottomSheet.show(requireActivity().supportFragmentManager,"this")

                    }
                    .addOnFailureListener { e ->

                    }
            }
            else{
                Toast.makeText(requireContext(),"Tag Title can't be empty",Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun setViews() {
        setBottomSheetConfig()
        binding.tagbg.setOnClickThrottleBounceListener {
            ColorPickerDialog
                .Builder(requireContext())
                .setTitle("Set Tag Color")
                .setColorShape(ColorShape.SQAURE)
                .setDefaultColor(initialbgcolor)
                .setColorListener { color, colorHex ->
                    initialbgcolor=colorHex
                    binding.tagbg.background=Color.parseColor(initialbgcolor).toDrawable()
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
                    binding.textbg.setBackgroundColor(Color.parseColor(initialtextcolor))
                }
                .setPositiveButton("Set")
                .show()
        }
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = false
    }


}







