package com.ncs.o2.UI.UIComponents.BottomSheets



import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Domain.Models.CheckList
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.Codes
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.databinding.AddQuickTaskBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddQuickTaskBottomSheet(private val message: com.ncs.o2.Domain.Models.Message) : BottomSheetDialogFragment(),SegmentSelectionBottomSheet.SegmentSelectionListener,
    SegmentSelectionBottomSheet.sendSectionsListListner,sectionDisplayBottomSheet.SectionSelectionListener{
    @Inject
    lateinit var utils : GlobalUtils.EasyElements
    lateinit var binding:AddQuickTaskBottomSheetBinding
    @Inject
    lateinit var firestoreRepository: FirestoreRepository


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AddQuickTaskBottomSheetBinding.inflate(inflater, container, false)
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

        binding.title.setText(message.content)

        binding.segment.setOnClickThrottleBounceListener {
            val segment = SegmentSelectionBottomSheet("Quick Task")
            segment.segmentSelectionListener = this
            segment.sectionSelectionListener = this
            segment.show(requireFragmentManager(), "Segment Selection")
        }

        binding.section.setOnClickThrottleBounceListener {

            if (binding.segment.text=="Segment") {
                toast("First select segment")
            } else {
                val sections = sectionDisplayBottomSheet(binding.segment.text.toString())
                sections.sectionSelectionListener = this
                sections.show(requireFragmentManager(), "Section Selection")
            }
        }

        binding.submit.setOnClickThrottleBounceListener {
            if (binding.segment.text=="Segment"){
                toast("Select Task Segment")
            }
            else if (binding.section.text=="Section"){
                toast("Select Task Section")
            }
            else if (binding.title.text?.trim().isNullOrBlank()){
                toast("Enter Task Title")
            }
            else {
                val task = Task(
                    title = binding.title.text?.trim().toString(),
                    description = "",
                    id = generateTaskID(PrefManager.getcurrentProject()),
                    difficulty = 1,
                    priority = 1,
                    status = 1,
                    assignee = "None",
                    assigner = PrefManager.getCurrentUserEmail(),
                    duration = "Not Set",
                    time_STAMP = Timestamp.now(),
                    tags = emptyList(),
                    project_ID = PrefManager.getcurrentProject(),
                    segment = binding.segment.text.toString(),
                    section = binding.section.text.toString(),
                    type = 1,
                    moderators = emptyList(),
                    last_updated = Timestamp.now()
                )

                postTask(task, mutableListOf())

            }
        }

    }

    private fun postTask(task: Task,checkList: MutableList<CheckList>){

        CoroutineScope(Dispatchers.Main).launch {

            firestoreRepository.postTask(task,checkList) { result ->

                when (result) {

                    is ServerResult.Failure -> {
                        binding.body.visible()
                        binding.progressBar.gone()
                    }

                    ServerResult.Progress -> {
                        binding.body.gone()
                        binding.progressBar.visible()
                    }

                    is ServerResult.Success -> {
                        PrefManager.putDraftTask(Task())
                        PrefManager.putDraftCheckLists(emptyList())
                        binding.body.visible()
                        binding.progressBar.gone()
                        toast("Task Created Successfully")
                        dismiss()
                    }
                }
            }
        }
    }

    fun generateTaskID(projectName: String):String{
        return "#${generateProjectNamePrefix(projectName)}-${RandomIDGenerator.generateRandomTaskId(4)}"
    }
    fun generateProjectNamePrefix(projectName: String): String {
        val words = projectName.split(" ")
        return when {
            words.size == 1 -> {
                if (projectName.length >= 2) {
                    "${projectName.substring(0, 2)}${projectName.last()}"
                } else {
                    projectName
                }
            }
            words.size >= 2 -> {
                val firstWord = words[0]
                val secondWord = words[1]
                if (firstWord.length >= 2) {
                    "${firstWord.substring(0, 2)}${secondWord.first()}${secondWord.last()}"
                } else {
                    projectName
                }
            }
            else -> projectName
        }.toUpperCase()
    }


    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }

    override fun onSegmentSelected(segmentName: String) {
        binding.segment.text = segmentName
        Codes.STRINGS.segmentText = segmentName
        binding.section.text="Section"
    }

    override fun sendSectionsList(list: MutableList<String>) {
    }

    override fun onSectionSelected(sectionName: String) {
        binding.section.text = sectionName
    }
}