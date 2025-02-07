package com.example.wallet.Activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.wallet.R

class TagSelectionActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag_selection)

        // 从Intent获取传递的信息
        val notificationText = intent.getStringExtra("notificationText")

        // 设置按钮的点击事件
        findViewById<Button>(R.id.tag_payment).setOnClickListener {
            onTagSelected("支付", notificationText)
        }
        findViewById<Button>(R.id.tag_transfer).setOnClickListener {
            onTagSelected("转账", notificationText)
        }
    }

    private fun onTagSelected(tag: String, notificationText: String?) {
        // 这里处理标签的选择
        Toast.makeText(this, "选择了标签: $tag 信息: $notificationText", Toast.LENGTH_SHORT).show()
        Log.d("TagSelectionActivity", "选择了标签: $tag 信息: $notificationText")
        finish()
    }
}