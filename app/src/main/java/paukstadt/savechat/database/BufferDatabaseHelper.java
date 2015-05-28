package paukstadt.savechat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BufferDatabaseHelper extends SQLiteOpenHelper
{
    private static final String DBNAME = "buffer.db";
    private static final int DBVERSION = 1;

    public static final String TABLE_NAME = "chat_buffer";
    public static final String PHONE = "phone";
    public static final String MIME = "mime";
    public static final String CONTENT = "content";
    public static final String TIMESTAMP = "time";
    public static final String[] ALLCOLUMNS = {PHONE, MIME, CONTENT, TIMESTAMP};

    public BufferDatabaseHelper(Context context)
    {
        super(context, DBNAME, null, DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table if not exists " + TABLE_NAME + " ( "
                    + PHONE + " text not null, "
                    + MIME + " text not null, "
                    + CONTENT + " blob not null, "
                    + TIMESTAMP + " int not null );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Do nothing for the moment
    }
}
