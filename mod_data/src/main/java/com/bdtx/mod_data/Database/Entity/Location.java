package com.bdtx.mod_data.Database.Entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Location {

    @Id
    public Long id;  // 时间戳（毫秒）
    public double longitude;
    public double latitude;
    public double altitude;
    public Long time;  // 时间戳（秒）
    public String contactNumber;
    @Generated(hash = 1357852576)
    public Location(Long id, double longitude, double latitude, double altitude,
            Long time, String contactNumber) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.time = time;
        this.contactNumber = contactNumber;
    }

    @Generated(hash = 375979639)
    public Location() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
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
    public Long getTime() {
        return this.time;
    }
    public void setTime(Long time) {
        this.time = time;
    }

    public String getContactNumber() {
        return this.contactNumber;
    }
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", altitude=" + altitude +
                ", time=" + time +
                ", contactNumber='" + contactNumber + '\'' +
                '}';
    }

}
