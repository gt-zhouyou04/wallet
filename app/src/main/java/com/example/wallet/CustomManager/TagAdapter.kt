package com.example.wallet.CustomManager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wallet.R

class TagAdapter(private var tags: List<Tag>, private val onDelete: (Int) -> Unit) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = tags[position]
        holder.bind(tag)
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
        fun bind(tag: Tag) {
            itemView.findViewById<TextView>(R.id.textViewTagName).text = tag.name
        }
    }
}
