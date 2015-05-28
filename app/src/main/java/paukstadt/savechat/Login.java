package paukstadt.savechat;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

import paukstadt.savechat.chat.Chat;
import paukstadt.savechat.contacts.ContactsList;
import paukstadt.savechat.database.UserDatabaseHelper;
import paukstadt.savechat.encryption.PasswordHelper;

public class Login extends ActionBarActivity
{
    private UserDatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private String openDirect = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Debug, replace by a simple call to init() (necessary to wait until ADB and logcat are initialized)
        Button start = (Button)findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                init();
                v.setVisibility(View.INVISIBLE);
            }
        });

        //Get extra which is only set if the activity gets started from a notification
        Bundle args = getIntent().getExtras();
        openDirect = args.getString(Chat.OPENDIRECT);
    }

    private void init()
    {
        //Get handle to the database (creating a new one if necessary)
        dbHelper = new UserDatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        //Get Views
        final ImageButton ok = (ImageButton)findViewById(R.id.password_enter);
        final TextView description = (TextView)findViewById(R.id.password_text);
        final EditText input = (EditText)findViewById(R.id.password_input);
        final EditText alias = (EditText)findViewById(R.id.password_alias);
        final EditText phone = (EditText)findViewById(R.id.password_phonenumber);

        //Register event handler to check if password is long enough (3 chars min.)
        input.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.length() >= 6) ok.setVisibility(View.VISIBLE);
                else ok.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });

        //Check if the app runs for the first time
        if(isRunningForTheFirstTime())
        {
            //Set description for first time start
            description.setText(R.string.password_text_first);

            //Display views for registration
            alias.setVisibility(View.VISIBLE);
            phone.setVisibility(View.VISIBLE);

            //Set event handler for login
            ok.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    register(input.getText().toString(), alias.getText().toString(), phone.getText().toString());
                }
            });
        }
        else
        {
            //Set description
            description.setText(R.string.password_text);

            //Set event handler for registering
            ok.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    login(input.getText().toString());
                }
            });
        }
    }

    private boolean isRunningForTheFirstTime()
    {
        //Cursor cursor = database.query(UserDatabaseHelper.TABLE_USER_NAME, UserDatabaseHelper.TABLE_USER_ALLCOLUMNS, "", null, null, null, null);
        Cursor cursor = database.rawQuery("select * from " + UserDatabaseHelper.TABLE_USER_NAME, null);
        int count = cursor.getCount();

        Log.d("Count", count + "");
        Log.d("CursorCount", cursor.getCount() + "");
        for(int i = 0; i < cursor.getColumnNames().length; i++)
        {
            Log.d("Column_" + i, cursor.getColumnNames()[i]);
        }

        cursor.close();

        return (count != 1);
    }

    private void login(String password)
    {
        //Get hash from password
        String hash = PasswordHelper.getSha1HashAsHex(password);

        //Get user data (including the hashed password)
        Cursor cursor = database.query(UserDatabaseHelper.TABLE_USER_NAME, UserDatabaseHelper.TABLE_USER_ALLCOLUMNS, "", null, null, null, null);

        //We checked already that this table only contains null or one entry in "isRunningForTheFirstTime"
        cursor.moveToFirst();

        //Get the hashed password and check for equality
        String secret = cursor.getString(cursor.getColumnIndex(UserDatabaseHelper.TABLE_USER_HASH));
        String phone = cursor.getString(cursor.getColumnIndex(UserDatabaseHelper.TABLE_USER_NUMBER));
        cursor.close();
        if(hash.equals(secret)) startApp(password, phone);
    }

    private void register(String password, String alias, String phone)
    {
        //Create the data which is written to the database
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.TABLE_USER_NUMBER, ClientServerContractClass.normalizePhoneNumber(phone));
        values.put(UserDatabaseHelper.TABLE_USER_ALIAS, alias);
        values.put(UserDatabaseHelper.TABLE_USER_ALIAS_UPDATED, true);
        values.put(UserDatabaseHelper.TABLE_USER_STATUS, "Hi, I'm using SaveChat!");
        values.put(UserDatabaseHelper.TABLE_USER_STATUS_UPDATED, false);
        values.put(UserDatabaseHelper.TABLE_USER_MIME, ClientServerContractClass.MIME_IMG_PNG);
        values.put(UserDatabaseHelper.TABLE_USER_AVATAR_UPDATED, false);
        values.put(UserDatabaseHelper.TABLE_USER_HASH, PasswordHelper.getSha1HashAsHex(password));

        //Get byte[] data from default image
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.user);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        values.put(UserDatabaseHelper.TABLE_USER_AVATAR, stream.toByteArray());
        bmp.recycle();

        //Write data to the database
        database.insert(UserDatabaseHelper.TABLE_USER_NAME, null, values);

        startApp(password, phone);
    }

    private void startApp(String password, String phone)
    {
        //Close database handles
        database.close();
        dbHelper.close();

        //Create and start intent of the main (Contacts) activity
        Intent intent = new Intent(this, ContactsList.class);
        intent.putExtra(PasswordHelper.SECRET, PasswordHelper.createSecret(password, phone));
        intent.putExtra(Chat.PHONE, phone);
        intent.putExtra(Chat.OPENDIRECT, openDirect);
        startActivity(intent);

        //Close and remove this activity from ram and history
        finish();
    }
}
