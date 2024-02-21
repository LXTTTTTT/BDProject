package com.bdtx.mod_data.Database;

import android.content.Context;
import android.util.Log;

import com.bdtx.mod_data.Database.Dao.ContactDao;
import com.bdtx.mod_data.Database.Dao.DaoMaster;
import com.bdtx.mod_data.Database.Dao.DaoSession;
import com.bdtx.mod_data.Database.Dao.MessageDao;
import com.bdtx.mod_data.Database.Entity.Contact;
import com.bdtx.mod_data.Database.Entity.Message;
import com.bdtx.mod_data.EventBus.BaseMsg;
import com.bdtx.mod_data.EventBus.UpdateMessageMsg;
import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_data.Global.Variable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

// 数据库使用工具
public class DaoUtils {

    public static String TAG = "DaoUtil";
    private Context mContext;
    //创建数据库的名字
//    private static final String DB_NAME = "ChatLXT.db";
    private static final String DB_NAME = "BDTX_Watch.db";

    //它里边实际上是保存数据库的对象
    private static DaoMaster mDaoMaster;

    //创建数据库的工具
    private static DaoMaster.DevOpenHelper mHelper;

    //管理gen里生成的所有的Dao对象里边带有基本的增删改查的方法
    private static DaoSession mDaoSession;


    private DaoUtils() {
    }

// 单例 ------------------------------------
    //多线程中要被共享的使用volatile关键字修饰  GreenDao管理类
    private volatile static DaoUtils instance;
    public static DaoUtils getInstance() {
        if (instance == null) {
            synchronized (DaoUtils.class) {
                if (instance == null) {
                    instance = new DaoUtils();
                }
            }
        }
        return instance;
    }

    // 初始化
    public void init(Context context) {
        this.mContext = context;
        addPlatformContact();
    }


    // 判断是否有存在数据库，如果没有则创建
    public DaoMaster getDaoMaster() {
        if (mDaoMaster == null) {
            mHelper = new DaoMaster.DevOpenHelper(mContext, DB_NAME, null);
            mDaoMaster = new DaoMaster(mHelper.getWritableDatabase());
        }
        return mDaoMaster;
    }

    // 完成对数据库的添加、删除、修改、查询操作，
    public DaoSession getDaoSession() {
        if (mDaoSession == null) {
            if (mDaoMaster == null) {
                mDaoMaster = getDaoMaster();
            }
            mDaoSession = mDaoMaster.newSession();
        }
        return mDaoSession;
    }

    // 首次初始化指挥中心
    public static void addPlatformContact(){
        List<Contact> contacts = getContactBuilder().where(ContactDao.Properties.Number.eq(Constant.PLATFORM_IDENTIFIER)).list();
        if (contacts.size() == 0){
            Contact contact = new Contact();
            contact.number = Constant.PLATFORM_IDENTIFIER;
            contact.remark = "指挥中心";
            contact.lastContent = "这里是指挥中心";
            contact.updateTime = System.currentTimeMillis() / 1000;
            contact.unreadCount = 0;
            getInstance().getDaoSession().insertOrReplace(contact);
        }
    }

    public static List<Contact> getContacts(){
        return getInstance().getDaoSession().getContactDao().loadAll();
    }

