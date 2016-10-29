package io.vphone.vphonedispatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

public class ServiceStarter extends BroadcastReceiver {
    private VPhoneDao datasource;

    public ServiceStarter() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        datasource = VPhoneDao.getInstance(context);
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean shouldStart = prefs.getBoolean(context.getString(R.string.service_enabled), false);
        if (shouldStart) {
            context.startService(new Intent(context, DispatcherService.class));
        }
    }
}
