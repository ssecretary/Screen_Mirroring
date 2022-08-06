package com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.cast.MediaSeekOptions;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.screen.mirroring.casttotv.tv.cast.screencast.Constant;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.RendererState;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.ARendererState;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IRendererCommand;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.QueueDataProvider;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter.WebVideoLinkAdapter;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.DeviceConnectListener;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.MediaFileModel;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.WebVideo;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.OnItemClickListner;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.Utils;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.RemoteMediaClientListener;
import com.screen.mirroring.casttotv.tv.cast.screencast.WebServerController;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ActivityVideoPlayBinding;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isChromeCastConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isDLNACastConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity.FullImageViewActivity.slideShowHandler;

public class WebVideoPlayActivity extends AppCompatActivity implements DeviceConnectListener, Observer, OnItemClickListner {

    ActivityVideoPlayBinding binding;
    static WebVideo webVideo;
    int position;

    private CastContext mCastContext;
    private final SessionManagerListener<CastSession> mSessionManagerListener =
            new MySessionManagerListener();
    private CastSession mCastSession;

    private WebServerController webServerController;
    private SessionManager sessionManager;
    private RemoteMediaClient remoteMediaClient;
    WebVideoLinkAdapter webVideoLinkAdapter;
    public static List<WebVideo> webVideoList;

    private RemoteMediaClient.Listener clientListener = new RemoteMediaClientListener() {
        @Override
        public void onStatusUpdated() {
            remoteMediaClient.removeListener(this);
        }
    };

    private ARendererState rendererState;
    private IRendererCommand rendererCommand;
    public AudioManager audioManager;
    private int mMaxVolume;

    BottomSheetDialog resolutionListDialog;

    public void setData(WebVideo data) {
        webVideo = data;
    }

    public void setData(List<WebVideo> data) {
        webVideoList = data;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_play);

//        if (upnpServiceController != null) {
//            upnpServiceController.resume(this);
//        }

        if (getIntent() != null) {
            position = getIntent().getIntExtra("extra_position", 0);
        }

        webVideo = webVideoList.get(position);

        webVideoLinkAdapter = new WebVideoLinkAdapter(this);
        webVideoLinkAdapter.setOnItemClickListner(this);
        webVideoLinkAdapter.setImageDataList(webVideoList);

        webServerController = new WebServerController(this);
        mCastContext = CastContext.getSharedInstance(this);
        sessionManager = mCastContext.getSessionManager();
        mCastSession = sessionManager.getCurrentCastSession();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (isChromeCastConnected) {
            mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        } else {
            mMaxVolume = 100;
        }

