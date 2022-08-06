package com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.mediaserver.MediaServer;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.ARendererState;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IRendererCommand;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.QueueDataProvider;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.DeviceConnectListener;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.OnItemClickListner;
import com.screen.mirroring.casttotv.tv.cast.screencast.castserver.CastServerService;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.MediaFileModel;
import com.screen.mirroring.casttotv.tv.cast.screencast.Constant;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.RemoteMediaClientListener;
import com.screen.mirroring.casttotv.tv.cast.screencast.WebServerController;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ActivityFullViewBinding;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter.FullImageViewPagerAdapter;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.IMAGE_PREFIX;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isChromeCastConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isDLNACastConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity.CastDeviceListActivity.factory;
import static com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity.CastDeviceListActivity.upnpServiceController;

public class FullImageViewActivity extends AppCompatActivity implements OnItemClickListner, DeviceConnectListener {

    ActivityFullViewBinding binding;
    private static List<MediaFileModel> imagesData = new ArrayList<>();

    private int currentPage = 0;
    int position;
    private FullImageViewPagerAdapter fullImageViewPagerAdapter;

    private Handler handler = new Handler(Looper.myLooper());
    public static Handler slideShowHandler;
    public static Runnable runnable;

    public void setData(List<MediaFileModel> data) {
        imagesData = data;
    }

    private CastContext mCastContext;
    private final SessionManagerListener<CastSession> mSessionManagerListener =
            new MySessionManagerListener();
    private CastSession mCastSession;

    private WebServerController webServerController;
    private SessionManager sessionManager;
    private RemoteMediaClient remoteMediaClient;

    private ARendererState rendererState;
    private IRendererCommand rendererCommand;
    QueueDataProvider provider;
    public static boolean isQueueImageDisplay = false;

