package io.vphone.vphonedispatcher;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DispatcherService extends Service {


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

        this.worker = new Worker();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        Log.v("Service DB", "Getting all SMSes");

        Thread m = new Thread(this.worker);
        m.start();


        return START_STICKY;
    }


    public class Worker implements Runnable {

        private boolean keepRunning = true;

        @Override
        public void run() {
            try {
                fetchMessagesAndSend();
            } catch (InterruptedException e) {
                // ignore, just get out of the thread
            }
        }

        private void fetchMessagesAndSend() throws InterruptedException {
            VPhoneDao datasource = new VPhoneDao(DispatcherService.this);
            datasource.open();
            BackendController backend = new BackendController(datasource.getSetting(VPhoneDao.DEVICE_KEY));
            try {
                while (keepRunning) {
                    final List<VPhoneSMS> values = datasource.getAllSMSs();
                    for(VPhoneSMS sms: values) {
                        if(backend.dispatch(sms))
                            datasource.deleteSMS(sms);
                    }
                    Thread.sleep(1000);
                }
            }finally {
                datasource.close();
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
