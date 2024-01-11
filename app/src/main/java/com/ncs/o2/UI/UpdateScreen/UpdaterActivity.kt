package com.ncs.o2.UI.UpdateScreen


import android.Manifest.permission.REQUEST_INSTALL_PACKAGES
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.ncs.o2.Domain.Models.Update
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.getVersionName
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.popInfinity
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotateInfinity
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.databinding.ActivityUpdaterBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class UpdaterActivity @Inject constructor() : AppCompatActivity() {

    private val TAG = "UpdaterActivity"
    private lateinit var APK_URL: String
    private  var updateGlo: Update? = null

    private val PERMISSION_REQUEST_INSTALL_PACKAGES = REQUEST_INSTALL_PACKAGES
    private val PERMISSION_REQUEST_WRITE_STORAGE = WRITE_EXTERNAL_STORAGE
    private val permissions: Array<String> = arrayOf(PERMISSION_REQUEST_INSTALL_PACKAGES, PERMISSION_REQUEST_WRITE_STORAGE)
    private val RC_INSTALL_PACKAGE_PERM = 123


    private val binding: ActivityUpdaterBinding by lazy {
        ActivityUpdaterBinding.inflate(layoutInflater)
    }
    private val viewModel: UpdaterScreenViewModel by lazy {
        ViewModelProvider(this)[UpdaterScreenViewModel::class.java]
    }

    private val util: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpScreen()

        val cacheUri = PrefManager.getDownloadedUpdateUri()
        Timber.tag(TAG).d("Cache uri : ${cacheUri}")

        if (cacheUri.toString() == "null") {
            initialView()
        } else {
            fileDownloadedView(true, cacheUri)
        }
    }

    private fun setUpScreen(){
        val update = intent.getSerializableExtra("UPDATE") as? Update

        if (update!= null){

            "Version ${getVersionName(this)}".also { binding.versionCodeCurrent.text = it }
            "| v${update.VERSION_NAME}".also { binding.versionTitle.text = it }
            "Download Size : ${update.DOWNLOAD_SIZE}".also { binding.textViewDownloadSize.text = it }
            binding.textViewReleaseNotes.text = update.RELEASE_NOTES
            APK_URL = update.UPDATE_URL

            updateGlo = update

        } else {
            util.singleBtnDialog("Problem","Problem in parsing update", "Okay") { finishAffinity() }
            updateGlo = null
        }
    }
    private fun requestRuntimePermissionsAndProceed() {

        if (ActivityCompat.checkSelfPermission(
                this,
                PERMISSION_REQUEST_INSTALL_PACKAGES
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            toast("Permissions have been granted")
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                PERMISSION_REQUEST_INSTALL_PACKAGES
            )
        ) {
            val dialog: GlobalUtils.EasyElements = GlobalUtils.EasyElements(this)

            dialog.singleBtnDialog(
                title = getString(R.string.allow_o2_to_install_updates),
                msg = getString(R.string.o2_requires_install_package_permission_to_install_the_updates_please_allow_to_proceed_with_update),
                btnText = getString(R.string.proceed),
                positive = {
                    ActivityCompat.requestPermissions(this, permissions, RC_INSTALL_PACKAGE_PERM)
                })
        } else {
            ActivityCompat.requestPermissions(this, permissions, RC_INSTALL_PACKAGE_PERM)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RC_INSTALL_PACKAGE_PERM) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toast("Permission has been granted")

            } else if (!shouldShowRequestPermissionRationale(PERMISSION_REQUEST_INSTALL_PACKAGES)) {

                val dialog: GlobalUtils.EasyElements = GlobalUtils.EasyElements(this)

                dialog.singleBtnDialog(
                    title = getString(R.string.allow_o2_to_install_updates),
                    msg = getString(R.string.o2_requires_install_package_permission_to_install_the_updates_please_allow_to_proceed_with_update),
                    btnText = getString(R.string.proceed),
                    positive = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:com.ncs.o2")
                        )
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    })

            } else {
                ActivityCompat.requestPermissions(this, permissions, RC_INSTALL_PACKAGE_PERM)
            }
        }
    }

    private fun initialView() {

        binding.installLayout.gone()
        binding.buttonDownload.visible()
        binding.progressBar.progress = 0
        binding.progressBarLayout.gone()

        setAnimationLottie(0)
        binding.lottieAnimationView.pauseAnimation()

        binding.lottieAnimationView.clearAnimation()
        binding.buttonDownload.setOnClickListener {
            if (packageManager.canRequestPackageInstalls()) {
                startDownloadProcess(APK_URL)
            } else {
                requestRuntimePermissionsAndProceed()
            }
        }
    }

    // Download process
    private fun startDownloadProcess(APK_URL: String) {

        Timber.tag(TAG).d("Updater running...")

        with(viewModel) {

            binding.progressBarLayout.visible()
            binding.progressBar.isIndeterminate = true
            binding.buttonDownload.gone()
            binding.lottieAnimationView.playAnimation()

            Handler(Looper.getMainLooper()).postDelayed({

                binding.lottieAnimationView.popInfinity(this@UpdaterActivity)
                binding.lottieAnimationView.rotateInfinity(this@UpdaterActivity)

                binding.progressBar.isIndeterminate = false
                startUpdater(APK_URL)
                downloadStatus.observe(this@UpdaterActivity) { status ->
                    handleDownloadStatus(status)
                }
            }, 3000)

        }

    }

    private fun handleDownloadStatus(downloadStatus: DownloadStatus) {

        val progress = downloadStatus.progress
        val status = downloadStatus.status

        binding.progressBar.progress = progress

        when (status) {
            DownloadManager.STATUS_FAILED -> {

                initialView()
                viewModel.resetValues()
                util.showSnackbar(binding.root, "Update Failed..", 500)
            }

            DownloadManager.STATUS_PAUSED -> {
                binding.progressBar.progress = progress
                util.showSnackbar(binding.root, "Update Paused..", 500)
            }

            DownloadManager.STATUS_PENDING -> {
                binding.progressBar.progress = progress
                //util.showSnackbar(binding.root, "Download Pending..", 500)
            }

            DownloadManager.STATUS_RUNNING -> {
                binding.progressBar.progress = progress
                "${progress}%".also { binding.progressTxt.text = it }
            }

            DownloadManager.STATUS_SUCCESSFUL -> {
                fileDownloadedView(false)
                Timber.tag(TAG).d("Download finished")
            }
        }
    }

    private fun fileDownloadedView(usingCacheUri: Boolean, cacheUri: Uri? = null) {

        binding.progressBarLayout.gone()
        binding.buttonDownload.gone()
        binding.installLayout.visible()
        setAnimationLottie(1)
        util.showSnackbar(binding.root, "Package Downloaded..", 500)

        binding.manualInstall.setOnClickListener {
            if (updateGlo!=null){
                openWebsiteInBrowser(this,updateGlo!!.UPDATE_URL)
            }
        }

        if (usingCacheUri) {
            binding.buttonInstall.setOnClickListener {
                installApk(this@UpdaterActivity, cacheUri)
            }
        } else {
            PrefManager.setDownloadedUpdateUri(viewModel.downloadURI)
            Timber.tag(TAG).d("View model uri : ${viewModel.downloadURI.path}")
            binding.buttonInstall.setOnClickListener {
                installApk(this@UpdaterActivity, viewModel.downloadURI)
            }
        }
    }
    private fun setAnimationLottie(value: Int) {
        if (value == 1) {
            binding.lottieAnimationView.setAnimation(R.raw.doneanim)
            binding.lottieAnimationView.clearAnimation()
            binding.lottieAnimationView.popInfinity(this)
            binding.lottieAnimationView.scaleX = 0.5F
            binding.lottieAnimationView.scaleY = 0.5F
        } else {
            binding.lottieAnimationView.setAnimation(R.raw.update)
            binding.lottieAnimationView.scaleX = 1.3F
            binding.lottieAnimationView.scaleY = 1.3F
        }
    }


    fun openWebsiteInBrowser(context: Context, websiteUrl: String) {
        val websiteUri: Uri = Uri.parse(websiteUrl)
        val intent = Intent(Intent.ACTION_VIEW, websiteUri)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
           toast("No browser found..")
        }
    }


    fun uriToFile(context: Context, uri: Uri): File? {
        val inputStream: InputStream?
        val outputFile: File

        try {
            inputStream = context.contentResolver.openInputStream(uri)

            val outputDir = context.cacheDir
            outputFile = File.createTempFile("temp", null, outputDir)

            val outputStream = FileOutputStream(outputFile)
            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            return outputFile
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }
    private fun installApk(context: Context?, uri: Uri?) {

        try {
            if (uri != null) {

                Timber.tag("Download Manager").d("Installing...")
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val packageManager: PackageManager = context!!.packageManager
                if (intent.resolveActivity(packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    Timber.tag("Download Manager").d("No application found to install the app")
                    util.showSnackbar(binding.root, "No application found to install the app", 1000)
                }
            } else {
                util.showSnackbar(binding.root, "Download failed : Invalid uri", 1000)
            }
        } catch (e: Exception) {

            util.showActionSnackbar(binding.root,"Problem in parsing update",10000,"Re-download") {
               initialView()
            }

            Timber.tag(TAG).e(e)

        }
    }
}