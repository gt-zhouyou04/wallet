package com.example.wallet.PayInfo

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.wallet.Activity.TagSelectionActivity

class PaymentAccessibilityService : AccessibilityService() {
    override fun onCreate() {
        super.onCreate()
        Log.d("PaymentAccessibilityService", "Service Created")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("PaymentAccessibilityService", "Service Connected")
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d("PaymentAccessibilityService", "Event triggered: ${AccessibilityEvent.eventTypeToString(event.eventType)}")

        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            val notification = event.parcelableData as? Notification
            notification?.let {
                val packageName = event.packageName.toString()
                val extras = it.extras
                for (key in extras.keySet()) {
                    val value = extras.get(key);
                    Log.d("PaymentAccessibilityService", "Key: $key, Value: $value")
                }
                val notificationText = notification.extras.getString(Notification.EXTRA_TEXT)
                Log.d("PaymentAccessibilityService", "Package: $packageName, Notification: $notificationText")

                notificationText?.let {
                    if (packageName == "com.tencent.mm" && (notificationText.contains("微信支付") || notificationText.contains("微信转账"))) {
                        showTagSelectionDialog(notificationText)
                    } else if (packageName == "com.eg.android.AlipayGphone" && (notificationText.contains("支出") || notificationText.contains("支付宝转账"))) {
                        showTagSelectionDialog(notificationText)
                    }
                }
            }
        }
    }


    override fun onInterrupt() {
    }

    private fun showTagSelectionDialog(notificationText: String) {
        val intent = Intent(this, TagSelectionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("notificationText", notificationText)
        }
        startActivity(intent)
    }
}