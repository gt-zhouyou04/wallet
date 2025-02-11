package com.example.wallet.CustomManager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wallet.R

class TagAdapter(private var tags: List<Tag>, private var isVisible: Boolean = true, private val onDelete: (Int) -> Unit) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false)
        view.findViewById<Button>(R.id.buttonDelete).apply {
            visibility = if (isVisible) View.VISIBLE else View.GONE
        }
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = tags[position]
        holder.bind(tag, position, selectedPosition == position)
        holder.itemView.setOnClickListener {
            if (selectedPosition != holder.adapterPosition) {
                notifyItemChanged(selectedPosition) // 取消之前的选中状态
                selectedPosition = holder.adapterPosition
                notifyItemChanged(selectedPosition) // 更新当前选中状态
            }
            selectedPosition = position
        }
        holder.itemView.findViewById<Button>(R.id.buttonDelete).setOnClickListener {
            onDelete(tag.id)
        }
    }

    override fun getItemCount() = tags.size

    fun setTags(newTags: List<Tag>) {
        tags = newTags
        notifyDataSetChanged()
    }

    class TagViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(tag: Tag, position: Int, isSelected: Boolean) {
            itemView.findViewById<TextView>(R.id.textViewTagName).text = tag.name
            itemView.isSelected = isSelected

            if (isSelected) {
                itemView.setBackgroundColor(R.color.colorPrimary)
            } else {
                itemView.setBackgroundColor(R.color.purple_500)
            }
        }
    }

    fun updateTags(newTags: List<Tag>) {
        tags = newTags
        notifyDataSetChanged() // 通知适配器数据已更新
    }
}
