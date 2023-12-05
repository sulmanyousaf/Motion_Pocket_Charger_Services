package com.funprimetechnology.app.se

import android.app.*
import android.content.*
import android.hardware.*
import android.media.*
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.funprimetechnology.app.R
import com.funprimetechnology.app.utils.SessionManager

class PocketService : Service() {
    var ringtone: Ringtone? = null
    var alarmActive: Boolean = false

    //    private val threshold: Float = 10.0f
    private val threshold: Float = 0.0f

    companion object {
        const val POCKET_CHANNEL_ID = "PocketServiceChannel"
        const val POCKET_NOTIFICATION_ID = 1
    }

    private lateinit var sensorManager: SensorManager
    private var pocketSensor: Sensor? = null
    private var pocketSensorListener: SensorEventListener? = null
    private var isPhoneInPocket = true
    private var unlockReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create the notification channel
        createNotificationChannel(POCKET_CHANNEL_ID, "Pocket Service Channel")

        // Show the service status notification
        showServiceStatusNotification(
            POCKET_NOTIFICATION_ID,
            POCKET_CHANNEL_ID,
            "Pocket Service",
            "Pocket Service is Running"
        )

        // Register the pocket sensor listener
        pocketSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        pocketSensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
                    val distance = event.values[0]
                    if (distance > threshold) {
                        // Phone is in the pocket
                        Log.e("PocketService", "Phone is in pocket")
                        Log.e("PocketService", "isPhoneInPocket: $isPhoneInPocket")
                        if (!isPhoneInPocket) {
                            isPhoneInPocket = true
                        }
                    } else {
                        // Phone is removed from the pocket
                        if (isPhoneInPocket) {
                            val sessionManager: SessionManager = SessionManager(applicationContext)
                            if (sessionManager.fetchAlarmState() == false) {
                                // Play the alarm siren when the phone is removed from the pocket for the first time
                                Log.e("PocketService", "Phone is removed from pocket")
                                Log.e("PocketService", "isPhoneInPocket: $isPhoneInPocket")
                                playAlarmSiren()
                            }
                        }
                        isPhoneInPocket = false
                    }
                }

//                val keyguardManager =
//                    getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//                if (keyguardManager.isKeyguardLocked) {
//                    if (alarmActive) {
//                        alarmActive = false
//                        stopAlarmSiren()
//                    }
//                }
            }


//            override fun onSensorChanged(event: SensorEvent?) {
//                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
//                    val x = event.values[0]
//                    val y = event.values[1]
//                    val z = event.values[2]
//
//                    val acceleration = sqrt(x * x + y * y + z * z)
//
//                    if (acceleration > threshold) {
//                        if (!alarmActive) {
//                            alarmActive = true
//                            playAlarmSiren()
//                        }
//                    } else {
//                        if (alarmActive) {
//                            alarmActive = false
//                            stopAlarmSiren()
//                        }
//                    }
//
                    // Check if the lock screen is active
//                    val keyguardManager =
//                        getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//                    if (keyguardManager.isKeyguardLocked) {
//                        if (alarmActive) {
//                            alarmActive = false
//                            stopAlarmSiren()
//                        }
//                    }
//                }
//            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(
            pocketSensorListener,
            pocketSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_USER_PRESENT) {
                    Log.e("PocketService", "Phone unlocked!", )
//                    Toast.makeText(context, "Phone unlocked!", Toast.LENGTH_SHORT).show()
                    val sessionManager : SessionManager = SessionManager(applicationContext)
                    if (sessionManager.fetchAlarmState() == true) {
                        if (!isPhoneInPocket) {
                            isPhoneInPocket = true
                        }
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
        // Unregister the pocket sensor listener when the service is destroyed
        sensorManager.unregisterListener(pocketSensorListener)
        unregisterReceiver(unlockReceiver)
        val sessionManager : SessionManager = SessionManager(applicationContext)
        if (sessionManager.fetchAlarmState() == true) {
            if (ringtone != null) {
                ringtone?.stop()
            }
            sessionManager.saveAlarmState(false)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
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
        val sessionManager: SessionManager = SessionManager(applicationContext)
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