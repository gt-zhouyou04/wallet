package com.example.wallet.Activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wallet.PayInfo.RecordManager.RecordAdapter
import com.example.wallet.CustomManager.TagAdapter
import com.example.wallet.CustomManager.TagViewModel
import com.example.wallet.PayInfo.RecordManager.RecordViewModel
import com.example.wallet.R

class MainActivity : AppCompatActivity() {
    private lateinit var tagViewModel: TagViewModel
    private lateinit var recordViewModel: RecordViewModel
    private lateinit var tagAdapter: TagAdapter
    private lateinit var recordAdapter: RecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 获取ViewModel
        tagViewModel = ViewModelProvider(this).get(TagViewModel::class.java)
        recordViewModel = ViewModelProvider(this).get(RecordViewModel::class.java)

        // 初始化Tag RecyclerView
        tagAdapter = TagAdapter(listOf()) { id -> tagViewModel.delete(id) }
        findViewById<RecyclerView>(R.id.recyclerViewTags).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = tagAdapter
        }

        // 初始化Record RecyclerView
        recordAdapter = RecordAdapter(listOf(), tagViewModel, this)
        findViewById<RecyclerView>(R.id.recyclerViewRecords).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = recordAdapter
        }

        // 观察标签数据
        tagViewModel.allTags.observe(this, Observer { tags ->
            tags?.let { tagAdapter.setTags(it) }
        })

        // 观察支出记录数据
        recordViewModel.records.observe(this, Observer { records ->
            records?.let { recordAdapter.setRecords(it) }
        })

        //todo: 添加打开显示悬浮窗权限的代码——android.permission.SYSTEM_ALERT_WINDOW
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}
