package com.example.introduce_yourself.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.introduce_yourself.Models.UserPostModel
import com.example.introduce_yourself.R
import kotlinx.android.synthetic.main.user_item_posts_item_row.view.*
import java.time.format.DateTimeFormatter

open class UserPostsAdapter(
    private val context: Context,
    private var listOfUsers: ArrayList<UserPostModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onClickListener: OnClickListener? = null

    private class OwnViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OwnViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.user_item_posts_item_row, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val ptr = listOfUsers[position]
        if (holder is OwnViewHolder) {
            holder.itemView.user_item_post_title.text = ptr.post_title
            holder.itemView.user_item_post_text.text = ptr.post_content
            holder.itemView.user_item_post_date.text =
                ptr.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, ptr)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return listOfUsers.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: UserPostModel)
    }
}