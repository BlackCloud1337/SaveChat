package paukstadt.savechat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UserDatabaseHelper extends SQLiteOpenHelper
{
    private final static String DBNAME = "savechat.db";
    private final static int DBVERSION = 1;

    public final static String TABLE_USER_NAME = "user";
    public final static String TABLE_USER_ID = "_id";
    public final static String TABLE_USER_ALIAS = "alias";
    public final static String TABLE_USER_ALIAS_UPDATED = "alias_updated";
    public final static String TABLE_USER_NUMBER = "phonenumber";
    public final static String TABLE_USER_MIME = "mime";
    public final static String TABLE_USER_AVATAR = "avatar";
    public final static String TABLE_USER_AVATAR_UPDATED = "avatar_updated";
    public final static String TABLE_USER_STATUS = "status";
    public final static String TABLE_USER_STATUS_UPDATED = "status_updated";
    public final static String TABLE_USER_HASH = "hash";
    public final static String[] TABLE_USER_ALLCOLUMNS = {TABLE_USER_ID, TABLE_USER_ALIAS, TABLE_USER_ALIAS_UPDATED, TABLE_USER_NUMBER, TABLE_USER_MIME, TABLE_USER_AVATAR, TABLE_USER_AVATAR_UPDATED, TABLE_USER_STATUS, TABLE_USER_STATUS_UPDATED, TABLE_USER_HASH};

    public final static String TABLE_CONTACT_NAME = "name";
    public final static String TABLE_CONTACTS_ID = "_id";
    public final static String TABLE_CONTACTS_ALIAS = "alias";
    public final static String TABLE_CONTACTS_NUMBER = "phonenumber";
    public final static String TABLE_CONTACTS_MIME = "mime";
    public final static String TABLE_CONTACTS_AVATAR = "avatar";
    public final static String TABLE_CONTACTS_STATUS = "status";
    public final static String TABLE_CONTACTS_LASTCONTACT = "lastcontact";
    public final static String TABLE_CONTACTS_IP = "lastip";
    public final static String TABLE_CONTACTS_PORT = "lastport";
    public final static String TABLE_CONTACTS_IPPORT_STATUS = "ipport_status";
    public final static String[] TABLE_CONTACTS_ALLCOLUMNS = {TABLE_CONTACTS_ID, TABLE_CONTACTS_ALIAS, TABLE_CONTACTS_NUMBER, TABLE_CONTACTS_MIME, TABLE_CONTACTS_AVATAR, TABLE_CONTACTS_STATUS, TABLE_CONTACTS_LASTCONTACT, TABLE_CONTACTS_IP, TABLE_CONTACTS_PORT, TABLE_CONTACTS_IPPORT_STATUS};

    public UserDatabaseHelper(Context context)
    {
        super(context, DBNAME, null, DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createUserTable = "create table if not exists " + TABLE_USER_NAME + " ( " +
                                    TABLE_USER_ID + " integer primary key autoincrement, " +
                                    TABLE_USER_ALIAS + " text, " +
                                    TABLE_USER_ALIAS_UPDATED + " integer, " +
                                    TABLE_USER_NUMBER + " text not null unique, " +
                                    TABLE_USER_MIME + " text, " +
                                    TABLE_USER_AVATAR + " blob, " +
                                    TABLE_USER_AVATAR_UPDATED + " integer, " +
                                    TABLE_USER_STATUS + " text, " +
                                    TABLE_USER_STATUS_UPDATED + " integer, " +
                                    TABLE_USER_HASH + " text not null );";
        Log.d("User_string: ", createUserTable);

        String createContactsTable = "create table if not exists " + TABLE_CONTACT_NAME + " ( " +
                                    TABLE_CONTACTS_ID + " integer primary key autoincrement, " +
                                    TABLE_CONTACTS_ALIAS + " text, " +
                                    TABLE_CONTACTS_NUMBER + " text not null unique, " +
                                    TABLE_CONTACTS_MIME + " text, " +
                                    TABLE_CONTACTS_AVATAR + " blob, " +
                                    TABLE_CONTACTS_STATUS + " text, " +
                                    TABLE_CONTACTS_LASTCONTACT + " integer, " +
                                    TABLE_CONTACTS_IP + " text, " +
                                    TABLE_CONTACTS_PORT + " integer, " +
                                    TABLE_CONTACTS_IPPORT_STATUS + " integer);";
        Log.d("Contacts_string. ", createContactsTable);

        db.execSQL(createUserTable);
        db.execSQL(createContactsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Do nothing for the moment
    }
}
