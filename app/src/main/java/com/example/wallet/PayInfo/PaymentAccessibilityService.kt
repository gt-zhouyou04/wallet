package com.example.wallet.PayInfo

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.wallet.R

class PaymentAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())

    // 创建前台服务通知
    private fun startForegroundService() {
        val channelId = "PaymentAccessibilityServiceChannel"
        val channelName = "Payment Accessibility Service"

        Log.d("PaymentAccessibilityService", "Starting foreground service")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("PaymentAccessibilityService", "Creating notification channel for Android O and above")
            val chan = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
            Log.d("PaymentAccessibilityService", "Notification channel created")
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 替换为你的通知图标
            .setContentTitle("记账服务正在运行")
            .setContentText("点击来打开应用")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        Log.d("PaymentAccessibilityService", "Notification built, starting foreground service")

        startForeground(1, notification)
        Log.d("PaymentAccessibilityService", "Foreground service started")
    }


    override fun onCreate() {
        super.onCreate()
        Log.d("PaymentAccessibilityService", "Service Created")
    }

    //todo：待优化，当前为了防止过一段时间在后台就不工作的问题，需在设备上设置省电策略为不限制，最好将其设为前台任务
    override fun onServiceConnected() {
        super.onServiceConnected()
//        startForegroundService()
        Log.d("PaymentAccessibilityService", "Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d("PaymentAccessibilityService", "Event: ${AccessibilityEvent.eventTypeToString(event.eventType)}")

        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Log.d("PaymentAccessibilityService", "Notification state changed detected")
            val notification = event.parcelableData as? Notification
            notification?.let {
                val packageName = event.packageName.toString()
                Log.d("PaymentAccessibilityService", "Package: $packageName")

                // 展开通知栏
                performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)

                // 延时处理，等待通知栏展开后进行内容读取
                handler.postDelayed({
                    readNotificationContents(packageName)
                }, 100) // 延时2秒进行读取，可根据需要调整
            }
        }
    }

    override fun onInterrupt() {}

    private fun readNotificationContents(packageName: String) {
        val rootNode = rootInActiveWindow ?: return
        Log.d("PaymentAccessibilityService", "Reading notification contents")

        // 遍历根节点，将文本信息传递给处理函数
        traverseNodeForText(rootNode, packageName)

        // 收起通知栏
        handler.postDelayed({
            performGlobalAction(GLOBAL_ACTION_BACK)
        }, 1) // 延时1秒进行收起，可根据需要调整
    }

    private fun traverseNodeForText(node: AccessibilityNodeInfo, packageName: String) {
        if (node.childCount == 0) {
            node.text?.let {
                Log.d("PaymentAccessibilityService", "Node Text: $it")

                if (packageName == "com.tencent.mm" && (it.contains("微信支付") || it.contains("微信转账"))) {
                    showDialog(it.toString())
                } else if (packageName == "com.eg.android.AlipayGphone" && (it.contains("支出") || it.contains("支付宝转账"))) {
                    showDialog(it.toString())
                }
            }
        } else {
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { traverseNodeForText(it, packageName) }
            }
        }
    }

    private fun showDialog(notificationText: String) {
        handler.post {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("支付信息")
                .setMessage(notificationText)
                .setPositiveButton("确定") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false) // 设置点击弹窗外部不消失
                .create()

            alertDialog.setCanceledOnTouchOutside(false) // 明确设置点击外部不消失

            alertDialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            alertDialog.show()
        }
    }

}
