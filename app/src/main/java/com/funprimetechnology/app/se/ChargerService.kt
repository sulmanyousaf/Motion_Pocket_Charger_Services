package com.funprimetechnology.app.se

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.funprimetechnology.app.R
import com.funprimetechnology.app.utils.SessionManager


class ChargerService : Service() {
    var ringtone: Ringtone? = null
    var alarmActive: Boolean = false

    companion object {
        const val CHARGER_CHANNEL_ID = "ChargerServiceChannel"
        const val CHARGER_NOTIFICATION_ID = 2
    }

    private var chargerBroadcastReceiver: BroadcastReceiver? = null
//    private var lockScreenReceiver: BroadcastReceiver? = null
    private var unlockReceiver: BroadcastReceiver? = null

    private lateinit var notificationManager: NotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create the notification channel
        createNotificationChannel(CHARGER_CHANNEL_ID, "Charger Service Channel")

        // Show the service status notification
        showServiceStatusNotification(
            CHARGER_NOTIFICATION_ID,
            CHARGER_CHANNEL_ID,
            "Charger Service",
            "Charger Service is Running"
        )

        // Register the charger broadcast receiver
        chargerBroadcastReceiver = object : BroadcastReceiver() {
            //            override fun onReceive(context: Context?, intent: Intent?) {
//                if (intent?.action == Intent.ACTION_POWER_DISCONNECTED) {
//                    // Play the alarm siren when the charger is disconnected
//                    playAlarmSiren()
//                }
//            }
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_POWER_DISCONNECTED) {
                    if (!alarmActive) {
                        val sessionManager : SessionManager = SessionManager(applicationContext)
                        if (sessionManager.fetchAlarmState() == false) {
                            alarmActive = true
                            Log.e("ChargerService", "start sound", )
                            playAlarmSiren()
                        }
                    }
                } else if (intent?.action == Intent.ACTION_POWER_CONNECTED) {
                    val sessionManager: SessionManager = SessionManager(applicationContext)
                    if (sessionManager.fetchAlarmState() == true) {
                        if (alarmActive) {
                            alarmActive = false
                            Log.e("ChargerService", "stop sound",)
                            Toast.makeText(applicationContext, "stop sound", Toast.LENGTH_SHORT)
                                .show()
                            stopAlarmSiren()
                        }
                    }
                }
            }
        }
        registerReceiver(chargerBroadcastReceiver, IntentFilter(Intent.ACTION_POWER_DISCONNECTED))
        registerReceiver(chargerBroadcastReceiver, IntentFilter(Intent.ACTION_POWER_CONNECTED))

        // Register the lockScreen broadcast receiver
//        lockScreenReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
//                    // Device has been locked
//                    Log.e("ChargerService", "ACTION_SCREEN_OFF", )
//                } else if (intent?.action == Intent.ACTION_SCREEN_ON) {
//                    // Device has been unlocked
//                    Log.e("ChargerService", "ACTION_SCREEN_ON", )
//                }
//            }
//        }
//        val lockScreenFilter = IntentFilter()
//        lockScreenFilter.addAction(Intent.ACTION_SCREEN_OFF)
//        lockScreenFilter.addAction(Intent.ACTION_SCREEN_ON)
//        registerReceiver(lockScreenReceiver, lockScreenFilter)

        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_USER_PRESENT) {
                    Log.e("ChargerService", "Phone unlocked!", )
//                    Toast.makeText(context, "Phone unlocked!", Toast.LENGTH_SHORT).show()
                    val sessionManager : SessionManager = SessionManager(applicationContext)
                    if (sessionManager.fetchAlarmState() == true) {
                        if (alarmActive) {
                            alarmActive = false
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
        // Unregister the charger broadcast receiver when the service is destroyed
        unregisterReceiver(chargerBroadcastReceiver)
//        unregisterReceiver(lockScreenReceiver)
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

    private fun showServiceStatusNotification(
        notificationId: Int,
        channelId: String,
        title: String,
        message: String
    ) {
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
//        val intent = Intent(this, LockScreenService::class.java)
//        startService(intent)
    }
    private fun stopAlarmSiren() {
        val sessionManager : SessionManager = SessionManager(applicationContext)
        sessionManager.saveAlarmState(false)
        if (ringtone != null) {
            ringtone?.stop()
        }
    }
}