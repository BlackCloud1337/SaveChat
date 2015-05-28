package paukstadt.savechat.chat;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Date;

import paukstadt.savechat.ClientServerContractClass;
import paukstadt.savechat.R;
import paukstadt.savechat.contacts.ContactsListDTO;
import paukstadt.savechat.encryption.PasswordHelper;

public class Chat extends ActionBarActivity implements View.OnClickListener
{
    public static final String _ID = "id";
    public static final String PHONE = "phone";
    public static final String OPENDIRECT = "open";

    private ChatListAdapter adapter;
    private String phone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Get secret to en-/decrypt database
        Bundle args = getIntent().getExtras();
        String secret = args.getString(PasswordHelper.SECRET);
        phone = args.getString(PHONE);

        //Get views
        ListView output = (ListView)findViewById(R.id.chat_output);
        ImageButton send = (ImageButton)findViewById(R.id.chat_send);

        //Set listener for send button
        send.setOnClickListener(this);

        //Set adapter for chat with secret to decrypt database
        adapter = new ChatListAdapter(this, secret, args.getInt(Chat._ID), phone);
        output.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_details)
        {
            //ToDo: Create AlertWindow to display connection details
            Toast.makeText(this, "Details clicked!", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(final View v)
    {
        //Get views
        EditText input = (EditText)findViewById(R.id.chat_input);
        final ProgressBar progress = (ProgressBar)findViewById(R.id.chat_progress);

        //Check if message to send is valid
        String msg = input.getText().toString();
        if (msg.equals("")) return;

        //Deactivate send button and display progress
        v.setEnabled(false);
        v.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.VISIBLE);
        progress.setIndeterminate(true);

        //Create connection info
        ContactsListDTO contactInfo = adapter.getContactInfo();
        ChatConnectionInfo connectionInfo = new ChatConnectionInfo(contactInfo.getIp(), contactInfo.getPort(), ClientServerContractClass.MIME_TEXT, msg.getBytes(), phone);

        //Start background task to send message
        new ChatSender()
        {
            @Override
            protected void onPostExecute(ChatConnectionInfo result)
            {
                if(result.getSuccess())
                {
                    ChatDTO dto = new ChatDTO();
                    dto.setId(-1); //-1 represents a new db entry
                    dto.setMime(result.getMime());
                    dto.setContent(result.getContent());
                    dto.setTimestamp(new Date());
                    dto.setFromMe(true);

                    adapter.addItem(dto);
                    adapter.notifyDataSetChanged();
                }
                else Toast.makeText(getApplicationContext(), "Connection error! Try again later.", Toast.LENGTH_LONG).show();

                //Activate send button and hide progress
                v.setEnabled(true);
                v.setVisibility(View.VISIBLE);
                progress.setVisibility(View.INVISIBLE);
                progress.setIndeterminate(false);
            }
        }.execute(connectionInfo);
    }
}
