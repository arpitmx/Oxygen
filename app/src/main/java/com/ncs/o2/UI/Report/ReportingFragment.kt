package com.ncs.o2.UI.Report

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.ncs.o2.Domain.Utility.ExtensionsUtil.invisible
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
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

    var imageCount=0
    var imagesPosted=0
    val urls:MutableList<String> = mutableListOf()
    val bitmaps:MutableList<Bitmap> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
     var type:MutableList<String> = mutableListOf()

    private val REQUEST_IMAGE_PICK = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportingBinding.inflate(inflater, container, false)
        if (activityBinding.fileName != null) {
            val internalStorageDir = requireActivity().filesDir
            val file = File(internalStorageDir, activityBinding.fileName)
            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageCount++
                bitmaps.add(bitmap)
            }
        }
        binding.desc.requestFocus()

        setUpViews()
        setImages()
        return binding.root
    }

    private fun setImages(){


        if (bitmaps.size==0){
            binding.imagesCont.gone()
            binding.placeholder.visible()
            binding.turnOff.gone()
        }
        else{
            binding.turnOff.visible()
            binding.imagesCont.visible()
            binding.placeholder.gone()
            val size=bitmaps.size
            when(size){
                1-> {
                    binding.image1.visible()
                    binding.image1.setImageBitmap(bitmaps[0])

                    binding.image3.gone()

                    binding.image2.gone()

                }
                2->{
                    binding.image1.visible()
                    binding.image1.setImageBitmap(bitmaps[0])

                    binding.image2.visible()
                    binding.image2.setImageBitmap(bitmaps[1])

                    binding.image3.gone()
                }

                3->{
                    binding.image1.visible()
                    binding.image1.setImageBitmap(bitmaps[0])

                    binding.image2.visible()
                    binding.image2.setImageBitmap(bitmaps[1])

                    binding.image3.visible()
                    binding.image3.setImageBitmap(bitmaps[2])
                }
            }

        }

    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                REQUEST_IMAGE_PICK -> {
                    val selectedImage = data?.data
                    bitmap = uriToBitmap(activityBinding.contentResolver, selectedImage!!)!!
                    bitmaps.add(bitmap)
                    imageCount++
                    setImages()
                }
            }
        }
    }

    fun uriToBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    private fun setUpViews(){

        activityBinding.binding.gioActionbar.titleTv.text="Feedback"



        binding.deleteIc.setOnClickThrottleBounceListener {
            bitmaps.removeLast()
            imageCount--
            setImages()
        }

        binding.addImages.setOnClickThrottleBounceListener {
            if (imageCount<3){
                pickImage()
            }
            else{
                toast("At maximum only 3 images can be added")
            }
        }



        binding.shakeSettings.setOnClickThrottleBounceListener {
            val transaction = fragmentManager?.beginTransaction()!!
            val fragment = ShakePrefrencesFragment()
            transaction.replace(R.id.shake_detected_fragment_container, fragment)
            transaction.commit()
        }

        binding.image1.setOnClickThrottleBounceListener {
            val _bitmap = bitmaps[0]
            val stream = ByteArrayOutputStream()
            _bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray: ByteArray = stream.toByteArray()
            val intent = Intent(requireContext(), ImageViewActivity::class.java)
            intent.putExtra("bitmap", byteArray)
            startActivity(intent)
        }

        binding.image2.setOnClickThrottleBounceListener {
            val _bitmap = bitmaps[1]
            val stream = ByteArrayOutputStream()
            _bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray: ByteArray = stream.toByteArray()
            val intent = Intent(requireContext(), ImageViewActivity::class.java)
            intent.putExtra("bitmap", byteArray)
            startActivity(intent)
        }

        binding.image3.setOnClickThrottleBounceListener {
            val _bitmap = bitmaps[2]
            val stream = ByteArrayOutputStream()
            _bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray: ByteArray = stream.toByteArray()
            val intent = Intent(requireContext(), ImageViewActivity::class.java)
            intent.putExtra("bitmap", byteArray)
            startActivity(intent)
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
            else if (binding.desc.text?.trim().isNullOrBlank()){
                binding.issueBody.visible()
                binding.progressbar.gone()
                toast("Description is required")
            }
            else{
                if (bitmaps.isNotEmpty()){
                    for (bit in bitmaps){
                        repository.uploadIssueImage(bit, "Ncso2v1").observe(viewLifecycleOwner) { result ->
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
                else{
                    postIssue(emptyList())
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
                    if (imagesPosted!=imageCount) {
                        urls.add(result.data)
                        imagesPosted++
                    }
                    if (imagesPosted==imageCount){
                        postIssue(urls)
                    }
                }
            }
        }
    }
    private fun postIssue(urls:List<String>){
        binding.issueBody.gone()
        binding.progressbar.visible()
        var desc=binding.desc.text?.trim().toString()
        if (urls.isNotEmpty()){
            for (url in urls){
                desc="![Image](${url}) $desc"
            }
        }
        val words = binding.desc.text?.trim().toString().split(" ")
        val title = if (words.size>=10) words.take(10).joinToString(" ") else binding.desc.text?.trim().toString()
        val id=generateTaskID("Ncso2v1")
        val task= Task(
            title = title,
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
            project_ID = "Ncso2v1", //Ncso2v1
            segment = "Issues", //Issues
            section = "Found 'em", //Found 'em
            type = if (type[0]== "Bug Found \uD83D\uDC1E") 1 else 2,
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