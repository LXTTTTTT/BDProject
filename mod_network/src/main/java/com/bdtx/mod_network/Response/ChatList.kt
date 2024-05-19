package com.bdtx.mod_network.Response

import com.bdtx.mod_network.Response.Chat

data class ChatList(
    var total:Int,
    var totalPage:Int,
    var items:List<Chat>
)