    // 添加联系人
    public void addContact(Message message){
        String number = message.getNumber();
        double longitude = 0.0d; double latitude = 0.0d; double altitude = 0.0d;
        // 只有接收到的消息带位置才更新联系人的位置
        if(message.getIoType()==Constant.TYPE_RECEIVE && message.getLongitude()!=0.0d){
            longitude = message.getLongitude();
            latitude = message.getLatitude();
            altitude = message.getAltitude();
        }
        List<Contact> contacts = getContactBuilder().where(ContactDao.Properties.Number.eq(number)).list();
        Contact contact = null;
        // 新增联系人
        if(contacts.size()<1){
            contact = new Contact();
            contact.number = number;
            Log.e(TAG, "添加联系人："+number);
        }
        // 更新联系人
        else {
            contact = contacts.get(0);
            Log.e(TAG, "更新联系人："+number);
        }

        contact.remark = number.equals(Constant.PLATFORM_IDENTIFIER)? "指挥中心":number;
        contact.lastContent = message.getContent();  // 更新最后内容
        contact.updateTime = System.currentTimeMillis() / 1000;  // 更新时间
        if(longitude!=0.0d){
            contact.longitude = longitude;
            contact.latitude = latitude;
            contact.altitude = altitude;
        }
        // 接收的消息，联系人未读消息数量+1
        if(message.getIoType()==Constant.TYPE_RECEIVE){
            contact.unreadCount++;
        }
        getDaoSession().insertOrReplace(contact);
        // 发送广播
        EventBus.getDefault().post(new BaseMsg<>(BaseMsg.Companion.getMSG_UPDATE_CONTACT(), null));
    }

    public void clearContactUnread(String number){
        List<Contact> contacts = getContactBuilder().where(ContactDao.Properties.Number.eq(number)).list();
        if(contacts.size()>0){
            Contact contact = contacts.get(0);
            contact.unreadCount = 0;
            getDaoSession().insertOrReplace(contact);
            EventBus.getDefault().post(new BaseMsg<>(BaseMsg.Companion.getMSG_UPDATE_CONTACT(), null));
            Log.e(TAG, "清除未读消息："+number);
        }
    }

    public static List<Message> getMessages(String number){
        return getInstance().getDaoSession().getMessageDao().queryBuilder().where(MessageDao.Properties.Number.eq(number)).list();
    }

    // 添加消息
    public void addMessage(Message message){
        addContact(message);  // 添加联系人
        if(message.getIoType()==Constant.TYPE_SEND){Variable.lastSendMsg = message;Variable.checkSendState();}  // 发送状态检测
        getDaoSession().insertOrReplace(message);
        // 发送广播
        EventBus.getDefault().post(new BaseMsg<>(BaseMsg.Companion.getMSG_UPDATE_MESSAGE(), new UpdateMessageMsg(message.getNumber())));
        Log.e(TAG, "添加消息：id-"+message.getId()+"/"+message.getContent());
    }




// 联系人数据库查询指令器（查询联系人时用这个） ----------------------
    public static QueryBuilder getContactBuilder(){
        return getInstance().getDaoSession().getContactDao().queryBuilder();
    }


// 关闭数据库：关闭所有的操作，数据库开启后，使用完毕要关闭 ---------
    public void closeConnection() {
        closeHelper();
        closeDaoSession();
    }

    public void closeHelper() {
        if (mHelper != null) {
            mHelper.close();
            mHelper = null;
        }
    }

    public void closeDaoSession() {
        if (mDaoSession != null) {
            mDaoSession.clear();
            mDaoSession = null;
        }
    }
}

/**
 * 1.插入
 * insert(User entity)： 插入一条记录, 当指定主键在表中存在时会发生异常
 * insertOrReplace(User entity) ： 当指定主键在表中存在时会覆盖数据,有该数据时则更新，推荐同步数据库时使用该方法
 * save(User entity):　save 类似于insertOrReplace，区别在于save会判断传入对象的key，有key的对象执行更新，无key的执行插入。当对象有key但并不在数据库时会执行失败.适用于保存本地列表。
 *
 * insertInTx(T... entities)：使用事务在数据库中插入给定的实体。
 * insertInTx(Iterable<T> entities)：使用事务操作，将给定的实体集合插入数据库。
 * insertInTx(Iterable<T> entities, boolean setPrimaryKey)：使用事务操作，将给定的实体集合插入数据库，并设置是否设定主键 。
 *
 * insertOrReplaceInTx(T... entities)：使用事务操作，将给定的实体插入数据库，若此实体类存在，则覆盖
 * insertOrReplaceInTx(Iterable<T> entities)：使用事务操作，将给定的实体插入数据库，若此实体类存在，则覆盖 。
 * insertOrReplaceInTx(Iterable<T> entities, boolean setPrimaryKey)：使用事务操作，将给定的实体插入数据库，若此实体类存在，则覆盖，并设置是否设定主键 。
 * insertWithoutSettingPk(T entity)：将给定的实体插入数据库,但不设定主键。
 *
 * // 新增数据插入相关API
 * save(T entity)：将给定的实体插入数据库
 * saveInTx(Iterable<T> entities)：将给定的实体集合插入数据库
 * saveInTx(T... entities)：使用事务操作，将给定的实体插入数据库
 */

