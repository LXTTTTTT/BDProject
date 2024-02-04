package com.bdtx.mod_util.Utils.Protocol;

import android.util.Log;

import com.bdtx.mod_data.Database.DaoUtils;
import com.bdtx.mod_data.Database.Entity.Message;
import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_util.Utils.DataUtils;
import com.bdtx.mod_util.Utils.FileUtils;
import com.bdtx.mod_util.Utils.FileUtils2;
import com.pancoit.compression.ZDCompression;

import java.io.File;
import java.io.FileOutputStream;

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
    public static void resolve90(String from,String data_str){
        try{
            String data = data_str.substring(12);  // 有效数据（去掉头和手机）
            long time = DataUtils.hex2Long(data.substring(0, 8));  // 时间（秒）
            String content = DataUtils.hex2String(data.substring(8));// 内容
            Log.e(TAG, "收到90消息: "+ content);
            // 创建消息记录
//            Message message = new Message();
//            message.setNumber(from);
//            message.setTime(time*1000);  // 使用接收到的消息时间
//            message.setContent(content);
//            message.setMessageType(Constant.TYPE_RECEIVE);
//            DaoUtils.getInstance().addMessage(message);
            addTextMessage(from,content);
        }catch (Exception e){e.printStackTrace();}
    }

    public static void resolve91(String from,String data_str){
        try{
            String data = data_str.substring(12);  // 有效数据（去掉头和手机）
            long time = DataUtils.hex2Long(data.substring(0, 8));  // 时间（秒）
            String content = DataUtils.hex2String(data.substring(28));// 内容
            Log.e(TAG, "收到91消息: "+ content);
            addTextMessage(from,content);
        }catch (Exception e){e.printStackTrace();}
    }

    public static void resolve92(String from,String data_str){
        try{
            String data = data_str.substring(12);  // 有效数据（去掉头和手机）
            long time = DataUtils.hex2Long(data.substring(0, 8));  // 时间
            String content = DataUtils.hex2String(data.substring(8));// 内容
            Log.e(TAG, "收到92消息: "+ content);
            addTextMessage(from,content);
        }catch (Exception e){e.printStackTrace();}
    }

    public static void resolve93(String from,String data_str){
        try{
            String data = data_str.substring(12);  // 有效数据（去掉头和手机）
            long time = DataUtils.hex2Long(data.substring(0, 8));  // 时间
            String content = DataUtils.hex2String(data.substring(28));// 内容
            Log.e(TAG, "收到93消息: "+ content);
            addTextMessage(from,content);
        }catch (Exception e){e.printStackTrace();}
    }

    public static void resolveA7(String from,String data_str){
        try{
            String data = data_str.substring(12);  // 有效数据（去掉头和手机）
            byte[] content_bytes = DataUtils.hex2bytes(data.substring(28));  // 消息原始数据
            // 存储语音文件的路径
            String voiceFilePath = FileUtils.getAudioPcmFile() + DataUtils.getTimeSerial() + "_receive.pcm";
            File voice_file = new File(voiceFilePath);
            if(!voice_file.exists()){voice_file.createNewFile();}
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try (FileOutputStream ops = new FileOutputStream(voice_file)){
                        // 使用中大压缩库解压出语音数据并保存
                        byte[] voice_bytes = ZDCompression.getInstance().unZip(content_bytes);
                        ops.write(voice_bytes);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    int seconds= FileUtils2.getPcmSeconds(voiceFilePath);  // 获取语音的秒数
                    addVoiceMessage(from,voiceFilePath,seconds);
                    Log.e(TAG, "收到A7消息，语音长度: "+seconds);
                }
            }).start();

        }catch (Exception e){e.printStackTrace();}
    }



    public static void addTextMessage(String from, String content){
        // 创建消息记录
        Message message = new Message();
        message.setNumber(from);
        message.setTime(DataUtils.getTimeSeconds());  // 使用实时接收消息时间
        message.setContent(content);
        message.setIoType(Constant.TYPE_RECEIVE);
        message.setMessageType(Constant.MESSAGE_TEXT);
        DaoUtils.getInstance().addMessage(message);  // 插入数据库
    }

    public static void addVoiceMessage(String from, String path, int seconds){
        // 创建消息记录
        Message message = new Message();
        message.setNumber(from);
        message.setTime(DataUtils.getTimeSeconds());  // 使用实时接收消息时间
        message.setContent("语音消息");
        message.setIoType(Constant.TYPE_RECEIVE);
        message.setMessageType(Constant.MESSAGE_VOICE);
        message.setVoicePath(path);
        message.setVoiceLength(seconds);
        DaoUtils.getInstance().addMessage(message);  // 插入数据库
    }

}
