package paukstadt.savechat.backgroundworkers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import paukstadt.savechat.ClientServerContractClass;
import paukstadt.savechat.Login;
import paukstadt.savechat.R;
import paukstadt.savechat.chat.Chat;
import paukstadt.savechat.database.BufferDatabaseHelper;
import paukstadt.savechat.database.UserDatabaseHelper;

public class MessageReceiver implements Runnable
{
    private Context context;

    public MessageReceiver(Context context)
    {
        this.context = context;
    }

    @Override
    public void run()
    {
        ServerSocket listener = null;
        Socket connection = null;
        BufferedReader reader = null;
        PrintWriter writer = null;
        BufferDatabaseHelper bufferDb;
        UserDatabaseHelper userDb;
        try
        {
            bufferDb = new BufferDatabaseHelper(context);
            userDb = new UserDatabaseHelper(context);
            listener = new ServerSocket(ClientServerContractClass.CLIENT_LISTENER_PORT);

            while(true)
            {
                try
                {
                    connection = listener.accept();
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())));

                    //Read a single line from the input stream (represents a Json-String)
                    String message = reader.readLine();
                    String phoneId;

                    //Try parse the json input string
                    JSONObject json = new JSONObject(message);
                    ContentValues values = new ContentValues();
                    phoneId = json.getString(ClientServerContractClass.MSG_CONTACTID);
                    values.put(BufferDatabaseHelper.PHONE, phoneId);
                    values.put(BufferDatabaseHelper.MIME, json.getString(ClientServerContractClass.MSG_MIME));
                    values.put(BufferDatabaseHelper.CONTENT, Base64.decode(json.getString(ClientServerContractClass.MSG_CONTENT), Base64.DEFAULT));
                    values.put(BufferDatabaseHelper.TIMESTAMP, new Date().getTime());

                    //If everything went well, report success to communication partner
                    writer.println(ClientServerContractClass.RESPONSE_ACK);
                    writer.flush();

                    //Save the received message in the buffer db
                    bufferDb.getWritableDatabase().insert(BufferDatabaseHelper.TABLE_NAME, null, values);

                    //Get user info
                    Cursor cursor = userDb.getReadableDatabase().query(UserDatabaseHelper.TABLE_CONTACT_NAME, UserDatabaseHelper.TABLE_CONTACTS_ALLCOLUMNS, UserDatabaseHelper.TABLE_CONTACTS_NUMBER + "=" + phoneId, null, null, null, null);
                    cursor.moveToFirst();
                    String alias = phoneId;
                    if(cursor.getCount() == 1)
                    {
                        alias = cursor.getString(cursor.getColumnIndex(UserDatabaseHelper.TABLE_CONTACTS_ALIAS));
                        if(alias == null || alias.equals("")) alias = phoneId;
                    }
                    else
                    {
                        InetSocketAddress address = (InetSocketAddress)connection.getRemoteSocketAddress();

                        ContentValues newContact = new ContentValues();
                        newContact.put(UserDatabaseHelper.TABLE_CONTACTS_ALIAS, "");
                        newContact.put(UserDatabaseHelper.TABLE_CONTACTS_NUMBER, phoneId);
                        newContact.put(UserDatabaseHelper.TABLE_CONTACTS_STATUS, "");
                        newContact.put(UserDatabaseHelper.TABLE_CONTACTS_PORT, address.getPort());
                        newContact.put(UserDatabaseHelper.TABLE_CONTACTS_IPPORT_STATUS, 0);
                        newContact.put(UserDatabaseHelper.TABLE_CONTACTS_LASTCONTACT, new Date().getTime());
                        newContact.put(UserDatabaseHelper.TABLE_CONTACTS_IP, address.getAddress().toString());

                        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.user);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        newContact.put(UserDatabaseHelper.TABLE_CONTACTS_AVATAR, stream.toByteArray());
                        bmp.recycle();

                        userDb.getWritableDatabase().insert(UserDatabaseHelper.TABLE_CONTACT_NAME, null, newContact);
                    }
                    cursor.close();

                    //Create a notification for the new received message
                    NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                    builder.setSmallIcon(R.mipmap.ic_launcher);
                    builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
                    builder.setContentTitle(alias + " has send you a message!");

                    Intent intent = new Intent(context, Login.class);
                    intent.putExtra(Chat.OPENDIRECT, phoneId);
                    PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

                    builder.setContentIntent(pIntent);

                    manager.notify(Integer.parseInt(phoneId), builder.build());
                }
                catch (Exception e)
                {
                    if (writer != null)
                    {
                        //If something went wrong, report failure to communication partner
                        writer.println(ClientServerContractClass.RESPONSE_ERR);
                        writer.flush();
                        writer.close();
                    }
                    if (reader != null) reader.close();
                    if (connection != null) connection.close();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                if (connection != null) connection.close();
                if (listener != null) listener.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
