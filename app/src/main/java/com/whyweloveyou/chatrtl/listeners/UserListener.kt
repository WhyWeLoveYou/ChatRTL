package com.whyweloveyou.chatrtl.listeners

import com.whyweloveyou.chatrtl.models.User

interface UserListener {
    fun onUserClicekd(user: User)
}