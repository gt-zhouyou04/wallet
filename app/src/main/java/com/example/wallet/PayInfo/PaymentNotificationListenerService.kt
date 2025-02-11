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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wallet.CustomManager.AppDatabase
import com.example.wallet.CustomManager.Record
import com.example.wallet.CustomManager.RecordDao
import com.example.wallet.CustomManager.Tag
import com.example.wallet.CustomManager.TagAdapter
import com.example.wallet.CustomManager.TagDao
import com.example.wallet.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PaymentNotificationListenerService : NotificationListenerService() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var db: AppDatabase
    private lateinit var tagDao: TagDao
    private lateinit var recordDao: RecordDao

    override fun onCreate() {
        super.onCreate()
        Log.d("PaymentNotificationListenerService", "Service Created")
        db = AppDatabase.getDatabase(this)
        tagDao = db.tagDao()
        recordDao = db.recordDao()

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

    fun extractAmount(notificationText: String): Double? {
        // 定义一个正则表达式来匹配数字部分
        val regex = """\d+(\.\d+)?""".toRegex()

        // 使用正则表达式查找匹配的内容
        val matchResult = regex.find(notificationText)

        // 如果找到匹配的内容，将其转换为Double
        return matchResult?.value?.toDoubleOrNull()
    }

    private fun showDialog(notificationText: String) {
        if (notificationText.equals("")) {
            return
        }
        handler.post {
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val dialogView = inflater.inflate(R.layout.activity_tag_selection, null)
            var allTags: List<Tag> = listOf()
            var amount = extractAmount(notificationText)
            var timestamp: Long = 0
            var tagId: Int = 0
            runBlocking {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        allTags = tagDao.getAllTagsCurrent()
                        // 对获取的数据进行展示或处理
                        allTags.forEach {
                            Log.d("ExampleService", "Item: ${it.name}")
                        }
                    } catch (e: IllegalStateException) {
                        Log.e("ExampleService", "Database error: ${e.message}")
                    }
                }.join()

                val tagAdapter = TagAdapter(allTags, false) {}
                dialogView.findViewById<RecyclerView>(R.id.recyclerViewTags).apply {
                    layoutManager = LinearLayoutManager(this@PaymentNotificationListenerService)
                    adapter = tagAdapter
                }
                timestamp = System.currentTimeMillis()
                val alertDialog = AlertDialog.Builder(this@PaymentNotificationListenerService)
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
                    tagId = tagAdapter.selectedPosition + 1
                    Log.d("ExampleService-=-=-=-=", "amount: ${amount}, timeStamp: ${timestamp}, tagId: ${tagId}")

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // 在协程中调用挂起函数
                            recordDao.insert(Record(amount = amount!!, tagId = tagId, timestamp = timestamp))
                            // 添加日志输出
                            Log.d("dwdadwdajodwidoa", "Record inserted successfully")
                        } catch (e: Exception) {
                            // 捕获任何异常并记录日志
                            Log.e("dwdadwdajodwidoa", "Failed to insert record: ${e.message}", e)
                        }
                    }

                    // 检查在主线程中某些代码是否对此产生影响
                    Log.d("dwdadwdajodwidoa", "Coroutine launched")
                    alertDialog.dismiss()
                }

                buttonTransfer.setOnClickListener {
                    alertDialog.dismiss()
                }

                alertDialog.show()
            }
        }
    }
}
