package com.ncs.o2.UI.CreateTask

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.text.getSpans
import androidx.core.view.isNotEmpty
import com.google.ai.client.generativeai.GenerativeModel
import com.ncs.o2.BuildConfig
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickSingleTimeBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.R
import com.ncs.o2.databinding.ActivityDescriptionEditorBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class DescriptionEditorActivity : AppCompatActivity() {


    val AUTOCOMPLETE_TEMPLATE: String = "!{[  ]}!"
    val NO_PROMPT_SELECTION: String = "NO_PROMPT_SELECTION_XYZ_0000"

    private val binding: ActivityDescriptionEditorBinding by lazy {
        ActivityDescriptionEditorBinding.inflate(layoutInflater)
    }
    private lateinit var generativeModel: GenerativeModel
    private val utils by lazy {
        GlobalUtils.EasyElements(this)
    }

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

    private fun setUpViews() {

        binding.gioActionbar.titleTv.text = getString(R.string.summary)
        binding.gioActionbar.btnDone.visible()
        binding.gioActionbar.btnFav.gone()
        binding.gioActionbar.btnRequestWork.gone()
        binding.gioActionbar.btnBack.setOnClickThrottleBounceListener {
            onBackPressed()
        }
        val summary = intent.getStringExtra("summary")
        if (summary.isNull){
            binding.summaryEt.append("What is kotlin")
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

    }

    val regex = Regex("!\\{\\[(.*?)\\]\\}!")
    val TAG = "DescriptionEditorActivity"


    private suspend fun O2Write(prompt: String, position: Int) {

        val filteredPrompt =
            prompt + "\nMake sure you keep it as simple, short and precise as possible"

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val output = CoroutineScope(Dispatchers.IO).async {
                    generativeModel.generateContent(filteredPrompt)
                }.await()
                val lastPos = output.text?.length
                binding.summaryEt.text.insert(position,output.text)

                //binding.summaryEt.text.clearSpans()
                //binding.summaryEt.setSelection(position, lastPos!!)

            } catch (e: Exception) {
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

                utils.showSnackbar(binding.root, "No prompts found", 3000)

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