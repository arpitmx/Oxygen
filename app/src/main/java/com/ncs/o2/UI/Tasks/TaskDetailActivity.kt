package com.ncs.o2.UI.Tasks

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import com.ncs.o2.Services.ApiClient
import com.ncs.o2.Utility.ExtensionsUtil.setOnClickBounceListener
import com.ncs.o2.Utility.ExtensionsUtil.setSingleClickListener
import com.ncs.o2.Utility.ExtensionsUtil.snackbar
import com.ncs.o2.databinding.ActivityTaskDetailBinding
import retrofit2.Call
import retrofit2.Response


class TaskDetailActivity : AppCompatActivity() {

    private val binding: ActivityTaskDetailBinding by lazy {
        ActivityTaskDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        

        val fcmtokenMI = "cmMiVraYTkyhLGCWh8aorx:APA91bGe-6OkspkpxE9-fpxsOwslGHAlwRxG45gbeg2dxY6MckcpS-PnOl1TQvOVaZ9E90VFtWCBw3qftKJS2DkdYCEgqgGrWxRrjnsbIz4SD0j40oeLbC3OfXRe9ebC38-2xoLMDjmN"
        val fcmtokenEmulator = "es4CBA66TeCyvGXjAwNnOi:APA91bFMqBwGPNp-g__CEw3EHSAQabLrVwnTBJnU-zYL1_5t_qnIZX06t96xkoXtBm7m1ZZzyqHzsOlv2-WgMEhnYHLCCJM5x7n8cnoAJb9Em3m5HCWd8t-ueocKX1cpfUopmmL1TVan"

        binding.requestButton.setOnClickBounceListener{
            val payload = buildNotificationPayload(fcmtokenMI)

            ApiClient.getApiService().sendNotification(payload).enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        binding.titleTv.snackbar("Request Sent")
                        binding.requestButton.text= "Wait for the response..."
                        binding.requestButton.isClickable=false

                    } else {
                        binding.titleTv.snackbar("Request Failed, ${response.code()}")

                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    binding.titleTv.snackbar("Request Failed, Retry")
                }
            })




        }




        binding.taskStatus.setOnClickBounceListener{}
        binding.duration.setOnClickBounceListener {}
        binding.difficulty.setOnClickBounceListener {}
        binding.tagLayout.setOnClickBounceListener { }

        binding.gioActionbar.btnBack.setOnClickBounceListener{
            onBackPressed()
        }


    }

    private fun buildNotificationPayload(token:String): JsonObject? {
        // compose notification json payload
        val payload = JsonObject()
        payload.addProperty("to", token)
        // compose data payload here
        val data = JsonObject()
        data.addProperty("title", "Work request")
        data.addProperty("body", "Armax wants to work on #1234")
        // add data payload
        payload.add("data", data)
        return payload
    }
}

