package com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.appnext.banners.BannerListener;
import com.appnext.core.AppnextError;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.DeviceConnectListener;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.OnItemClickListner;
import com.screen.mirroring.casttotv.tv.cast.screencast.ads.LoadAds;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.MediaFileModel;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.PreferencesUtility;
import com.screen.mirroring.casttotv.tv.cast.screencast.Constant;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ActivityPhotosBinding;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter.AudioAdapter;

import java.io.File;
import java.util.List;

import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isChromeCastConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isDLNACastConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity.CastDeviceListActivity.upnpServiceController;

public class AudioActivity extends AppCompatActivity implements OnItemClickListner, DeviceConnectListener {

    ActivityPhotosBinding binding;
    static List<MediaFileModel> audioList;
    AudioAdapter audioAdapter;

    static String albumName;

    LoadAds loadAds;
    public String showPlateformAd = "admob";

    private CastSession mCastSession;
    private CastContext mCastContext;
    private SessionManager sessionManager;
    private final SessionManagerListener<CastSession> mSessionManagerListener =
            new MySessionManagerListener();

    public void setData(List<MediaFileModel> data, String folderName) {
        audioList = data;
        albumName = folderName;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_photos);


        binding.toolbar.setTitle(albumName);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadAds = LoadAds.getInstance(this);
        showPlateformAd = PreferencesUtility.getShowPlateformAd(this);

        mCastContext = CastContext.getSharedInstance(this);
        sessionManager = mCastContext.getSessionManager();
        mCastSession = sessionManager.getCurrentCastSession();

        if (audioList.size() > 10) {
//            loadBanner();
        }

        audioAdapter = new AudioAdapter(this);
        audioAdapter.setOnItemClickListner(this);

        initView();
    }

    public void initView() {
        if (showPlateformAd.equalsIgnoreCase(Constant.ADMOB_AD_KEY)) {
            loadAds.loadNativeAd(binding.fram, binding.advertizeLayout, R.layout.ads_unified1);
        } else if (showPlateformAd.equalsIgnoreCase(Constant.FACEBOOK_AD_KEY)) {
            loadAds.loadfacebookNativeAd(binding.fram, binding.advertizeLayout, R.layout.ads_unified_facebook);
        } else if (showPlateformAd.equalsIgnoreCase(Constant.APP_NEXT_AD_KEY)) {
            loadAds.loadAppNextNativeAd(binding.fram, binding.advertizeLayout, R.layout.ads_unified_app_next);
        }

        binding.progressBar.setVisibility(View.GONE);
        binding.emptyView.setVisibility(View.GONE);
        binding.photosListView.setLayoutManager(new LinearLayoutManager(this));
        audioAdapter.setImageDataList(audioList);
        binding.photosListView.setAdapter(audioAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (isConnected) {
            menu.findItem(R.id.media_route_menu_item).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_cast_connected));
        } else
            menu.findItem(R.id.media_route_menu_item).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_cast));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.media_route_menu_item:
                CastDeviceListActivity castDeviceListActivity = new CastDeviceListActivity();
                castDeviceListActivity.setDeviceConnectListener(this);
                startActivityForResult(new Intent(this, CastDeviceListActivity.class), 100);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (upnpServiceController != null) {
            upnpServiceController.resume(this);
        }
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
    public void onDeviceConnect(boolean isConnected) {
        Constant.isConnected = isConnected;
        invalidateOptionsMenu();
    }

    @Override
    public void onItemClick(int position) {
        if (!isConnected) {
            startActivity(new Intent(AudioActivity.this, CastDeviceListActivity.class));
            return;
        } else {
            if (isChromeCastConnected) {
                if (mCastSession == null) {
                    startActivity(new Intent(AudioActivity.this, CastDeviceListActivity.class));
                    return;
                }
            } else if (isDLNACastConnected) {
                if (upnpServiceController.getSelectedRenderer() == null) {
                    startActivity(new Intent(AudioActivity.this, CastDeviceListActivity.class));
                    return;
                }
            }
        }

        AudioPlayActivity videoPlayActivity = new AudioPlayActivity();
        videoPlayActivity.setData(audioList);
        Intent intent = new Intent(this, AudioPlayActivity.class);
        intent.putExtra("extra_position", position);
        startActivity(intent);
    }

    public void viewMediaFile(MediaFileModel mediaFileModel) {
        File file = new File(mediaFileModel.getFilePath());

        Uri fromFile;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                fromFile = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            } catch (Exception e) {
                fromFile = Uri.fromFile(file);
            }
        } else {
            fromFile = Uri.fromFile(file);
        }

        Intent editIntent = new Intent(Intent.ACTION_VIEW);
        editIntent.setDataAndType(fromFile, "audio/*");
        editIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(editIntent, null));
    }

    private class MySessionManagerListener implements SessionManagerListener<CastSession> {

        @Override
        public void onSessionEnded(CastSession session, int error) {
            if (session == mCastSession) {
                mCastSession = null;
            }
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionResumed(CastSession session, boolean wasSuspended) {
            mCastSession = session;
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionStarted(CastSession session, String sessionId) {
            mCastSession = session;
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionStarting(CastSession session) {

        }

        @Override
        public void onSessionStartFailed(CastSession session, int error) {
        }

        @Override
        public void onSessionEnding(CastSession session) {
        }

        @Override
        public void onSessionResuming(CastSession session, String sessionId) {
        }

        @Override
        public void onSessionResumeFailed(CastSession session, int error) {
        }

        @Override
        public void onSessionSuspended(CastSession session, int reason) {
        }
    }
}
