package com.bdtx.mod_network.Response;

import java.util.ArrayList;

// 标准 GSON 解析类：登录后返回的账户信息
public class Account {

    public String token;
    public String account;
    public String addr;
    public String avatar;
    public ArrayList<EmergencyContacts> emergencyContacts;
    public Gender gender;
    public Boolean initPwd;
    public int starBeans;
    public String nickname;
    public int subscribedUnreadMsgNum;
    public String webRoleType;
    public Boolean enterpriseAccount;
    public String webAccount;
    public ArrayList<String> terminals;

    public class Gender{
        public String name;
        public String value;
    }

    public class EmergencyContacts{
        public String name;
        public String phone;
    }

    @Override
    public String toString() {
        return "Account{" +
                "token='" + token + '\'' +
                ", account='" + account + '\'' +
                ", addr='" + addr + '\'' +
                ", avatar='" + avatar + '\'' +
                ", emergencyContacts=" + emergencyContacts +
                ", gender=" + gender +
                ", initPwd=" + initPwd +
                ", starBeans=" + starBeans +
                ", nickname='" + nickname + '\'' +
                ", subscribedUnreadMsgNum=" + subscribedUnreadMsgNum +
                ", webRoleType='" + webRoleType + '\'' +
                ", enterpriseAccount=" + enterpriseAccount +
                ", webAccount='" + webAccount + '\'' +
                ", terminals=" + terminals +
                '}';
    }
}
