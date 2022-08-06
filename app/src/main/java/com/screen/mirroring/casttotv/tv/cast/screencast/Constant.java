package com.screen.mirroring.casttotv.tv.cast.screencast;

import java.util.ArrayList;
import java.util.List;

public class Constant {

    public static final String PREF_RATE_US = "pref_rate_us";
    public static final String PREF_LANGUAGE_POSITION = "pref_language_position";

    public final static String VIDEO_PREFIX = "v-";
    public final static String AUDIO_PREFIX = "a-";
    public final static String IMAGE_PREFIX = "i-";

    public static String PREF_SHOW_PLATEFORM_ID = "show_plateform_id";
    public static String PREF_NATIVE_AD_ID = "native_ad_id";
    public static String PREF_BANNER_AD_ID = "banner_ad_id";
    public static String PREF_INTERSIAL_AD_ID = "intersial_ad_id";
    public static String PREF_ADMOB_UNIT_ID = "admob_unit_id";
    public static String PREF_APP_OPEN_AD_ID = "app_open_ad_id";
    public static String PREF_LAST_INTERSISIAL_INTERVAL = "last_interstitial_interval";
    public static String PREF_PLAY_URL = "play_url";
    public static String PREF_SHOW_PLAY_URL = "show_play_url";

    public static String NATIVE_AD_KEY = "native_id";
    public static String INTERSIAL_AD_KEY = "interstitial_id";
    public static String APP_OPEN_AD_KEY = "app_open";
    public static String BANNER_AD_KEY = "banner_id";
    public static String UNIT_AD_KEY = "app_id";
    public static String INTERSISIAL_INTERVAL = "ITimeInterval";

    public static String APP_NEXT_AD_KEY = "appnext";
    public static String FACEBOOK_AD_KEY = "facebook";
    public static String ADMOB_AD_KEY = "admob";
    public static String SHOW_PLATEFORM_AD_KEY = "showplatform";
    public static String PLAY_URL_KEY = "Play_url";
    public static String SHOW_PLAY_URL_KEY = "show_playurl";

    public static boolean OPEN_ADS = false;
    public static boolean SHOW_OPEN_ADS = true;

    public static final int POS_HOW_TO_USE = 0;
    public static final int POS_CHANGE_LANGUAGE = 1;
    public static final int POS_SHARE_NOW = 2;
    public static final int POS_RATE_US = 3;
    public static final int POS_MORE_APPS = 4;
    public static final int POS_PRIVACY_POLICY = 5;
    public static final int POS_ABOUT_US = 6;

    public static boolean isConnected = false;
    public static boolean isChromeCastConnected = false;
    public static boolean isDLNACastConnected = false;

    public static boolean isPhotoAdLoad = false;
    public static boolean isVideoAdLoad = false;
    public static boolean isAudioAdLoad = false;
    public static boolean isWebAdLoad = false;
    public static boolean isMainAdLoad = false;

    public static List<Object> castDeviceList = new ArrayList<>();

    public static boolean IS_APP_FIRST = true;

    public static Object SELECTED_DEVICE_POSITION = "";

}
