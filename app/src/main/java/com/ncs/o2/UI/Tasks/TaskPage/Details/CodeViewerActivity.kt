package com.ncs.o2.UI.Tasks.TaskPage.Details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.R
import com.ncs.o2.databinding.ActivityCodeViewerBinding
import com.ncs.versa.Constants.Endpoints
import io.github.kbiakov.codeview.adapters.Options
import io.github.kbiakov.codeview.highlight.ColorTheme
import io.github.kbiakov.codeview.highlight.Font


class CodeViewerActivity : AppCompatActivity() {


    lateinit var code : String

    private val binding: ActivityCodeViewerBinding by lazy {
        ActivityCodeViewerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setViews()
    }

    private fun setViews(){
        setUpCodeViewer()
        setUpActionBar()
    }

    private fun setUpCodeViewer() {
        val intent = intent
        val receivedCode = intent.getStringExtra(Endpoints.CodeViewer.CODE)

        if (receivedCode!=null){

            code = receivedCode

            binding.codeView.setOptions(Options.Default.get(this)
                .withLanguage("kotlin")
                .withCode(receivedCode)
                .withFont(Font.Consolas)
                .withTheme(ColorTheme.MONOKAI))

            binding.codeView.setupShadows(true)

        }

    }

    private fun shareCode(textToShare: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare)
        sendIntent.type = "text/plain"

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }


    private fun copyToClipboard(textToCopy: String) {
        val clipboardManager: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Copied Text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
    }


    private fun setUpActionBar() {
        binding.actionbar.btnBack.setOnClickThrottleBounceListener{
            onBackPressed()
        }

        binding.actionbar.btnShare.setOnClickThrottleBounceListener {
            shareCode(code)
        }

        binding.actionbar.btnCopy.setOnClickThrottleBounceListener {
            copyToClipboard(code)
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        finish()
    }
}


