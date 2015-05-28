package paukstadt.savechat.contacts;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import paukstadt.savechat.backgroundworkers.BackgroundService;
import paukstadt.savechat.chat.Chat;
import paukstadt.savechat.R;
import paukstadt.savechat.Settings;
import paukstadt.savechat.encryption.PasswordHelper;

public class ContactsList extends ActionBarActivity implements AdapterView.OnItemClickListener
{
    private ContactsListAdapter adapter = new ContactsListAdapter(this);
    private String secret = "";
    private String phone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);

        //Initialize contacts list
        ListView list = (ListView)findViewById(R.id.contacts);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

        //Get arguments which were passed from login
        Bundle args = getIntent().getExtras();
        secret = args.getString(PasswordHelper.SECRET);
        phone = args.getString(Chat.PHONE);

        //Start background service (if they weren't started after boot up)
        Intent service = new Intent(this, BackgroundService.class);
        startService(service);

        //Check if the app was started with set openDirect parameter
        String openDirect = args.getString(Chat.OPENDIRECT);
        if(openDirect != null)
        {
            //Open the given contact (identified by its phone number) directly to get to the chat
            Intent intent = new Intent(this, Chat.class);
            intent.putExtra(Chat._ID, adapter.getItemId(openDirect));
            intent.putExtra(PasswordHelper.SECRET, secret);
            intent.putExtra(Chat.PHONE, phone);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_contacts_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_settings)
        {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Intent intent = new Intent(this, Chat.class);
        intent.putExtra(Chat._ID, id);
        intent.putExtra(PasswordHelper.SECRET, secret);
        intent.putExtra(Chat.PHONE, phone);
        startActivity(intent);
    }
}
