package com.bdtx.mod_util.Utils.Protocol;

import android.util.Log;

import com.bdtx.mod_util.Utils.DataUtils;

public class TDWTUtils {

    private static final String TAG = "TDWTUtil";

    // 封装 ---------------------------------------------------------
    // 90 0000000000 65b0855e ccecbfd5
    public static String encapsulated90(String content_str){
        StringBuilder hex;
        // 消息类型0x90
        byte requestHeaderByte = (byte) 0x90;
        hex = new StringBuilder(DataUtils.byte2hex(requestHeaderByte));
        // 接收者电话号码
        hex.append("0000000000");
        Log.e(TAG, "90协议: "+hex);
        // 时间戳
        long time_long = System.currentTimeMillis()/1000;
        String time_hex = DataUtils.long2Hex(time_long);
        hex.append(time_hex.substring(time_hex.length()-8));
        Log.e(TAG, "90协议: "+hex);
        // 内容
        String content_hex = DataUtils.string2Hex(content_str);
        hex.append(content_hex);
        Log.e(TAG, "90协议: "+hex);
        return hex.toString();
    }

    public static String encapsulated92(String content_str){
        StringBuilder hex;
        // 消息类型0x90
        byte requestHeaderByte = (byte) 0x92;
        hex = new StringBuilder(DataUtils.byte2hex(requestHeaderByte));
        // 接收者电话号码
        hex.append("0000000000");
        // 时间戳
        long time_long = System.currentTimeMillis()/1000;
        String time_hex = DataUtils.long2Hex(time_long);
        hex.append(time_hex.substring(time_hex.length()-8));
        // 内容
        String content_hex = DataUtils.string2Hex(content_str);
        hex.append(content_hex);
        return hex.toString();
    }

// 解析 ---------------------------------------------------------
    // 90 0000000000 65b0855e ccecbfd5
//    public static void resolve90(String from,String data_str){
//        try{
//            String data = data_str.substring(12);  // 有效数据（去掉头和手机）
//            long time = DataUtils.hex2Long(data.substring(0, 8));  // 时间（秒）
//            String content = DataUtils.hex2String(data.substring(8));// 内容
//            Log.e(TAG, "收到90消息: "+ content);
//            // 创建消息记录
//            Message message = new Message();
//            message.setNumber(from);
//            message.setTime(time*1000);
//            message.setContent(content);
//            message.setMessageType(Constant.MESSAGE_TYPE_RECEIVE);
//            DaoUtil.getInstance().addMessage(message);
//        }catch (Exception e){e.printStackTrace();}
//    }
//
//    public static void resolve91(String from,String data_str){
//        try{
//            String data = data_str.substring(12);  // 有效数据（去掉头和手机）
//            long time = DataUtil.hex2Long(data.substring(0, 8));  // 时间（秒）
//            String content = DataUtil.hex2String(data.substring(28));// 内容
//            Log.e(TAG, "收到91消息: "+ content);
//            // 创建消息记录
//            Message message = new Message();
//            message.setNumber(from);
//            message.setTime(time*1000);
//            message.setContent(content);
//            message.setMessageType(Constant.MESSAGE_TYPE_RECEIVE);
//            DaoUtil.getInstance().addMessage(message);
//        }catch (Exception e){e.printStackTrace();}
//    }
//
//    public static void resolve92(String from,String data_str){
//        try{
//            String data = data_str.substring(12);  // 有效数据（去掉头和手机）
//            long time = DataUtil.hex2Long(data.substring(0, 8));  // 时间
//            String content = DataUtil.hex2String(data.substring(8));// 内容
//            Log.e(TAG, "收到92消息: "+ content);
//            // 创建消息记录
//            Message message = new Message();
//            message.setNumber(from);
//            message.setTime(time*1000);
//            message.setContent(content);
//            message.setMessageType(Constant.MESSAGE_TYPE_RECEIVE);
//            DaoUtil.getInstance().addMessage(message);
//        }catch (Exception e){e.printStackTrace();}
//    }
//
//    public static void resolve93(String from,String data_str){
//        try{
//            String data = data_str.substring(12);  // 有效数据（去掉头和手机）
//            long time = DataUtil.hex2Long(data.substring(0, 8));  // 时间
//            String content = DataUtil.hex2String(data.substring(28));// 内容
//            Log.e(TAG, "收到93消息: "+ content);
//            // 创建消息记录
//            Message message = new Message();
//            message.setNumber(from);
//            message.setTime(time*1000);
//            message.setContent(content);
//            message.setMessageType(Constant.MESSAGE_TYPE_RECEIVE);
//            DaoUtil.getInstance().addMessage(message);
//        }catch (Exception e){e.printStackTrace();}
//    }

}