/**
 * 2.查询
 * //查询全部
 * List<User> list = mUserDao.queryBuilder().list();
 *
 * //查询 name等于xyh8的数据
 * List<User> list= mUserDao.queryBuilder().where(UserDao.Properties.Name.eq("xyh8")).list();
 *
 * //查询 name不等于xyh8的数据
 * List<User> list= mUserDao.queryBuilder().where(UserDao.Properties.Name.notEq("xyh8")).list();
 *
 * //like  模糊查询
 * //查询 name以xyh3开头的数据
 * List<User> list = mUserDao.queryBuilder().where(UserDao.Properties.Name.like("xyh3%")).list();
 *
 * //between 区间查询 年龄在20到30之间
 *  List<User> list = mUserDao.queryBuilder().where(UserDao.Properties.Age.between(20,30)).list();
 *
 * //gt: greater than 半开区间查询，年龄大于18
 * List<User> list = mUserDao.queryBuilder().where(UserDao.Properties.Age.gt(18)).list();
 *
 * //ge: greater equal 半封闭区间查询，年龄大于或者等于18
 * List<User> list = mUserDao.queryBuilder().where(UserDao.Properties.Age.ge(18)).list();
 *
 * //lt: less than 半开区间查询，年龄小于18
 * List<User> list = mUserDao.queryBuilder().where(UserDao.Properties.Age.lt(18)).list();
 *
 * //le: less equal 半封闭区间查询，年龄小于或者等于18
 * List<User> list = mUserDao.queryBuilder().where(UserDao.Properties.Age.le(18)).list();
 */

/**
 * 3.排序
 * //名字以xyh8开头，年龄升序排序
 *  List<User> list = mUserDao.queryBuilder()
 *                 .where(UserDao.Properties.Name.like("xyh8%"))
 *                 .orderAsc(UserDao.Properties.Age)
 *                 .list();
 *
 * //名字以xyh8开头，年龄降序排序
 *  List<User> list = mUserDao.queryBuilder()
 *                 .where(UserDao.Properties.Name.like("xyh8%"))
 *                 .orderDesc(UserDao.Properties.Age)
 *                 .list();
 */

/**
 *  4.更新
 *  update(T entity) ：更新给定的实体
 *
 * updateInTx(Iterable<T> entities) ：使用事务操作，更新给定的实体
 *
 * updateInTx(T... entities)：使用事务操作，更新给定的实体
 *        studentDao.update(student);
 *         studentDao.updateInTx(student);
 */

/**
 * 5.删除
 * //删除全部
 *  mUserDao.deleteAll();
 *
 * delete(T entity)：从数据库中删除给定的实体
 *
 * deleteByKey(K key)：从数据库中删除给定Key所对应的实体
 *
 * deleteInTx(T... entities)：使用事务操作删除数据库中给定的实体
 *
 * deleteInTx(<T> entities)：使用事务操作删除数据库中给定实体集合中的实体
 *
 * deleteByKeyInTx(K... keys)：使用事务操作删除数据库中给定的所有key所对应的实体
 *
 * deleteByKeyInTx(Iterable<K> keys)：使用事务操作删除数据库中给定的所有key所对应的实体
 */