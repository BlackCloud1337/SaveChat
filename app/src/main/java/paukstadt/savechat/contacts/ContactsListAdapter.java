package paukstadt.savechat.contacts;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import paukstadt.savechat.R;
import paukstadt.savechat.database.UserDatabaseHelper;

public class ContactsListAdapter extends BaseAdapter
{
    private Activity activity;
    private UserDatabaseHelper dbHelper;

    public ContactsListAdapter(Activity activity)
    {
        this.activity = activity;
        dbHelper = new UserDatabaseHelper(activity);
    }

    @Override
    public int getCount()
    {
        int count;

        Cursor cursor = dbHelper.getReadableDatabase().query(UserDatabaseHelper.TABLE_CONTACT_NAME, UserDatabaseHelper.TABLE_USER_ALLCOLUMNS, "", null, null, null, null);
        count = cursor.getCount();
        cursor.close();

        return count;
    }

    @Override
    public Object getItem(int position)
    {
        ContactsListDTO dto = new ContactsListDTO(activity);

        Cursor cursor = dbHelper.getReadableDatabase().query(UserDatabaseHelper.TABLE_CONTACT_NAME, UserDatabaseHelper.TABLE_CONTACTS_ALLCOLUMNS, "", null, null, null, null);
        cursor.moveToPosition(position);

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
    public long getItemId(int position)
    {
        long tmp;
        Cursor cursor = dbHelper.getReadableDatabase().query(UserDatabaseHelper.TABLE_CONTACT_NAME, UserDatabaseHelper.TABLE_CONTACTS_ALLCOLUMNS, "", null, null, null, null);
        cursor.moveToPosition(position);
        tmp = cursor.getLong(cursor.getColumnIndex(UserDatabaseHelper.TABLE_CONTACTS_ID));
        cursor.close();
        return tmp;
    }

    public long getItemId(String phone)
    {
        long id;

        Cursor cursor = dbHelper.getReadableDatabase().query(UserDatabaseHelper.TABLE_CONTACT_NAME, UserDatabaseHelper.TABLE_CONTACTS_ALLCOLUMNS, UserDatabaseHelper.TABLE_CONTACTS_NUMBER + "=" + phone, null, null, null, null);
        id = cursor.getLong(cursor.getColumnIndex(UserDatabaseHelper.TABLE_CONTACTS_ID));
        cursor.close();

        return id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView == null)
        {
            convertView = activity.getLayoutInflater().inflate(R.layout.listentry_contacts_list, parent);
        }

        //Retrieve information
        ContactsListDTO dto = (ContactsListDTO)getItem(position);

        //Find views
        ImageView image = (ImageView)convertView.findViewById(R.id.user_avatar);
        ImageView connection = (ImageView)convertView.findViewById(R.id.user_connection);
        TextView aliasOrNumber = (TextView)convertView.findViewById(R.id.user_name);
        TextView status = (TextView)convertView.findViewById(R.id.user_status);

        //Fill view with information
        image.setImageBitmap(dto.getAvatar());

        if(dto.getIp() == null) connection.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.con_bad));
        else if (dto.getHeartbeat()) connection.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.con_high));
        else connection.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.con_med));

        status.setText(dto.getStatus());

        if(dto.getAlias().equals("")) aliasOrNumber.setText(dto.getNumber());
        else aliasOrNumber.setText(dto.getAlias());

        return convertView;
    }
}