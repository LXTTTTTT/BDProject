package com.bdtx.mod_network.Response

data class Chat(
    var id:String,
    var account:String,
    var nickname:String,
    var chatTimeStr:String?,
    var content:String
)