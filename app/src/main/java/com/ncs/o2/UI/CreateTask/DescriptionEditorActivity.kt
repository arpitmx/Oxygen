package com.ncs.o2.UI.CreateTask

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import br.tiagohm.markdownview.css.InternalStyleSheet
import br.tiagohm.markdownview.css.styles.Github
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.storage.StorageReference
import com.ncs.o2.BuildConfig
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.progressGone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.progressGoneSlide
import com.ncs.o2.Domain.Utility.ExtensionsUtil.progressVisible
import com.ncs.o2.Domain.Utility.ExtensionsUtil.progressVisibleSlide
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickSingleTimeBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.Details.CodeViewerActivity
import com.ncs.o2.UI.Tasks.TaskPage.Details.ImageAdapter
import com.ncs.o2.UI.Tasks.TaskPage.Details.ImageViewerActivity
import com.ncs.o2.UI.createProject.createProjectViewModel
import com.ncs.o2.databinding.ActivityDescriptionEditorBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream

@AndroidEntryPoint
class DescriptionEditorActivity : AppCompatActivity(),ImageAdapter.ImagesListner {


    val AUTOCOMPLETE_TEMPLATE: String = "!{[  ]}!"
    val NO_PROMPT_SELECTION: String = "NO_PROMPT_SELECTION_XYZ_0000"

    private val binding: ActivityDescriptionEditorBinding by lazy {
        ActivityDescriptionEditorBinding.inflate(layoutInflater)
    }
    private lateinit var generativeModel: GenerativeModel
    private val utils by lazy {
        GlobalUtils.EasyElements(this)
    }
    private val viewModel: CreateTaskViewModel by viewModels()

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private var bitmap: Bitmap? = null

    private val issueTemplate = "" +
            "**Description:**\n" +
            "[Provide a detailed description of the issue. Include any relevant background information, steps to reproduce, and expected vs actual behavior.]\n" +
            "\n" +
            "**Steps to Reproduce:**\n" +
            "1. [Step 1]\n" +
            "2. [Step 2]\n" +
            "3. [Step 3]\n" +
            "   ...\n" +
            "\n" +
            "**Expected Result:**\n" +
            "[Clearly describe what you expected to happen.]\n" +
            "\n" +
            "**Actual Result:**\n" +
            "[Describe what actually happened. Include any error messages or unexpected behavior.]\n" +
            "\n" +
            "**Screenshots / Attachments:**\n" +
            "[If applicable, include screenshots or attachments that help illustrate the issue.]\n" +
            "\n" +
            "**Additional Information:**\n" +
            "[Any additional context, information, or configuration details that might be relevant.]\n" +
            "\n" +
            "**Related Issues:**\n" +
            "[List any related issues or dependencies.]\n" +
            "\n" +
            "**Notes for Reviewers:**\n" +
            "\n"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val summary = intent.getStringExtra("summary")
        if (!summary.isNull){
            binding.summaryEt.setText(summary)
        }

