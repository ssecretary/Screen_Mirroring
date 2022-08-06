package com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.screen.mirroring.casttotv.tv.cast.screencast.Constant;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.ads.LoadAds;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.PreferencesUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import static com.screen.mirroring.casttotv.tv.cast.screencast.BuildConfig.DEBUG;

public class BaseActivity extends AppCompatActivity {

    FirebaseRemoteConfig mFirebaseRemoteConfig;
    public String unitId;
    String nativeAd;
    String bannerAd;
    String intersialAd;
    String appOpenAd;
    String showPlateForm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings.Builder configBuilder = new FirebaseRemoteConfigSettings.Builder();
        if (DEBUG) {
            long cacheInterval = 0;
            configBuilder.setMinimumFetchIntervalInSeconds(cacheInterval);
        }

        mFirebaseRemoteConfig.setConfigSettingsAsync(configBuilder.build());
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_confing_defaults);

        loadMyAdIds();
        new Handler(Looper.myLooper()).postDelayed(() -> loadMyAdIds(), 2000);
    }

    public void loadMyAdIds() {
        mFirebaseRemoteConfig.fetchAndActivate() // <- add the zero
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {

                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        showPlateForm = mFirebaseRemoteConfig.getString(Constant.SHOW_PLATEFORM_AD_KEY);
                        if (showPlateForm == null || showPlateForm.isEmpty()) {
                            showPlateForm = Constant.ADMOB_AD_KEY;
                        }

                        PreferencesUtility.setShowPlateformAd(BaseActivity.this, showPlateForm);
                        String getAdIds = mFirebaseRemoteConfig.getString(showPlateForm);
                        try {
                            String getAdsData = mFirebaseRemoteConfig.getString("data");
                            if (!TextUtils.isEmpty(getAdsData)) {
                                JSONObject adsObject = new JSONObject(getAdsData);
                                JSONArray adsArray = adsObject.getJSONArray("ADS_DATA");
                                String array = adsArray.toString();
                                PreferencesUtility.setAdData(BaseActivity.this, array);
                            }

                            JSONObject jsonObject = new JSONObject(getAdIds);
                            unitId = jsonObject.getString(Constant.UNIT_AD_KEY);
                            nativeAd = jsonObject.getString(Constant.NATIVE_AD_KEY);
                            bannerAd = jsonObject.getString(Constant.BANNER_AD_KEY);
                            intersialAd = jsonObject.getString(Constant.INTERSIAL_AD_KEY);
                            appOpenAd = jsonObject.getString(Constant.APP_OPEN_AD_KEY);

                            PreferencesUtility.setAdMobUnitId(BaseActivity.this, unitId);
                            PreferencesUtility.setBannerAdId(BaseActivity.this, bannerAd);
                            PreferencesUtility.setIntersialAdId(BaseActivity.this, intersialAd);
                            PreferencesUtility.setNativeAdId(BaseActivity.this, nativeAd);
                            PreferencesUtility.setAppOpenAdId(BaseActivity.this, appOpenAd);

                            if (showPlateForm.equalsIgnoreCase(Constant.ADMOB_AD_KEY)) {
                                MobileAds.initialize(BaseActivity.this, unitId);

//                                RequestConfiguration.Builder builder = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("CCCD84BED84381F19285E46D3DBE9642"));
//                                MobileAds.setRequestConfiguration(builder.build());
                            }

                            LoadAds.getInstance(BaseActivity.this);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
