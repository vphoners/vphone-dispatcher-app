package io.vphone.vphonedispatcher;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DispatcherService extends Service {
    public static boolean isStarted = false;

    public final static String SERVICE_URL = "https://vphone.io/api/sms";

    private volatile VPhoneDao datasource;
    private Worker worker;

    public DispatcherService() {
    }
//    private NotificationManager mNM;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        DispatcherService getService() {
            return DispatcherService.this;
        }
    }

    @Override
    public void onCreate() {
//        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        isStarted = true;

        datasource = new VPhoneDao(this);
        datasource.open();


        this.worker = new Worker(datasource);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        Log.v("Service DB", "Getting all SMSes");

        Thread m = new Thread(this.worker);
        m.start();


        return START_NOT_STICKY;
    }


    private class Worker implements Runnable {

        private VPhoneDao datasource;
        private boolean keepRunning;

        public Worker(VPhoneDao datasource) {
            this.datasource = datasource;
            this.keepRunning = true;
        }

        @Override
        public void run() {

            try {
                fetchMessagesAndSend();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void fetchMessagesAndSend() throws JSONException {


            final List<VPhoneSMS> values = datasource.getAllSMSs(1, true);
            Log.v("Current SMSes in DB", "Current SMSes in database: " + values);

            final JSONObject jsonInfo = new JSONObject();
            final JSONArray jsonMsgArray = new JSONArray();
            try {

                //for (VPhoneSMS smsToSend : values) {

                if (values != null && values.size() > 0 && values.get(0) != null) {
                    jsonInfo.put("from", values.get(0).getSmsfrom());
                    jsonInfo.put("timestamp", values.get(0).getSmstimestamp());
                    jsonInfo.put("body", values.get(0).getSmsbody());

                    datasource.updateProcessingSMS(values.get(0), true);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.v("Sending", jsonInfo.toString());
            CustomAsyncTask sendSms = new CustomAsyncTask(SERVICE_URL, RequestMethod.POST, null, jsonInfo, new CustomAsyncTaskExecution<JSONObject>() {

                @Override
                public void preExecution() {

                }

                @Override
                public void postExecution(JSONObject resultParam) {
                    try {
                        if (resultParam != null && resultParam.get("success").equals("true")) {
                            for (VPhoneSMS sentSMS : values) {
                                datasource.deleteSMS(sentSMS);
                            }
                        } else {
                            for (VPhoneSMS sentSMS : values) {
                                datasource.updateProcessingSMS(sentSMS, false);
                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            if (keepRunning) {
                if (jsonInfo.has("from")) {
                    sendSms.start();
                }
                try {
                    Thread.sleep(1000);
                    fetchMessagesAndSend();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }

        public void setRunning(boolean running) {
            this.keepRunning = running;
        }
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
//        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
        worker.setRunning(false);
        isStarted = false;


        datasource.close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_started, Toast.LENGTH_SHORT).show();
    }
}