        setUpViews()
        setUpGemini()
    }

    private fun setUpGemini() {
        generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    private fun showProg(flag: Boolean) {
        if (flag) {
            binding.gemProg.visible()
        } else {
            binding.gemProg.gone()
        }
    }


    private fun toggleProgress(show: Boolean){
        if (show){
            binding.gioActionbar.summaryProgressbar.progressVisible(this,500)
            binding.summaryEt.isEnabled = false
        }else{
            binding.gioActionbar.summaryProgressbar.progressGone(this,500)
            binding.summaryEt.isEnabled = true
        }
    }

    private fun setUpViews() {
        binding.btnPreview.visible()
        toggleProgress(false)

        binding.gioActionbar.titleTv.text = getString(R.string.summary)
        binding.gioActionbar.btnDone.visible()
        binding.gioActionbar.btnBack.setOnClickThrottleBounceListener {
            onBackPressed()
        }
        val summary = intent.getStringExtra("summary")
        if (summary.isNull){
            binding.summaryEt.setText(issueTemplate)
        }
        binding.btnWrite.setOnClickThrottleBounceListener {
            CoroutineScope(Dispatchers.Main).launch {
                startAutoCompleteProcess()
            }


            binding.btnAutoBlock.setOnClickThrottleBounceListener {
                binding.summaryEt.text.insert(
                    binding.summaryEt.selectionStart,
                    " ${AUTOCOMPLETE_TEMPLATE} "
                )
            }
        }

        binding.btnCodeBlock.setOnClickThrottleBounceListener {

            binding.summaryEt.text.insert(
                binding.summaryEt.selectionStart,
                " \n ``` Code_Lang \n Code \n``` "
            )

        }

        binding.btnChecklist.setOnClickThrottleBounceListener {
            binding.summaryEt.text.insert(
                binding.summaryEt.selectionStart,
                " \n - [ ] List_Text "
            )

        }

        binding.btnLink.setOnClickThrottleBounceListener {

            binding.summaryEt.text.insert(
                binding.summaryEt.selectionStart,
                " [Link Text](Link URL) "
            )

        }

        binding.btnImg.setOnClickThrottleBounceListener {
            pickImage()
        }
        binding.gioActionbar.btnDone.setOnClickThrottleBounceListener {
            if (binding.summaryEt.text.toString().isEmpty()) {
                utils.showSnackbar(binding.root, "Enter something in summary first", 3000)
            } else {
                val resultIntent = Intent()
                resultIntent.putExtra("summary", binding.summaryEt.text.toString())
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        binding.btnPreview.setOnClickThrottleBounceListener {
            binding.btnEdit.visible()
            setUpTaskDescription(binding.summaryEt.text.toString())
        }
        binding.btnEdit.setOnClickThrottleBounceListener {
            binding.summaryEt.visible()
            binding.markdownView.gone()
            binding.imageRecyclerView.gone()
        }
    }

    val regex = Regex("!\\{\\[(.*?)\\]\\}!")
    val TAG = "DescriptionEditorActivity"


    private suspend fun O2Write(prompt: String, position: Int) {

        val filteredPrompt = prompt + "\nMake sure you keep it as simple, short and precise as possible"

        CoroutineScope(Dispatchers.Main).launch {
            try {
                toggleProgress(true)
                val output = CoroutineScope(Dispatchers.IO).async {
                    generativeModel.generateContent(filteredPrompt)
                }.await()
                val lastPos = output.text?.length
                binding.summaryEt.text.insert(position,output.text)
                toggleProgress(false)
            } catch (e: Exception) {
                toggleProgress(false)
                withContext(Dispatchers.Main) {
                    utils.singleBtnDialog("Problem", e.message.toString(), "Okay", {})
                }
            }
        }
    }

    //                generativeModel.generateContentStream(filteredPrompt).collect { chunk ->
//                    binding.summaryEt.text.insert(lastPos, chunk.text)
//                    lastPos += chunk.text!!.length + 1
//                }

    //            generativeModel.generateContentStream(prompt).collect { chunk ->
//                binding.summaryEt.text.insert(position,chunk.text)
//            }
    private suspend fun startAutoCompleteProcess() {

        try {
            var position: Int = -1
            val prompt: String

            val startPos = binding.summaryEt.selectionStart
            val endPos = binding.summaryEt.selectionEnd

            if (startPos == endPos) {
                prompt = NO_PROMPT_SELECTION
            } else {
                prompt = binding.summaryEt.text.subSequence(startPos, endPos).toString()
                position = startPos
            }

            if (prompt == NO_PROMPT_SELECTION) {

                utils.showSnackbar(binding.root, "Select prompt text to process..", 3000)

            } else {
                Timber.tag(TAG).d("Prompt : $prompt")
                Timber.tag(TAG).d("$position")

                binding.summaryEt.text.delete(startPos, endPos)
                O2Write(prompt, position)

            }
        } catch (e: Exception) {
            utils.singleBtnDialog("Failure", e.message.toString(), "Okay", {})
        }
    }
    val script = """
    var allPreTags = document.querySelectorAll('pre');

allPreTags.forEach(function(preTag) {
  preTag.addEventListener('click', function() {
    var clickedText = preTag.textContent;
    var languageType = preTag.getAttribute('language');
    send.sendCode(clickedText, languageType);
  });
});

var allImgTags = document.querySelectorAll('img');
var imgArray = [];

allImgTags.forEach(function(imgTag) {
  if (imgTag.tagName.toLowerCase() === 'img' && imgTag.parentElement.tagName.toLowerCase() !== 'pre') {
    imgTag.addEventListener('click', function() {
      send.sendsingleImage(imgTag.src);
    });
    imgArray.push(imgTag.src);
  }
});

var allMarkdownImgTags = document.querySelectorAll('img[src^="!["]');
allMarkdownImgTags.forEach(function(markdownImgTag) {
  // Extract the image URL from the Markdown image template
  var markdownImgSrc = markdownImgTag.getAttribute('src').match(/\(([^)]+)\)/)[1];
  imgArray.push(markdownImgSrc);
});

send.sendImages(imgArray);


"""
    private fun setUpTaskDescription(description: String) {
        binding.summaryEt.gone()
        val css: InternalStyleSheet = Github()

        with(css) {
            addFontFace(
                "o2font",
                "normal",
                "normal",
                "normal",
                "url('file:///android_res/font/sfregular.ttf')"
            )
            addRule("body", "font-family:o2font")
            addRule("body", "font-size:16px")
            addRule("body", "line-height:21px")
            addRule("body", "background-color: #E3E1E1")
            addRule("body", "color: #222222")
            addRule("body", "padding: 0px 0px 0px 0px")
            addRule("a", "color: #0000FF")
            addRule("pre", "border: 1px solid #000;")
            addRule("pre", "border-radius: 4px;")
            addRule("pre", "max-height: 400px;")
            addRule("pre", "overflow:auto")
            addRule("pre", "white-space: pre-line")

        }

        binding.markdownView.settings.javaScriptEnabled = true
        binding.markdownView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        binding.markdownView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)

        binding.markdownView.addStyleSheet(css)
        binding.markdownView.addJavascriptInterface(AndroidToJsInterface(), "send")

        binding.markdownView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.evaluateJavascript(script) {}
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val intent = Intent(Intent.ACTION_VIEW, request?.url)
                startActivity(intent)
                return true
            }

        }

        binding.markdownView.loadMarkdown(description)
        binding.markdownView.visible()

    }
    inner class AndroidToJsInterface {
        @JavascriptInterface
        fun sendCode(codeText: String, language: String?) {
            this@DescriptionEditorActivity.runOnUiThread {

                val codeViewerIntent =
                    Intent(this@DescriptionEditorActivity, CodeViewerActivity::class.java)
                codeViewerIntent.putExtra(
                    Endpoints.CodeViewer.CODE,
                    codeText.trimIndent().trim()
                )
                codeViewerIntent.putExtra(
                    Endpoints.CodeViewer.LANG,
                    language?.trimIndent()?.trim()
                )

                startActivity(codeViewerIntent)
                this@DescriptionEditorActivity.overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_left
                )
            }
        }

        @JavascriptInterface
        fun sendImages(imageUrls: Array<String>) {
            this@DescriptionEditorActivity.runOnUiThread {
                val recyclerView = binding.imageRecyclerView
                recyclerView.visible()
                recyclerView.layoutManager =
                    LinearLayoutManager(
                        this@DescriptionEditorActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                Log.d("list", imageUrls.toMutableList().toString())
                val adapter =
                    ImageAdapter(imageUrls.toMutableList(), this@DescriptionEditorActivity)
                recyclerView.adapter = adapter
            }
        }

        @JavascriptInterface
        fun sendsingleImage(imageUrl: String) {
            this@DescriptionEditorActivity.runOnUiThread {
                val recyclerView = binding.imageRecyclerView
                recyclerView.visible()
                recyclerView.layoutManager =
                    LinearLayoutManager(
                        this@DescriptionEditorActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                Log.d("list", listOf(imageUrl).toMutableList().toString())
                val adapter =
                    ImageAdapter(listOf(imageUrl).toMutableList(), this@DescriptionEditorActivity)
                recyclerView.adapter = adapter
            }
        }

    }
    override fun onImageClicked(position: Int, imageList: MutableList<String>) {
        val imageViewerIntent =
            Intent(this, ImageViewerActivity::class.java)
        imageViewerIntent.putExtra("position", position)
        imageViewerIntent.putStringArrayListExtra("images", ArrayList(imageList))
        startActivity(
            ImageViewerActivity.createIntent(
                this,
                ArrayList(imageList),
                position,
            ),
        )

    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    bitmap = imageBitmap
                    uploadImageToFirebaseStorage(bitmap!!,RandomIDGenerator.generateRandomTaskId(5))

                }

                REQUEST_IMAGE_PICK -> {
                    val selectedImage = data?.data
                    bitmap = uriToBitmap(this.contentResolver, selectedImage!!)
                    uploadImageToFirebaseStorage(bitmap!!,RandomIDGenerator.generateRandomTaskId(5))

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
    private fun uploadImageToFirebaseStorage(bitmap: Bitmap, taskID:String) {

        viewModel.uploadImagethroughRepository(bitmap, taskID).observe(this) { result ->

            when(result){
                is ServerResult.Failure -> {
                    binding.progressBar.gone()
                    utils.singleBtnDialog_InputError("Upload Errors",
                        "There was an issue in uploading the image, ${result.exception.message},\n\nplease retry",
                        "Retry"
                    ) {
                    }
                }
                is ServerResult.Progress -> {
                    binding.progressBar.visible()
                }
                is ServerResult.Success -> {
                    binding.progressBar.gone()
                    val imgStorageReference = result.data
                    getImageDownloadUrl(imgStorageReference)
                }
            }
        }
    }
    private fun getImageDownloadUrl(imageRef: StorageReference) {

        viewModel.getImageUrlThroughRepository(imageRef).observe(this) { result ->

            when(result){
                is ServerResult.Failure -> {
                    binding.progressBar.gone()
                    utils.singleBtnDialog_InputError("Upload Errors",
                        "There was an issue in uploading the image, ${result.exception.message},\n\nplease retry",
                        "Retry"
                    ) {
                        imageRef.delete()
                    }
                }
                ServerResult.Progress -> {
                    binding.progressBar.visible()

                }
                is ServerResult.Success -> {
                    binding.progressBar.gone()
                    binding.summaryEt.text.insert(
                        binding.summaryEt.selectionStart,
                        "\n ![image!](${result.data}) \n "
                    )
                }
            }
        }
    }


}


//
//val matchRegex = regex.find(fullTxt)
//val group = matchRegex?.groups?.get(1)?.value
//var tempStr : String?
//
//group.let {prompt ->
//
//    val startIndex = matchRegex?.range?.start!!
//    val endIndex = matchRegex.range.last + 1
//
//    Timber.tag(TAG).d("Pos : Start-> ${startIndex}, End->${endIndex}")
//    Timber.tag(TAG).d("String : ${fullTxt.substring(startIndex,endIndex)}")
//    position = startIndex
//    withContext(Dispatchers.Main){
//        binding.summaryEt.text.delete(startIndex,endIndex)
//    }
//    tempStr = prompt
//}

//                 utils.showSnackbar(binding.root,"No Prompt to write about..",5000)

//
//CoroutineScope(Dispatchers.Main).launch {
//    generativeModel.generateContentStream(prompt).collect { chunk ->
//        binding.summaryEt.append(chunk.text)
//    }
//}