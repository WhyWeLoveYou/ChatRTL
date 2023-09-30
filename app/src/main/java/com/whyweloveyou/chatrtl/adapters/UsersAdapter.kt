package com.whyweloveyou.chatrtl.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.whyweloveyou.chatrtl.databinding.ItemContainerUserBinding
import com.whyweloveyou.chatrtl.listeners.UserListener
import com.whyweloveyou.chatrtl.models.User

class UsersAdapter(private var users: List<User>, private var userListener: UserListener) :
    RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemContainerUserBinding = ItemContainerUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(itemContainerUserBinding)
    }

    fun UserAdapters(users: List<User>, userListener: UserListener) {
        this.users = users
        this.userListener = userListener
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.setUserData(user)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class UserViewHolder(private var binding: ItemContainerUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setUserData(user: User) {
            binding.textName.text = user.name
            binding.textEmail.text = user.email
            binding.imageProfile.setImageBitmap(getUserImage(user.image))
            binding.root.setOnClickListener {
                userListener.onUserClicekd(user)
            }
        }

        private fun getUserImage(encodedImage: String): Bitmap {
            val decodedBytes = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        }
    }
}
