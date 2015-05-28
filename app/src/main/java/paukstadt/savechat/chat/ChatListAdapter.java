package paukstadt.savechat.chat;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import paukstadt.savechat.R;
import paukstadt.savechat.contacts.ContactsListDTO;
import paukstadt.savechat.database.BufferDatabaseHelper;
import paukstadt.savechat.database.ChatDatabaseHelper;
import paukstadt.savechat.database.UserDatabaseHelper;
import paukstadt.savechat.encryption.PasswordHelper;

public class ChatListAdapter extends BaseAdapter
{
    private String tableName = "";
    private Activity activity;
    private byte[] secret = null;
    private ChatDatabaseHelper chatDb;
    private BufferDatabaseHelper bufferDb;
    private int id = -1;

    public ChatListAdapter(Activity activity, String secret, int id, String phone)
    {
        this.activity = activity;
        this.secret = secret.getBytes();
        this.id = id;
        this.tableName = ChatDatabaseHelper.NAMEPREFIX + getContactInfo().getNumber();

        //Create handle to the database and create table for this contact if not exists
        chatDb = new ChatDatabaseHelper(activity);
        chatDb.getWritableDatabase().execSQL(String.format(ChatDatabaseHelper.CREATETABLESTRING, tableName));

        //Initialize this adapter. Is used to transfer any buffered messages from the MessageReceiver into the save chat_ db
        bufferDb = new BufferDatabaseHelper(activity);
        initAdapter(phone);
    }

    private void initAdapter(String phone)
    {
        Cursor cursor = bufferDb.getReadableDatabase().query(BufferDatabaseHelper.TABLE_NAME, BufferDatabaseHelper.ALLCOLUMNS, BufferDatabaseHelper.PHONE + "=" + phone, null, null, null, null);
        cursor.moveToFirst();

        while(!cursor.isAfterLast())
        {
            ContentValues values = new ContentValues();
            values.put(ChatDatabaseHelper.FROMME, PasswordHelper.encrypt(Boolean.toString(false).getBytes(), secret));
            values.put(ChatDatabaseHelper.MIME, PasswordHelper.encrypt(cursor.getString(cursor.getColumnIndex(BufferDatabaseHelper.MIME)).getBytes(), secret));
            values.put(ChatDatabaseHelper.CONTENT, PasswordHelper.encrypt(cursor.getBlob(cursor.getColumnIndex(BufferDatabaseHelper.CONTENT)), secret));
            values.put(ChatDatabaseHelper.TIMESTAMP, PasswordHelper.encrypt(new Date(cursor.getLong(cursor.getColumnIndex(BufferDatabaseHelper.TIMESTAMP))).toString().getBytes(), secret));

            chatDb.getWritableDatabase().insert(tableName, null, values);
        }

        cursor.close();

        bufferDb.getWritableDatabase().delete(BufferDatabaseHelper.TABLE_NAME, BufferDatabaseHelper.PHONE + "=" + phone, null);
    }

    public void addItem(ChatDTO dto)
    {
        ContentValues values = new ContentValues();
        values.put(ChatDatabaseHelper.FROMME, PasswordHelper.encrypt(Boolean.toString(dto.getFromMe()).getBytes(), secret));
        values.put(ChatDatabaseHelper.MIME, PasswordHelper.encrypt(dto.getMime().getBytes(), secret));
        values.put(ChatDatabaseHelper.CONTENT, PasswordHelper.encrypt(dto.getContent(), secret));
        values.put(ChatDatabaseHelper.TIMESTAMP, PasswordHelper.encrypt(dto.getTimestamp().toString().getBytes(), secret));

        chatDb.getWritableDatabase().insert(tableName, null, values);
    }

