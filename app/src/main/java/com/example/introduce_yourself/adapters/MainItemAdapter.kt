package com.recyclerviewapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.introduce_yourself.Models.ReadUserModel
import com.example.introduce_yourself.R
import kotlinx.android.synthetic.main.main_item_row.view.*

open class UsersList(
    private var listOfUsers: ArrayList<ReadUserModel>
) : RecyclerView.Adapter<UsersList.OwnViewHolder>() {
//    private var onClickListener: OnClickListener? = null

    class OwnViewHolder(val item: View) : RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OwnViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.main_item_row, parent, false
        )
        return OwnViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OwnViewHolder, position: Int) {
        val ptr = listOfUsers[position]
        holder.item.main_tv_user_name.text = ptr.name + " " + ptr.surname
        holder.item.main_tv_user_email.text = ptr.email
        holder.item.main_tv_user_description.text = ptr.description //TODO: limit length ~70chars
        holder.item.main_iv_user_picture.setImageBitmap(byteArrayToBitmap(ptr.profile_picture))
        //passing which position was clicked on rv
        //passing ptr
//        holder.item.setOnClickListener {
//            if (onClickListener != null) {
//                onClickListener!!.onClick(position, ptr)
//            }
//        }
        holder.item.setOnClickListener {
            val bundle = bundleOf(USERNAME_KEY to listOfUsers[position])

            holder.item.findNavController().navigate(
                R.id.action_home_to_user_card,
                bundle)
        }
    }

    override fun getItemCount(): Int {
        return listOfUsers.size
    }

    companion object {
        const val USERNAME_KEY = "userName"
    }

//    fun setOnClickListener(onClickListener: OnClickListener) {
//        this.onClickListener = onClickListener
//    }
//
//    interface OnClickListener {
//        fun onClick(position: Int, model: ReadUserModel)
//    }

    private fun byteArrayToBitmap(data: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(data, 0, data.size)
    }
}