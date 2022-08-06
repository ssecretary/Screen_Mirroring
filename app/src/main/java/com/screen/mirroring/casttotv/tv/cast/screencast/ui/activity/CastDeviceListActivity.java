package com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.controller.Factory;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.controller.IUpnpServiceController;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.ARendererState;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IDeviceDiscoveryObserver;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IRendererCommand;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IUpnpDevice;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.DeviceConnectListener;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.OnItemClickListner;
import com.screen.mirroring.casttotv.tv.cast.screencast.Constant;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ActivityCastDeviceBinding;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IFactory;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter.CastDeviceAdapter;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.SELECTED_DEVICE_POSITION;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.castDeviceList;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isChromeCastConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isDLNACastConnected;

public class CastDeviceListActivity extends AppCompatActivity implements OnItemClickListner, IDeviceDiscoveryObserver, Observer {

    ActivityCastDeviceBinding binding;

    CastDeviceAdapter castDeviceAdapter;
    static DeviceConnectListener deviceConnectListener;

    private CastSession mCastSession;
    MediaRouter mMediaRouter;
    MediaRouteSelector mediaRouteSelector;
    private CastContext mCastContext;
    private final SessionManagerListener<CastSession> mSessionManagerListener =
            new MySessionManagerListener();
    boolean isConnectionStart = false;

    List<Object> castDeviceList1;

