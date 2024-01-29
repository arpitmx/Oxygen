package com.ncs.o2.HelperClasses

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ShakeDetector(
    context: Context,
    sensitivity: Float,
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var mThresholdAcceleration:Float = 0.0f
    private var mSensorBundles :ArrayList<SensorBundle>
    private val INTERVAL = 500
    private val mLock: Any = Any()

    init {
        mThresholdAcceleration=sensitivity
        mSensorBundles=ArrayList()
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

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        val sensorBundle = SensorBundle(
            sensorEvent.values[0],
            sensorEvent.values[1],
            sensorEvent.values[2],
            sensorEvent.timestamp
        )

        synchronized(mLock) {
            if (mSensorBundles.size == 0) {
                mSensorBundles.add(sensorBundle)
            }
            else if (sensorBundle.timestamp - mSensorBundles[mSensorBundles.size - 1].timestamp > INTERVAL) {
                mSensorBundles.add(sensorBundle)
            } else {

            }
        }

        performCheck()
    }

    private fun performCheck() {
        synchronized(mLock) {
            val vector = intArrayOf(0, 0, 0)
            val matrix = arrayOf(
                intArrayOf(0, 0),
                intArrayOf(0, 0),
                intArrayOf(0, 0)
            )

            for (sensorBundle in mSensorBundles) {
                if (sensorBundle.xAcc > mThresholdAcceleration && vector[0] < 1) {
                    vector[0] = 1
                    matrix[0][0]++
                }
                if (sensorBundle.xAcc < -mThresholdAcceleration && vector[0] > -1) {
                    vector[0] = -1
                    matrix[0][1]++
                }
                if (sensorBundle.yAcc > mThresholdAcceleration && vector[1] < 1) {
                    vector[1] = 1
                    matrix[1][0]++
                }
                if (sensorBundle.yAcc < -mThresholdAcceleration && vector[1] > -1) {
                    vector[1] = -1
                    matrix[1][1]++
                }
                if (sensorBundle.zAcc > mThresholdAcceleration && vector[2] < 1) {
                    vector[2] = 1
                    matrix[2][0]++
                }
                if (sensorBundle.zAcc < -mThresholdAcceleration && vector[2] > -1) {
                    vector[2] = -1
                    matrix[2][1]++
                }
            }

            for (axis in matrix) {
                for (direction in axis) {
                    if (direction < 3) {
                        return
                    }
                }
            }

            onShake.invoke()
            mSensorBundles.clear()
        }
    }

}
private data class SensorBundle(
    val xAcc: Float,
    val yAcc: Float,
    val zAcc: Float,
    val timestamp: Long
)

