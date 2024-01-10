package com.ncs.o2.UI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import br.tiagohm.markdownview.css.InternalStyleSheet
import br.tiagohm.markdownview.css.styles.Github
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.invisible
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.Details.CodeViewerActivity
import com.ncs.o2.databinding.ActivityNewChangesBinding
import com.ncs.versa.Constants.Endpoints

class NewChanges : AppCompatActivity() {

    private lateinit var binding: ActivityNewChangesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewChangesBinding.inflate(layoutInflater)
        setViews()
        setUpTaskDescription(Endpoints.new_changes_des)

        binding.gioActionbar.btnClose.setOnClickThrottleBounceListener{
            onBackPressed()
        }
        setContentView(binding.root)
    }

    private fun setViews(){
        binding.gioActionbar.titleTv.text = "What's New"
        binding.gioActionbar.btnClose.visible()
        binding.gioActionbar.btnBack.invisible()
        binding.gioActionbar.btnFav.gone()
        binding.gioActionbar.btnRequestWork.gone()
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

    send.sendImages(imgArray);
    
"""

    private fun setUpTaskDescription(description: String) {

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
            addRule("body", "font-size:20px")
            addRule("body", "line-height:28px")
            addRule("body", "background-color: #222222")
            addRule("body", "color: #fff")
            addRule("body", "padding: 0px 0px 0px 0px")
            addRule("a", "color: #86ff7c")
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
//                activityViewListner.showProgressbar(false)
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
        //binding.markdownView.animFadein(requireActivity(), 500)

    }

    inner class AndroidToJsInterface {
        @JavascriptInterface
        fun sendCode(codeText: String, language: String?) {

            this@NewChanges.runOnUiThread {
                val codeViewerIntent = Intent(this@NewChanges, CodeViewerActivity::class.java)
                codeViewerIntent.putExtra(Endpoints.CodeViewer.CODE, codeText.trimIndent().trim())
                codeViewerIntent.putExtra(Endpoints.CodeViewer.LANG, language?.trimIndent()?.trim())

                startActivity(codeViewerIntent)
                this@NewChanges.overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_left
                )
            }
        }

//        @JavascriptInterface
//        fun sendImages(imageUrls: Array<String>) {
//            requireActivity().runOnUiThread {
//                val recyclerView = binding.imageRecyclerView
//                recyclerView.layoutManager =
//                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//                Log.d("list", imageUrls.toMutableList().toString())
//                val adapter = ImageAdapter(imageUrls.toMutableList(), this@TaskDetailsFragment)
//                recyclerView.adapter = adapter
//            }
//        }
//
//        @JavascriptInterface
//        fun sendsingleImage(imageUrl: String) {
//            requireActivity().runOnUiThread {
//                onImageClicked(0, mutableListOf(imageUrl))
//            }
//        }

    }
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }
}