    public static IUpnpServiceController upnpServiceController = null;
    public static IFactory factory = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cast_device);

        binding.toolbar.setTitle(getString(R.string.title_cast_devices));
        binding.toolbar.setNavigationIcon(R.drawable.ic_close);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        castDeviceList1 = new ArrayList<>();
        castDeviceAdapter = new CastDeviceAdapter(this);
        castDeviceAdapter.setOnItemClickListner(this);

        mCastContext = CastContext.getSharedInstance(this);

        mMediaRouter = MediaRouter.getInstance(this);
        mediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)).build();

        initView();

        if (castDeviceList != null && castDeviceList.size() > 0) {
            castDeviceList1.addAll(castDeviceList);
            castDeviceAdapter.notifyDataSetChanged();
            binding.searchLayout.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
        } else {
            displayCastDeviceList();
        }

        if (Constant.isChromeCastConnected || isDLNACastConnected) {
            binding.discoonect.setVisibility(View.VISIBLE);
        } else {
            binding.discoonect.setVisibility(View.GONE);
        }
    }

    public void displayCastDeviceList() {

        castDeviceList1.clear();
        castDeviceAdapter.notifyDataSetChanged();

        getDevicesList();

        if (factory == null)
            factory = new Factory();

        // Upnp service
        if (upnpServiceController == null)
            upnpServiceController = factory.createUpnpServiceController(this);

        if (upnpServiceController != null) {
            upnpServiceController.getRendererDiscovery().addObserver(this);
            upnpServiceController.addSelectedRendererObserver(this);
        }
    }

    public void setDeviceConnectListener(DeviceConnectListener deviceConnectListener) {
        this.deviceConnectListener = deviceConnectListener;
    }

    public void initView() {
        binding.castDeviceList.setLayoutManager(new LinearLayoutManager(this));
        binding.castDeviceList.setAdapter(castDeviceAdapter);
        castDeviceAdapter.setFolderDataList(castDeviceList1);

        binding.discoonect.setOnClickListener(v -> {
            if (isChromeCastConnected) {
                disconnectChromeCast();
                Toast.makeText(CastDeviceListActivity.this, getString(R.string.device_disconnect_successfully), Toast.LENGTH_SHORT).show();
            } else if (isDLNACastConnected) {
                disconnectDLNA();
                Toast.makeText(CastDeviceListActivity.this, getString(R.string.device_disconnect_successfully), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getDevicesList() {
        startRouteScan(5000L, new ScanCallback() {
            @Override
            void onRouteUpdate(List<MediaRouter.RouteInfo> routes) {
                if (getContext().getCastState() != CastState.NO_DEVICES_AVAILABLE) {
                    stopRouteScan(this);
                    CastSession session = getSessionManager().getCurrentCastSession();
                    if (session != null) {
                    }
                }

                if (routes != null && routes.size() > 0) {
                    for (MediaRouter.RouteInfo routeInfo : routes) {
                        CastDevice device = CastDevice.getFromBundle(routeInfo.getExtras());
                        boolean duplicate = false;

                        if (device != null) {
                            for (int i = 0; i < castDeviceList1.size(); i++) {
                                if (castDeviceList1.get(i) != null && castDeviceList1.get(i) instanceof MediaRouter.RouteInfo) {
                                    MediaRouter.RouteInfo castDevice = (MediaRouter.RouteInfo) castDeviceList1.get(i);
                                    if (castDevice != null) {
                                        if (castDevice.getName().equals(routeInfo.getName())) {
                                            duplicate = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (!duplicate && device != null) {
                                binding.progressBar.setVisibility(View.GONE);
                                binding.searchLayout.setVisibility(View.GONE);
                                castDeviceList1.add(routeInfo);
                                castDeviceList.add(routeInfo);
                                castDeviceAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        }, null);
    }

    public void disconnectDLNA() {
        isDLNACastConnected = false;
        SELECTED_DEVICE_POSITION = "";
        castDeviceAdapter.notifyDataSetChanged();
        ARendererState rendererState = factory.createRendererState();
        IRendererCommand rendererCommand = factory.createRendererCommand(rendererState);
        if (rendererCommand != null) {
            rendererCommand.commandStop();
            rendererCommand.pause();
        }

//        upnpServiceController.pause();
        upnpServiceController.getServiceListener().getServiceConnexion().onServiceDisconnected(null);
        upnpServiceController.getRendererDiscovery().removeObserver(this);
        upnpServiceController.delSelectedRendererObserver(this);
        upnpServiceController.getServiceListener().clearListener();
        isConnected = false;
        Constant.isChromeCastConnected = false;
        if (deviceConnectListener != null) {
            deviceConnectListener.onDeviceConnect(false);
        }
        binding.discoonect.setVisibility(View.GONE);
    }

    public void disconnectChromeCast() {
        Constant.isChromeCastConnected = false;
        SessionManager mSessionManager = mCastContext.getSessionManager();
        mSessionManager.endCurrentSession(true);

        mMediaRouter.unselect(MediaRouter.UNSELECT_REASON_DISCONNECTED);

        isConnected = false;
        Constant.isChromeCastConnected = false;
        if (deviceConnectListener != null) {
            deviceConnectListener.onDeviceConnect(false);
        }

        SELECTED_DEVICE_POSITION = "";
        castDeviceAdapter.notifyDataSetChanged();
        binding.discoonect.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (upnpServiceController != null) {
            upnpServiceController.resume(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        CastContext.getSharedInstance(this).getSessionManager()
                .addSessionManagerListener(mSessionManagerListener, CastSession.class);
        super.onStart();
    }

    @Override
    public void onStop() {
        CastContext.getSharedInstance(this).getSessionManager()
                .removeSessionManagerListener(mSessionManagerListener, CastSession.class);
        super.onStop();
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        return mCastContext.onDispatchVolumeKeyEventBeforeJellyBean(event)
                || super.dispatchKeyEvent(event);
    }

    @Override
    protected void onDestroy() {
        Log.v("Activity", "ondestroy");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cast_device, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_how_to_use:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
            case R.id.action_refresh:
                castDeviceList.clear();
                binding.searchLayout.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.VISIBLE);
                displayCastDeviceList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    public void onItemClick(int position) {
        if (castDeviceList1 != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
            if (castDeviceList1.get(position) instanceof MediaRouter.RouteInfo) {
                if (isDLNACastConnected) {
                    disconnectDLNA();
                }
                Constant.isChromeCastConnected = true;
                Constant.isDLNACastConnected = false;
                getMediaRouter().selectRoute((MediaRouter.RouteInfo) castDeviceList1.get(position));
            } else if (castDeviceList1.get(position) instanceof IUpnpDevice) {
                if (isChromeCastConnected) {
                    disconnectChromeCast();
                }
                Constant.isDLNACastConnected = true;
                Constant.isChromeCastConnected = false;
                IUpnpDevice device = (IUpnpDevice) castDeviceList1.get(position);
                upnpServiceController.setSelectedRenderer(device, false);
                isConnected = true;
                if (deviceConnectListener != null) {
                    deviceConnectListener.onDeviceConnect(true);
                }
                binding.progressBar.setVisibility(View.GONE);
                binding.discoonect.setVisibility(View.VISIBLE);
                Toast.makeText(CastDeviceListActivity.this, getString(R.string.device_connect_successfully), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void addedDevice(IUpnpDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.progressBar.setVisibility(View.GONE);
                binding.searchLayout.setVisibility(View.GONE);
                if (castDeviceList1 != null && castDeviceList1.size() > 0) {
                    boolean duplicate = false;
                    for (int i = 0; i < castDeviceList1.size(); i++) {

                        if (castDeviceList1.get(i) != null && castDeviceList1.get(i) instanceof IUpnpDevice) {
                            IUpnpDevice castDevice = (IUpnpDevice) castDeviceList1.get(i);
                            if (castDevice != null) {
                                if (castDevice.getFriendlyName().equalsIgnoreCase(device.getFriendlyName())) {
                                    duplicate = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (!duplicate && device != null) {
                        castDeviceList1.add(device);
                        castDeviceList.add(device);
                        castDeviceAdapter.notifyDataSetChanged();
                    }
                } else {
                    castDeviceList1.add(device);
                    castDeviceList.add(device);
                    castDeviceAdapter.notifyDataSetChanged();
                }

//                if (upnpServiceController != null && upnpServiceController.getSelectedRenderer() != null) {
//                    if (device.equals(upnpServiceController.getSelectedRenderer())) {
//                        SELECTED_DEVICE_POSITION = device;
//                        castDeviceAdapter.notifyDataSetChanged();
//                    }
//                }
                Log.v("TAG", "New device detected : " + device.getDisplayString());
            }
        });
    }

    @Override
    public void removedDevice(IUpnpDevice device) {
        Log.e("TAG ------->", "Device removed : " + device.getFriendlyName());
    }

    public void updateDeviceList(CastDevice selectedDevice) {
        for (Object object : castDeviceList) {
            if (object instanceof MediaRouter.RouteInfo) {
                MediaRouter.RouteInfo castDevice = (MediaRouter.RouteInfo) object;
                CastDevice device = CastDevice.getFromBundle(castDevice.getExtras());
                if (device.getFriendlyName().equals(selectedDevice.getFriendlyName())) {
                    SELECTED_DEVICE_POSITION = castDevice;
                    castDeviceAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        Log.v("TAG", "Device update : ");
    }

    private class   MySessionManagerListener implements SessionManagerListener<CastSession> {

        @Override
        public void onSessionEnded(CastSession session, int error) {
            Log.e("onSessionEnded ------->", String.valueOf(error));
            binding.progressBar.setVisibility(View.GONE);
            if (session == mCastSession) {
                mCastSession = null;
            }
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionResumed(CastSession session, boolean wasSuspended) {
            isConnected = true;
            isConnectionStart = false;
            if (deviceConnectListener != null) {
                deviceConnectListener.onDeviceConnect(true);
            }

            isChromeCastConnected = true;
            updateDeviceList(session.getCastDevice());
            binding.discoonect.setVisibility(View.VISIBLE);
            mCastSession = session;
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionStarted(CastSession session, String sessionId) {
            isConnected = true;
            isConnectionStart = false;
            if (deviceConnectListener != null) {
                deviceConnectListener.onDeviceConnect(true);
            }
            binding.discoonect.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);

            Toast.makeText(CastDeviceListActivity.this, getString(R.string.device_connect_successfully), Toast.LENGTH_SHORT).show();
            mCastSession = session;
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionStarting(CastSession session) {
            isConnectionStart = true;
            Toast.makeText(CastDeviceListActivity.this, "Connection starting....", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSessionStartFailed(CastSession session, int error) {
            SELECTED_DEVICE_POSITION = "";
            castDeviceAdapter.notifyDataSetChanged();
        }

        @Override
        public void onSessionEnding(CastSession session) {
            SELECTED_DEVICE_POSITION = "";
            castDeviceAdapter.notifyDataSetChanged();
        }

        @Override
        public void onSessionResuming(CastSession session, String sessionId) {
        }

        @Override
        public void onSessionResumeFailed(CastSession session, int error) {
        }

        @Override
        public void onSessionSuspended(CastSession session, int reason) {
            SELECTED_DEVICE_POSITION = "";
            castDeviceAdapter.notifyDataSetChanged();
        }
    }

    public abstract static class ScanCallback extends MediaRouter.Callback {
        /**
         * Called whenever a route is updated.
         *
         * @param routes the currently available routes
         */
        abstract void onRouteUpdate(List<MediaRouter.RouteInfo> routes);

        /**
         * records whether we have been stopped or not.
         */
        private boolean stopped = false;
        /**
         * Global mediaRouter object.
         */
        private MediaRouter mediaRouter;

        /**
         * Sets the mediaRouter object.
         *
         * @param router mediaRouter object
         */
        void setMediaRouter(MediaRouter router) {
            this.mediaRouter = router;
        }

        /**
         * Call this method when you wish to stop scanning.
         * It is important that it is called, otherwise battery
         * life will drain more quickly.
         */
        void stop() {
            stopped = true;
        }

        private void onFilteredRouteUpdate() {
            if (stopped || mediaRouter == null) {
                return;
            }
            List<MediaRouter.RouteInfo> outRoutes = new ArrayList<>();
            // Filter the routes
            for (MediaRouter.RouteInfo route : mediaRouter.getRoutes()) {
                // We don't want default routes, or duplicate active routes
                // or multizone duplicates https://github.com/jellyfin/cordova-plugin-chromecast/issues/32
                Bundle extras = route.getExtras();
                if (extras != null) {
                    CastDevice.getFromBundle(extras);
                    if (extras.getString("com.google.android.gms.cast.EXTRA_SESSION_ID") != null) {
                        continue;
                    }
                }
                if (route != null && route.getDescription() != null) {
                    if (!route.isDefault()
                            && !route.getDescription().equals("Google Cast Multizone Member")
                            && route.getPlaybackType() == MediaRouter.RouteInfo.PLAYBACK_TYPE_REMOTE
                    ) {
                        outRoutes.add(route);
                    }
                }
            }
            onRouteUpdate(outRoutes);
        }

        @Override
        public final void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
            onFilteredRouteUpdate();
        }

        @Override
        public final void onRouteChanged(MediaRouter router, MediaRouter.RouteInfo route) {
            onFilteredRouteUpdate();
        }

        @Override
        public final void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
            onFilteredRouteUpdate();
        }

    }

    public void startRouteScan(Long timeout, ScanCallback callback, Runnable onTimeout) {
        // Add the callback in active scan mode
        runOnUiThread(new Runnable() {
            public void run() {
                callback.setMediaRouter(getMediaRouter());

                if (timeout != null && timeout == 0) {
                    // Send out the one time routes
                    callback.onFilteredRouteUpdate();
                    return;
                }

                // Add the callback in active scan mode
                getMediaRouter().addCallback(new MediaRouteSelector.Builder()
                                .addControlCategory(CastMediaControlIntent.categoryForCast(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID))
                                .build(),
                        callback,
                        MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);

                // Send out the initial routes after the callback has been added.
                // This is important because if the callback calls stopRouteScan only once, and it
                // happens during this call of "onFilterRouteUpdate", there must actually be an
                // added callback to remove to stop the scan.
                callback.onFilteredRouteUpdate();

                if (timeout != null) {
                    // remove the callback after timeout ms, and notify caller
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // And stop the scan for routes
                            getMediaRouter().removeCallback(callback);
                            // Notify
                            if (onTimeout != null) {
                                onTimeout.run();
                            }
                        }
                    }, timeout);
                }
            }
        });
    }

    public void stopRouteScan(ScanCallback callback) {
        runOnUiThread(new Runnable() {
            public void run() {
                callback.stop();
                getMediaRouter().removeCallback(callback);
            }
        });
    }

    private MediaRouter getMediaRouter() {
        return MediaRouter.getInstance(this);
    }

    private CastContext getContext() {
        return CastContext.getSharedInstance(this);
    }

    private SessionManager getSessionManager() {
        return getContext().getSessionManager();
    }

    private static InetAddress getLocalIpAdressFromIntf(String intfName) {
        try {
            NetworkInterface intf = NetworkInterface.getByName(intfName);
            if (intf.isUp()) {
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address)
                        return inetAddress;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("TAG", "Unable to get ip adress for interface " + intfName);
        }
        return null;
    }

    public static InetAddress getLocalIpAddress(Context ctx) throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        if (ipAddress != 0)
            return InetAddress.getByName(String.format("%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff)));

        Log.d("TAG", "No ip adress available throught wifi manager, try to get it manually");

        InetAddress inetAddress;

        inetAddress = getLocalIpAdressFromIntf("wlan0");
        if (inetAddress != null) {
            Log.d("TAG", "Got an ip for interfarce wlan0");
            return inetAddress;
        }

        inetAddress = getLocalIpAdressFromIntf("usb0");
        if (inetAddress != null) {
            Log.d("TAG", "Got an ip for interfarce usb0");
            return inetAddress;
        }

        return InetAddress.getByName("0.0.0.0");
    }
}
