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
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.DeviceConnectListener;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.OnItemClickListner;
import com.screen.mirroring.casttotv.tv.cast.screencast.ads.LoadAds;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.MediaFileModel;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.MediaFolderData;
import com.screen.mirroring.casttotv.tv.cast.screencast.permission.PermissionCallback;
import com.screen.mirroring.casttotv.tv.cast.screencast.permission.PermissionManager;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.PreferencesUtility;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.Utils;
import com.screen.mirroring.casttotv.tv.cast.screencast.Constant;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ActivityPhotosBinding;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter.PhotosAlbumAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

//import es.munix.multidisplaycast.CastManager;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isConnected;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isMainAdLoad;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isPhotoAdLoad;

public class PhotosActivity extends AppCompatActivity implements OnItemClickListner, DeviceConnectListener {

    ActivityPhotosBinding binding;
    List<MediaFileModel> imageList;
    List<MediaFolderData> folderDataList;
    public LinkedHashMap<String, ArrayList<MediaFileModel>> bucketimagesDataHashMap;
    PhotosAlbumAdapter photosAlbumAdapter;

    LoadAds loadAds;
    public String showPlateformAd = "admob";

    private final PermissionCallback permissionReadstorageCallback = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            if (isMainAdLoad) {
                loadAds.showFullAd(4);
            } else {
                loadAds.showFullAd(1);
            }
            getAlbumImages();
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

        binding.toolbar.setTitle(R.string.title_photos);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadAds = LoadAds.getInstance(this);
        showPlateformAd = PreferencesUtility.getShowPlateformAd(this);

        imageList = new ArrayList<>();
        folderDataList = new ArrayList<>();
        bucketimagesDataHashMap = new LinkedHashMap<>();
        photosAlbumAdapter = new PhotosAlbumAdapter(this);
        photosAlbumAdapter.setOnItemClickListner(this);

        initView();

        if (PermissionManager.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (isMainAdLoad) {
                loadAds.showFullAd(4);
            } else {
                loadAds.showFullAd(1);
            }
            getAlbumImages();
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
        binding.photosListView.setAdapter(photosAlbumAdapter);
    }

    public void getAlbumImages() {
        Observable.fromCallable(() -> {
            getImagesList();
            return true;
        }).subscribeOn(Schedulers.io())
                .subscribe((result) -> {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (folderDataList.size() != 0) {
                            binding.emptyView.setVisibility(View.GONE);
                            binding.photosListView.setVisibility(View.VISIBLE);
                            photosAlbumAdapter.setFolderDataList(folderDataList);
                        } else {
                            binding.emptyView.setVisibility(View.VISIBLE);
                            binding.photosListView.setVisibility(View.GONE);
                        }
                    });
                });
    }

    public void getImagesList() {
        folderDataList.clear();
        Cursor mCursor = null;

        String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.TITLE
                , MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media._ID};

        mCursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // Uri
                projection, // Projection
                null,
                null,
                "LOWER(" + MediaStore.Images.Media.DATE_MODIFIED + ") DESC");

        if (mCursor != null) {
            File file = null;
            if (mCursor.moveToFirst()) {
                do {
                    String path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String title = mCursor.getString(mCursor.getColumnIndex(projection[1]));
                    if (title != null) {
                    } else {
                        title = "";
                    }
                    String bucketName = mCursor.getString(mCursor.getColumnIndex(projection[2]));
                    String id = mCursor.getString(3);

                    MediaFileModel imagesData = new MediaFileModel();
                    imagesData.setFilePath(path);

                    imagesData = new MediaFileModel();
                    imagesData.setFilePath(path);
                    imagesData.setFileName(title);
                    imagesData.setType(1);
                    imagesData.setFolderName(bucketName);
                    imagesData.setId(id);

                    Log.d("TAG", "getAllImagesAndVideoData: " + id);

                    if (bucketimagesDataHashMap.containsKey(bucketName)) {
                        ArrayList<MediaFileModel> imagesData1 = bucketimagesDataHashMap.get(bucketName);
                        imagesData1.add(imagesData);
                        bucketimagesDataHashMap.put(bucketName, imagesData1);

                    } else {
                        ArrayList<MediaFileModel> imagesData1 = new ArrayList<>();
                        imagesData1.add(imagesData);
                        bucketimagesDataHashMap.put(bucketName, imagesData1);
                    }
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        }

        Set<String> keys = bucketimagesDataHashMap.keySet();
        ArrayList<String> listkeys = new ArrayList<>();
        listkeys.addAll(keys);

        for (int i = 0; i < listkeys.size(); i++) {
            ArrayList<MediaFileModel> imagesData = bucketimagesDataHashMap.get(listkeys.get(i));

            MediaFolderData bucketData = new MediaFolderData();
            bucketData.setFolderName(listkeys.get(i));
            bucketData.setMediaFileList(imagesData);
            File folderFile = new File(Utils.getParentPath(imagesData.get(0).getFilePath()));
            bucketData.setFolderPath(Utils.getParentPath(imagesData.get(0).getFilePath()));
            double length = 0;
            for (MediaFileModel mediaFileListModel : imagesData) {
                length = length + mediaFileListModel.getLength();
            }

            bucketData.setLength(length);
            bucketData.setLastModified(folderFile.lastModified());
            folderDataList.add(bucketData);
        }
    }

    private void checkPermissionAndThenLoad() {
        //check for permission
        if (PermissionManager.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                && PermissionManager.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (!isPhotoAdLoad) {
                isPhotoAdLoad = true;
                loadAds.showFullAd(1);
            }
            getAlbumImages();
        } else {
            if (PermissionManager.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(binding.getRoot(), "Screen Mirroring will need to read external storage to display images on your device.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                PermissionManager.askForPermission(PhotosActivity.this, new String[]{
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
    public void onItemClick(int position) {
        PhotosListActivity photosListActivity = new PhotosListActivity();
        photosListActivity.setData(folderDataList.get(position).getMediaFileList(), folderDataList.get(position).getFolderName());
        startActivity(new Intent(this, PhotosListActivity.class));
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
    public void onDeviceConnect(boolean isConnected) {
        Constant.isConnected = isConnected;
        invalidateOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (CastDeviceListActivity.upnpServiceController != null) {
            CastDeviceListActivity.upnpServiceController.resume(this);
        }
    }
}
