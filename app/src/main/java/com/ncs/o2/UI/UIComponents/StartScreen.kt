package com.ncs.o2.UI.UIComponents

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.window.SplashScreen
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.ncs.o2.Constants.TestingConfig
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadeOut
import com.ncs.o2.Domain.Utility.ExtensionsUtil.bounce
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotateInfinity
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.AuthScreenActivity
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.databinding.ActivitySplashScreenBinding


class StartScreen : AppCompatActivity(){

    private val binding : ActivitySplashScreenBinding by lazy  {
        ActivitySplashScreenBinding.inflate(layoutInflater)
    }

    companion object{
        val DELAY= 0L
        val DELAY_ACTIVITY_START = 0L
    }

   private lateinit var o2logo : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        o2logo = findViewById(R.id.splashBg_startScreen)
        o2logo.rotateInfinity(this)
        setUpViews(TestingConfig.isTesting)

    }


    private fun setUpViews(isTesting: Boolean){

        if (isTesting){

            startActivity(Intent(this, TestingConfig.activity))
            finishAffinity()


        }else {


            if (FirebaseAuth.getInstance().currentUser != null) {

                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()

//                Handler(Looper.myLooper()!!).postDelayed({
//                    o2logo.animation.cancel()
//                    o2logo.animFadeOut(this, DELAY_ACTIVITY_START)
//                    Handler(Looper.myLooper()!!).postDelayed({
//                        o2logo.gone()
//
//                    }, DELAY_ACTIVITY_START)
//                }, DELAY)

            }else {
                Handler(Looper.myLooper()!!).postDelayed({
                    startActivity(Intent(this, AuthScreenActivity::class.java))
                    finishAffinity()
                }, DELAY)

            }
        }


    }
}