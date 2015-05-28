package paukstadt.savechat.backgroundworkers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootHandler extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d("BootReceiver", "BOOT_COMPLETED received. Starting service.");
        Intent service = new Intent(context, BackgroundService.class);
        context.startService(service);
        Log.d("BootReceiver", "Background service started.");
    }
}
