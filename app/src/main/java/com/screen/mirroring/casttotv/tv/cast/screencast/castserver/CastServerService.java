
package com.screen.mirroring.casttotv.tv.cast.screencast.castserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class CastServerService extends Service {

    public final static int SERVER_PORT = 8080;
    public final static String IP_ADDRESS = "127.0.0.1";
    public final static String ROOT_DIR = ".";
    public final static boolean QUIET = false;
    NanoHTTPD server;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.i("HTTPSERVICE", "Creating and starting httpService");
        Toast.makeText(getApplicationContext(), "Creating and starting httpService", Toast.LENGTH_SHORT).show();
        super.onCreate();

        String rootDir = intent.getStringExtra(ROOT_DIR);
        String ip = intent.getStringExtra(IP_ADDRESS);

        server = new SimpleWebServer(ip, 8080, new File(rootDir), QUIET);
        try {
            server.start();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Log.i("HTTPSERVICE", "IOException: " + ioe.getMessage());
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("HTTPSERVICE", "Destroying httpService");
        server.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }
}
