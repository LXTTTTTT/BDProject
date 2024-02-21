package com.bdtx.mod_util.Utils

import android.graphics.Color
import android.util.Log
import android.view.View
import com.bdtx.mod_data.Database.DaoUtils
import com.bdtx.mod_data.Database.Entity.Message
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_data.Global.Variable
import com.bdtx.mod_data.ViewModel.MainVM
import com.bdtx.mod_util.Utils.Protocol.TDWTUtils
import com.pancoit.compression.ZDCompression
import java.util.*

object SendMessageUtils {

    fun getMainVM() = ApplicationUtils.getGlobalViewModel(MainVM::class.java)

    val TAG = "SendMessageUtils"

    fun send_text(target_number: String,content: String,have_location:Boolean){
        if(!checkSend(TYPE_TEXT)){return}
        // 新建消息
        val message = Message().apply {
            setNumber(target_number)
            setContent(content)
            setTime(DataUtils.getTimeSeconds())
            setMessageType(Constant.MESSAGE_TEXT)
            setIoType(Constant.TYPE_SEND)
            setState(Constant.STATE_SENDING)
            if(have_location){
                longitude = getMainVM().deviceLongitude.value!!
                latitude = getMainVM().deviceLatitude.value!!
                altitude = getMainVM().deviceAltitude.value!!
            }
        }
        DaoUtils.getInstance().addMessage(message)
        // 平台消息
        if (target_number == Constant.PLATFORM_IDENTIFIER) {
            BluetoothTransferUtils.getInstance().sendMessage(Variable.getSystemNumber().toString(), 2, TDWTUtils.encapsulated93(message))
        } else {
            BluetoothTransferUtils.getInstance().sendMessage(target_number, 2, TDWTUtils.encapsulated91(message))
        }
    }

    fun send_voice(target_number: String,path: String,seconds:Int){
        if(!checkSend(TYPE_VOICE)){return}
        // 新建消息
        var message = Message().apply {
            setNumber(target_number)
            setContent("语音消息")
            setVoicePath(path)
            setVoiceLength(seconds)
            setTime(DataUtils.getTimeSeconds())
            setMessageType(Constant.MESSAGE_VOICE)
            setIoType(Constant.TYPE_SEND)
            setState(Constant.STATE_SENDING)
        }
        DaoUtils.getInstance().addMessage(message)
        // 平台消息
        var target = if(target_number == Constant.PLATFORM_IDENTIFIER){Variable.getSystemNumber().toString()}else{target_number}
        BluetoothTransferUtils.getInstance().sendMessage(target, 2, TDWTUtils.encapsulatedA7(message))
    }

    fun send_sos(status: String,body: String,count: Int,content: String){
        if(!checkSend(TYPE_TEXT)){return}
        val target = Variable.getSystemNumber().toString()
        val message_content = "$content \n紧急情况：$status \n身体情况：$body \n人数：$count"
        // 新建消息
        var message = Message().apply {
            setNumber(Constant.PLATFORM_IDENTIFIER)
            setContent(message_content)
            setTime(DataUtils.getTimeSeconds())
            setMessageType(Constant.MESSAGE_TEXT)
            setIoType(Constant.TYPE_SEND)
            setState(Constant.STATE_SENDING)
            isSOS = true
            longitude = getMainVM().deviceLongitude.value!!
            latitude = getMainVM().deviceLatitude.value!!
            altitude = getMainVM().deviceAltitude.value!!
        }
        DaoUtils.getInstance().addMessage(message)
        val status_code = getStatusCode(status)
        val body_code = getStatusCode(body)
        val count_code = DataUtils.padWithZeros(Integer.toHexString(count),2)
        BluetoothTransferUtils.getInstance().sendMessage(target, 2, TDWTUtils.encapsulated13(status_code,body_code,count_code,content))
    }

    fun getStatusCode(status: String):String{
        var code = "00"
        when(status){
            Constant.BODY_STATUS_GREAT->code="00"
            Constant.BODY_STATUS_WALK->code="01"
            Constant.BODY_STATUS_BLOOD->code="02"
            Constant.BODY_STATUS_HUNGRY->code="03"
            Constant.BODY_STATUS_INJURED->code="04"

            Constant.SOS_STATUS_OTHER->code="00"
            Constant.SOS_STATUS_LOST->code="01"
            Constant.SOS_STATUS_FLOOD->code="02"
            Constant.SOS_STATUS_FALL->code="03"
            Constant.SOS_STATUS_DAMAGED->code="04"
            Constant.SOS_STATUS_ROCKFALL->code="05"
            Constant.SOS_STATUS_ACCIDENT->code="06"
            Constant.SOS_STATUS_HYPOTHERMIA->code="07"
            Constant.SOS_STATUS_HEATSTROKE->code="08"
            Constant.SOS_STATUS_SICKNESS->code="09"
            Constant.SOS_STATUS_HEARTATTACK->code="0A"
            Constant.SOS_STATUS_POISON->code="0B"
        }
        return code
    }

    // 发送条件检测
    val TYPE_TEXT = 0; val TYPE_VOICE = 1
    fun checkSend(type:Int):Boolean{
        if(getMainVM().isConnectDevice.value == false){
            GlobalControlUtils.showToast("未连接北斗设备!",0)
            return false
        }
        if(getMainVM().waitTime.value!! > 0){
            GlobalControlUtils.showToast("发送频度未到!",0)
            return false
        }
        if(!getMainVM().isSignalWell()){
            GlobalControlUtils.showToast("当前卫星信号不佳!",0)
            return false
        }
        if(type == TYPE_VOICE){
            // 测试，先
            // 语音发送等级要求
            if(getMainVM().deviceCardLevel.value!!<3){
                GlobalControlUtils.showToast("请用三级以上北斗卡发送语音消息!",0)
                return false
            }

            // 压缩库剩余次数检测（未激活的情况下）
            if(!ZDCompressionUtils.isVoiceOnline()){
                try {
                    val array = ZDCompression.getInstance().vOffInfo.split(",".toRegex()).toTypedArray()
                    val leave = Integer.valueOf(array[0])
                    Log.e(TAG, "剩余语音压缩试用次数: $leave" )
                    if(leave<1){
                        GlobalControlUtils.showToast("剩余语音压缩试用次数不足，请联系商务人员!",0)
                        return false
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
        return true
    }

}