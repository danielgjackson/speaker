package dev.danjackson.speaker

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

const val tag: String = "BackgroundSound"
const val channelId: String = "BackgroundSoundChannelId"

class BackgroundSound : Service() {

    private fun getNotification(): Notification {
        val cid =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(channelId, tag, NotificationManager.IMPORTANCE_LOW)
                notificationChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_SECRET
                val service = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                service.createNotificationChannel(notificationChannel)
                channelId
            } else {
                ""
            }

        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(Intent(this@BackgroundSound, MainActivity::class.java))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            } else {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, cid)
        return notificationBuilder.setOngoing(true)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            //.setTicker(getText(R.string.notification_ticker_text))
            .setSmallIcon(R.drawable.ic_surround_sound)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    // --- service (re-)started ---

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val start = intent?.getBooleanExtra("start", false)
        //println("BACKGROUND-SOUND: onStartCommand() start=$start")
        if (start == true) {
            startForeground(1, getNotification())
            Monitor.getInstance(this.applicationContext).mediaStart()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Monitor.getInstance(this.applicationContext).mediaStop()
        super.onDestroy()
    }

}
