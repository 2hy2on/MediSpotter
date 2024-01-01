package ddwucom.mobile.medispotter

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MyBroadcastReceiver : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        val msg = intent?.getStringExtra("MSG")
        val reqCode = intent?.getIntExtra("reqCode", 0)
        val hospitalName = intent?.getStringExtra("hospitalName")

        Log.d("MyBroadcastReceiver", "Received: ${msg}")
        val pendingIntent: PendingIntent?
                = intent?.let {
            PendingIntent.getBroadcast(context, 0,
                it, PendingIntent.FLAG_IMMUTABLE)
        }

        var builder = context?.let {
            NotificationCompat.Builder(it, "MY_CHANNEL2")
                .setSmallIcon(R.drawable.baseline_local_hospital_24)
                .setContentTitle("예약")
                .setContentText("${hospitalName} 예약 시간입니다.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        }

        val notiManager = context?.let { NotificationManagerCompat.from(it) }
        if (builder != null) {
            if (notiManager != null) {
                if (reqCode != null) {
                    notiManager.notify( reqCode, builder.build())
                }
            }
        }
    }

}