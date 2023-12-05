package com.funprimetechnology.app.se

import android.app.*
import android.content.*
import android.hardware.*
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.funprimetechnology.app.R
import com.funprimetechnology.app.utils.SessionManager
import kotlin.math.sqrt

class MotionService : Service(), SensorEventListener {
    private var ringtone: Ringtone? = null
    private var alarmActive: Boolean = false
    companion object {
        const val MOTION_CHANNEL_ID = "MotionServiceChannel"
        const val MOTION_NOTIFICATION_ID = 3
    }

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var isMotionDetected = false
    private var unlockReceiver: BroadcastReceiver? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create the notification channel
        createNotificationChannel(MOTION_CHANNEL_ID, "Motion Service Channel")

        // Show the service status notification
        showServiceStatusNotification(
            MOTION_NOTIFICATION_ID,
            MOTION_CHANNEL_ID,
            "Motion Service",
            "Motion Service is Running"
        )

        // Get the sensor manager and accelerometer sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Register the accelerometer sensor listener
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_USER_PRESENT) {
                    Log.e("MotionService", "Phone unlocked!", )
//                    Toast.makeText(context, "Phone unlocked!", Toast.LENGTH_SHORT).show()
                    val sessionManager : SessionManager = SessionManager(applicationContext)
                    if (sessionManager.fetchAlarmState() == true) {
                        isMotionDetected = false
                        if (ringtone != null) {
                            ringtone?.stop()
                        }
                        sessionManager.saveAlarmState(false)
                    }
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(unlockReceiver, filter)

        // Return the super.onStartCommand method
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the accelerometer sensor listener when the service is destroyed
        Log.e("MotionService", "onDestroy called", )
        sensorManager?.unregisterListener(this)
        unregisterReceiver(unlockReceiver)

//        if (MainActivity.result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
        val sessionManager : SessionManager = SessionManager(applicationContext)
        if (sessionManager.fetchAlarmState() == true) {
            if (ringtone != null) {
                Log.e("MotionService", "ringtone is not null",)
                ringtone?.stop()
            }
            sessionManager.saveAlarmState(false)
        }
//            MainActivity.audioManager?.abandonAudioFocusRequest(MainActivity.audioFocusRequest)
//        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Get the acceleration values
        val x = event?.values?.get(0) ?: 0f
        val y = event?.values?.get(1) ?: 0f
        val z = event?.values?.get(2) ?: 0f

        // Calculate the acceleration magnitude
        val magnitude = sqrt((x * x + y * y + z * z).toDouble())

        // Check if the magnitude is greater than a certain threshold
        if (magnitude > 20) {
            // Play the alarm siren if motion is detected
            if (!isMotionDetected) {
//                if (MainActivity.result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    val sessionManager : SessionManager = SessionManager(applicationContext)
                    if (sessionManager.fetchAlarmState() == false) {
                        Log.e("MotionService", "isMotionDetected is false",)
                        playAlarmSiren()
                        isMotionDetected = true
                    }
//                }
            }
        } else {
            isMotionDetected = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    private fun createNotificationChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
    }

    private fun showServiceStatusNotification(notificationId: Int, channelId: String, title: String, message: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .build()
        startForeground(notificationId, notification)
    }

    private fun playAlarmSiren() {
        val sessionManager : SessionManager = SessionManager(applicationContext)
        sessionManager.saveAlarmState(true)

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone?.isLooping = true
        }
        ringtone?.play()
    }

    private fun stopAlarmSiren() {
        if (ringtone != null) {
            ringtone?.stop()
        }
    }

}




