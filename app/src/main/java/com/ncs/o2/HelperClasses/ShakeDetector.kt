package com.ncs.o2.HelperClasses

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.google.firebase.Timestamp

class ShakeDetector(
    context: Context,
    private val sensitivity: Float,
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastTime: Long = 0
    private var lastUpdate: Long = 0
    private var lastX: Float = 0.0f
    private var lastY: Float = 0.0f
    private var lastZ: Float = 0.0f

    private var cooldownTime: Long = 0
    private val cooldownInterval = 5000

    init {
        registerListener()
    }

    fun registerListener() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun unregisterListener() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            val timeDifference = currentTime - lastTime

            if (timeDifference > SHAKE_THRESHOLD_INTERVAL) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val deltaX = x - lastX
                val deltaY = y - lastY
                val deltaZ = z - lastZ

                val speed = Math.sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()).toFloat()

                if (speed > sensitivity && !isInCooldown()) {
                    onShake.invoke()
                    setCooldown()
                }

                lastTime = currentTime
                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    private fun isInCooldown(): Boolean {
        val currentTime = Timestamp.now().seconds
        return currentTime - cooldownTime < cooldownInterval
    }

    private fun setCooldown() {
        cooldownTime = Timestamp.now().seconds
    }

    companion object {
        private const val SHAKE_THRESHOLD_INTERVAL = 1000
    }
}
