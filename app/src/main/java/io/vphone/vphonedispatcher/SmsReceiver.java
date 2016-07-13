package io.vphone.vphonedispatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
    private VPhoneDao datasource;

    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            datasource = new VPhoneDao(context);
            datasource.open();
            
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;
            if (bundle != null){
                //---retrieve the SMS message received---
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        long timestamp = msgs[i].getTimestampMillis();
                        String msg_from = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();

                        datasource.createSms(msgBody, msg_from, String.valueOf(timestamp));
                    }


                }catch(Exception e){
//                            Log.d("Exception caught",e.getMessage());
                }
            }
            datasource.close();
        }
    }
}
