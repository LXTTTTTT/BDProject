package com.bdtx.mod_data.Global;


import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.bdtx.mod_data.Database.DaoUtils;
import com.bdtx.mod_data.Database.Entity.Message;
import com.bdtx.mod_data.EventBus.BaseMsg;
import com.bdtx.mod_data.EventBus.UpdateMessageMsg;
import com.tencent.mmkv.MMKV;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

// 项目使用的全局变量
public class Variable {
    private static String TAG = "Variable";

    public static boolean isARouterInit = false;

    public static int getSystemNumber(){return MMKV.defaultMMKV().decodeInt(Constant.SYSTEM_NUMBER,Constant.DEFAULT_PLATFORM_NUMBER);}
    public static int getCompressRate(){return MMKV.defaultMMKV().decodeInt(Constant.VOICE_COMPRESSION_RATE,666);}

    public static String getSwiftMsg(){return MMKV.defaultMMKV().decodeString(Constant.SWIFT_MESSAGE, "");}
    public static void setSwiftMsg(String commands){MMKV.defaultMMKV().encode(Constant.SWIFT_MESSAGE, commands);}
    public static void addSwiftMsg(String command){
        String commands = command+Constant.SWIFT_MESSAGE_SYMBOL +getSwiftMsg();
        setSwiftMsg(commands);
    }
    public static void removeSwiftMsg(int position){
        String commands_str = getSwiftMsg();
        String new_commands = "";
        Log.e(TAG, "拿到快捷消息: "+commands_str);
        if(commands_str==null||commands_str.equals("")){return;}
        String[] commands_arr = commands_str.split(Constant.SWIFT_MESSAGE_SYMBOL);
        List<String> commands_list = Arrays.asList(commands_arr);
        for (int i = 0; i < commands_list.size(); i++) {
            if(i==position){continue;}
            new_commands += commands_list.get(i)+Constant.SWIFT_MESSAGE_SYMBOL;
        }
        setSwiftMsg(new_commands);
    }

    public static String getKey(){return MMKV.defaultMMKV().decodeString(Constant.VO_ONLINE_ACTIVATION_KEY,"");}
    public static void setKey(String key){MMKV.defaultMMKV().encode(Constant.VO_ONLINE_ACTIVATION_KEY,key);}
    public static boolean isVoiceOnline(){
        String voKey = getKey();
        Log.e(TAG, "当前保存的语音key: "+voKey );
        return !"".equals(voKey);
    }

    public static Message lastSendMsg = null;
    public static CountDownTimer countDownTimer = null;
    public static void checkSendState(){
        if(lastSendMsg==null){return;}
        countDownTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(lastSendMsg.getState()==Constant.STATE_SUCCESS || lastSendMsg.getState()==Constant.STATE_FAILURE){
                    DaoUtils.getInstance().getDaoSession().insertOrReplace(lastSendMsg);  // 记得插入数据库
                    // 发送广播
                    EventBus.getDefault().post(new BaseMsg<>(BaseMsg.Companion.getMSG_UPDATE_MESSAGE(), new UpdateMessageMsg(lastSendMsg.number)));
                    cancel();countDownTimer = null;lastSendMsg = null;
                }
            }

            @Override
            public void onFinish() {
                if(lastSendMsg.getState()==Constant.STATE_SENDING){
                    lastSendMsg.setState(Constant.STATE_FAILURE);
                }
                DaoUtils.getInstance().getDaoSession().insertOrReplace(lastSendMsg);
                // 发送广播
                EventBus.getDefault().post(new BaseMsg<>(BaseMsg.Companion.getMSG_UPDATE_MESSAGE(), new UpdateMessageMsg(lastSendMsg.number)));
                countDownTimer = null;lastSendMsg = null;
            }
        };
        countDownTimer.start();
    }

}
