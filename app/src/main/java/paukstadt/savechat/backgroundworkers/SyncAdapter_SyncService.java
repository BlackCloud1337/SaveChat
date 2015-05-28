package paukstadt.savechat.backgroundworkers;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncAdapter_SyncService extends Service
{
    private static SyncAdapter_Sync sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate()
    {
        synchronized (sSyncAdapterLock)
        {
            if (sSyncAdapter == null)
            {
                sSyncAdapter = new SyncAdapter_Sync(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