//class MotionService : Service() {
//
//    private var isServiceRunning = false
//    private var motionNotificationId = 1002
//    private var motionChannelId = "Motion Service Channel"
//    private var alarmMediaPlayer: MediaPlayer? = null
//    private val motionSensor: Sensor by lazy {
//        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//    }
//
//    private val screenOffReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            stopAlarm()
//        }
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        if (intent?.action == "START") {
//            startMotionService()
//        } else {
//            stopMotionService()
//        }
//        return START_NOT_STICKY
//    }
//
//    private fun startMotionService() {
//        if (!isServiceRunning) {
//            isServiceRunning = true
//            showServiceStatusNotification(motionNotificationId, motionChannelId, "Motion Service", "Motion Service is Running")
//            registerScreenOffReceiver()
//            startMotionSensor()
//        }
//    }
//
//    private fun stopMotionService() {
//        if (isServiceRunning) {
//            isServiceRunning = false
//            unregisterScreenOffReceiver()
//            stopMotionSensor()
//            stopAlarm()
//            stopForeground(true)
//        }
//    }
//
//    private fun startMotionSensor() {
//        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        sensorManager.registerListener(motionSensorEventListener, motionSensor, SensorManager.SENSOR_DELAY_NORMAL)
//    }
//
//    private fun stopMotionSensor() {
//        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        sensorManager.unregisterListener(motionSensorEventListener)
//    }
//
//    private val motionSensorEventListener = object : SensorEventListener {
//        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//
//        override fun onSensorChanged(event: SensorEvent?) {
//            event?.let {
//                val x = it.values[0]
//                val y = it.values[1]
//                val z = it.values[2]
//                val acceleration = sqrt(x * x + y * y + z * z)
//                if (acceleration > 15.0f) {
//                    playAlarmSiren()
//                }
//            }
//        }
//    }
//
//    private fun showServiceStatusNotification(notificationId: Int, channelId: String, title: String, message: String) {
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setContentTitle(title)
//            .setContentText(message)
//            .setSmallIcon(R.drawable.ic_notification)
//            .build()
//        startForeground(notificationId, notification)
//    }
//
//    private fun playAlarmSiren() {
//        if (alarmMediaPlayer == null) {
//            alarmMediaPlayer = MediaPlayer.create(this, R.raw.alarm_notification).apply {
//                alarmMediaPlayer?.isLooping = true
//                alarmMediaPlayer?.start()
//            }
//        }
//    }
//
//    private fun stopAlarm() {
//        if (alarmMediaPlayer != null) {
//            alarmMediaPlayer?.stop()
//            alarmMediaPlayer?.release()
//            alarmMediaPlayer = null
//        }
//    }
//
//    private fun registerScreenOffReceiver() {
//        val intentFilter = IntentFilter().apply {
//            addAction(Intent.ACTION_SCREEN_OFF)
//        }
//        registerReceiver(screenOffReceiver, intentFilter)
//    }
//
//    private fun unregisterScreenOffReceiver() {
//        unregisterReceiver(screenOffReceiver)
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//}



//class MotionService : Service(), SensorEventListener {
//
//    private lateinit var sensorManager: SensorManager
//    private lateinit var accelerometer: Sensor
//
//    private var alarmPlaying = false
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//
//        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        super.onStartCommand(intent, flags, startId)
//
//        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
//
//        showServiceStatusNotification(
//            motionNotificationId,
//            motionChannelId,
//            "Motion Service",
//            "Motion Service is Running"
//        )
//
//        startForeground(motionNotificationId, createForegroundNotification())
//
//        return START_STICKY
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//
//        sensorManager.unregisterListener(this)
//        stopForeground(true)
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//
//    override fun onSensorChanged(event: SensorEvent?) {
//        val x = event?.values?.get(0) ?: 0f
//        val y = event?.values?.get(1) ?: 0f
//        val z = event?.values?.get(2) ?: 0f
//
//        val acceleration = sqrt(x * x + y * y + z * z)
//
//        if (acceleration > threshold && !alarmPlaying) {
//            playAlarmSiren()
//            alarmPlaying = true
//        }
//    }
//
//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//        // Stop alarm when lock screen is activated
//        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
//        filter.addAction(Intent.ACTION_SCREEN_OFF)
//        val receiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
//                    stopAlarmSiren()
//                    alarmPlaying =



//class MotionDetectionActivity : AppCompatActivity(), SensorEventListener {
//
//    private lateinit var sensorManager: SensorManager
//    private lateinit var accelerometer: Sensor
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // initialize sensor manager and accelerometer sensor
//        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//    }
//
//    override fun onStart() {
//        super.onStart()
//
//        // register sensor listener when activity starts
//        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
//    }
//
//    override fun onStop() {
//        super.onStop()
//
//        // unregister sensor listener when activity stops
//        sensorManager.unregisterListener(this)
//    }
//
//    override fun onSensorChanged(event: SensorEvent) {
//        // detect motion and play alarm siren if necessary
//        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
//            val x = event.values[0]
//            val y = event.values[1]
//            val z = event.values[2]
//            val acceleration = sqrt(x * x + y * y + z * z)
//            if (acceleration > threshold) {
//                playAlarmSiren()
//            }
//        }
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
//        // do nothing
//    }
//
//    override fun onPause() {
//        super.onPause()
//
//        // send broadcast to MotionService when the screen is turned off
//        if (isScreenTurningOff()) {
//            val intent = Intent(this, MotionService::class.java)
//            intent.action = MotionService.ACTION_STOP_ALARM
//            startService(intent)
//        }
//    }
//
//    private fun isScreenTurningOff(): Boolean {
//        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
//        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH &&
//                powerManager.isInteractive.not()
//    }
//}