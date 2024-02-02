package com.bdtx.mod_data.Database.Entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;
import org.greenrobot.greendao.DaoException;
import com.bdtx.mod_data.Database.Dao.DaoSession;
import com.bdtx.mod_data.Database.Dao.LocationDao;
import com.bdtx.mod_data.Database.Dao.ContactDao;

// 联系人实体类
@Entity
public class Contact {
    @Id
    public String number;
    public String remark;  // 备注
    public String lastContent;  // 最后一条通信内容
    public Long updateTime;  // 更新时间（秒时间戳）
    public int unreadCount;  // 未读消息
    public String draft;  // 草稿
    public double longitude = 0.0;  // 经度
    public double latitude = 0.0;  // 纬度
    public int altitude = 0;  // 高度
    @ToMany(referencedJoinProperty = "contactNumber")
    public List<Location> locations;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 2046468181)
    private transient ContactDao myDao;
    @Generated(hash = 1333380994)
    public Contact(String number, String remark, String lastContent,
            Long updateTime, int unreadCount, String draft, double longitude,
            double latitude, int altitude) {
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
    public int getAltitude() {
        return this.altitude;
    }
    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 183000684)
    public List<Location> getLocations() {
        if (locations == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            LocationDao targetDao = daoSession.getLocationDao();
            List<Location> locationsNew = targetDao._queryContact_Locations(number);
            synchronized (this) {
                if (locations == null) {
                    locations = locationsNew;
                }
            }
        }
        return locations;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1398170159)
    public synchronized void resetLocations() {
        locations = null;
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
    @Generated(hash = 2088270543)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getContactDao() : null;
    }

}
