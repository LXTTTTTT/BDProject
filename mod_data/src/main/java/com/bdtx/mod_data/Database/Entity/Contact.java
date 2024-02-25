package com.bdtx.mod_data.Database.Entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

// 联系人实体类
@Entity
public class Contact {
    @Id
    public String number;
    public String remark;  // 备注
    public String lastContent;  // 最后一条通信内容
    public Long updateTime;  // 更新时间（秒时间戳）
    public int unreadCount = 0;  // 未读消息
    public String draft;  // 草稿
    public double longitude = 0.0;  // 经度
    public double latitude = 0.0;  // 纬度
    public double altitude = 0;  // 高度
    @Generated(hash = 2074665335)
    public Contact(String number, String remark, String lastContent,
            Long updateTime, int unreadCount, String draft, double longitude,
            double latitude, double altitude) {
        this.number = number;
        this.remark = remark;
        this.lastContent = lastContent;
        this.updateTime = updateTime;
        this.unreadCount = unreadCount;
        this.draft = draft;
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }
    @Generated(hash = 672515148)
    public Contact() {
    }
    public String getNumber() {
        return this.number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getRemark() {
        return this.remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
    public String getLastContent() {
        return this.lastContent;
    }
    public void setLastContent(String lastContent) {
        this.lastContent = lastContent;
    }
    public Long getUpdateTime() {
        return this.updateTime;
    }
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
    public int getUnreadCount() {
        return this.unreadCount;
    }
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
    public String getDraft() {
        return this.draft;
    }
    public void setDraft(String draft) {
        this.draft = draft;
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
   

}
