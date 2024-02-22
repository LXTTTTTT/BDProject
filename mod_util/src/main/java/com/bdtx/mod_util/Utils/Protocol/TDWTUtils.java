package com.bdtx.mod_util.Utils.Protocol;

import android.util.Log;

import androidx.annotation.Nullable;

import com.bdtx.mod_data.Database.DaoUtils;
import com.bdtx.mod_data.Database.Entity.Message;
import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_data.Global.Variable;
import com.bdtx.mod_data.ViewModel.MainVM;
import com.bdtx.mod_util.Utils.ApplicationUtils;
import com.bdtx.mod_util.Utils.DataUtils;
import com.bdtx.mod_util.Utils.FileUtils;
import com.bdtx.mod_util.Utils.FileUtils2;
import com.bdtx.mod_util.Utils.ZDCompressionUtils;
import com.pancoit.compression.ZDCompression;

import java.io.File;
import java.io.FileOutputStream;

public class TDWTUtils {

    private static final String TAG = "TDWTUtil";
    private static MainVM getMainVM(){
        return ApplicationUtils.INSTANCE.getGlobalViewModel(MainVM.class);
    }
    // 封装 ---------------------------------------------------------
    // 90 0000000000 65b0855e ccecbfd5
    public static String encapsulated90(Message message){
        String content_str = message.getContent();
        StringBuilder hex;
        // 消息类型0x90
        byte requestHeaderByte = (byte) 0x90;
        hex = new StringBuilder(DataUtils.byte2hex(requestHeaderByte));
        // 接收者电话号码
        hex.append("FFFFFFFFFF");
        Log.e(TAG, "90协议: "+hex);
        // 时间戳
        long time_long = DataUtils.getTimeSeconds();
        String time_hex = DataUtils.long2Hex(time_long);
        hex.append(time_hex.substring(time_hex.length()-8));
        Log.e(TAG, "90协议: "+hex);
        // 内容
        String content_hex = DataUtils.string2Hex(content_str);
        hex.append(content_hex);
        Log.e(TAG, "90协议: "+hex);
        return hex.toString().toUpperCase();
    }

    public static String encapsulated91(Message message){
        String content_str = message.getContent();
        Location location = new Location();
        location.longitude = message.getLongitude();
        location.latitude = message.getLatitude();
        location.altitude = message.getAltitude();
        StringBuilder hex;
        // 消息类型0x91
        byte requestHeaderByte = (byte) 0x91;
        hex = new StringBuilder(DataUtils.byte2hex(requestHeaderByte));
        // 接收者电话号码
        hex.append("FFFFFFFFFF");
        // 时间戳
        long time_long = DataUtils.getTimeSeconds();
        String time_hex = DataUtils.long2Hex(time_long);
        hex.append(time_hex.substring(time_hex.length()-8));
        // 位置
        hex.append(getLocation(location));
        // 内容
        String content_hex = DataUtils.string2Hex(content_str);
        hex.append(content_hex);
        Log.e(TAG, "91协议: "+hex);
        return hex.toString().toUpperCase();
    }

    public static String encapsulated92(Message message){
        String content_str = message.getContent();
        StringBuilder hex;
        // 消息类型0x90
        byte requestHeaderByte = (byte) 0x92;
        hex = new StringBuilder(DataUtils.byte2hex(requestHeaderByte));
        // 接收者电话号码
//        hex.append("FFFFFFFFFF");
        String phone_hex = DataUtils.long2Hex(110110110);
        hex.append(phone_hex.substring(phone_hex.length()-10));
        // 时间戳
        long time_long = DataUtils.getTimeSeconds();
        String time_hex = DataUtils.long2Hex(time_long);
        hex.append(time_hex.substring(time_hex.length()-8));
        // 内容
        String content_hex = DataUtils.string2Hex(content_str);
        hex.append(content_hex);
        return hex.toString().toUpperCase();
    }

    // 93 000690259E 65D55F27 06C3DED0 01615240 0028 BAC3B5C4CAD5B5BD
    public static String encapsulated93(Message message){
        String content_str = message.getContent();
        Location location = new Location();
        location.longitude = message.getLongitude();
        location.latitude = message.getLatitude();
        location.altitude = message.getAltitude();
        StringBuilder hex;
        // 消息类型0x93
        byte requestHeaderByte = (byte) 0x93;
        hex = new StringBuilder(DataUtils.byte2hex(requestHeaderByte));
        // 接收者电话号码
//        hex.append("FFFFFFFFFF");
        String phone_hex = DataUtils.long2Hex(110110110);
        hex.append(phone_hex.substring(phone_hex.length()-10));
        Log.e(TAG, "93协议: "+hex);
        // 时间戳
        long time_long = DataUtils.getTimeSeconds();
        String time_hex = DataUtils.long2Hex(time_long);
        hex.append(time_hex.substring(time_hex.length()-8));
        Log.e(TAG, "93协议: "+hex);
        // 位置
        hex.append(getLocation(location));
        // 内容
        String content_hex = DataUtils.string2Hex(content_str);
        hex.append(content_hex);
        Log.e(TAG, "93协议: "+hex);
        return hex.toString().toUpperCase();
    }

