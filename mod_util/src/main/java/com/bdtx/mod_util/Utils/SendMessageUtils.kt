package com.bdtx.mod_util.Utils

import com.bdtx.mod_data.Database.DaoUtil
import com.bdtx.mod_data.Database.Entity.Message
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_data.Global.Variable
import com.bdtx.mod_util.Utils.Protocol.TDWTUtils

object SendMessageUtils {






    fun send_text(target_number: String,content: String){
        // 新建消息
        var message = Message().apply {
            setNumber(target_number)
            setContent(content)
            setTime(DataUtils.getTimeSeconds())
            setMessageType(Constant.MESSAGE_TEXT)
            setIoType(Constant.TYPE_SEND)
            setState(Constant.STATE_SENDING)
        }
        DaoUtil.getInstance().addMessage(message)
        // 平台消息
        if (target_number == Constant.platform_identifier) {
            BluetoothTransferUtils.getInstance().sendMessage(Variable.getSystemNumber().toString(), 2, TDWTUtils.encapsulated92(content))
        } else {
            BluetoothTransferUtils.getInstance().sendMessage(target_number, 2, TDWTUtils.encapsulated90(content))
        }
    }

}