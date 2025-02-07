package com.example.wallet.PayInfo.RecordManager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.wallet.CustomManager.TagViewModel
import com.example.wallet.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.wallet.CustomManager.Record

class RecordAdapter(
    private var records: List<Record>,
    private val tagViewModel: TagViewModel,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]
        holder.bind(record, tagViewModel, lifecycleOwner)
    }

    override fun getItemCount(): Int {
        return records.size
    }

    fun setRecords(newRecords: List<Record>) {
        records = newRecords
        notifyDataSetChanged()
    }

    class RecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val amountTextView: TextView = view.findViewById(R.id.textViewAmount)
        private val timestampTextView: TextView = view.findViewById(R.id.textViewTimestamp)
        private val tagTextView: TextView = view.findViewById(R.id.textViewTag)

        fun bind(record: Record, tagViewModel: TagViewModel, lifecycleOwner: LifecycleOwner) {
            amountTextView.text = "金额: ${record.amount}"
            timestampTextView.text = "日期: ${convertTimestampToDate(record.timestamp)}"

            // 观察标签名变化
            tagViewModel.getTagNameById(record.tagId).observe(lifecycleOwner, Observer { tagName ->
                tagTextView.text = "标签: ${tagName ?: "未知标签"}"
            })
        }

        private fun convertTimestampToDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = Date(timestamp)
            return sdf.format(date)
        }
    }
}
