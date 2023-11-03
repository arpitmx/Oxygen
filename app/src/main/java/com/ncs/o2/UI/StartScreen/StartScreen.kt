package com.ncs.o2.UI.StartScreen

import android.R.attr.duration
import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.fadeIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.ncs.o2.Constants.TestingConfig
import com.ncs.o2.Domain.Utility.ExtensionsUtil.blink
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotateInfinity
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.AuthScreenActivity
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.databinding.ActivitySplashScreenBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StartScreen : AppCompatActivity() {


    private val util: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this@StartScreen)
    }

    private val binding: ActivitySplashScreenBinding by lazy {
        ActivitySplashScreenBinding.inflate(layoutInflater)
    }

    companion object {
        val DELAY = 500L
        val DELAY_ACTIVITY_START = 0L
    }

    private lateinit var ball: ImageView
    private lateinit var valueAnimator: ValueAnimator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setBallAnimator()
        setUpViews(TestingConfig.isTesting)

    }

    lateinit var scaleAnimation:ScaleAnimation

    private fun setBallTouchAnimator() {

        ball = binding.ball
        ball.rotateInfinity(this)
        val maxsize = 12f
        val change = 3f

        ball.setOnClickThrottleBounceListener{

            this.performHapticFeedback()
            val scale = ball.scaleX

            scaleAnimation = ScaleAnimation(
                scale,
                scale + change,
                scale,
                scale + change,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )
            ball.scaleX = scale+change
            ball.scaleY = scale+change

            scaleAnimation.duration = 200
            scaleAnimation.fillAfter = true
            ball.startAnimation(scaleAnimation)
            ball.rotateInfinity(this)

            // Check if the target scale has been reached

            // Check if the target scale has been reached
            if (ball.getScaleX() >= maxsize) {
                // Start the new activity
                startMainActivity()
            }
        }

//        valueAnimator = ValueAnimator.ofFloat(1f, maxsize)
//        valueAnimator.setDuration(600L)
//
//        valueAnimator.addUpdateListener {
//
//            val scale = valueAnimator.animatedValue as Float
//            ball.setScaleX(scale)
//            ball.setScaleY(scale)
//
//        }
//
//        valueAnimator.addListener(object : Animator.AnimatorListener {
//            override fun onAnimationStart(animator: Animator) {
//            }
//
//            override fun onAnimationEnd(animator: Animator) {
//                startMainActivity()
//            }
//
//            override fun onAnimationCancel(animator: Animator) {
//            }
//
//            override fun onAnimationRepeat(animator: Animator) {
//            }
//        })
    }

    private fun setBallAnimator() {

        ball = binding.ball
        ball.rotateInfinity(this)
        val maxsize = 15f

        valueAnimator = ValueAnimator.ofFloat(1f, maxsize)
        valueAnimator.setDuration(400L)

        valueAnimator.addUpdateListener {

            val scale = valueAnimator.animatedValue as Float
            ball.setScaleX(scale)
            ball.setScaleY(scale)

        }

        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
            }

            override fun onAnimationEnd(animator: Animator) {
                startMainActivity()
            }

            override fun onAnimationCancel(animator: Animator) {
            }

            override fun onAnimationRepeat(animator: Animator) {
            }
        })
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(
            R.anim.fadein,
            R.anim.fadeout
        )

        finishAffinity()
    }

    private fun setUpViews(isTesting: Boolean) {

        if (isTesting) {

            startActivity(Intent(this, TestingConfig.activity))
            finishAffinity()


        } else {


            if (FirebaseAuth.getInstance().currentUser != null) {


                FirebaseFirestore.getInstance().collection("Users")
                    .document(FirebaseAuth.getInstance().currentUser?.email!!)
                    .get(Source.SERVER)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val document = task.result
                            if (document != null && document.exists()) {
                                val isDetailsAdded = document.getBoolean("DETAILS_ADDED")
                                val isPhotoAdded = document.getBoolean("PHOTO_ADDED")

                                if (isDetailsAdded == true && isPhotoAdded == true) {
                                    Handler(Looper.getMainLooper()).postDelayed(
                                        {
                                            ball.animation.cancel()
                                            ball.post {
                                                valueAnimator.start()
                                            }
                                        }, DELAY
                                    )


                                } else if (isDetailsAdded == false) {
                                    val intent = Intent(this, AuthScreenActivity::class.java)
                                    intent.putExtra("isDetailsAdded", "false")
                                    intent.putExtra("showchooser", "false")
                                    startActivity(intent)
                                    finishAffinity()
                                } else if (isPhotoAdded == false) {
                                    val intent = Intent(this, AuthScreenActivity::class.java)
                                    intent.putExtra("isPhotoAdded", "false")
                                    intent.putExtra("showchooser", "false")
                                    startActivity(intent)
                                    finishAffinity()
                                }
                            }
                        } else {

                            val exception = task.exception
                            exception?.printStackTrace()
                            Toast.makeText(this, "Error in loading", Toast.LENGTH_SHORT).show()

                            util.singleBtnDialog_ErrorConnection(
                                "Failure in loading",
                                "Check your internet connection and try again. ",
                                "Okay"
                            ) {
                                finishAffinity()
                            }
                        }
                    }


            } else {
                Handler(Looper.myLooper()!!).postDelayed({
                    val intent = Intent(this, AuthScreenActivity::class.java)
                    intent.putExtra("isDetailsAdded", "false")
                    intent.putExtra("isPhotoAdded", "false")
                    intent.putExtra("showchooser", "true")
                    startActivity(intent)
                    finishAffinity()
                }, DELAY)

            }
        }


    }
}