    private RemoteMediaClient.Listener clientListener = new RemoteMediaClientListener() {
        @Override
        public void onStatusUpdated() {
            remoteMediaClient.removeListener(this);
        }
    };

    Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            new Handler(Looper.myLooper()).postDelayed(() -> {
                binding.fullImageviewViewpager.setBackgroundColor(Color.BLACK);
                hidelayout(binding.fullViewToolbar);
            }, 10);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_full_view);

        if (upnpServiceController != null) {
            upnpServiceController.resume(this);
        }

        if (getIntent() != null) {
            position = getIntent().getIntExtra("extra_position", 0);
            currentPage = position;
        }

        setSupportActionBar(binding.fullViewToolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle("");

        webServerController = new WebServerController(this);
        mCastContext = CastContext.getSharedInstance(this);
        sessionManager = mCastContext.getSessionManager();
        mCastSession = sessionManager.getCurrentCastSession();

        slideShowHandler = new Handler(Looper.myLooper());
        if (runnable != null) {
            slideShowHandler.removeCallbacks(runnable);
        }
        slideShowHandler.removeCallbacksAndMessages(null);

        isQueueImageDisplay = false;

        initView();
        castImage();

    }

    public void initView() {

        if (rendererState == null && isDLNACastConnected) {
            rendererState = factory.createRendererState();
            rendererCommand = factory.createRendererCommand(rendererState);
            if (rendererState == null || rendererCommand == null) {
                return;
            }
            rendererCommand.resume();
            rendererCommand.updateFull();
        }

        if (imagesData.size() >= 2) {
            if (imagesData.get(1) == null) {
                imagesData.remove(1);
            }
        }

        fullImageViewPagerAdapter = new FullImageViewPagerAdapter(getApplicationContext(), imagesData, this);
        binding.fullImageviewViewpager.setAdapter(fullImageViewPagerAdapter);
        binding.fullImageviewViewpager.setCurrentItem(position);

        binding.fullViewToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.fullImageviewViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (!isQueueImageDisplay) {
                    currentPage = i;
                    invalidateOptionsMenu();
                    castImage();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        binding.stopLayout.setOnClickListener(v -> {
            if (isDLNACastConnected) {
                if (rendererCommand != null) {
                    rendererCommand.commandStop();
                    rendererCommand.pause();
                }
            } else if (isChromeCastConnected) {
                if (isQueueImageDisplay) {
                    isQueueImageDisplay = false;
                    binding.playLayout.setImageResource(R.drawable.ic_play);
                    slideShowHandler.removeCallbacks(runnable);
                    slideShowHandler.removeCallbacksAndMessages(null);
                }
                remoteMediaClient.stop();
            }
            finish();
        });

        binding.nextLayout.setOnClickListener(v -> {
            if (currentPage < imagesData.size() - 1) {
                currentPage = currentPage + 1;
                binding.fullImageviewViewpager.setCurrentItem(currentPage);
            }
        });

        binding.previousLayout.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage = currentPage - 1;
                binding.fullImageviewViewpager.setCurrentItem(currentPage);
            }
        });

        binding.playLayout.setOnClickListener(v -> {
            if (isQueueImageDisplay) {
                isQueueImageDisplay = false;
                binding.playLayout.setImageResource(R.drawable.ic_play);
                slideShowHandler.removeCallbacks(runnable);
                slideShowHandler.removeCallbacksAndMessages(null);
            } else {
                binding.playLayout.setImageResource(R.drawable.ic_pause);
                playQueueImages();
            }
        });

        handler.postDelayed(hideRunnable, 1000);
    }

    public void playQueueImages() {
        isQueueImageDisplay = true;

        slideShowHandler.removeCallbacksAndMessages(null);
        slideShowHandler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage > 0 && currentPage < imagesData.size() - 1 && isQueueImageDisplay) {
                    currentPage = currentPage + 1;
                    castImage();
                    if (isQueueImageDisplay) {
                        handler.postDelayed(this, 2 * 1000);
                    }
                }
            }
        }, 2 * 1000);

    }

    public void castImage() {
        stopCastServer();
        binding.progressBar.setVisibility(View.VISIBLE);

        MediaFileModel video = imagesData.get(currentPage);

        if (!isConnected) {
            startActivity(new Intent(FullImageViewActivity.this, CastDeviceListActivity.class));
            return;
        } else {
            if (isChromeCastConnected) {
                if (mCastSession == null) {
                    startActivity(new Intent(FullImageViewActivity.this, CastDeviceListActivity.class));
                    return;
                }
            } else if (isDLNACastConnected) {
                if (upnpServiceController.getSelectedRenderer() == null) {
                    startActivity(new Intent(FullImageViewActivity.this, CastDeviceListActivity.class));
                    return;
                }
            }
        }

        if (isChromeCastConnected) {
            provider = QueueDataProvider.getInstance(this);
            provider.setIsImage(true);
            provider.clearQueue();

            MediaInfo mediaInfo1 = webServerController.getMediaInfo(video.getFilePath(), true);

            remoteMediaClient = mCastSession.getRemoteMediaClient();
            if (remoteMediaClient == null) {
                return;
            }

            remoteMediaClient.addListener(clientListener);
            remoteMediaClient.load(mediaInfo1, true, 0);

            remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
                @Override
                public void onPreloadStatusUpdated() {
                    super.onPreloadStatusUpdated();
                    binding.progressBar.setVisibility(View.GONE);
                }
            });
        } else if (isDLNACastConnected) {
            binding.progressBar.setVisibility(View.GONE);
            String ip1 = null;
            try {
                ip1 = CastDeviceListActivity.getLocalIpAddress(this).getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            String extension = "";
            int dot = video.getFilePath().lastIndexOf('.');
            if (dot >= 0)
                extension = video.getFilePath().substring(dot).toLowerCase();
            String mediaUrl = "http://" + ip1 + ":" + MediaServer.port + "/" + IMAGE_PREFIX + video.getId() + extension;

            video.setMediaCastUrl(mediaUrl);

            IRendererCommand rendererCommand = factory.createRendererCommand(factory.createRendererState());
            if (rendererCommand != null) {
                rendererCommand.launchItem(video);
            }
        }
    }

    public void stopCastServer() {
        Intent castServerService = new Intent(this, CastServerService.class);
        stopService(castServerService);
    }

    public void hidelayout(final View view) {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, -view.getHeight());
        animate.setDuration(500);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    private void showTopLayout(View view) {
        binding.fullViewToolbar.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                -binding.fullViewToolbar.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        binding.fullViewToolbar.startAnimation(animate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClick(int position) {
        if (binding.fullViewToolbar.getVisibility() != View.VISIBLE) {
            new Handler().postDelayed(() -> {
                binding.fullImageviewViewpager.setBackgroundColor(Color.BLACK);
                showTopLayout(binding.fullViewToolbar);
                handler.removeCallbacks(hideRunnable);
                handler.postDelayed(hideRunnable, 5000);
            }, 10);
        } else {
            new Handler().postDelayed(() -> {
                binding.fullImageviewViewpager.setBackgroundColor(Color.BLACK);
                hidelayout(binding.fullViewToolbar);
                handler.removeCallbacks(hideRunnable);

            }, 10);

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
    protected void onResume() {
        mCastSession = sessionManager.getCurrentCastSession();
        super.onResume();
    }

    @Override
    protected void onPause() {
//        mCastSession = null;
//        upnpServiceController.pause();
//        upnpServiceController.getServiceListener().getServiceConnexion().onServiceDisconnected(null);
        super.onPause();
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        return mCastContext.onDispatchVolumeKeyEventBeforeJellyBean(event)
                || super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (isConnected) {
            menu.findItem(R.id.media_route_menu_item).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_cast_connected_white));
        } else
            menu.findItem(R.id.media_route_menu_item).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_cast_white));
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
    public void onDeviceConnect(boolean isConnected) {
        Constant.isConnected = isConnected;
        invalidateOptionsMenu();
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
            castImage();
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionStarted(CastSession session, String sessionId) {
            mCastSession = session;
            castImage();
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
