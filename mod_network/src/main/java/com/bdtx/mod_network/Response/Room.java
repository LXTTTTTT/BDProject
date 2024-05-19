package com.bdtx.mod_network.Response;

// 获取自身发起的直播间
public class Room {

    public String id;
    public String account;
    public String title;
    public String startTimeStr;
    public String endTimeStr;
    public String image;
    public String nickname;
    public int visitorNum;
    public RoomStatus roomStatus;
    public DestInfo destInfo;
    public String roomNo;

    // 添加构造函数、Getter 和 Setter 方法

    // 内部类，表示房间状态
    public class RoomStatus {
        public String name;
        public String value;
    }

    // 内部类，表示目的地信息
    public class DestInfo {
        public double lng;
        public double lat;
        public double remainingDistance;
    }


}
