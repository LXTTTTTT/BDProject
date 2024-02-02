package com.bdtx.mod_data.Database.Entity;

import android.os.Handler;
import android.util.Log;

import com.bdtx.mod_data.Global.Constant;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;
import com.bdtx.mod_data.Database.Dao.DaoSession;
import com.bdtx.mod_data.Database.Dao.LocationDao;
import com.bdtx.mod_data.Database.Dao.MessageDao;


// 聊天消息的实体类
@Entity
public class Message {

    @Id(autoincrement = true)  // 自增
    public Long id;
    public String number;  // 号码
    public String content;
    public Long time;  // 时间戳（秒）

    public int messageType = Constant.MESSAGE_TEXT;  // 消息类型
    public int state = Constant.STATE_SENDING;  // 发送状态
    public int ioType = Constant.TYPE_SEND;  // 发送/接收

    public int voiceLength;  // 语音消息的长度
    public String voicePath;  // 语音文件路径
    private String fromNumber;  // 发送方号码

    @ToOne (joinProperty = "id")  // Location 的 id 要和这里指定的 id 相同
    public Location location ;  // 位置
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 859287859)
    private transient MessageDao myDao;

    @Generated(hash = 1563668676)
    public Message(Long id, String number, String content, Long time, int messageType,
            int state, int ioType, int voiceLength, String voicePath, String fromNumber) {
        this.id = id;
        this.number = number;
        this.content = content;
        this.time = time;
        this.messageType = messageType;
        this.state = state;
        this.ioType = ioType;
        this.voiceLength = voiceLength;
        this.voicePath = voicePath;
        this.fromNumber = fromNumber;
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

    @Generated(hash = 1068795426)
    private transient Long location__resolvedKey;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1085432286)
    public Location getLocation() {
        Long __key = this.id;
        if (location__resolvedKey == null || !location__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            LocationDao targetDao = daoSession.getLocationDao();
            Location locationNew = targetDao.load(__key);
            synchronized (this) {
                location = locationNew;
                location__resolvedKey = __key;
            }
        }
        return location;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 746508213)
    public void setLocation(Location location) {
        synchronized (this) {
            this.location = location;
            id = location == null ? null : location.getId();
            location__resolvedKey = id;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 747015224)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMessageDao() : null;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getFromNumber() {
        return this.fromNumber;
    }

    public void setFromNumber(String fromNumber) {
        this.fromNumber = fromNumber;
    }





}
