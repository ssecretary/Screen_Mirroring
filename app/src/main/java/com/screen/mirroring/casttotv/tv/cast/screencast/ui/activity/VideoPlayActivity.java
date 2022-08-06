package com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.mediarouter.media.MediaRouter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaSeekOptions;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.RendererState;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.mediaserver.MediaServer;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.ARendererState;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IRendererCommand;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IRendererState;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.QueueDataProvider;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter.QueueListAdapter;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.DeviceConnectListener;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.MediaFileModel;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.OnItemClickListner;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.Utils;
import com.screen.mirroring.casttotv.tv.cast.screencast.Constant;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.RemoteMediaClientListener;
import com.screen.mirroring.casttotv.tv.cast.screencast.WebServerController;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ActivityVideoPlayBinding;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;
import com.yandex.metrica.impl.ob.G;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

//import es.munix.multidisplaycast.CastManager;

import static com.google.android.gms.cast.MediaStatus.REPEAT_MODE_REPEAT_ALL;
import static com.google.android.gms.cast.MediaStatus.REPEAT_MODE_REPEAT_ALL_AND_SHUFFLE;
import static com.google.android.gms.cast.MediaStatus.REPEAT_MODE_REPEAT_OFF;
import static com.google.android.gms.cast.MediaStatus.REPEAT_MODE_REPEAT_SINGLE;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.VIDEO_PREFIX;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.castDeviceList;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isChromeCastConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isDLNACastConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity.CastDeviceListActivity.upnpServiceController;
import static com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity.FullImageViewActivity.slideShowHandler;

public class VideoPlayActivity extends AppCompatActivity implements DeviceConnectListener, Observer, OnItemClickListner {

    private static final int PRELOAD_TIME_S = 20;

    ActivityVideoPlayBinding binding;
    static List<MediaFileModel> videoList;
    int position;
    MediaFileModel video;

    private CastContext mCastContext;
    private final SessionManagerListener<CastSession> mSessionManagerListener =
            new MySessionManagerListener();
    private CastSession mCastSession;

    private WebServerController webServerController;
    private SessionManager sessionManager;
    private RemoteMediaClient remoteMediaClient;

    private ARendererState rendererState;
    private IRendererCommand rendererCommand;

    public AudioManager audioManager;
    private int mMaxVolume;
    private int repeateMode = 1;
    QueueDataProvider provider;
    QueueListAdapter queueListAdapter;
    BottomSheetDialog queueListDialog;
    List<MediaQueueItem> mediaQueueItemList;

    private RemoteMediaClient.Listener clientListener = new RemoteMediaClientListener() {
        @Override
        public void onStatusUpdated() {
            remoteMediaClient.removeListener(this);
        }
    };

