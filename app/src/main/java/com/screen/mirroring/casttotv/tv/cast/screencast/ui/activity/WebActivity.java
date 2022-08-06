package com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.appnext.banners.BannerListener;
import com.appnext.core.AppnextError;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.gson.Gson;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.fragment.SearchFragment;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.DeviceConnectListener;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.OnItemClickListner;
import com.screen.mirroring.casttotv.tv.cast.screencast.ads.LoadAds;
import com.screen.mirroring.casttotv.tv.cast.screencast.browser.VideoContentSearch;
import com.screen.mirroring.casttotv.tv.cast.screencast.browser.VideoDetectionInitiator;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.WebVideo;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.PreferencesUtility;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.Utils;
import com.screen.mirroring.casttotv.tv.cast.screencast.Constant;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ActivityWebBinding;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter.WebVideoLinkAdapter;
import com.screen.mirroring.casttotv.tv.cast.screencast.vimeourlextractor.VimeoExtractor;
import com.screen.mirroring.casttotv.tv.cast.screencast.vimeourlextractor.VimeoFile;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isChromeCastConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isDLNACastConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isMainAdLoad;
import static com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity.CastDeviceListActivity.upnpServiceController;

public class WebActivity extends AppCompatActivity implements OnItemClickListner, DeviceConnectListener {

    public ActivityWebBinding binding;

    boolean temp_next = false;

    private boolean isDetecting = true;
    private SSLSocketFactory defaultSSLSF;
    private VideoDetectionInitiator videoDetectionInitiator;
    public String url;
    List<WebVideo> webVideoList;
    WebVideoLinkAdapter webVideoLinkAdapter;
    boolean isYouTubeVideo = false;
    LoadAds loadAds;
    public String showPlateformAd = "admob";

