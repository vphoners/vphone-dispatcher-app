package io.vphone.vphonedispatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

public class ServiceStarter extends BroadcastReceiver {
    private VPhoneDao datasource;

    public ServiceStarter() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        datasource = new VPhoneDao(context);
        datasource.open();
        try {

            boolean shouldStart = Boolean.valueOf(datasource.getSetting(VPhoneDao.START_SERVICE));
            if (shouldStart) {
                context.startService(new Intent(context, DispatcherService.class));
            }
        }finally {
            datasource.close();
        }
    }
}