    private BroadcastReceiver nextVideoDLNAPlay = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (rendererState != null && rendererState.getState() == IRendererState.State.PLAY && binding != null && binding.seekBar.getProgress() > binding.seekBar.getMax() - 20) {
                playNextVideo();
            }
        }
    };

    public void setData(List<MediaFileModel> data) {
        videoList = data;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_play);

        if (upnpServiceController != null) {
            upnpServiceController.resume(this);
        }

        if (getIntent() != null) {
            position = getIntent().getIntExtra("extra_position", 0);
        }

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (isChromeCastConnected) {
            mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        } else {
            mMaxVolume = 100;
        }
        webServerController = new WebServerController(this);
        mCastContext = CastContext.getSharedInstance(this);
        sessionManager = mCastContext.getSessionManager();
        mCastSession = sessionManager.getCurrentCastSession();

        mediaQueueItemList = new ArrayList<>();

        video = videoList.get(position);

        queueListAdapter = new QueueListAdapter(this, false);
        queueListAdapter.setOnItemClickListner(this);
        queueListAdapter.setMediaQueueItemList(videoList);

        initView();
        startControlPoint();
        castVideo();

    }

    public void initView() {
        if (isConnected) {
            binding.actionCast.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_cast_connected));
        } else
            binding.actionCast.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_cast));

        binding.fileName.setText(video.getFileName());

        Glide.with(this).load(video.getFilePath()).apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(18))
                .diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_thumbnail))
                .into(binding.thumbnailIcon);

        binding.next.setOnClickListener(v -> {
            if (position < videoList.size()) {
                binding.ivPlayPause.setImageResource(R.drawable.ic_pause_wbg);
                position = position + 1;
                MediaQueueItem mediaQueueItem = null;
                if (isChromeCastConnected) {
                    mediaQueueItem = provider.getCurrentItem();
                }
                if (isChromeCastConnected && mediaQueueItem != null) {
                    remoteMediaClient.queueNext(null);
                } else {
                    if (position < videoList.size()) {
                        video = videoList.get(position);
                        Glide.with(this).load(video.getFilePath()).apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_video_placeholder))
                                .into(binding.thumbnailIcon);
                        binding.fileName.setText(video.getFileName());
                        castVideo();
                    }
                }
            } else {
                Toast.makeText(this, "Already last video play.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.previous.setOnClickListener(v -> {
            if (position > 0) {
                binding.ivPlayPause.setImageResource(R.drawable.ic_pause_wbg);
                position = position - 1;
                MediaQueueItem mediaQueueItem = null;
                if (isChromeCastConnected) {
                    mediaQueueItem = provider.getCurrentItem();
                }
                if (isChromeCastConnected && mediaQueueItem != null) {
                    remoteMediaClient.queuePrev(null);
                } else {
                    if (position >= 0) {
                        video = videoList.get(position);
                        Glide.with(this).load(video.getFilePath()).apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_video_placeholder))
                                .into(binding.thumbnailIcon);
                        binding.fileName.setText(video.getFileName());
                        castVideo();
                    }
                }
            } else {
                Toast.makeText(this, "Already last video play.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.ivPlayPause.setOnClickListener(v -> {
            if (isChromeCastConnected) {
                if (remoteMediaClient.isPlaying()) {
                    binding.ivPlayPause.setImageResource(R.drawable.ic_play_wbg);
                    remoteMediaClient.pause();
                } else {
                    binding.ivPlayPause.setImageResource(R.drawable.ic_pause_wbg);
                    remoteMediaClient.play();
                }
            } else if (isDLNACastConnected) {
                if (rendererState.getState() == RendererState.State.PLAY) {
                    rendererCommand.commandPause();
                    binding.ivPlayPause.setImageResource(R.drawable.ic_play_wbg);
                } else {
                    rendererCommand.commandPlay();
                    binding.ivPlayPause.setImageResource(R.drawable.ic_pause_wbg);
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
                if (isChromeCastConnected) {
                    int progress = seekBar.getProgress();
                    remoteMediaClient.seek(progress);
                } else if (isDLNACastConnected) {
                    if (rendererState == null)
                        return;

                    int position = seekBar.getProgress();

                    long t = (long) ((1.0 - ((double) seekBar.getMax() - position) / (seekBar.getMax())) * rendererState
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

        binding.queueLayout.setOnClickListener(v -> {
            showQueueListDialog();
        });

        binding.modeLayout.setOnClickListener(v -> {
            if (repeateMode == 3) {
                repeateMode = 1;
            } else {
                repeateMode = repeateMode + 1;
            }
            if (isChromeCastConnected) {
                remoteMediaClient.queueSetRepeatMode(repeateMode, null);
            } else if (isDLNACastConnected) {
            }
            setRepeateModeIcon();
        });

        binding.previousTenLayout.setOnClickListener(v -> {
            if (isChromeCastConnected) {
                if (binding.seekBar.getProgress() > 0) {
                    remoteMediaClient.seek(binding.seekBar.getProgress() - (15 * 1000));
                }
            } else if (isDLNACastConnected) {
                if (binding.seekBar.getProgress() > 0) {
                    int position = binding.seekBar.getProgress();

                    long t = (long) ((1.0 - ((double) binding.seekBar.getMax() - position) / (binding.seekBar.getMax())) * rendererState
                            .getDurationSeconds());

                    t = t - (15);
                    long h = t / 3600;
                    long m = (t - h * 3600) / 60;
                    long s = t - h * 3600 - m * 60;
                    String seek = formatTime(h, m, s);

                    if (rendererCommand != null)
                        rendererCommand.commandSeek(seek);
                }
            }
            Toast.makeText(this, "-15", Toast.LENGTH_SHORT).show();
        });

        binding.nextTenLayout.setOnClickListener(v -> {
            if (isChromeCastConnected) {
                MediaSeekOptions mediaSeekOptions = new MediaSeekOptions.Builder().setPosition(binding.seekBar.getProgress() + (15 * 1000)).build();
                remoteMediaClient.seek(mediaSeekOptions);
            } else if (isDLNACastConnected) {
                int position = binding.seekBar.getProgress();

                long t = (long) ((1.0 - ((double) binding.seekBar.getMax() - position) / (binding.seekBar.getMax())) * rendererState
                        .getDurationSeconds());
                t = t + (15);
                long h = t / 3600;
                long m = (t - h * 3600) / 60;
                long s = t - h * 3600 - m * 60;
                String seek = formatTime(h, m, s);

                if (rendererCommand != null)
                    rendererCommand.commandSeek(seek);
            }
            Toast.makeText(this, "+15", Toast.LENGTH_SHORT).show();
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
            } else if (isDLNACastConnected && rendererState != null) {
            }
        });

        binding.speedSeekbar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                String[] speedValues = getResources().getStringArray(R.array.play_back_speed);
                if (isChromeCastConnected && remoteMediaClient != null) {
                    remoteMediaClient.setPlaybackRate(Double.parseDouble(speedValues[seekParams.thumbPosition]));
                } else if (isDLNACastConnected && rendererState != null) {
                    rendererState.setPlaybackSpeed(speedValues[seekParams.thumbPosition]);
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

    public void setRepeateModeIcon() {
        switch (repeateMode) {
            case REPEAT_MODE_REPEAT_OFF:
                binding.modeLayout.setImageResource(R.drawable.cast_abc_scrubber_control_off_mtrl_alpha);
                break;
            case REPEAT_MODE_REPEAT_ALL:
                binding.modeLayout.setImageResource(R.drawable.ic_repeat);
                break;
            case REPEAT_MODE_REPEAT_SINGLE:
                binding.modeLayout.setImageResource(R.drawable.ic_repeat_one);
                break;
            case REPEAT_MODE_REPEAT_ALL_AND_SHUFFLE:
                binding.modeLayout.setImageResource(R.drawable.ic_shuffle);
                break;
        }
    }

    public void getRandomVideoPosition() {
        Random random = new Random();
        int position = random.nextInt(videoList.size());
        this.position = position;
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

    public void castVideo() {
        if (slideShowHandler != null) {
            FullImageViewActivity.isQueueImageDisplay = false;
            slideShowHandler.removeCallbacks(FullImageViewActivity.runnable);
            slideShowHandler.removeCallbacksAndMessages(null);
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        if (!isConnected) {
            startActivity(new Intent(VideoPlayActivity.this, CastDeviceListActivity.class));
            return;
        } else {
            if (isChromeCastConnected) {
                if (mCastSession == null) {
                    startActivity(new Intent(VideoPlayActivity.this, CastDeviceListActivity.class));
                    return;
                }
            } else if (isDLNACastConnected) {
                if (upnpServiceController.getSelectedRenderer() == null) {
                    startActivity(new Intent(VideoPlayActivity.this, CastDeviceListActivity.class));
                    return;
                }
            }
        }

        if (isChromeCastConnected) {
            MediaInfo mediaInfo1 = webServerController.getMediaInfo(video.getFilePath(), false);

            binding.seekBar.setMax(Integer.parseInt(video.getDuration()));
            binding.currentTimeText.setText("00:00");
            binding.totalTimeText.setText(Utils.formatMillis(Integer.parseInt(video.getDuration())));
            remoteMediaClient = mCastSession.getRemoteMediaClient();
            if (remoteMediaClient == null) {
                return;
            }

            provider = QueueDataProvider.getInstance(this);
            provider.setIsImage(false);
            provider.clearQueue();

            MediaQueueItem queueItem = new MediaQueueItem.Builder(mediaInfo1).setAutoplay(
                    true).setPreloadTime(PRELOAD_TIME_S).build();

            mediaQueueItemList.add(queueItem);
            MediaQueueItem[] items = Utils
                    .rebuildQueueAndAppend(provider.getItems(), queueItem);

            remoteMediaClient.queueLoad(items, provider.getCount(),
                    REPEAT_MODE_REPEAT_OFF, null);

            remoteMediaClient.addListener(clientListener);
//            remoteMediaClient.load(mediaInfo1, true, 0);


            remoteMediaClient.addProgressListener((l, l1) -> {
                binding.seekBar.setProgress(Integer.parseInt(l + ""));
                binding.currentTimeText.setText(Utils.formatMillis((int) l));
                binding.totalTimeText.setText(Utils.formatMillis((int) l1));
            }, 100);

            provider.setOnQueueDataChangedListener(() -> {
                MediaQueueItem mediaQueueItem = provider.getCurrentItem();
                if (mediaQueueItem != null && mediaQueueItem.getMedia() != null) {
                    if (!isDestroyed() && !isFinishing()) {
                        binding.fileName.setText(mediaQueueItem.getMedia().getMetadata().getString(MediaMetadata.KEY_TITLE));
                        binding.seekBar.setMax((int) mediaQueueItem.getMedia().getStreamDuration());
                        Glide.with(this).load(mediaQueueItem.getMedia().getMetadata().getImages().get(0).getUrl()).apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_video_placeholder))
                                .into(binding.thumbnailIcon);
                        queueListAdapter.notifyDataSetChanged();
                    }
                }
            });

            remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
                @Override
                public void onPreloadStatusUpdated() {
                    super.onPreloadStatusUpdated();
                    if (remoteMediaClient.getMediaQueue().getItemCount() == 0) {
                        addQueueVideoList();
                    }
                    binding.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onQueueStatusUpdated() {
                    super.onQueueStatusUpdated();
                }
            });


        } else if (isDLNACastConnected) {
            binding.seekBar.setMax(100);
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
            String mediaUrl = "http://" + ip1 + ":" + MediaServer.port + "/" + VIDEO_PREFIX + video.getId() + extension;

            video.setMediaCastUrl(mediaUrl);

            IRendererCommand rendererCommand = CastDeviceListActivity.factory.createRendererCommand(CastDeviceListActivity.factory.createRendererState());
            rendererCommand.launchItem(video);
        }
    }

    public void addQueueVideoList() {
        if (isChromeCastConnected && !isDestroyed() && !isFinishing()) {
            if (position < videoList.size() - 1) {
                for (int i = position + 1; i < videoList.size(); i++) {
                    MediaFileModel mediaInfo = videoList.get(i);
                    MediaInfo mediaInfo1 = webServerController.getMediaInfo(mediaInfo.getFilePath(), false);
                    MediaQueueItem queueItem = new MediaQueueItem.Builder(mediaInfo1).setAutoplay(
                            true).setPreloadTime(PRELOAD_TIME_S).build();
                    remoteMediaClient.queueAppendItem(queueItem, null);
                    if (mediaQueueItemList.size() < videoList.size()) {
                        mediaQueueItemList.add(queueItem);
                    }

                }
            }

            if (position > 0) {
                for (int i = 0; i < position; i++) {
                    MediaFileModel mediaInfo = videoList.get(i);
                    MediaInfo mediaInfo1 = webServerController.getMediaInfo(mediaInfo.getFilePath(), false);
                    MediaQueueItem queueItem = new MediaQueueItem.Builder(mediaInfo1).setAutoplay(
                            true).setPreloadTime(PRELOAD_TIME_S).build();
                    remoteMediaClient.queueAppendItem(queueItem, null);
                    if (mediaQueueItemList.size() < videoList.size()) {
                        mediaQueueItemList.add(queueItem);
                    }
                }
            }
        }
    }

    public void showQueueListDialog() {
        queueListDialog = new BottomSheetDialog(this, R.style.TransparentDialog);
        queueListDialog.setContentView(R.layout.dialog_queue_layout);

        queueListDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        RecyclerView queueListView = queueListDialog.findViewById(R.id.queueListView);
        queueListView.setLayoutManager(new LinearLayoutManager(this));
        queueListView.setAdapter(queueListAdapter);

        queueListDialog.show();
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
        registerReceiver(nextVideoDLNAPlay, new IntentFilter("playNextVideo"));
        super.onResume();
    }

    @Override
    protected void onPause() {
        mCastSession = null;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nextVideoDLNAPlay);
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        return mCastContext.onDispatchVolumeKeyEventBeforeJellyBean(event)
                || super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (rendererCommand != null) {
//            rendererCommand.commandStop();
        }
        super.onBackPressed();
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

    public void startControlPoint() {
        runOnUiThread(() -> {
            if (rendererState == null || rendererState.getState() == IRendererState.State.STOP) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
            }
        });

        if (rendererState == null && isDLNACastConnected) {
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
            runOnUiThread(() -> {
                binding.currentTimeText.setText(rendererState.getPosition().replace("-", ""));
                binding.totalTimeText.setText(rendererState.getDuration());
                if (rendererState.getState() == RendererState.State.PLAY) {
                    binding.ivPlayPause.setImageResource(R.drawable.ic_pause_wbg);
                } else {
                    binding.ivPlayPause.setImageResource(R.drawable.ic_play_wbg);
                }

                binding.seekBar.setProgress(rendererState.getElapsedPercent());
//                binding.volumeProgressbar.setProgress(rendererState.getVolume());
            });
        }
    }

    public void playNextVideo() {
        if (repeateMode == 3) {
            getRandomVideoPosition();
        } else if (repeateMode == 1) {
            if (position == videoList.size() - 1) {
                position = 0;
            } else {
                position = position + 1;
            }
        }
        if (position < videoList.size()) {
            binding.ivPlayPause.setImageResource(R.drawable.ic_pause);
            video = videoList.get(position);
            Glide.with(this).load(video.getFilePath()).apply(new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_video_placeholder))
                    .into(binding.thumbnailIcon);
            castVideo();
        } else {
            if (rendererCommand != null) {
                rendererCommand.commandStop();
                rendererCommand.pause();
            }
        }
    }

    @Override
    public void onItemClick(int position) {
        if (isChromeCastConnected) {
            if (mediaQueueItemList.size() < position) {
                queueListDialog.dismiss();
                return;
            }

            MediaQueueItem[] items = new MediaQueueItem[mediaQueueItemList.size()];
            items = mediaQueueItemList.toArray(items);
            position = position - 1;
            if (position < 0) {
                position = 0;
            }
            remoteMediaClient.queueLoad(items, position,
                    MediaStatus.REPEAT_MODE_REPEAT_OFF, null);
        } else if (isDLNACastConnected) {
            this.position = position;
            video = videoList.get(position);
            castVideo();
        }
        queueListDialog.dismiss();
    }

    private class MySessionManagerListener implements SessionManagerListener<CastSession> {

        @Override
        public void onSessionEnded(CastSession session, int error) {
            if (session == mCastSession) {
                mCastSession = null;
            }
            invalidateOptionsMenu();
            Log.e("TAG", " Session Ended");
        }

        @Override
        public void onSessionResumed(CastSession session, boolean wasSuspended) {
            mCastSession = session;
            remoteMediaClient = mCastSession.getRemoteMediaClient();
            castVideo();
            invalidateOptionsMenu();
            Log.e("TAG", " Session Resumed");
        }

        @Override
        public void onSessionStarted(CastSession session, String sessionId) {
            mCastSession = session;
            remoteMediaClient = mCastSession.getRemoteMediaClient();
            castVideo();
            invalidateOptionsMenu();
            Log.e("TAG", " Session Started");
        }

        @Override
        public void onSessionStarting(CastSession session) {
            remoteMediaClient = mCastSession.getRemoteMediaClient();
        }

        @Override
        public void onSessionStartFailed(CastSession session, int error) {
        }

        @Override
        public void onSessionEnding(CastSession session) {
        }

        @Override
        public void onSessionResuming(CastSession session, String sessionId) {
            remoteMediaClient = mCastSession.getRemoteMediaClient();
        }

        @Override
        public void onSessionResumeFailed(CastSession session, int error) {
        }

        @Override
        public void onSessionSuspended(CastSession session, int reason) {
            remoteMediaClient = mCastSession.getRemoteMediaClient();
        }
    }

    private String formatTime(long h, long m, long s) {
        return ((h >= 10) ? "" + h : "0" + h) + ":" + ((m >= 10) ? "" + m : "0" + m) + ":"
                + ((s >= 10) ? "" + s : "0" + s);
    }
}