    private CastSession mCastSession;
    private CastContext mCastContext;
    private SessionManager sessionManager;
    private final SessionManagerListener<CastSession> mSessionManagerListener =
            new MySessionManagerListener();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_web);

        setSupportActionBar(binding.toolbar);

        loadAds = LoadAds.getInstance(this);
        showPlateformAd = PreferencesUtility.getShowPlateformAd(this);

        mCastContext = CastContext.getSharedInstance(this);
        sessionManager = mCastContext.getSessionManager();
        mCastSession = sessionManager.getCurrentCastSession();

        if (isMainAdLoad) {
            loadAds.showFullAd(4);
        } else {
            loadAds.showFullAd(1);
        }

        webVideoList = new ArrayList<>();
        webVideoLinkAdapter = new WebVideoLinkAdapter(this);
        webVideoLinkAdapter.setOnItemClickListner(this);
        defaultSSLSF = HttpsURLConnection.getDefaultSSLSocketFactory();
        videoDetectionInitiator = new VideoDetectionInitiator(new ConcreteVideoContentSearch());

        changeFabColor();
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

        binding.videoLinkList.setLayoutManager(new LinearLayoutManager(this));
        binding.videoLinkList.setAdapter(webVideoLinkAdapter);

        binding.searchView.setOnClickListener(v -> {
            SearchFragment search_fragment = new SearchFragment("", 0, WebActivity.this);
            fragmentLoad(search_fragment);
        });

        binding.youTubeLayout.setOnClickListener(v -> {
            url = "https://www.youtube.com/";
            loadWebView();
        });

        binding.googleLayout.setOnClickListener(v -> {
            url = "https://www.google.com/";
            loadWebView();
        });

        binding.buzzVideoLayout.setOnClickListener(v -> {
            url = "https://www.buzzvideos.com/";
            loadWebView();
        });

        binding.liveStreamLayout.setOnClickListener(v -> {
//            url = "https://livestream.com/watch";
            url = "https://in.yahoo.com/";
            loadWebView();
        });

        binding.imdbLayout.setOnClickListener(v -> {
            url = "https://www.imdb.com/";
            loadWebView();
        });

        binding.vimeoLayout.setOnClickListener(v -> {
            url = "https://vimeo.com/";
            loadWebView();
        });


        binding.refresh.setOnClickListener(v -> {
            binding.webVideoView.setVisibility(View.GONE);
            binding.webView.reload();
        });

        binding.home.setOnClickListener(v -> {
            changeFabColor();
            binding.webVideoView.setVisibility(View.GONE);
            if (binding.webView.canGoBack()) {
                temp_next = true;
            } else if (binding.mainViewLayout.getVisibility() != View.VISIBLE) {
                temp_next = true;
            }
            binding.playbackUrl.setVisibility(View.GONE);
            binding.webView.setVisibility(View.GONE);
            binding.mainViewLayout.setVisibility(View.VISIBLE);

            binding.searchView.setText(getString(R.string.title_browser));
        });

        binding.next.setOnClickListener(v -> {
            binding.webVideoView.setVisibility(View.GONE);
            if (temp_next) {
                temp_next = false;
                if (binding.mainViewLayout.getVisibility() == View.VISIBLE) {
                    binding.mainViewLayout.setVisibility(View.GONE);
                    binding.webView.setVisibility(View.VISIBLE);
                    binding.playbackUrl.setVisibility(View.GONE);
                    binding.searchView.setText(binding.webView.getUrl());
                } else if (binding.webView.getVisibility() != View.VISIBLE) {
                    binding.mainViewLayout.setVisibility(View.GONE);
                    binding.webView.setVisibility(View.VISIBLE);
                    binding.playbackUrl.setVisibility(View.GONE);
                    binding.searchView.setText(binding.webView.getUrl());
                }
            } else {
                if (binding.webView.canGoForward()) {
                    if (binding.mainViewLayout.getVisibility() == View.VISIBLE) {
                        binding.mainViewLayout.setVisibility(View.GONE);
                        binding.webView.setVisibility(View.VISIBLE);
                        binding.playbackUrl.setVisibility(View.GONE);
                        binding.searchView.setText(binding.webView.getUrl());
                    } else {
                        binding.webView.goForward();
                    }
                }
            }
        });
        binding.previous.setOnClickListener(v -> {
            binding.webVideoView.setVisibility(View.GONE);
            isYouTubeVideo = false;
            if (binding.webView.canGoBack()) {
                binding.webView.goBack();
            } else if (binding.mainViewLayout.getVisibility() != View.VISIBLE) {
                binding.mainViewLayout.setVisibility(View.VISIBLE);
                binding.webView.setVisibility(View.GONE);
                binding.playbackUrl.setVisibility(View.GONE);
                temp_next = true;
                binding.searchView.setText(getString(R.string.title_browser));
            }

        });

        binding.backArrow.setOnClickListener(v -> {
            finish();
        });

        binding.playbackUrl.setOnClickListener(v -> {
            if (webVideoLinkAdapter.getItemCount() > 0) {
                binding.webVideoView.setVisibility(View.VISIBLE);
            } else {
                showNoResourceFoundDialog();
            }
        });

        binding.webVideoView.setOnClickListener(v -> {
            if (binding.webVideoView.getVisibility() == View.VISIBLE) {
                binding.webVideoView.setVisibility(View.GONE);
            }
        });
    }

    public void loadWebView() {
        HandlerThread thread = new HandlerThread("Video Extraction Thread");
        thread.start();
        final Handler extractVideoHandler = new Handler(thread.getLooper());

        WebSettings webSettings = binding.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
//        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        binding.webView.setWebViewClient(new WebViewClient() {
            private VideoExtractionRunnable videoExtract = new VideoExtractionRunnable();
            private ConcreteVideoContentSearch videoSearch = new ConcreteVideoContentSearch();
            private String currentPage = binding.webView.getUrl();

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                if (!url.startsWith("intent")) {
                    return super.shouldOverrideUrlLoading(view, url);
                }
                return true;
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (!request.getUrl().toString().startsWith("intent")) {
                    return super.shouldOverrideUrlLoading(view, request);
                }
                return true;
            }

            @Override
            public void onPageStarted(final WebView view, final String url, Bitmap favicon) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        binding.searchView.setText(url);
                        WebActivity.this.url = url;
                    }
                });

                binding.progressBar.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                binding.playbackUrl.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadResource(final WebView view, final String url) {
                final String page = view.getUrl();
                final String title = view.getTitle();

                if (!page.equals(currentPage)) {
                    currentPage = page;
                    isYouTubeVideo = false;
                    changeFabColor();

                    webVideoLinkAdapter.clearData();
                    videoDetectionInitiator.clear();
                }

                videoExtract.setUrl(url);
                videoExtract.setTitle(title);
                videoExtract.setPage(page);
                extractVideoHandler.post(videoExtract);
            }

            class VideoExtractionRunnable implements Runnable {
                private String url = "https://";
                private String title = "";
                private String page = "";

                public void setUrl(String url) {
                    this.url = url;
                }

                public void setTitle(String title) {
                    this.title = title;
                }

                public void setPage(String page) {
                    this.page = page;
                }

                @Override
                public void run() {
                    try {
                        if (page.contains("youtube")) {
                            String youtubelink = page;
                            if (page.contains("v=")) {
                                youtubelink = page.split("v=")[1];
                                youtubelink = "https://www.youtube.com/watch?v=" + youtubelink;
                            }
                            if (!isYouTubeVideo) {
                                isYouTubeVideo = true;
                                new YouTubeExtractor(WebActivity.this) {
                                    @Override
                                    public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                                        Log.e("------>", new Gson().toJson(ytFiles));
                                        setYoutubeVideo(ytFiles, vMeta);
                                    }
                                }.extract(youtubelink, true, true);
                            }
                        } else if (page.contains("vimeo.com")) {
                            if (!isYouTubeVideo) {
                                isYouTubeVideo = true;
                                new VimeoExtractor() {
                                    @Override
                                    protected void onExtractionComplete(ArrayList<VimeoFile> vimeoFileArrayList) {
                                        //complete
                                        setVimeoVideo(vimeoFileArrayList);
                                    }

                                    @Override
                                    protected void onExtractionFail(String Error) {
                                        //fail
                                        Toast.makeText(WebActivity.this, "Video not found", Toast.LENGTH_SHORT).show();
                                    }
                                }.Extractor(WebActivity.this, page);
                            }

                        } else {
                            String urlLowerCase = url.toLowerCase();
                            String[] filters = getResources().getStringArray(R.array.videourl_filters);
                            boolean urlMightBeVideo = false;
                            for (String filter : filters) {
                                if (urlLowerCase.contains(filter)) {
                                    urlMightBeVideo = true;
                                    break;
                                }
                            }

                            if (urlMightBeVideo) {
                                videoSearch.newSearch(url, page, title);

                                if (isDetecting) {
                                    videoSearch.run();
                                } else {
                                    videoDetectionInitiator.reserve(url, page, title);
                                }
                            }
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return super.shouldInterceptRequest(view, url);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return shouldInterceptRequest(view, request.getUrl().toString());
            }
        });

        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.mainViewLayout.setVisibility(View.GONE);
                    binding.playbackUrl.setVisibility(View.GONE);
                    binding.webView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                setTitle(title);

            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
            }
        });

        binding.webView.loadUrl(url);
    }

    public void fragmentLoad(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_load, fragment);
        transaction.addToBackStack("frag");
        transaction.commit();
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
    public void onDeviceConnect(boolean isConnected) {
        Constant.isConnected = isConnected;
        invalidateOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (upnpServiceController != null) {
//            upnpServiceController.resume(this);
//        }
        binding.webView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.webView.onPause();
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
    public void onItemClick(int position) {
        binding.webVideoView.setVisibility(View.GONE);
        if (position < webVideoList.size()) {
            if (!isConnected) {
                startActivity(new Intent(WebActivity.this, CastDeviceListActivity.class));
                return;
            } else {
                if (isChromeCastConnected) {
                    if (mCastSession == null) {
                        startActivity(new Intent(WebActivity.this, CastDeviceListActivity.class));
                        return;
                    }
                } else if (isDLNACastConnected) {
                    if (upnpServiceController.getSelectedRenderer() == null) {
                        startActivity(new Intent(WebActivity.this, CastDeviceListActivity.class));
                        return;
                    }
                }
            }
            WebVideoPlayActivity webVideoPlayActivity = new WebVideoPlayActivity();
            webVideoPlayActivity.setData(webVideoList);
            startActivity(new Intent(this, WebVideoPlayActivity.class).putExtra("extra_position", position));
        }
    }

    public class ConcreteVideoContentSearch extends VideoContentSearch {

        @Override
        public void onStartInspectingURL() {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (binding.progressBar.getVisibility() == View.GONE) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                }
            });

            Utils.disableSSLCertificateChecking();
        }

        @Override
        public void onFinishedInspectingURL(boolean finishedAll) {
            HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSF);
            if (finishedAll) {
                new Handler(Looper.getMainLooper()).post(() -> binding.progressBar.setVisibility(View.GONE));
            }
        }

        @Override
        public void onVideoFound(String size, String type, String link,
                                 String name, String page, boolean chunked,
                                 String website, String width, String height, String thumbnail_url) {

            doBounceAnimation(binding.playbackUrl);

            binding.playbackUrl.setImageResource(R.drawable.ic_play_color_accent);

            WebVideo webVideo = new WebVideo(size, type, link, name, page, website, "", chunked, false, false, width, height, thumbnail_url);
            webVideoList.add(webVideo);
            webVideoLinkAdapter.addItem(webVideo);
        }
    }

    public void setYoutubeVideo(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
        if (ytFiles != null && ytFiles.size() != 0) {
            webVideoList.clear();
            webVideoLinkAdapter.clearData();
            List<YtFile> arrayList = new ArrayList<YtFile>(ytFiles.size());
            for (int i = 0; i < ytFiles.size(); i++)
                arrayList.add(ytFiles.valueAt(i));

            for (YtFile ytFile : arrayList) {
                WebVideo webVideo = new WebVideo(String.valueOf(vMeta.getVideoLength()), ytFile.getFormat().getExt(), ytFile.getUrl(), vMeta.getTitle(), "", "", "", false, false, false, String.valueOf(ytFile.getFormat().getFps()), String.valueOf(ytFile.getFormat().getHeight()), vMeta.getThumbUrl());

                boolean duplicate = false;
                for (WebVideo v : webVideoList) {
                    if (v.getHeight().equals(webVideo.getHeight())) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate && !webVideo.getHeight().equals("-1") && !webVideo.getLink().contains("&url=")) {
                    webVideoList.add(webVideo);
                    webVideoLinkAdapter.addItem(webVideo);
                }
            }

            if (webVideoList.size() > 0) {
                doBounceAnimation(binding.playbackUrl);
                binding.playbackUrl.setVisibility(View.VISIBLE);
                binding.playbackUrl.setImageResource(R.drawable.ic_play_color_accent);
            }
            isYouTubeVideo = true;
        } else {
            isYouTubeVideo = false;
        }
    }

    public void setVimeoVideo(ArrayList<VimeoFile> vimeoFileArrayList) {
        if (vimeoFileArrayList != null && vimeoFileArrayList.size() != 0) {
            doBounceAnimation(binding.playbackUrl);
            binding.playbackUrl.setVisibility(View.VISIBLE);
            binding.playbackUrl.setImageResource(R.drawable.ic_play_color_accent);

            for (VimeoFile vimeoFile : vimeoFileArrayList) {
                WebVideo webVideo = new WebVideo(String.valueOf(vimeoFile.getSize()), vimeoFile.getExt(), vimeoFile.getUrl(), vimeoFile.getFilename(), "", "", "", false, false, false, vimeoFile.getQuality(), vimeoFile.getQuality(), vimeoFile.getUrl());
                webVideoList.add(webVideo);
                webVideoLinkAdapter.addItem(webVideo);
            }
            isYouTubeVideo = true;
        } else {
            isYouTubeVideo = false;
        }
    }

    public void changeFabColor() {
        binding.playbackUrl.setImageResource(R.drawable.ic_play_grey);
    }

    private void doBounceAnimation(View targetView) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Animation animCrossFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.bounce);

                targetView.startAnimation(animCrossFadeIn);
            }
        });
    }

    @Override
    public void onBackPressed() {
        isYouTubeVideo = false;
        binding.webVideoView.setVisibility(View.GONE);
        webVideoLinkAdapter = new WebVideoLinkAdapter(WebActivity.this);
        if (binding.webView.canGoBack()) {
            binding.webView.goBack();
        } else if (binding.mainViewLayout.getVisibility() != View.VISIBLE) {
            binding.mainViewLayout.setVisibility(View.VISIBLE);
            binding.webView.setVisibility(View.GONE);
            binding.playbackUrl.setVisibility(View.GONE);
            temp_next = true;
            binding.searchView.setText(getString(R.string.title_browser));
            getSupportFragmentManager().popBackStack();
            changeFabColor();
        } else {
            super.onBackPressed();
        }
    }

    public void showNoResourceFoundDialog() {
        final Dialog rateUsDialog = new Dialog(this);
        rateUsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        rateUsDialog.setContentView(R.layout.dialog_no_resource_found);
        rateUsDialog.setCancelable(false);

        TextView actionOk = rateUsDialog.findViewById(R.id.actionOk);

        actionOk.setOnClickListener(v -> {
            rateUsDialog.dismiss();
        });
        rateUsDialog.show();
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
