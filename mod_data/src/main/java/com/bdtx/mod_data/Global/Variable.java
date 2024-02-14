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

    public static int getSystemNumber(){return MMKV.defaultMMKV().decodeInt(Constant.SYSTEM_NUMBER,Constant.DEFAULT_PLATFORM_NUMBER);}
    public static int getCompressRate(){return MMKV.defaultMMKV().decodeInt(Constant.VOICE_COMPRESSION_RATE,666);}
    // MMKV无法在模块之间共享数据，直接保存或使用单例
//    public static void setSystemNumber(int number){MMKV.defaultMMKV().encode(Constant.SYSTEM_NUMBER,number);}
//    public static void setCompressRate(int rate){MMKV.defaultMMKV().encode(Constant.VOICE_COMPRESSION_RATE,rate);}

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

    public static Message lastSendMsg = null;
    public static CountDownTimer countDownTimer = null;
    private final static Handler mainHandler = new Handler(Looper.getMainLooper());
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








    public static boolean DebugMode = true;  // 调试模式

    public static boolean isConnectBluetooth = false;  // 是否连接 蓝牙
    public static boolean isConnectSerialPort = false;  // 是否连接 串口
    public static boolean isConnectUSBAccessory = false;  // 是否连接 USB附件
    public static boolean isConnectUSB = false;  // 是否连接 USB
    public static boolean isConnectTCPServer = false;  // 是否连接 TCP 服务器
    public static boolean isRecording = false;  // 是否正在录音

    public static String token;  // token
    public static int frequency = 60;  // 当前上报频度
    public static String sos_content = "请求救援";  // 默认的 SOS 内容
    public static String ok_content = "报平安";  // 默认的 OK 内容
    public static boolean need_phone = false;  // 是否需要 拨打电话


    // 当前设备的 经度、纬度、高度、速度 (未转换前)
    public static double gpsLongitude = 0;
    public static double gpsLatitude = 0;
    public static double gpsAltitude = 0;
    public static double speed = 0;


//    public static String getAccount(){return SharedPreferencesUtil.getInstance().getString(Constant.ACCOUNT_S, Constant.DEFAULT_ACCOUNT);}
//    public static void setAccount(String account){SharedPreferencesUtil.getInstance().setString(Constant.ACCOUNT_S, account);}
//    public static String getPassword(){return SharedPreferencesUtil.getInstance().getString(Constant.PASSWORD_S, Constant.DEFAULT_PASSWORD);}
//    public static void setPassword(String password){SharedPreferencesUtil.getInstance().setString(Constant.PASSWORD_S, password);}
//    public static int systemNumber = SharedPreferencesUtil.getInstance().getInt("system_number",15950044);


}
