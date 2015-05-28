package paukstadt.savechat.backgroundworkers;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncAdapter_AuthenticatorService extends Service
{
    private SyncAdapter_Authenticator mAuthenticator;

    @Override
    public void onCreate()
    {
        mAuthenticator = new SyncAdapter_Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mAuthenticator.getIBinder();
    }
}