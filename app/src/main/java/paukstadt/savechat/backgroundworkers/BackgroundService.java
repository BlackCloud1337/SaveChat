package paukstadt.savechat.backgroundworkers;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class BackgroundService extends Service
{
    public static final String AUTHORITY = "paukstadt.savechat.provider";
    public static final String ACCOUNT_TYPE = "paukstadt.savechat.com";
    public static final String ACCOUNT = "savechat_sync_account";

    Account account;

    @Override
    public void onCreate()
    {
        //Create MessageReceiver-Thread here. This way it will only be created one time
        //because onCreate is only called if the service is created whereas onStartCommand is called each time
        //a call to startService(thisService) is made.
        new Thread(new MessageReceiver(this)).start();

        //Create account and set behaviour for sync adapter
        account = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager manager = (AccountManager)getSystemService(ACCOUNT_SERVICE);
        manager.addAccountExplicitly(account, null, null);
        getContentResolver().setSyncAutomatically(account, AUTHORITY, true);
        getContentResolver().addPeriodicSync(account, AUTHORITY, Bundle.EMPTY, 60L * 10L);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //Only return that this service should be restarted if it gets destroyed by the system due to low resources
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        //Return null because we don't need binding capabilities for the moment
        return null;
    }

    @Override
    public void onDestroy()
    {

    }
}
