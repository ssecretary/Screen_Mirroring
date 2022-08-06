package com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import com.appnext.banners.BannerListener;
import com.appnext.core.AppnextError;
import com.google.android.material.snackbar.Snackbar;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.DeviceConnectListener;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.OnItemClickListner;
import com.screen.mirroring.casttotv.tv.cast.screencast.ads.LoadAds;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.MediaFileModel;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.MediaFolderData;
import com.screen.mirroring.casttotv.tv.cast.screencast.permission.PermissionCallback;
import com.screen.mirroring.casttotv.tv.cast.screencast.permission.PermissionManager;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.PreferencesUtility;
import com.screen.mirroring.casttotv.tv.cast.screencast.Constant;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ActivityPhotosBinding;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter.MusicAlbumAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isAudioAdLoad;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isMainAdLoad;

public class MusicAlbumActivity extends AppCompatActivity implements OnItemClickListner, DeviceConnectListener {

    ActivityPhotosBinding binding;

    public LinkedHashMap<String, ArrayList<MediaFileModel>> musicDataHashMap;
    public List<MediaFolderData> musicAlbumList;
    List<MediaFileModel> audioList;
    MusicAlbumAdapter musicAlbumAdapter;

    LoadAds loadAds;
    public String showPlateformAd = "admob";

    private final PermissionCallback permissionReadstorageCallback = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            if (isMainAdLoad){
                loadAds.showFullAd(4);
            }else {
                loadAds.showFullAd(1);
            }
            getAudios();
        }

        @Override
        public void permissionRefused() {
            finish();
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_photos);

        binding.toolbar.setTitle(R.string.title_audios);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadAds = LoadAds.getInstance(this);
        showPlateformAd = PreferencesUtility.getShowPlateformAd(this);

        audioList = new ArrayList<>();
        musicDataHashMap = new LinkedHashMap<>();
        musicAlbumList = new ArrayList<>();

        musicAlbumAdapter = new MusicAlbumAdapter(this);
        musicAlbumAdapter.setOnItemClickListner(this);

        initView();

        if (PermissionManager.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (isMainAdLoad){
                loadAds.showFullAd(4);
            }else {
                loadAds.showFullAd(1);
            }
            getAudios();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkPermissionAndThenLoad();
            }
        }
    }

    public void initView() {
        if (showPlateformAd.equalsIgnoreCase(Constant.ADMOB_AD_KEY)) {
            loadAds.loadNativeAd(binding.fram, binding.advertizeLayout, R.layout.ads_unified1);
        } else if (showPlateformAd.equalsIgnoreCase(Constant.FACEBOOK_AD_KEY)) {
            loadAds.loadfacebookNativeAd(binding.fram, binding.advertizeLayout, R.layout.ads_unified_facebook);
        } else if (showPlateformAd.equalsIgnoreCase(Constant.APP_NEXT_AD_KEY)) {
            loadAds.loadAppNextNativeAd(binding.fram, binding.advertizeLayout, R.layout.ads_unified_app_next);
        }

        binding.photosListView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.photosListView.setAdapter(musicAlbumAdapter);
    }

    public void getAudios() {
        Observable.fromCallable(() -> {
            getAudioList();
            return true;
        }).subscribeOn(Schedulers.io())
                .subscribe((result) -> {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (audioList.size() != 0) {
                            binding.emptyView.setVisibility(View.GONE);
                            binding.photosListView.setVisibility(View.VISIBLE);
                            musicAlbumAdapter.setFolderDataList(musicAlbumList);
                        } else {
                            binding.emptyView.setVisibility(View.VISIBLE);
                            binding.photosListView.setVisibility(View.GONE);
                        }
                    });
                });
    }

    public void getAudioList() {
        audioList.clear();
        Cursor mCursor = null;

        String[] projection = {MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATE_MODIFIED
                , MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media._ID};

        mCursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, // Uri
                projection, // Projection
                null,
                null,
                "LOWER(" + MediaStore.Images.Media.DATE_MODIFIED + ") DESC");

        if (mCursor != null) {
            File file = null;
            if (mCursor.moveToFirst()) {
                do {
                    String path = mCursor.getString(mCursor.getColumnIndex(projection[0]));
                    String title = mCursor.getString(mCursor.getColumnIndex(projection[1]));
                    long date = mCursor.getLong(mCursor.getColumnIndex(projection[2]));
                    int duration = mCursor.getInt(mCursor.getColumnIndex(projection[3]));
                    long size = mCursor.getLong(4);
                    String album = mCursor.getString(5);
                    long albumId = mCursor.getLong(6);
                    String title1 = mCursor.getString(7);
                    String id = mCursor.getString(mCursor.getColumnIndex(projection[8]));
                    try {
                        file = new File(path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    MediaFileModel musicData = new MediaFileModel();
                    musicData.setFilePath(path);

                    if (file != null && file.exists()) {

                        musicData = new MediaFileModel();
                        musicData.setFilePath(path);
                        musicData.setFileName(title);
                        musicData.setFileType(path.substring(path.lastIndexOf(".") + 1));
                        musicData.setType(3);
                        musicData.setLength(size);
                        musicData.setDuration(String.valueOf(duration));
                        musicData.setSongAlbum(album);
                        musicData.setAlbumId(albumId);
                        musicData.setId(id);

                        Log.d("TAG", "getAllImagesAndVideoData: ");
                        audioList.add(musicData);

                        if (musicDataHashMap.containsKey(album)) {

                            ArrayList<MediaFileModel> music1 = musicDataHashMap.get(album);
                            music1.add(musicData);
                            musicDataHashMap.put(album, music1);

                        } else {

                            ArrayList<MediaFileModel> imagesData1 = new ArrayList<>();
                            imagesData1.add(musicData);
                            musicDataHashMap.put(album, imagesData1);

                        }
                    }
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        }

        Set<String> keys = musicDataHashMap.keySet();
        ArrayList<String> listkeys = new ArrayList<>();
        listkeys.addAll(keys);

        for (int i = 0; i < listkeys.size(); i++) {
            ArrayList<MediaFileModel> musicData = musicDataHashMap.get(listkeys.get(i));

            if (musicData != null) {
                MediaFolderData bucketData = new MediaFolderData();
                bucketData.setFolderName(listkeys.get(i));
                bucketData.setMediaFileList(musicData);
                musicAlbumList.add(bucketData);
            }
        }
    }

    private void checkPermissionAndThenLoad() {
        //check for permission
        if (PermissionManager.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                && PermissionManager.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (!isAudioAdLoad){
                isAudioAdLoad = true;
                loadAds.showFullAd(1);
            }
            getAudios();
        } else {
            if (PermissionManager.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(binding.getRoot(), "Screen Mirroring will need to read external storage to display videos on your device.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                PermissionManager.askForPermission(MusicAlbumActivity.this, new String[]{
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissionReadstorageCallback);
                            }
                        }).show();
            } else {
                PermissionManager.askForPermission(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissionReadstorageCallback);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
    public void onItemClick(int position) {
        AudioActivity audioActivity = new AudioActivity();
        audioActivity.setData(musicAlbumList.get(position).getMediaFileList(), musicAlbumList.get(position).getFolderName());
        startActivity(new Intent(this, AudioActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (upnpServiceController != null) {
//            upnpServiceController.resume(this);
//        }
    }
}
