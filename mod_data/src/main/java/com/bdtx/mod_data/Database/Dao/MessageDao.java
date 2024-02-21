package com.bdtx.mod_data.Database.Dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.bdtx.mod_data.Database.Entity.Message;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "MESSAGE".
*/
public class MessageDao extends AbstractDao<Message, Long> {

    public static final String TABLENAME = "MESSAGE";

    /**
     * Properties of entity Message.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Number = new Property(1, String.class, "number", false, "NUMBER");
        public final static Property Content = new Property(2, String.class, "content", false, "CONTENT");
        public final static Property Time = new Property(3, Long.class, "time", false, "TIME");
        public final static Property IsSOS = new Property(4, Boolean.class, "isSOS", false, "IS_SOS");
        public final static Property MessageType = new Property(5, int.class, "messageType", false, "MESSAGE_TYPE");
        public final static Property State = new Property(6, int.class, "state", false, "STATE");
        public final static Property IoType = new Property(7, int.class, "ioType", false, "IO_TYPE");
        public final static Property VoiceLength = new Property(8, int.class, "voiceLength", false, "VOICE_LENGTH");
        public final static Property VoicePath = new Property(9, String.class, "voicePath", false, "VOICE_PATH");
        public final static Property FromNumber = new Property(10, String.class, "fromNumber", false, "FROM_NUMBER");
        public final static Property Longitude = new Property(11, double.class, "longitude", false, "LONGITUDE");
        public final static Property Latitude = new Property(12, double.class, "latitude", false, "LATITUDE");
        public final static Property Altitude = new Property(13, double.class, "altitude", false, "ALTITUDE");
    }


    public MessageDao(DaoConfig config) {
        super(config);
    }
    
    public MessageDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"MESSAGE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"NUMBER\" TEXT," + // 1: number
                "\"CONTENT\" TEXT," + // 2: content
                "\"TIME\" INTEGER," + // 3: time
                "\"IS_SOS\" INTEGER," + // 4: isSOS
                "\"MESSAGE_TYPE\" INTEGER NOT NULL ," + // 5: messageType
                "\"STATE\" INTEGER NOT NULL ," + // 6: state
                "\"IO_TYPE\" INTEGER NOT NULL ," + // 7: ioType
                "\"VOICE_LENGTH\" INTEGER NOT NULL ," + // 8: voiceLength
                "\"VOICE_PATH\" TEXT," + // 9: voicePath
                "\"FROM_NUMBER\" TEXT," + // 10: fromNumber
                "\"LONGITUDE\" REAL NOT NULL ," + // 11: longitude
                "\"LATITUDE\" REAL NOT NULL ," + // 12: latitude
                "\"ALTITUDE\" REAL NOT NULL );"); // 13: altitude
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"MESSAGE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Message entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String number = entity.getNumber();
        if (number != null) {
            stmt.bindString(2, number);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(3, content);
        }
 
        Long time = entity.getTime();
        if (time != null) {
            stmt.bindLong(4, time);
        }
 
        Boolean isSOS = entity.getIsSOS();
        if (isSOS != null) {
            stmt.bindLong(5, isSOS ? 1L: 0L);
        }
        stmt.bindLong(6, entity.getMessageType());
        stmt.bindLong(7, entity.getState());
        stmt.bindLong(8, entity.getIoType());
        stmt.bindLong(9, entity.getVoiceLength());
 
        String voicePath = entity.getVoicePath();
        if (voicePath != null) {
            stmt.bindString(10, voicePath);
        }
 
        String fromNumber = entity.getFromNumber();
        if (fromNumber != null) {
            stmt.bindString(11, fromNumber);
        }
        stmt.bindDouble(12, entity.getLongitude());
        stmt.bindDouble(13, entity.getLatitude());
        stmt.bindDouble(14, entity.getAltitude());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Message entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String number = entity.getNumber();
        if (number != null) {
            stmt.bindString(2, number);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(3, content);
        }
 
        Long time = entity.getTime();
        if (time != null) {
            stmt.bindLong(4, time);
        }
 
        Boolean isSOS = entity.getIsSOS();
        if (isSOS != null) {
            stmt.bindLong(5, isSOS ? 1L: 0L);
        }
        stmt.bindLong(6, entity.getMessageType());
        stmt.bindLong(7, entity.getState());
        stmt.bindLong(8, entity.getIoType());
        stmt.bindLong(9, entity.getVoiceLength());
 
        String voicePath = entity.getVoicePath();
        if (voicePath != null) {
            stmt.bindString(10, voicePath);
        }
 
        String fromNumber = entity.getFromNumber();
        if (fromNumber != null) {
            stmt.bindString(11, fromNumber);
        }
        stmt.bindDouble(12, entity.getLongitude());
        stmt.bindDouble(13, entity.getLatitude());
        stmt.bindDouble(14, entity.getAltitude());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Message readEntity(Cursor cursor, int offset) {
        Message entity = new Message( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // number
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // content
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3), // time
            cursor.isNull(offset + 4) ? null : cursor.getShort(offset + 4) != 0, // isSOS
            cursor.getInt(offset + 5), // messageType
            cursor.getInt(offset + 6), // state
            cursor.getInt(offset + 7), // ioType
            cursor.getInt(offset + 8), // voiceLength
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // voicePath
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // fromNumber
            cursor.getDouble(offset + 11), // longitude
            cursor.getDouble(offset + 12), // latitude
            cursor.getDouble(offset + 13) // altitude
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Message entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setNumber(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setContent(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setTime(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
        entity.setIsSOS(cursor.isNull(offset + 4) ? null : cursor.getShort(offset + 4) != 0);
        entity.setMessageType(cursor.getInt(offset + 5));
        entity.setState(cursor.getInt(offset + 6));
        entity.setIoType(cursor.getInt(offset + 7));
        entity.setVoiceLength(cursor.getInt(offset + 8));
        entity.setVoicePath(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setFromNumber(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setLongitude(cursor.getDouble(offset + 11));
        entity.setLatitude(cursor.getDouble(offset + 12));
        entity.setAltitude(cursor.getDouble(offset + 13));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Message entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Message entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Message entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
