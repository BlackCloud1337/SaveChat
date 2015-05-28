package paukstadt.savechat.backgroundworkers;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;

import paukstadt.savechat.ClientServerContractClass;
import paukstadt.savechat.database.UserDatabaseHelper;

public class SyncAdapter_Sync extends AbstractThreadedSyncAdapter
{
    ContentResolver mContentResolver;
    UserDatabaseHelper userDB;

    public SyncAdapter_Sync(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
        init(context);
    }

    public SyncAdapter_Sync(Context context, boolean autoInitialize, boolean allowParallelSyncs)
    {
        super(context, autoInitialize, allowParallelSyncs);
        init(context);
    }

    private void init(Context context)
    {
        mContentResolver = context.getContentResolver();
        userDB = new UserDatabaseHelper(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
    {
        JSONObject userJson = createUserData();
        JSONObject contactsJson = createContactsJson();

        Socket connection = null;
        PrintWriter sender = null;
        BufferedReader receiver = null;

        try
        {
            connection = new Socket(ClientServerContractClass.SERVER_IP, ClientServerContractClass.SERVER_PORT);
            sender = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())), true);
            receiver = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            if(userJson != null)
            {
                sender.println(userJson.toString());
                sender.flush();
            }

            if(contactsJson != null)
            {
                sender.println(contactsJson.toString());
                sender.flush();

                updateContactsDb(new JSONObject(receiver.readLine()));
            }
        }
        catch (Exception e)
        {
            //Nothing to do here
        }
        finally
        {
            try
            {
                if(sender != null) sender.close();
                if(receiver != null) receiver.close();
                if(connection != null) connection.close();
            }
            catch (Exception e)
            {
                //Nothing to do here
            }
        }
    }

    private JSONObject createUserData()
    {
        //Get user info
        Cursor cursor = userDB.getReadableDatabase().query(UserDatabaseHelper.TABLE_USER_NAME, UserDatabaseHelper.TABLE_USER_ALLCOLUMNS, "", null, null, null, null);
        if (cursor.getCount() != 1) return null;

        //Create user json
        JSONObject userJson = new JSONObject();

        try
        {
            //Set id
            userJson.put(ClientServerContractClass.JSON_USER_ID, cursor.getString(cursor.getColumnIndex(UserDatabaseHelper.TABLE_USER_NUMBER)));

            //Check if alias needs to be updated
            boolean check = (cursor.getInt(cursor.getColumnIndex(UserDatabaseHelper.TABLE_USER_ALIAS_UPDATED)) == 1);
            if (check) userJson.put(ClientServerContractClass.JSON_ALIAS, cursor.getString(cursor.getColumnIndex(UserDatabaseHelper.TABLE_USER_ALIAS)));

            //Check if status needs to be updated
            check = (cursor.getInt(cursor.getColumnIndex(UserDatabaseHelper.TABLE_USER_STATUS_UPDATED)) == 1);
            if(check) userJson.put(ClientServerContractClass.JSON_STATUS, cursor.getString(cursor.getColumnIndex(UserDatabaseHelper.TABLE_USER_STATUS)));

            //Check if avatar needs to be updated
            check = (cursor.getInt(cursor.getColumnIndex(UserDatabaseHelper.TABLE_USER_AVATAR_UPDATED)) == 1);
            if(check)
            {
                JSONObject avatarObject = new JSONObject();
                avatarObject.put(ClientServerContractClass.JSON_AVATAR_MIME, cursor.getString(cursor.getColumnIndex(UserDatabaseHelper.TABLE_USER_MIME)));
                avatarObject.put(ClientServerContractClass.JSON_AVATAR_CONTENT, Base64.encode(cursor.getBlob(cursor.getColumnIndex(UserDatabaseHelper.TABLE_USER_AVATAR)), Base64.DEFAULT));

                userJson.put(ClientServerContractClass.JSON_AVATAR, avatarObject);
            }
        }
        catch (Exception e)
        {
            return null;
        }
        finally
        {
            cursor.close();
        }

        return userJson;
    }

    private JSONObject createContactsJson()
    {
        //Create contacts json
        JSONObject contactsJson = new JSONObject();

        //Get telephone and SaveChat contacts
        Cursor handy = mContentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        Cursor app = userDB.getReadableDatabase().query(UserDatabaseHelper.TABLE_CONTACT_NAME, UserDatabaseHelper.TABLE_CONTACTS_ALLCOLUMNS, "", null, null, null, null);

        try
        {
            //Add all app contacts to json
            app.moveToFirst();
            while (!app.isAfterLast())
            {
                contactsJson.put(app.getString(app.getColumnIndex(UserDatabaseHelper.TABLE_CONTACTS_NUMBER)), app.getLong(app.getColumnIndex(UserDatabaseHelper.TABLE_CONTACTS_LASTCONTACT)));
            }

            //Loop over all phone contacts, check if they are already app contacts and if not, add them to the json
            handy.moveToFirst();
            while(!handy.isAfterLast())
            {
                String id = handy.getString(handy.getColumnIndex(ContactsContract.Contacts._ID));
                if(Integer.parseInt(handy.getString(handy.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                {
                    Cursor numbers = mContentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{ id }, null);
                    while(numbers.moveToNext())
                    {
                        String number = ClientServerContractClass.normalizePhoneNumber(numbers.getString(numbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                        if(!appContactsContains(number))
                        {
                            contactsJson.put(number, 0L);
                        }
                    }
                    numbers.close();
                }
            }
        }
        catch (Exception e)
        {
            return null;
        }
        finally
        {
            handy.close();
            app.close();
        }

        return contactsJson;
    }

    private void updateContactsDb(JSONObject answer)
    {
        try
        {
            //Loop over all entries of the answer
            Iterator<String> keys = answer.keys();

            while(keys.hasNext())
            {
                String key = keys.next();
                JSONObject current = answer.getJSONObject(key);

                ContentValues values = new ContentValues();
                if(current.has(ClientServerContractClass.JSON_ALIAS)) values.put(UserDatabaseHelper.TABLE_CONTACTS_ALIAS, current.getString(ClientServerContractClass.JSON_ALIAS));
                if(current.has(ClientServerContractClass.JSON_STATUS)) values.put(UserDatabaseHelper.TABLE_CONTACTS_STATUS, current.getString(ClientServerContractClass.JSON_STATUS));
                if(current.has(ClientServerContractClass.JSON_LASTCONTACT)) values.put(UserDatabaseHelper.TABLE_CONTACTS_LASTCONTACT, current.getLong(ClientServerContractClass.JSON_LASTCONTACT));
                if(current.has(ClientServerContractClass.JSON_IP)) values.put(UserDatabaseHelper.TABLE_CONTACTS_IP, current.getString(ClientServerContractClass.JSON_IP));
                if(current.has(ClientServerContractClass.JSON_PORT)) values.put(UserDatabaseHelper.TABLE_CONTACTS_PORT, current.getInt(ClientServerContractClass.JSON_PORT));
                if(current.has(ClientServerContractClass.JSON_HEARTBEAT)) values.put(UserDatabaseHelper.TABLE_CONTACTS_IPPORT_STATUS, current.getBoolean(ClientServerContractClass.JSON_HEARTBEAT));
                if(current.has(ClientServerContractClass.JSON_AVATAR))
                {
                    JSONObject avatar = current.getJSONObject(ClientServerContractClass.JSON_AVATAR);
                    values.put(UserDatabaseHelper.TABLE_CONTACTS_MIME, avatar.getString(ClientServerContractClass.JSON_AVATAR_MIME));
                    values.put(UserDatabaseHelper.TABLE_CONTACTS_AVATAR, Base64.decode(avatar.getString(ClientServerContractClass.JSON_AVATAR_CONTENT), Base64.DEFAULT));
                }

                if(appContactsContains(key))
                {
                    userDB.getWritableDatabase().update(UserDatabaseHelper.TABLE_CONTACT_NAME, values, UserDatabaseHelper.TABLE_CONTACTS_NUMBER + "=" + key, null);
                }
                else
                {
                    values.put(UserDatabaseHelper.TABLE_CONTACTS_NUMBER, key);
                    userDB.getWritableDatabase().insert(UserDatabaseHelper.TABLE_CONTACT_NAME, null, values);
                }
            }
        }
        catch (Exception e)
        {
            //Nothing to do here
        }
    }

    private boolean appContactsContains(String handyNumber)
    {
        Cursor cursor = userDB.getReadableDatabase().query(UserDatabaseHelper.TABLE_CONTACT_NAME, UserDatabaseHelper.TABLE_CONTACTS_ALLCOLUMNS, UserDatabaseHelper.TABLE_CONTACTS_NUMBER + "=" + handyNumber, null, null, null, null);
        boolean tmp = (cursor.getCount() > 0);
        cursor.close();
        return tmp;
    }
}
