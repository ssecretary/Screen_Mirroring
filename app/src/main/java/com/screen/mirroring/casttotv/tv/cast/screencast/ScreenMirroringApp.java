package com.screen.mirroring.casttotv.tv.cast.screencast;

import android.app.Application;

import com.appnext.base.Appnext;
import com.facebook.ads.AudienceNetworkAds;
import com.onesignal.OneSignal;
import com.screen.mirroring.casttotv.tv.cast.screencast.ads.AppOpenManager;
import com.screen.mirroring.casttotv.tv.cast.screencast.permission.PermissionManager;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

public class ScreenMirroringApp extends Application {

    private static AppOpenManager appOpenManager;
    private String ONESIGNAL_APP_ID = "a0eea9c1-8b15-4241-9cc3-efd52f52ddba";

    @Override
    public void onCreate() {
        super.onCreate();

        PermissionManager.init(this);
        appOpenManager = new AppOpenManager(this);

        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(getResources().getString(R.string.app_metrica)).build();
        YandexMetrica.activate(getApplicationContext(), config);
        YandexMetrica.enableActivityAutoTracking(this);

        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        AudienceNetworkAds.initialize(this);
        Appnext.init(this);
    }
}
