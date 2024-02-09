package com.bdtx.mod_util.Utils

import android.util.Log
import com.bdtx.mod_data.Database.DaoUtils
import com.bdtx.mod_data.Database.Entity.Location
import com.bdtx.mod_data.Database.Entity.Message
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_data.Global.Variable
import com.bdtx.mod_data.ViewModel.MainVM
import com.bdtx.mod_util.Utils.Protocol.TDWTUtils

object SendMessageUtils {

    fun getMainVM() = ApplicationUtils.getGlobalViewModel(MainVM::class.java)

    val TAG = "SendMessageUtils"

    fun send_text(target_number: String,content: String,have_location:Boolean){
        if(!checkSend()){return}
        // 新建消息
        val message = Message().apply {
            setNumber(target_number)
            setContent(content)
            setTime(DataUtils.getTimeSeconds())
            setMessageType(Constant.MESSAGE_TEXT)
            setIoType(Constant.TYPE_SEND)
            setState(Constant.STATE_SENDING)
            if(have_location){
                val location = Location().apply {
                    this.id = DataUtils.getTimeMillis()
                    this.longitude = getMainVM().deviceLongitude.value!!
                    this.latitude = getMainVM().deviceLatitude.value!!
                    this.altitude = getMainVM().deviceAltitude.value!!
                    this.time = id/1000
                    Log.e(TAG, "send_text LOCATION: ${this.toString()}" )
                }
                setLocation(location)
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
        if(!checkSend()){return}
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
        val target = Variable.getSystemNumber().toString()
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
    fun checkSend():Boolean{

        return true
    }

}