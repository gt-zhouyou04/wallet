package com.example.wallet.PayInfo

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.AlertDialog
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.WindowManager

class PaymentAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        Log.d("PaymentAccessibilityService", "Service Created")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
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
                }, 2000) // 延时2秒进行读取，可根据需要调整
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
        }, 1000) // 延时1秒进行收起，可根据需要调整
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
                .create()

            alertDialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            alertDialog.show()
        }
    }
}