    public static String encapsulatedA7(Message message){
        StringBuilder hex;
        byte requestHeaderByte = (byte) 0xA7;
        hex = new StringBuilder(DataUtils.byte2hex(requestHeaderByte));
        // 接收者电话号码
//        hex.append("FFFFFFFFFF");
        String phone_hex = DataUtils.long2Hex(110110110);
        hex.append(phone_hex.substring(phone_hex.length()-10));
        // 内容
        try {
            byte[] audio_bytes;
            int rate = Variable.getCompressRate()==666 ? getEncoderCodeRate(message.getVoiceLength()):Variable.getCompressRate();
            if(Variable.isVoiceOnline()){
                audio_bytes = ZDCompression.getInstance().zip(message.getVoicePath(),rate);
            } else {
                audio_bytes= ZDCompression.getInstance().off_voice_zip(message.getVoicePath(),rate);
            }
            hex.append(DataUtils.bytes2Hex(audio_bytes));
        }catch (Exception e){
            e.printStackTrace();
        }
        return hex.toString().toUpperCase();
    }

    // 13 00 FFFFFFFFFF 06F27460 017601A9 00C1 06 04 0A C7EBC7F3D6A7D4AE
    // 13 00 FFFFFFFFFF 06F2745C 017601AF 00C2 06 04 0A D3F6B5BDC6E4CBFCD7B4BFF6
    // 13 00 FFFFFFFFFF 06F273CF 01760210 00C1 03 03 01
    public static String encapsulated13(String status,String body,String count,String content){
        StringBuilder hex;
        byte requestHeaderByte = (byte) 0x13;
        hex = new StringBuilder(DataUtils.byte2hex(requestHeaderByte));
        hex.append("00");  // 保留字段
//        hex.append("FFFFFFFFFF");  // 接收者电话号码
        String phone_hex = DataUtils.long2Hex(110110110);
        hex.append(phone_hex.substring(phone_hex.length()-10));
        Location location = new Location();
        location.longitude = getMainVM().getDeviceLongitude().getValue();
        location.latitude = getMainVM().getDeviceLatitude().getValue();
        location.altitude = getMainVM().getDeviceAltitude().getValue();
        hex.append(getLocation(location));  // 位置
        hex.append(status);  // 状态
        hex.append(body);  // 身体状况
        hex.append(count);  // 人数
        hex.append(DataUtils.string2Hex(content));  // 内容
        return hex.toString().toUpperCase();
    }

    // 根据秒数及卡等级来使用不同的压缩率
    public static int getEncoderCodeRate(int seconds){
        int compressionRate = ZDCompression.bitRate_450;
        int card_level = ApplicationUtils.INSTANCE.getGlobalViewModel(MainVM.class).getDeviceCardLevel().getValue();
        compressionRate = calculateEncoderCodeRate(card_level,seconds);
        Log.e(TAG, "自动码率: 等级-"+card_level+" 秒数-"+seconds+" 选择码率-"+ compressionRate);
        return compressionRate;
    }

    private static final int[][] THRESHOLDS = {
            {3},         // card_level 2
            {3, 5, 7},  // card_level 3
            {3, 6, 11, 13},  // card_level 4
            {5, 11, 19, 23},  // card_level 5
    };

    private static final int[][] BIT_RATES = {
            // card_level 2
            {ZDCompression.bitRate_450},
            // card_level 3
            {ZDCompression.bitRate_1200, ZDCompression.bitRate_700, ZDCompression.bitRate_450},
            // card_level 4
            {ZDCompression.bitRate_2400, ZDCompression.bitRate_1200, ZDCompression.bitRate_700, ZDCompression.bitRate_450},
            // card_level 5
            {ZDCompression.bitRate_2400, ZDCompression.bitRate_1200, ZDCompression.bitRate_700, ZDCompression.bitRate_450}
    };
    // 5 10 ， 2 3
    public static int calculateEncoderCodeRate(int card_level, int seconds) {
        int encoderCodeRate = ZDCompression.bitRate_450;
        if (card_level >= 2 && card_level <= 5) {
            int[] thresholds = THRESHOLDS[card_level - 2];  // {5, 11, 19, 23}
            int[] bitRates = BIT_RATES[card_level - 2];  // {ZDCompression.bitRate_2400, ZDCompression.bitRate_1200, ZDCompression.bitRate_700, ZDCompression.bitRate_450}
            for (int i = 0; i < thresholds.length; i++) {
                if (seconds < thresholds[i]) {
                    encoderCodeRate = bitRates[i];
                    break;
                }
            }
        }
        return encoderCodeRate;
    }

