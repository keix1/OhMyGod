package com.keix1.ohmygod

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.SoundPool
import android.os.Bundle
import android.os.PowerManager
import android.support.wearable.activity.WearableActivity
import android.widget.Button
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import kotlin.concurrent.thread


class MainActivity : WearableActivity(), SensorEventListener {

    @BindView(R.id.text)
    lateinit var textView: TextView
    lateinit var arrayAcceleration: Array<Float>
    lateinit var lock: PowerManager.WakeLock
    private var gestureFlag1: Boolean? = null
    private var gestureFlag2: Boolean? = null
    private var gestureFlag3: Boolean? = null
    lateinit var mp: MediaPlayer


    val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Enables Always-on
        setAmbientEnabled()

        ButterKnife.bind(this)
        textView.text = "hogehoge"
        arrayAcceleration = arrayOf(0.0f,0.0f,0.0f)
        gestureFlag1 = false
        gestureFlag2 = false
        gestureFlag3 = false

        val ringType = RingtoneManager.TYPE_NOTIFICATION
        val soundUri = RingtoneManager.getActualDefaultRingtoneUri(this, ringType)
        val ringtone = RingtoneManager.getRingtone(applicationContext, soundUri)

        val button = findViewById(R.id.button) as Button
        button.setOnClickListener{ ringtone.play() }

        var pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        lock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My tag")
        lock.acquire()
        mp = MediaPlayer.create(this, R.raw.mikami)



    }

    override fun onDestroy() {
        super.onDestroy()
        lock.release()

    }

    private fun gesture() {
        if(arrayAcceleration[0] < -7 && -12 < arrayAcceleration[1] && arrayAcceleration[1] < -8 && -5 < arrayAcceleration[2] && arrayAcceleration[2] < 5) {
            gestureFlag1 = true
        }
        if( gestureFlag1!! && -3 < arrayAcceleration[0] && arrayAcceleration[0] < 3 &&  arrayAcceleration[1] > -6 && -5 < arrayAcceleration[2] && arrayAcceleration[2] < 5) {
            gestureFlag2 = true
        }
        if(gestureFlag1!! && gestureFlag2!!) {
            if(arrayAcceleration[1] > 7) {
                gestureFlag1 = false
                gestureFlag2 = false
                if(mp.isPlaying()) {
                    mp.stop()
                    try {
                        mp.prepare()
                    } catch(e : Exception) {

                    }
                } else {
                    mp.start()
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        textView.text = event!!.values.zip("XYZ".toList()).fold("") { acc, pair ->
            "$acc${pair.second}: ${pair.first}\n"
        }
        arrayAcceleration[0] = event.values[0]
        arrayAcceleration[1] = event.values[1]
        arrayAcceleration[2] = event.values[2]

        gesture()
    }

}