        startControlPoint();
        initView();
        castVideo();
    }

    public void initView() {

        binding.queueText.setText(getString(R.string.action_resolution));
        binding.queueLayout.setImageResource(R.drawable.ic_resolution);

        Glide.with(this)
                .load(webVideo.getLink())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        binding.thumbnailIcon.setImageResource(R.drawable.ic_video_placeholder);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();

                        if (bitmap != null) {
                            binding.thumbnailIcon.setImageBitmap(bitmap);
                        } else {
                            binding.thumbnailIcon.setImageResource(R.drawable.ic_thumbnail);
                        }
                        return false;
                    }
                })
                .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(18))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)).into(binding.thumbnailIcon);

        binding.modeLayout.setColorFilter(ContextCompat.getColor(this, R.color.md_grey_500));

        binding.next.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.no_more), Toast.LENGTH_SHORT).show();
        });

        binding.previous.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.no_more), Toast.LENGTH_SHORT).show();
        });

        binding.ivPlayPause.setOnClickListener(v -> {
            if (Constant.isChromeCastConnected) {
                if (remoteMediaClient.isPlaying()) {
                    binding.ivPlayPause.setImageResource(R.drawable.ic_play_wbg);
                    remoteMediaClient.pause();
                } else {
                    binding.ivPlayPause.setImageResource(R.drawable.ic_pause_wbg);
                    remoteMediaClient.play();
                }
            }
        });

        binding.seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (isChromeCastConnected) {
                    remoteMediaClient.seek(progress);
                } else if (isDLNACastConnected) {
                    long t = (long) ((1.0 - ((double) binding.seekBar.getMax() - position) / (binding.seekBar.getMax())) * rendererState
                            .getDurationSeconds());
                    long h = t / 3600;
                    long m = (t - h * 3600) / 60;
                    long s = t - h * 3600 - m * 60;
                    String seek = formatTime(h, m, s);

                    if (rendererCommand != null)
                        rendererCommand.commandSeek(seek);
                }
            }
        });

        binding.volumeDown.setOnClickListener(v -> {
            setVolumeDown();
        });

        binding.volumeUp.setOnClickListener(v -> {
            setVolumeUp();
        });

        binding.stopLayout.setOnClickListener(v -> {
            if (isDLNACastConnected) {
                if (rendererCommand != null) {
                    rendererCommand.commandStop();
                    rendererCommand.pause();
                }
            } else if (isChromeCastConnected) {
                remoteMediaClient.stop();
            }
            finish();
        });

        binding.previousTenLayout.setOnClickListener(v -> {

            if (isChromeCastConnected) {
                remoteMediaClient.seek(binding.seekBar.getProgress() - (15 * 1000));
            } else if (isDLNACastConnected) {
                int position = binding.seekBar.getProgress() - (15 * 1000);

                long t = (long) ((1.0 - ((double) binding.seekBar.getMax() - position) / (binding.seekBar.getMax())) * rendererState
                        .getDurationSeconds());
                long h = t / 3600;
                long m = (t - h * 3600) / 60;
                long s = t - h * 3600 - m * 60;
                String seek = formatTime(h, m, s);

                if (rendererCommand != null)
                    rendererCommand.commandSeek(seek);
            }
            Toast.makeText(this, "-15", Toast.LENGTH_SHORT).show();
        });

        binding.nextTenLayout.setOnClickListener(v -> {
            if (isChromeCastConnected) {
                MediaSeekOptions mediaSeekOptions = new MediaSeekOptions.Builder().setPosition(binding.seekBar.getProgress() + (15 * 1000)).build();
                remoteMediaClient.seek(mediaSeekOptions);
            } else if (isDLNACastConnected) {
                int position = binding.seekBar.getProgress() + (15 * 1000);

                long t = (long) ((1.0 - ((double) binding.seekBar.getMax() - position) / (binding.seekBar.getMax())) * rendererState
                        .getDurationSeconds());
                long h = t / 3600;
                long m = (t - h * 3600) / 60;
                long s = t - h * 3600 - m * 60;
                String seek = formatTime(h, m, s);

                if (rendererCommand != null)
                    rendererCommand.commandSeek(seek);
            }
            Toast.makeText(this, "+15", Toast.LENGTH_SHORT).show();
        });

        binding.queueLayout.setOnClickListener(v -> {
            showResolutionListDialog();
        });

        binding.actionCast.setOnClickListener(v -> {
            CastDeviceListActivity castDeviceListActivity = new CastDeviceListActivity();
            castDeviceListActivity.setDeviceConnectListener(this);
            startActivityForResult(new Intent(this, CastDeviceListActivity.class), 100);
        });

        binding.actionBack.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.muteFile.setOnClickListener(v -> {
            if (isChromeCastConnected && mCastSession != null) {
                try {
                    if (mCastSession.isMute()) {
                        binding.muteFile.setImageResource(R.drawable.ic_vol_unmute);
                    } else {
                        binding.muteFile.setImageResource(R.drawable.ic_vol_mute);
                    }
                    mCastSession.setMute(!mCastSession.isMute());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (isDLNACastConnected && rendererState != null) {
                if (rendererState.isMute()) {
                    binding.muteFile.setImageResource(R.drawable.ic_vol_unmute);
                } else {
                    binding.muteFile.setImageResource(R.drawable.ic_vol_mute);
                }
                rendererState.setMute(!rendererState.isMute());
            }
        });

        binding.actionSpeed.setOnClickListener(v -> {
            if (isChromeCastConnected && remoteMediaClient != null) {
                if (binding.speedLayout.getVisibility() == View.VISIBLE) {
                    binding.speedLayout.setVisibility(View.GONE);
                } else {
                    binding.speedLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.speedSeekbar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                String[] speedValues = getResources().getStringArray(R.array.play_back_speed);
                if (isChromeCastConnected && remoteMediaClient != null) {
                    remoteMediaClient.setPlaybackRate(Double.parseDouble(speedValues[seekParams.thumbPosition]));
                }
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });
    }

    public void setVolumeUp() {
        int volume = 0;
        if (isChromeCastConnected) {
            if (mCastSession != null && mCastSession.getVolume() < 100) {
                try {
                    mCastSession.setVolume(mCastSession.getVolume() + 0.01);
                    Toast.makeText(this, Math.round((mCastSession.getVolume() + 0.01) * 100) + "", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else if (isDLNACastConnected) {
            if (rendererCommand != null && rendererState != null) {
                volume = rendererState.getVolume();
                if (volume < 0) {
                    volume = 0;
                }
                rendererCommand.setVolume(volume + 1);
                Toast.makeText(this, volume + "", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setVolumeDown() {
        int volume = 0;
        if (isChromeCastConnected) {
            if (mCastSession != null && mCastSession.getVolume() > 0) {
                try {
                    mCastSession.setVolume(mCastSession.getVolume() - 0.01);
                    Toast.makeText(this, Math.round((mCastSession.getVolume() - 0.01) * 100) + "", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (isDLNACastConnected) {
            if (rendererCommand != null && rendererState != null) {
                volume = rendererState.getVolume();
                volume = volume - 1;
                if (volume < 0) {
                    volume = 0;
                }
                rendererCommand.setVolume(volume);
            }
        }
    }

    public void showResolutionListDialog() {
        resolutionListDialog = new BottomSheetDialog(this, R.style.TransparentDialog);
        resolutionListDialog.setContentView(R.layout.dialog_queue_layout);

        resolutionListDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView titleText = resolutionListDialog.findViewById(R.id.titleText);
        titleText.setText(getString(R.string.action_resolution));

        RecyclerView queueListView = resolutionListDialog.findViewById(R.id.queueListView);
        queueListView.setLayoutManager(new LinearLayoutManager(this));
        queueListView.setAdapter(webVideoLinkAdapter);

        resolutionListDialog.show();
    }

    public void castVideo() {
        if (slideShowHandler != null) {
            FullImageViewActivity.isQueueImageDisplay = false;
            slideShowHandler.removeCallbacks(FullImageViewActivity.runnable);
            slideShowHandler.removeCallbacksAndMessages(null);
        }

        if (!Constant.isConnected) {
            startActivity(new Intent(WebVideoPlayActivity.this, CastDeviceListActivity.class));
            return;
        } else {
            if (Constant.isChromeCastConnected) {
                if (mCastSession == null) {
                    startActivity(new Intent(WebVideoPlayActivity.this, CastDeviceListActivity.class));
                    return;
                }
            } else if (Constant.isDLNACastConnected) {
                if (CastDeviceListActivity.upnpServiceController.getSelectedRenderer() == null) {
                    startActivity(new Intent(WebVideoPlayActivity.this, CastDeviceListActivity.class));
                    return;
                }
            }
        }

        if (Constant.isChromeCastConnected) {
            QueueDataProvider provider = QueueDataProvider.getInstance(this);
            provider.clearQueue();

            com.google.android.gms.cast.MediaInfo mediaInfo1 = webServerController.getUrlMediaInfo(webVideo);
            binding.currentTimeText.setText("00:00");
            remoteMediaClient = mCastSession.getRemoteMediaClient();
            if (remoteMediaClient == null) {
                return;
            }
            remoteMediaClient.addListener(clientListener);
            remoteMediaClient.load(mediaInfo1, true, 0);

            remoteMediaClient.addProgressListener((l, l1) -> {
                binding.seekBar.setProgress(Integer.parseInt(l + ""));
                binding.currentTimeText.setText(Utils.formatMillis((int) l));
                binding.seekBar.setMax(Integer.parseInt(l1 + ""));
                binding.totalTimeText.setText(Utils.formatMillis((int) l1));
            }, 100);

            remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
                @Override
                public void onPreloadStatusUpdated() {
                    super.onPreloadStatusUpdated();
                    binding.progressBar.setVisibility(View.GONE);
                }
            });
        } else if (Constant.isDLNACastConnected) {
            MediaFileModel fileModel = new MediaFileModel();
            fileModel.setFileName(webVideo.getName());
            fileModel.setType(2);
            binding.progressBar.setVisibility(View.GONE);

            fileModel.setMediaCastUrl(webVideo.getLink());
            IRendererCommand rendererCommand = CastDeviceListActivity.factory.createRendererCommand(CastDeviceListActivity.factory.createRendererState());
            rendererCommand.launchItem(fileModel);
        }
    }

    public void startControlPoint() {
        if (rendererState == null && Constant.isDLNACastConnected) {
            rendererState = CastDeviceListActivity.factory.createRendererState();
            rendererCommand = CastDeviceListActivity.factory.createRendererCommand(rendererState);
            if (rendererState == null || rendererCommand == null) {
                return;
            }

            rendererCommand.resume();

            rendererState.addObserver(this);
            rendererCommand.updateFull();
        }

        if (rendererState != null) {
            binding.currentTimeText.setText(rendererState.getRemainingDuration().replace("-", ""));
            binding.totalTimeText.setText(rendererState.getDuration());
            if (rendererState.getState() == RendererState.State.PLAY) {
                binding.ivPlayPause.setImageResource(R.drawable.ic_pause_wbg);
            } else {
                binding.ivPlayPause.setImageResource(R.drawable.ic_play_wbg);
            }

            binding.seekBar.setProgress(rendererState.getElapsedPercent());
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
        mCastSession = null;
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

        if (Constant.isConnected) {
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
    public void onDeviceConnect(boolean isConnected) {
        Constant.isConnected = isConnected;
        invalidateOptionsMenu();
    }

    @Override
    public void update(Observable o, Object arg) {
        startControlPoint();
    }

    @Override
    public void onItemClick(int position) {
        webVideo = webVideoList.get(position);
        castVideo();
        resolutionListDialog.dismiss();
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
            castVideo();
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionStarted(CastSession session, String sessionId) {
            mCastSession = session;
            castVideo();
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

    private String formatTime(long h, long m, long s) {
        return ((h >= 10) ? "" + h : "0" + h) + ":" + ((m >= 10) ? "" + m : "0" + m) + ":"
                + ((s >= 10) ? "" + s : "0" + s);
    }
}
