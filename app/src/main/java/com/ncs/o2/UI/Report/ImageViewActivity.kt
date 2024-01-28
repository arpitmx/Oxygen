package com.ncs.o2.UI.Report

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.igreenwood.loupe.Loupe
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.R
import com.ncs.o2.databinding.ActivityImageViewBinding

class ImageViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val byteArray = intent.getByteArrayExtra("bitmap")
        if (byteArray != null) {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            binding.image.setImageBitmap(bitmap)
        }
        val loupe=Loupe.create(binding.image, binding.container) {
            onViewTranslateListener = object : Loupe.OnViewTranslateListener {

                override fun onStart(view: ImageView) {
                }

                override fun onViewTranslate(view: ImageView, amount: Float) {
                }

                override fun onRestore(view: ImageView) {
                }

                override fun onDismiss(view: ImageView) {
                    finish()
                }
            }
        }
        binding.btnBack.setOnClickThrottleBounceListener{
            finish()
        }

    }

}