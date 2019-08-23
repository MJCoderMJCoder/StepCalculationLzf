package com.lzf.stepcalculationlzf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        //        context.startService(new Intent(context, SensorListener.class));
        Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivityIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.startActivity(mainActivityIntent);
    }
}
