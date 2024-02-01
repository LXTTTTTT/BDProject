package com.bdtx.mod_data.Global;


import android.media.AudioFormat;

// 项目使用的全局常量
public class Constant {

// 路由路径 ---------------------------------------------------------
    public static final String MESSAGE_ACTIVITY = "/main/activity/message";
    public static final String STATE_ACTIVITY = "/main/activity/state";
    public static final String SOS_ACTIVITY = "/main/activity/sos";
    public static final String MAP_ACTIVITY = "/main/activity/map";
    public static final String SETTING_ACTIVITY = "/main/activity/setting";
    public static final String VOICE_AUTH_ACTIVITY = "/main/activity/setting/voice_auth";
    public static final String COMPRESSION_RATE_ACTIVITY = "/main/activity/setting/compression_rate";
    public static final String PLATFORM_SETTING_ACTIVITY = "/main/activity/setting/platform_setting";
    public static final String MESSAGE_TYPE_ACTIVITY = "/main/activity/setting/message_type";
    public static final String ABOUT_US_ACTIVITY = "/main/activity/setting/about_us";

    public static final String CONNECT_BLUETOOTH_ACTIVITY = "/main/activity/connect_bluetooth";

// 通用常量 ---------------------------------------------------------

    public static final String platform_identifier = "110110110";  // 平台标识
    public static final int default_platform_number = 15950044;  // 默认平台号码

    public static final String CONTACT_ID = "contact_id";  // 联系人唯一标识（卡号）

    public static int MESSAGE_TEXT = 1;  // 文本消息
    public static int MESSAGE_VOICE = 2;  // 语音消息
    public static int TYPE_SEND = 1;  // 发送消息
    public static int TYPE_RECEIVE = 2;  // 接收消息
    public static int STATE_SENDING = 20;  // 发送中
    public static int STATE_SUCCESS = 21;  // 发送成功
    public static int STATE_FAILURE = 22;  // 发送失败

    // SharedPreferences 字符变量
    public static final String VOICE_COMPRESSION_RATE = "voice_compression_rate";  // 改变压缩码率
    public static final String SYSTEM_NUMBER = "system_number";  // 平台号码

    public static final String IS_VOICE_AUTH = "is_voice_auth";  // 语音授权标识
    public static final String VOICE_AUTH_NO = "no";  // 语音未授权
    public static final String VOICE_AUTH_YES = "yes";  // 语音已授权

    public static final String VO_ONLINE_ACTIVATION_KEY = "vo_online_activation_key";  // 语音在线激活key
    public static final String PIC_ONLINE_ACTIVATION_KEY = "pic_online_activation_key";  // 图片在线激活key
    public static final String VO_OFF_ACTIVATION_VALUE = "A90A411BDBF02DBEBV";  // 离线语音key
    public static final String PIC_OFF_ACTIVATION_VALUE = "A90A411BDBF02DBEBP";  // 离线图片key


}