    public ContactsListDTO getContactInfo()
    {
        ContactsListDTO dto = new ContactsListDTO(activity);

        UserDatabaseHelper userDb = new UserDatabaseHelper(activity);
        Cursor cursor = userDb.getReadableDatabase().query(UserDatabaseHelper.TABLE_CONTACT_NAME, UserDatabaseHelper.TABLE_CONTACTS_ALLCOLUMNS, UserDatabaseHelper.TABLE_CONTACTS_ID + "=" + id, null, null, null, null);
        cursor.moveToFirst();

        //Set simple data types
        dto.setId(cursor.getInt(cursor.getColumnIndex(UserDatabaseHelper.TABLE_CONTACTS_ID)));
        dto.setAlias(cursor.getString(cursor.getColumnIndex(UserDatabaseHelper.TABLE_CONTACTS_ALIAS)));
        dto.setNumber(cursor.getString(cursor.getColumnIndex(UserDatabaseHelper.TABLE_CONTACTS_NUMBER)));
        dto.setStatus(cursor.getString(cursor.getColumnIndex(UserDatabaseHelper.TABLE_CONTACTS_STATUS)));
        dto.setPort(cursor.getInt(cursor.getColumnIndex(UserDatabaseHelper.TABLE_CONTACTS_PORT)));

        //Set heartbeat
        int bool = cursor.getInt(cursor.getColumnIndex(UserDatabaseHelper.TABLE_CONTACTS_IPPORT_STATUS));
        dto.setHeartbeat(bool == 1);

        //Set date
        long unix = cursor.getInt(cursor.getColumnIndex(UserDatabaseHelper.TABLE_CONTACTS_LASTCONTACT));
        dto.setLastContact(new Date(unix * 1000));

        //Set avatar
        byte[] blob = cursor.getBlob(cursor.getColumnIndex(UserDatabaseHelper.TABLE_CONTACTS_AVATAR));
        dto.setAvatar(BitmapFactory.decodeByteArray(blob, 0, blob.length));

        //Set address
        try
        {
            dto.setIp(InetAddress.getByName(cursor.getString(cursor.getColumnIndex(UserDatabaseHelper.TABLE_CONTACTS_IP))));
        }
        catch (UnknownHostException ex)
        {
            dto.setIp(null);
        }

        cursor.close();

        return dto;
    }

    @Override
    public int getCount()
    {
        int count;
        Cursor cursor = chatDb.getReadableDatabase().query(tableName, ChatDatabaseHelper.ALLCOLUMNS, "", null, null, null, null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public Object getItem(int position)
    {
        ChatDTO dto = new ChatDTO();

        Cursor cursor = chatDb.getReadableDatabase().query(tableName, ChatDatabaseHelper.ALLCOLUMNS, ChatDatabaseHelper.ID + "=" + position, null, null, null, null);
        cursor.moveToFirst();

        dto.setId(cursor.getInt(cursor.getColumnIndex(ChatDatabaseHelper.ID)));

        //Everything except the id needs to be decrypted
        byte[] tmp;

        //Mime
        tmp = cursor.getBlob(cursor.getColumnIndex(ChatDatabaseHelper.MIME));
        tmp = PasswordHelper.decrypt(tmp, secret);
        dto.setMime(new String(tmp));

        //Content
        tmp = cursor.getBlob(cursor.getColumnIndex(ChatDatabaseHelper.CONTENT));
        tmp = PasswordHelper.decrypt(tmp, secret);
        dto.setContent(tmp);

        //FromMe
        tmp = cursor.getBlob(cursor.getColumnIndex(ChatDatabaseHelper.FROMME));
        tmp = PasswordHelper.decrypt(tmp, secret);
        dto.setFromMe(Boolean.parseBoolean(new String(tmp)));

        //Timestamp
        tmp = cursor.getBlob(cursor.getColumnIndex(ChatDatabaseHelper.TIMESTAMP));
        tmp = PasswordHelper.decrypt(tmp, secret);
        long unix = Long.parseLong(new String(tmp));
        dto.setTimestamp(new Date(unix * 1000));

        cursor.close();
        return dto;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ChatDTO chatInfo = (ChatDTO)getItem(position);

        //For the moment there is no need to check for the mime type because it is only possible to send text
        if(chatInfo.getFromMe())
        {
            convertView = activity.getLayoutInflater().inflate(R.layout.listentry_chat_send, parent);
            TextView output = (TextView)convertView.findViewById(R.id.chat_send_output);
            TextView timestamp = (TextView)convertView.findViewById(R.id.chat_send_timestamp);
            output.setText(new String(chatInfo.getContent()));
            timestamp.setText(new SimpleDateFormat("dd.MM - HH:mm", Locale.GERMANY).format(chatInfo.getTimestamp()));
        }
        else
        {
            ContactsListDTO contactInfo = getContactInfo();

            convertView = activity.getLayoutInflater().inflate(R.layout.listentry_chat_received, parent);
            TextView output = (TextView)convertView.findViewById(R.id.chat_received_output);
            ImageView avatar = (ImageView)convertView.findViewById(R.id.chat_received_avatar);
            TextView timestamp = (TextView)convertView.findViewById(R.id.chat_received_timestamp);

            output.setText(new String(chatInfo.getContent()));
            avatar.setImageBitmap(contactInfo.getAvatar());
            timestamp.setText(new SimpleDateFormat("dd.MM - HH:mm", Locale.GERMANY).format(chatInfo.getTimestamp()));
        }

        return convertView;
    }
}
