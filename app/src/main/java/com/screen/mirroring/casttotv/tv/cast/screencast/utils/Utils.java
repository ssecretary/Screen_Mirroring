package com.screen.mirroring.casttotv.tv.cast.screencast.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.android.gms.cast.MediaQueueItem;
import com.screen.mirroring.casttotv.tv.cast.screencast.BuildConfig;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Utils {
    private static final String TAG = "Utils";

    public static boolean isOrientationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT;
    }

    public static String getFilenameFromPath(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String getParentPath(String path) {
        if (path.endsWith(getFilenameFromPath(path))) {
            return path.substring(0, path.length() - getFilenameFromPath(path).length());
        }
        return "";
    }

    public static String formatMillis(int millisec) {
        int seconds = (int) (millisec / 1000);
        int hours = seconds / (60 * 60);
        seconds %= (60 * 60);
        int minutes = seconds / 60;
        seconds %= 60;

        String time;
        if (hours > 0) {
            time = String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            time = String.format(Locale.ROOT, "%d:%02d", minutes, seconds);
        }
        return time;
    }

    public static final String makeShortTimeString(final Context context, long secs) {
        int totalSeconds = (int) (secs);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    public static void shareApp(Context context) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String appUrl = "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;
            shareIntent.putExtra(Intent.EXTRA_TEXT, appUrl);
            context.startActivity(shareIntent);
        } catch (Exception e) {
            e.printStackTrace();
            //e.toString();
        }
    }

    public static void rateusApp(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
        } catch (android.content.ActivityNotFoundException anfe) {
            anfe.printStackTrace();
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void showErrorDialog(Context context, String errorString) {
        new AlertDialog.Builder(context).setTitle(R.string.action_error)
                .setMessage(errorString)
                .setPositiveButton(R.string.action_ok, (DialogInterface.OnClickListener) (dialog, id) -> dialog.cancel())
                .create()
                .show();
    }

    public static String getMimeTypeFromFilePath(String filePath) {
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFilenameExtension(filePath));
        return (mimeType == null) ? null : mimeType;
    }

    public static String getFilenameExtension(String path) {
        return path.substring(path.lastIndexOf(".") + 1);
    }

    public static String getMimeType(Context context, Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static boolean isConnectedToLocalNetwork(Context context) {
        boolean connected = false;
        if (!connected) {
            Log.d(TAG, "isConnectedToLocalNetwork: see if it is an WIFI");
            connected = isConnectedToWifi(context);
        }
        if (!connected) {
            Log.d(TAG, "isConnectedToLocalNetwork: see if it is an Ethernet");
            connected = isConnectedToEthernet(context);
        }
        if (!connected) {
            Log.d(TAG, "isConnectedToLocalNetwork: see if it is an WIFI AP");
            connected = isConnectedToWifiAP(context);
        }
        if (!connected) {
            Log.d(TAG, "isConnectedToLocalNetwork: see if it is an USB AP");
            connected = isConnectedToUSBAP(context);
        }
        if (!connected) {
            Log.d(TAG, "isConnectedToLocalNetwork: see if it is an Mobile");
            connected = isConnectedToMobile(context);
        }
        return connected;
    }

    public static boolean isConnectedToMobile(Context context) {
        return isConnectedToNetwork(context, ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean isConnectedToWifi(Context context) {
        return isConnectedToNetwork(context, ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isConnectedToEthernet(Context context) {
        return isConnectedToNetwork(context, ConnectivityManager.TYPE_ETHERNET);
    }

    public static boolean isConnectedToNetwork(Context context, int type) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean connected = networkInfo != null && networkInfo.isConnectedOrConnecting()
                && (networkInfo.getType() == type);
        return connected;
    }

    public static boolean isConnectedToWifiAP(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        boolean connected = false;
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            connected = (Boolean) method.invoke(wifiManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return connected;
    }

    public static boolean isConnectedToUSBAP(Context context) {
        boolean connected = false;
        try {
            for (NetworkInterface netInterface : Collections.list(NetworkInterface
                    .getNetworkInterfaces())) {
                if (netInterface.getDisplayName().startsWith("rndis")) {
                    connected = true;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return connected;
    }

    public static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        }};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @SuppressLint("BadHostnameVerifier")
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static MediaQueueItem[] rebuildQueueAndAppend(List<MediaQueueItem> items,
                                                         MediaQueueItem currentItem) {
        if (items == null || items.isEmpty()) {
            return new MediaQueueItem[]{currentItem};
        }
        MediaQueueItem[] rebuiltQueue = new MediaQueueItem[items.size() + 1];
        for (int i = 0; i < items.size(); i++) {
            rebuiltQueue[i] = rebuildQueueItem(items.get(i));
        }
        rebuiltQueue[items.size()] = currentItem;

        return rebuiltQueue;
    }

    public static MediaQueueItem rebuildQueueItem(MediaQueueItem item) {
        return new MediaQueueItem.Builder(item).clearItemId().build();
    }

    public static MediaQueueItem[] rebuildQueue(List<MediaQueueItem> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        MediaQueueItem[] rebuiltQueue = new MediaQueueItem[items.size()];
        for (int i = 0; i < items.size(); i++) {
            rebuiltQueue[i] = rebuildQueueItem(items.get(i));
        }

        return rebuiltQueue;
    }

    public static int getBottomNavigationHeight(Context context) {
        Resources resources = context.getResources();
        if (hasNavigationBar(resources)) {
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
        }

        return 0;
    }

    private static boolean hasNavigationBar(Resources resources) {
        int hasNavBarId = resources.getIdentifier("config_showNavigationBar",
                "bool", "android");
        return hasNavBarId > 0 && resources.getBoolean(hasNavBarId);
    }
}
