package com.bdtx.mod_data.Database.Dao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.bdtx.mod_data.Database.Entity.Contact;
import com.bdtx.mod_data.Database.Entity.Location;
import com.bdtx.mod_data.Database.Entity.Message;

import com.bdtx.mod_data.Database.Dao.ContactDao;
import com.bdtx.mod_data.Database.Dao.LocationDao;
import com.bdtx.mod_data.Database.Dao.MessageDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig contactDaoConfig;
    private final DaoConfig locationDaoConfig;
    private final DaoConfig messageDaoConfig;

    private final ContactDao contactDao;
    private final LocationDao locationDao;
    private final MessageDao messageDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        contactDaoConfig = daoConfigMap.get(ContactDao.class).clone();
        contactDaoConfig.initIdentityScope(type);

        locationDaoConfig = daoConfigMap.get(LocationDao.class).clone();
        locationDaoConfig.initIdentityScope(type);

        messageDaoConfig = daoConfigMap.get(MessageDao.class).clone();
        messageDaoConfig.initIdentityScope(type);

        contactDao = new ContactDao(contactDaoConfig, this);
        locationDao = new LocationDao(locationDaoConfig, this);
        messageDao = new MessageDao(messageDaoConfig, this);

        registerDao(Contact.class, contactDao);
        registerDao(Location.class, locationDao);
        registerDao(Message.class, messageDao);
    }
    
    public void clear() {
        contactDaoConfig.clearIdentityScope();
        locationDaoConfig.clearIdentityScope();
        messageDaoConfig.clearIdentityScope();
    }

    public ContactDao getContactDao() {
        return contactDao;
    }

    public LocationDao getLocationDao() {
        return locationDao;
    }

    public MessageDao getMessageDao() {
        return messageDao;
    }

}
