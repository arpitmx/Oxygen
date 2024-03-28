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
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.Details.CodeViewerActivity
import com.ncs.o2.databinding.ActivityNewChangesBinding
import com.ncs.versa.Constants.Endpoints

class NewChanges : AppCompatActivity() {

    private lateinit var binding: ActivityNewChangesBinding
    var type:String?=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewChangesBinding.inflate(layoutInflater)
        type=intent.getStringExtra("type")

        if (type=="popup"){
            PrefManager.setPopUpVisibility(false)
        }


        setViews()
        val desc="""
            ## Release Notes [24.2.24] - [24/02/2024]

            <div style="width:100%;height:0;padding-bottom:83%;position:relative;"><iframe src="https://giphy.com/embed/o75ajIFH0QnQC3nCeD" width="100%" height="100%" style="position:absolute" frameBorder="0" class="giphy-embed" allowFullScreen></iframe></div><p><a href="https://giphy.com/gifs/theoffice-o75ajIFH0QnQC3nCeD"></a></p>
            
            ## Minor bug fixes, general UI and performance improvements.

            ### Enhancements

            - Task Drafting Enhancements: Improvements in saving the tasks as drafts in case of unexpected failures so that the progress is not lost.

            - Improvements for moderators: Now easily keep track of the tasks you are moderating, UI improvements.

            - Checklists creation: Moderators can add more checklists after the task has been created.

            - Edit task summary: Task summary can now be edited after the task has been created.

            - **Saturday Update:**
              1. **Team Viewer:**
                  1. Member count [resolved].
                  2. Username addition [resolved].
              2. **Bug Fixes:**
                  1. **Channels:**
                      1. Moderation tags were visible on images [resolved].
                      2. Mention users duplication resolved [resolved].
                  2. **Link Preview:**
                      1. Regex not matching some types of links [resolved].
                  3. Login related issues fixed.
              3. **Task Archiving:**
                  1. Tasks can be archived now.
              4. Possible bug fixes to resolved crashes.


            ### Performance Improvements

            - Improved opening of the screens and reduced load times.

            ### User Interface Changes

            - Revamped UI of user's workspace.

            - Made general improvements in UX.

            ### How to Update

            - Latest Version of O2 is 24.2.24, it can be downloaded from the release page, find version 24.2.15 at [Release Page](https://github.com/arpitmx/Oxygen/releases)

            ### Feedback

            - Feedback is the key to enhancing user experience, it is our appeal to the users for providing any constructive feedback about any issues faced by them.


        """.trimIndent()
        setUpTaskDescription(desc)

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



    }
    override fun onBackPressed() {
        super.onBackPressed()
        if (type=="popup"){
            startActivity(Intent(this@NewChanges, MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.slide_out_right)
        }
        else{
            overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.slide_out_right)
            finish()
        }

    }
}