package paukstadt.savechat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatDatabaseHelper extends SQLiteOpenHelper
{
    private static final int VERSION = 1;
    private static final String NAME = "chats.db";

    public static final String NAMEPREFIX = "chat_";
    public static final String ID = "_id";
    public static final String FROMME = "fromMe";
    public static final String TIMESTAMP = "timestamp";
    public static final String MIME = "mime";
    public static final String CONTENT = "content";
    public static final String[] ALLCOLUMNS = { ID, FROMME, TIMESTAMP, MIME, CONTENT};

    public static String CREATETABLESTRING = "create table if not exists {0} ( "
                                                    + ID + " integer primary key autoincrement, "
                                                    + FROMME + " blob not null, "
                                                    + TIMESTAMP + " blob not null, "
                                                    + MIME + " blob not null, "
                                                    + CONTENT + " blob not null );";

    public ChatDatabaseHelper(Context context)
    {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //Do nothing here, because the tables are created for each contact (NAMEPREFIX + phonenumber)
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Do nothing for the moment
    }
}
