package io.vphone.vphonedispatcher;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

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
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        this.worker = new Worker();
        worker.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }




    public class Worker extends Thread {

        private boolean keepRunning = true;

        @Override
        public void run() {
            Log.v("vphone", "starting dispatcher");
            try {
                fetchMessagesAndSend();
            } catch (InterruptedException e) {
                // ignore, just get out of the thread
            }
        }

        private void fetchMessagesAndSend() throws InterruptedException {
            VPhoneDao datasource = VPhoneDao.getInstance(DispatcherService.this);
            BackendController backend = new BackendController(getDeviceKey());
            while (keepRunning) {
                final List<VPhoneSMS> values = datasource.getAllSMSs();
                for(VPhoneSMS sms: values) {
                    Log.v("vphone", "dispatching sms " + sms.getId());
                    if(backend.dispatch(sms))
                        datasource.deleteSMS(sms);
                }
                Thread.sleep(1000);
            }
        }

        public void setRunning(boolean running) {
            this.keepRunning = running;
        }
    }

    private String getDeviceKey() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        return prefs.getString(getString(R.string.device_key), null);
    }

    @Override
    public void onDestroy() {
        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
        worker.setRunning(false);
        worker.interrupt();
        try {
            worker.join();
        } catch (InterruptedException e) {
        }
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