    public static String getLocation(Location location){
        StringBuilder location_str = new StringBuilder();
        if(location==null || location.longitude==0.0d){
            Log.e(TAG, "location 为 null");
            location_str.append("FFFFFFFFFFFFFFFFFFFF");
        }else{
            double longitude_double = location.longitude;
            String longitude_str = DataUtils.toLength(DataUtils.int2Hex((int)(longitude_double*1000000)),8);
            location_str.append(longitude_str);
            double latitude_double = location.latitude;
            String latitude_str = DataUtils.toLength(DataUtils.int2Hex((int)(latitude_double*1000000)),8);
            location_str.append(latitude_str);
            double altitude_double = location.altitude;
            String altitude_str = DataUtils.toLength(DataUtils.int2Hex((int)(altitude_double)),4);
            location_str.append(altitude_str);
        }
        return location_str.toString();
    }


// 解析 ---------------------------------------------------------
    // 平台下发：A0000690259E65D6BA75CAD5B5BDC1CBC2F0

    // 90 0000000000 65b0855e ccecbfd5
    public static void resolve90(String from,String data_str){
        try{
            String data = data_str.substring(12);  // 有效数据（去掉头和手机）
            long time = DataUtils.hex2Long(data.substring(0, 8));  // 时间（秒）
            String content = DataUtils.hex2String(data.substring(8));// 内容
            Log.e(TAG, "收到90消息: "+ content);
            addTextMessage(from,content,null);
        }catch (Exception e){e.printStackTrace();}
    }

    // 91 0000000000 65C31FC8 06F274E6 017601A7 00D6 B2E2CAD4
    public static void resolve91(String from,String data_str){
        try{
            String data = data_str.substring(12);  // 有效数据（去掉头和手机）
            long time = DataUtils.hex2Long(data.substring(0, 8));  // 时间（秒）
            String location_hex = data.substring(8,28);
            String content = DataUtils.hex2String(data.substring(28));// 内容
            // 位置
            Location location = new Location();
            location.longitude = DataUtils.hex2Long(location_hex.substring(0,8)) / 1000000.0;
            location.latitude = DataUtils.hex2Long(location_hex.substring(8,16)) / 1000000.0;
            location.altitude = DataUtils.hex2Long(location_hex.substring(16,20));
            Log.e(TAG, "收到91消息: "+ content);
            addTextMessage(from,content,location);
        }catch(Exception e){e.printStackTrace();}
    }

    public static void resolve92(String from,String data_str){
        try{
            String data = data_str.substring(12);  // 有效数据（去掉头和手机）
            long time = DataUtils.hex2Long(data.substring(0, 8));  // 时间
            String content = DataUtils.hex2String(data.substring(8));// 内容
            Log.e(TAG, "收到92消息: "+ content);
            addTextMessage(from,content,null);
        }catch (Exception e){e.printStackTrace();}
    }

    public static void resolve93(String from,String data_str){
        try{
            String data = data_str.substring(12);  // 有效数据（去掉头和手机）
            long time = DataUtils.hex2Long(data.substring(0, 8));  // 时间
            String location_hex = data.substring(8,28);
            String content = DataUtils.hex2String(data.substring(28));// 内容
            // 位置
            Location location = new Location();
            location.longitude = DataUtils.hex2Long(location_hex.substring(0,8)) / 1000000.0;
            location.latitude = DataUtils.hex2Long(location_hex.substring(8,16)) / 1000000.0;
            location.altitude = DataUtils.hex2Long(location_hex.substring(16,20));
            Log.e(TAG, "收到93消息: "+ content);
            addTextMessage(from,content,location);
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

    public static void addTextMessage(String from, String content, @Nullable Location location){
        // 创建消息记录
        Message message = new Message();
        message.setNumber(from);
        message.setTime(DataUtils.getTimeSeconds());  // 使用实时接收消息时间
        message.setContent(content);
        message.setIoType(Constant.TYPE_RECEIVE);
        message.setMessageType(Constant.MESSAGE_TEXT);
        if(location!=null){
            message.setLongitude(location.longitude);
            message.setLatitude(location.latitude);
            message.setAltitude(location.altitude);
        }
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

    private static class Location {
        double longitude;
        double latitude;
        double altitude;
    }

}
