package com.screen.mirroring.casttotv.tv.cast.screencast.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.screen.mirroring.casttotv.tv.cast.screencast.Constant;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;

import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.PREF_LANGUAGE_POSITION;
import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.PREF_RATE_US;

public class PreferencesUtility {

    private static PreferencesUtility sInstance;
    private static volatile SharedPreferences mPreferences;

    public PreferencesUtility(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferencesUtility getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (PreferencesUtility.class) {
                if (sInstance == null) {
                    sInstance = new PreferencesUtility(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    public static void setRateUs(Context context, boolean showPlateform) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PREF_RATE_US, showPlateform);
        editor.commit();
    }

    public static boolean getRateUS(Context context) {
        return mPreferences.getBoolean(PREF_RATE_US, false);
    }

    public static void setSelectedLanguagePosition(Context context, int position) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(PREF_LANGUAGE_POSITION, position);
        editor.commit();
    }

    public static int getSelectedLanguagePosition(Context context) {
        return mPreferences.getInt(PREF_LANGUAGE_POSITION, 4);
    }

    public static void setShowPlateformAd(Context context, String showPlateform) {
        SharedPreferences sp = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(Constant.PREF_SHOW_PLATEFORM_ID, showPlateform).commit();
    }

    public static String getShowPlateformAd(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(Constant.PREF_SHOW_PLATEFORM_ID, Constant.ADMOB_AD_KEY);
    }

    public static void setNativeAdId(Context context, String nativeId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(Constant.PREF_NATIVE_AD_ID, nativeId).commit();
    }

    public static String getNativeAdId(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(Constant.PREF_NATIVE_AD_ID, context.getString(R.string.native_id));
    }

    public static void setIntersialAdId(Context context, String intersialId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(Constant.PREF_INTERSIAL_AD_ID, intersialId).commit();
    }

    public static String getIntersialAdId(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(Constant.PREF_INTERSIAL_AD_ID, context.getString(R.string.interstitial_id));
    }

    public static void setBannerAdId(Context context, String bannerId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(Constant.PREF_BANNER_AD_ID, bannerId).commit();
    }

    public static String getBannerAdId(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(Constant.PREF_BANNER_AD_ID, context.getString(R.string.banner_id));
    }

    public static void setAppOpenAdId(Context context, String appOpenId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(Constant.PREF_APP_OPEN_AD_ID, appOpenId).commit();
    }

    public static String getAppOpenId(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(Constant.PREF_APP_OPEN_AD_ID, context.getString(R.string.app_open_id));
    }

    public static void setAdMobUnitId(Context context, String appId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(Constant.PREF_ADMOB_UNIT_ID, appId).commit();
    }

    public static String getAdMobUnitId(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(Constant.PREF_ADMOB_UNIT_ID, context.getString(R.string.admob_app_id));
    }

    public static void setLastTimeShowInterstitial(Context context, long time) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(Constant.PREF_LAST_INTERSISIAL_INTERVAL, time).commit();
    }

    public static long getLastTimeShowInterstitial(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(Constant.PREF_LAST_INTERSISIAL_INTERVAL, 0);
    }

    public static String getAdData(Context context) {

        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);

        return preferences.getString("AdData", "");

    }

    public static void setAdData(Context context, String adData) {

        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("AdData", adData);
        editor.apply();

    }
}
