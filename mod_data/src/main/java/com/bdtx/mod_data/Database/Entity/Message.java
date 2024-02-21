package com.bdtx.mod_data.Database.Entity;

import com.bdtx.mod_data.Global.Constant;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;


// 聊天消息的实体类
@Entity
public class Message {

    @Id(autoincrement = true)  // 自增
    public Long id;
    public String number;  // 号码
    public String content;
    public Long time;  // 时间戳（秒）
    private Boolean isSOS = false;  // 是否SOS消息

    public int messageType = Constant.MESSAGE_TEXT;  // 消息类型
    public int state = Constant.STATE_SENDING;  // 发送状态
    public int ioType = Constant.TYPE_SEND;  // 发送/接收

    public int voiceLength;  // 语音消息的长度
    public String voicePath;  // 语音文件路径
    private String fromNumber;  // 发送方号码

    private double longitude = 0.0d;  // 经度
    private double latitude = 0.0d;  // 纬度
    private double altitude = 0.0d;  // 高度

    @Generated(hash = 1476412485)
    public Message(Long id, String number, String content, Long time, Boolean isSOS,
            int messageType, int state, int ioType, int voiceLength,
            String voicePath, String fromNumber, double longitude, double latitude,
            double altitude) {
        this.id = id;
        this.number = number;
        this.content = content;
        this.time = time;
        this.isSOS = isSOS;
        this.messageType = messageType;
        this.state = state;
        this.ioType = ioType;
        this.voiceLength = voiceLength;
        this.voicePath = voicePath;
        this.fromNumber = fromNumber;
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }
    @Generated(hash = 637306882)
    public Message() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNumber() {
        return this.number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Long getTime() {
        return this.time;
    }
    public void setTime(Long time) {
        this.time = time;
    }
    public int getMessageType() {
        return this.messageType;
    }
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
    public int getState() {
        return this.state;
    }
    public void setState(int state) {
        this.state = state;
    }
    public int getIoType() {
        return this.ioType;
    }
    public void setIoType(int ioType) {
        this.ioType = ioType;
    }
    public int getVoiceLength() {
        return this.voiceLength;
    }
    public void setVoiceLength(int voiceLength) {
        this.voiceLength = voiceLength;
    }
    public String getVoicePath() {
        return this.voicePath;
    }
    public void setVoicePath(String voicePath) {
        this.voicePath = voicePath;
    }
    public String getFromNumber() {
        return this.fromNumber;
    }
    public void setFromNumber(String fromNumber) {
        this.fromNumber = fromNumber;
    }
    public double getLongitude() {
        return this.longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public double getLatitude() {
        return this.latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getAltitude() {
        return this.altitude;
    }
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
    public Boolean getIsSOS() {
        return this.isSOS;
    }
    public void setIsSOS(Boolean isSOS) {
        this.isSOS = isSOS;
    }


}
