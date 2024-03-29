package com.ncs.o2.UI.CreateTask

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.ncs.o2.Domain.Models.CheckList
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performShakeHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.NetworkChangeReceiver
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.HelperClasses.ShakeDetector
import com.ncs.o2.R
import com.ncs.o2.UI.Report.ShakeDetectedActivity
//import com.ncs.o2.UI.Tasks.TaskPage.Chat.ExampleGrammarLocator
import com.ncs.o2.UI.UIComponents.Adapters.CheckListAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.CheckListBottomSheet
import com.ncs.o2.databinding.ActivityChecklistBinding
import com.ncs.versa.Constants.Endpoints
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.data.DataUriSchemeHandler
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.syntax.Prism4jThemeDarkula
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


@AndroidEntryPoint
class ChecklistActivity : AppCompatActivity(),CheckListBottomSheet.checkListItemListener,CheckListAdapter.CheckListItemListener,
    NetworkChangeReceiver.NetworkChangeCallback {

    private val binding: ActivityChecklistBinding by lazy {
        ActivityChecklistBinding.inflate(layoutInflater)
    }
    private val checkList_rv: RecyclerView by lazy {
        binding.checkListRecyclerview
    }
    private lateinit var listener: checkListListener

    private lateinit var shakeDetector: ShakeDetector

    private var list:MutableList<CheckList> = mutableListOf()
    private lateinit var checkListAdapter:CheckListAdapter
    @Inject
    lateinit var utils : GlobalUtils.EasyElements
    private val networkChangeReceiver = NetworkChangeReceiver(this,this)
    private val intentFilter by lazy{
        IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        registerReceiver(true)


        listener = ListenerHolder.checkListListener!!
        val dataList = intent.getSerializableExtra("checkListArray") as ArrayList<CheckList>?
        list.addAll(dataList!!)
        initViews()
    }
    private fun initViews(){

        list.distinctBy { it.id }

        if (list.isEmpty()){
            binding.checkListRecyclerview.gone()
            binding.noCheckList.visible()
            binding.addText.text="Add a new checkList"
        }

        if (list.isNotEmpty()){
            binding.checkListRecyclerview.visible()
            binding.noCheckList.gone()
            binding.addText.text="Add More"
            setCheckListRecyclerView(list)
        }

        binding.addCheckList.setOnClickThrottleBounceListener{
            val number=list.size+1
            val checkListBottomSheet = CheckListBottomSheet(count = number,this, CheckList(id = ""))
            checkListBottomSheet.show(supportFragmentManager, "checkList")
        }
        binding.gioActionbar.btnNext.setOnClickListener {
            listener.sendcheckListarray(list)
            finish()
        }

        binding.gioActionbar.btnCross.setOnClickListener {
            finish()
        }
    }
    private var receiverRegistered = false

    fun registerReceiver(flag : Boolean){
        if (flag){
            if (!receiverRegistered) {
                registerReceiver(networkChangeReceiver,intentFilter)
                receiverRegistered = true
            }
        }else{
            if (receiverRegistered){
                unregisterReceiver(networkChangeReceiver)
                receiverRegistered = false
            }
        }
    }
    private fun setCheckListRecyclerView(list: MutableList<CheckList>) {

        checkListAdapter = CheckListAdapter(list = list,markwon= markwon,this,true)
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        with(checkList_rv) {
            this.layoutManager = layoutManager
            adapter = checkListAdapter
            edgeEffectFactory = BounceEdgeEffectFactory()
        }
    }

    override fun checkListItem(checkList: CheckList, isEdited: Boolean,position: Int) {
        val existingIndex = list.indexOfFirst { it.id == checkList.id }

        if (existingIndex != -1) {
            list[existingIndex] = checkList
        } else {
            list.add(checkList)
        }

        initViews()
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
        if (PrefManager.getShakePref()){
            initShake()
            shakeDetector.registerListener()
        }
    }
    interface checkListListener{
        fun sendcheckListarray(list: MutableList<CheckList>)
    }

    object ListenerHolder {
        var checkListListener: checkListListener? = null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    // TODO: Include prism 4j in production
    private val markwon: Markwon by lazy {
//        val prism4j = Prism4j(ExampleGrammarLocator())
        val activity = this
        Markwon.builder(activity)
            .usePlugin(ImagesPlugin.create())
            .usePlugin(GlideImagesPlugin.create(activity))
            .usePlugin(TablePlugin.create(activity))
            .usePlugin(TaskListPlugin.create(activity))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
//            .usePlugin(SyntaxHighlightPlugin.create(prism4j, Prism4jThemeDarkula.create()))

            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configure(registry: MarkwonPlugin.Registry) {
                    registry.require(ImagesPlugin::class.java) { imagesPlugin ->
                        imagesPlugin.addSchemeHandler(DataUriSchemeHandler.create())
                    }
                }
            })
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        .blockQuoteColor(resources.getColor(R.color.primary))
                        .linkColor(resources.getColor(R.color.primary))
                        .codeBlockTextSize(30)
                }
            })

            .build()
    }

    override fun removeCheckList(position: Int) {
        list.removeAt(position)
        toast("Removed")
        initViews()
    }

    override fun onClick(position: Int) {
        val number=position+1
        val checkListBottomSheet = CheckListBottomSheet(count = number,this, checkList = list[position])
        checkListBottomSheet.show(supportFragmentManager, "checkList")
    }

    override fun onCheckBoxClick(id: String, isChecked: Boolean, position: Int) {

    }
    override fun onStart() {
        super.onStart()
        registerReceiver(true)
    }

    override fun onStop() {
        super.onStop()
        registerReceiver(false)
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onPause() {
        super.onPause()
        registerReceiver(false)
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        registerReceiver(false)
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onOnlineModePositiveSelected() {
        PrefManager.setAppMode(Endpoints.ONLINE_MODE)
        utils.restartApp()
    }

    override fun onOfflineModePositiveSelected() {
        startActivity(intent)
        PrefManager.setAppMode(Endpoints.OFFLINE_MODE)
    }

    override fun onOfflineModeNegativeSelected() {
        networkChangeReceiver.retryNetworkCheck()
    }


    private fun initShake(){
        val shakePref=PrefManager.getShakePref()
        Log.d("shakePref",shakePref.toString())
        if (shakePref){

            val sensi=PrefManager.getShakeSensitivity()
            when(sensi){
                1->{
                    shakeDetector = ShakeDetector(this, Endpoints.defaultLightSensi,onShake = {
                        performShakeHapticFeedback()
                        takeScreenshot(this)
                    })
                }
                2->{
                    shakeDetector = ShakeDetector(this, Endpoints.defaultMediumSensi,onShake = {
                        performShakeHapticFeedback()
                        takeScreenshot(this)
                    })
                }
                3->{
                    shakeDetector = ShakeDetector(this, Endpoints.defaultHeavySensi,onShake = {
                        performShakeHapticFeedback()
                        takeScreenshot(this)
                    })
                }
            }
        }
    }

    fun takeScreenshot(activity: Activity) {
        Log.e("takeScreenshot", activity.localClassName)
        val rootView = activity.window.decorView.rootView
        rootView.isDrawingCacheEnabled = true
        val bitmap = rootView.drawingCache
        val currentTime = Timestamp.now().seconds
        val filename = "screenshot_$currentTime.png"
        val internalStorageDir = activity.filesDir
        val file = File(internalStorageDir, filename)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            rootView.isDrawingCacheEnabled = false
            moveToReport(filename)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun moveToReport(filename: String) {
        val intent = Intent(this, ShakeDetectedActivity::class.java)
        intent.putExtra("filename", filename)
        intent.putExtra("type","report")
        startActivity(intent)
    }

    fun moveToShakeSettings() {
        val intent = Intent(this, ShakeDetectedActivity::class.java)
        intent.putExtra("type","settings")
        startActivity(intent)
    }




}