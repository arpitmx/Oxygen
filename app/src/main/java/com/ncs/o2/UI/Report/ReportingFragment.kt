package com.ncs.o2.UI.Report

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.chip.Chip
import com.google.firebase.Timestamp
import com.google.firebase.storage.StorageReference
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.CheckList
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.CreateTask.CreateTaskActivity
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.databinding.FragmentReportingBinding
import com.ncs.o2.databinding.FragmentShakePrefrencesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ReportingFragment : Fragment() {

    lateinit var binding: FragmentReportingBinding
    private val activityBinding: ShakeDetectedActivity by lazy {
        (requireActivity() as ShakeDetectedActivity)
    }

    lateinit var bitmap: Bitmap

    @Inject
    @FirebaseRepository
    lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
     var type:MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportingBinding.inflate(inflater, container, false)
        setUpViews()
        return binding.root
    }
    private fun setUpViews(){
        if (activityBinding.fileName != null) {
            val internalStorageDir = requireActivity().filesDir
            val file = File(internalStorageDir, activityBinding.fileName)
            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(file.absolutePath)
                binding.image.setImageBitmap(bitmap)
            }


        }

        binding.shakeSettings.setOnClickThrottleBounceListener {
            val transaction = fragmentManager?.beginTransaction()!!
            val fragment = ShakePrefrencesFragment()
            transaction.replace(R.id.shake_detected_fragment_container, fragment)
            transaction.commit()
        }


        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val clickedChip: Chip? = group.findViewById(checkedId)
            clickedChip?.isChecked = true

            if (type.isEmpty()){
                type.add(clickedChip?.text.toString())
            }
            else{
                type.clear()
                type.add(clickedChip?.text.toString())
            }

        }


        binding.submitIssue.setOnClickThrottleBounceListener{
            binding.issueBody.gone()
            binding.progressbar.visible()
            if (type.isEmpty()){
                binding.issueBody.visible()
                binding.progressbar.gone()
                toast("Issue Type is required")
            }
            else if (binding.title.text?.trim().isNullOrBlank()){
                binding.issueBody.visible()
                binding.progressbar.gone()
                toast("Title is required")
            }
            else if (binding.desc.text?.trim().isNullOrBlank()){
                binding.issueBody.visible()
                binding.progressbar.gone()
                toast("Description is required")
            }
            else{
                repository.uploadIssueImage(bitmap, "Ncso2v1").observe(viewLifecycleOwner) { result ->
                    when(result){
                        is ServerResult.Failure -> {
                            toast("Something went wrong")
                            startActivity(Intent(requireContext(),MainActivity::class.java))
                        }
                        is ServerResult.Progress -> {
                        }
                        is ServerResult.Success -> {
                            val imgStorageReference = result.data
                            getIconDownloadUrl(imgStorageReference)
                        }
                    }
                }
            }

        }
    }


    private fun getIconDownloadUrl(imageRef: StorageReference)  {
        binding.issueBody.gone()
        binding.progressbar.visible()
        repository.getProjectIssueUrl(imageRef).observe(viewLifecycleOwner) { result ->

            when(result){
                is ServerResult.Failure -> {
                    toast("Something went wrong")
                    startActivity(Intent(requireContext(),MainActivity::class.java))
                }
                ServerResult.Progress -> {
                }
                is ServerResult.Success -> {
                    postIssue(result.data)
                }
            }
        }
    }
    private fun postIssue(url:String){
        binding.issueBody.gone()
        binding.progressbar.visible()
        val image="![Image]($url)"
        val desc="$image ${binding.desc.text?.trim().toString()}"
        val id=generateTaskID("Ncso2v1")
        val task= Task(
            title = binding.title.text?.trim().toString(),
            description = desc,
            id = id,
            difficulty = 1,
            priority = 1,
            status = 1,
            assignee = "None",
            assigner = PrefManager.getCurrentUserEmail(),
            duration = "6 Hours",
            time_STAMP = Timestamp.now(),
            tags = emptyList(),
            project_ID = "Ncso2v1",
            segment = "Issues",
            section = "Found 'em",
            type = if (type[0]== "Bug Found \uD83D\uDC1E") 1 else 3,
            moderators = emptyList(),
            last_updated = Timestamp.now()
        )
        val list:MutableList<CheckList> = mutableListOf()
        if (binding.desc.text?.trim().toString().length>=200){
            list.add(CheckList(id = RandomIDGenerator.generateRandomTaskId(5),
                title = task.title, desc = binding.desc.text?.trim().toString().substring(0,200), done = false, index = 0))
        }
        else{
            list.add(CheckList(id = RandomIDGenerator.generateRandomTaskId(5),
                title = task.title, desc = binding.desc.text?.trim().toString(), done = false, index = 0))
        }

        CoroutineScope(Dispatchers.Main).launch {

            repository.postTask(task, list) { result ->

                when (result) {

                    is ServerResult.Failure -> {
                        toast("Something went wrong")
                        startActivity(Intent(requireContext(),MainActivity::class.java))
                    }

                    ServerResult.Progress -> {
                    }

                    is ServerResult.Success -> {
                        binding.issueBody.visible()
                        binding.progressbar.gone()
                        toast("Issue Submitted")
                        startActivity(Intent(requireContext(),MainActivity::class.java))
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


}