package com.example.wallet.PayInfo

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.wallet.R

class PaymentNotificationListenerService : NotificationListenerService() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        Log.d("PaymentNotificationListenerService", "Service Created")

        // 创建前台服务通知
        val channelId = "PaymentNotificationListenerServiceChannel"
        val channelName = "Payment Notification Listener Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 替换为你的通知图标
            .setContentTitle("记账服务正在运行")
            .setContentText("点击来打开应用")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        startForeground(1, notification)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("PaymentNotificationListenerService", "Listener Connected")

        // 检查并请求 OVERLAY 权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("PaymentNotificationListenerService", "Listener Disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notification = sbn.notification
        val extras = notification.extras
        val notificationContent = extras.getString("android.text") ?: ""

        Log.d("PaymentNotificationListenerService", "Package: $packageName, Content: $notificationContent")

        // 检查 notification 的 packageName，提前结束不必要的逻辑处理
        if (packageName != "com.tencent.mm" && packageName != "com.eg.android.AlipayGphone") {
            return
        }

        // 处理微信和支付宝通知内容
        if (packageName == "com.tencent.mm" && (notificationContent.contains("微信支付") || notificationContent.contains("微信转账"))) {
            showDialog(notificationContent)
        } else if (packageName == "com.eg.android.AlipayGphone" && (notificationContent.contains("支出") || notificationContent.contains("支付宝转账"))) {
            showDialog(notificationContent)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
    }

    private fun showDialog(notificationText: String) {
        handler.post {
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val dialogView = inflater.inflate(R.layout.activity_tag_selection, null)

            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false) // 设置点击弹窗外部不消失
                .create()

            alertDialog.setCanceledOnTouchOutside(false) // 明确设置点击外部不消失

            // 判断 Android 版本，并设置适当的窗口类型
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                alertDialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            } else {
                alertDialog.window?.setType(WindowManager.LayoutParams.TYPE_PHONE)
            }

            val buttonPay = dialogView.findViewById<Button>(R.id.tag_payment)
            val buttonTransfer = dialogView.findViewById<Button>(R.id.tag_transfer)

            buttonPay.setOnClickListener {
                Toast.makeText(this, "支付", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }

            buttonTransfer.setOnClickListener {
                Toast.makeText(this, "转账", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }

            alertDialog.show()
        }
    }
}
