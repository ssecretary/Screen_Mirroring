package com.screen.mirroring.casttotv.tv.cast.screencast.ads;


import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.appnext.ads.interstitial.Interstitial;
import com.appnext.banners.BannerAdRequest;
import com.appnext.banners.BannerSize;
import com.appnext.banners.BannerView;
import com.appnext.core.AppnextError;
import com.appnext.nativeads.NativeAd;
import com.appnext.nativeads.NativeAdListener;
import com.appnext.nativeads.NativeAdRequest;
import com.appnext.nativeads.NativeAdView;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.NativeAdLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.screen.mirroring.casttotv.tv.cast.screencast.Constant;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.PreferencesUtility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.isMainAdLoad;

public class LoadAds {

    private static final String TAG = "LoadAds";
    private static LoadAds loadAds;
    private static boolean isShowAdOnLoad = false;
    private static InterstitialAd interstitialAds;
    private Activity context;
    public static boolean isAdLoadFaild = false;
    public static String showPlateformAd = "admob";
    private static com.facebook.ads.InterstitialAd facebookInterstitialAds;
    private static Interstitial appNextInterstitialAds;
    public com.google.android.gms.ads.nativead.NativeAd nativeAd;
    public NativeAd appNextNativeAd;
    public com.facebook.ads.NativeAd facebookNativeAd;

    public LoadAds(Activity context) {
        this.context = context;
        showPlateformAd = PreferencesUtility.getShowPlateformAd(context);
        if (showPlateformAd.equalsIgnoreCase(Constant.ADMOB_AD_KEY)) {
            loadAdMobAd();
        } else if (showPlateformAd.equalsIgnoreCase(Constant.FACEBOOK_AD_KEY)) {
            loadFacbookAd();
        } else if (showPlateformAd.equalsIgnoreCase(Constant.APP_NEXT_AD_KEY)) {
            loadAppNextAd();
        }
    }

    public void loadAdMobAd() {
        interstitialAds = new InterstitialAd(context);
//        interstitialAds.setAdUnitId(context.getResources().getString(R.string.intersial_id));
        interstitialAds.setAdUnitId(PreferencesUtility.getIntersialAdId(context));
        interstitialAds.setAdListener(new ToastAdListener(context) {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                System.out.println(">>>> is is ad:::" + isShowAdOnLoad);
                if (isShowAdOnLoad) {
                    Constant.SHOW_OPEN_ADS = false;
                    interstitialAds.show();
                }
                isShowAdOnLoad = false;
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.e("errorCode ---->", String.valueOf(errorCode));
                isAdLoadFaild = true;
                Constant.SHOW_OPEN_ADS = true;
                super.onAdFailedToLoad(errorCode);
//                AdLoard();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Constant.SHOW_OPEN_ADS = true;
                AdLoard();
            }
        });
        AdLoard();
    }

    public void loadFacbookAd() {
        if (facebookInterstitialAds != null) {
            facebookInterstitialAds.destroy();
            facebookInterstitialAds = null;
        }

        facebookInterstitialAds = new com.facebook.ads.InterstitialAd(context, PreferencesUtility.getIntersialAdId(context));
        facebookInterstitialAds.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {

            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                Constant.SHOW_OPEN_ADS = true;
                loadFacbookAd();
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                Log.e("onError ---->", adError.getErrorMessage());
                if (adError.getErrorCode() == 1001) {
                    loadFacbookAd();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.e("onAdLoaded ---->", "---->onError ");
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }
        });
//        AdSettings.addTestDevice("54707d55-2289-4a1e-ae72-51743490bd55");
        facebookInterstitialAds.loadAd();
    }

    public void loadAppNextAd() {
        appNextInterstitialAds = new Interstitial(context, PreferencesUtility.getIntersialAdId(context));
        appNextInterstitialAds.setOnAdErrorCallback(s -> {
            Log.e("Error ----->", s);
            isAdLoadFaild = true;
        });

        appNextInterstitialAds.setOnAdClosedCallback(() -> Constant.SHOW_OPEN_ADS = true);
        appNextInterstitialAds.loadAd();
    }

    public static LoadAds getInstance(Activity context) {
        showPlateformAd = PreferencesUtility.getShowPlateformAd(context);
        if (loadAds == null) {
            loadAds = new LoadAds(context);
        }
        return loadAds;
    }

    public static LoadAds getInstance(Activity context, boolean isShowAdOnLoad) {

        LoadAds.isShowAdOnLoad = isShowAdOnLoad;
        if (loadAds == null) {
            loadAds = new LoadAds(context);
        }
        return loadAds;
    }

    public InterstitialAd getIntersialAd() {
        return interstitialAds;
    }

    public com.facebook.ads.InterstitialAd getFacebookIntersialAd() {
        return facebookInterstitialAds;
    }

    public Interstitial getAppNextIntersialAd() {
        return appNextInterstitialAds;
    }

    public static void AdLoard() {
        // interstitialAds.loadAd(new AdRequest.Builder().build());
        interstitialAds.loadAd(new AdRequest.Builder().build());
    }

    public boolean show_intertial(Context context) {
        boolean isshow = false;
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long lastshowntimeinterval = PreferencesUtility.getLastTimeShowInterstitial(context);
        long interval = getTimeLimit();
        long timediff = currentTime - lastshowntimeinterval;
        if ((timediff) >= interval) {
            PreferencesUtility.setLastTimeShowInterstitial(context, currentTime);
            isshow = true;
        }
        return isshow;
    }

    public long getTimeLimit() {
//        return (FirebaseRemoteConfig.getInstance().getLong(Constant.INTERSISIAL_INTERVAL) * 1000);
        return 0;
    }

    public void showFullAd(final int Adfrequency) {
        showPlateformAd = PreferencesUtility.getShowPlateformAd(context);
        Random random = new Random();
        int num = random.nextInt(Adfrequency);
        Log.d("Advert random", "Ads num :- " + num);

        if (showPlateformAd.equalsIgnoreCase(Constant.ADMOB_AD_KEY)) {
            if (Adfrequency == 1) {
                if (interstitialAds != null && interstitialAds.isLoaded()) {
                    if (show_intertial(context)) {
                        Constant.SHOW_OPEN_ADS = false;
                        interstitialAds.show();
                        isMainAdLoad = true;
                    }
                } else if (interstitialAds != null && !interstitialAds.isLoading()) {
                    AdLoard();
                } else {
                    loadAdMobAd();
                }
            } else if (num == 3) {
                if (interstitialAds != null && interstitialAds.isLoaded()) {
                    if (show_intertial(context)) {
                        Constant.SHOW_OPEN_ADS = false;
                        interstitialAds.show();
                        isMainAdLoad = true;
                    }
                } else if (interstitialAds != null && !interstitialAds.isLoading()) {
                    AdLoard();
                } else {
                    loadAdMobAd();
                }
            }
        } else if (showPlateformAd.equalsIgnoreCase(Constant.FACEBOOK_AD_KEY)) {
            if (Adfrequency == 1) {
                if (facebookInterstitialAds != null && facebookInterstitialAds.isAdLoaded()) {
                    if (show_intertial(context)) {
                        Constant.SHOW_OPEN_ADS = false;
                        facebookInterstitialAds.show();
                        isMainAdLoad = true;
                    }
                } else if (facebookInterstitialAds != null) {
                    facebookInterstitialAds.loadAd();
                } else {
                    loadFacbookAd();
                }
            } else if (num == 3) {
                if (facebookInterstitialAds != null && facebookInterstitialAds.isAdLoaded()) {
                    if (show_intertial(context)) {
                        Constant.SHOW_OPEN_ADS = false;
                        facebookInterstitialAds.show();
                        isMainAdLoad = true;
                    }
                } else if (facebookInterstitialAds != null) {
                    facebookInterstitialAds.loadAd();
                } else {
                    loadFacbookAd();
                }
            }
        } else if (showPlateformAd.equalsIgnoreCase(Constant.APP_NEXT_AD_KEY)) {
            if (Adfrequency == 1) {
                if (appNextInterstitialAds != null && appNextInterstitialAds.isAdLoaded()) {
                    if (show_intertial(context)) {
                        Constant.SHOW_OPEN_ADS = false;
                        appNextInterstitialAds.showAd();
                        isMainAdLoad = true;
                    }
                } else if (appNextInterstitialAds != null) {
                    appNextInterstitialAds.loadAd();
                }
            } else if (num == 3) {
                if (appNextInterstitialAds != null && appNextInterstitialAds.isAdLoaded()) {
                    if (show_intertial(context)) {
                        Constant.SHOW_OPEN_ADS = false;
                        appNextInterstitialAds.showAd();
                        isMainAdLoad = true;
                    }
                } else if (appNextInterstitialAds != null) {
                    appNextInterstitialAds.loadAd();
                }
            }
        }
    }

    AdView adMobAdView;

    public AdView getBannerAdView() {
        return adMobAdView;
    }

    public void loadAdaptiveBanner(Context context, FrameLayout adContainerView) {

        MobileAds.initialize(context, initializationStatus -> {
        });

        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        adMobAdView = new AdView(context);
//        adMobAdView.setAdUnitId(context.getResources().getString(R.string.banner_id));
        adMobAdView.setAdUnitId(PreferencesUtility.getBannerAdId(context));
        adContainerView.removeAllViews();
        adContainerView.addView(adMobAdView);

        AdSize adSize = getAdSize(context, adContainerView);
        adMobAdView.setAdSize(adSize);

        AdRequest adRequest =
                new AdRequest.Builder().build();

        // Start loading the ad in the background.
        adMobAdView.loadAd(adRequest);
    }

    public AdSize getAdSize(Context context, FrameLayout adContainerView) {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }

    public com.facebook.ads.AdView getFacebookBannerAdView() {
        return adView;
    }

    com.facebook.ads.AdView adView;

    public void loadFacebookBannerAd(LinearLayout adContainer) {
        adView = new com.facebook.ads.AdView(context, PreferencesUtility.getBannerAdId(context), com.facebook.ads.AdSize.BANNER_HEIGHT_50);
        adContainer.addView(adView);
//        AdSettings.addTestDevice("b17e3bdf-fa40-4278-8707-8a76d67c3e73");
        adView.loadAd();
    }

    public void loadAppNextBannerAd(BannerView bannerView) {
        BannerAdRequest banner_request = new BannerAdRequest();
        bannerView.setPlacementId(PreferencesUtility.getBannerAdId(context));
        bannerView.setBannerSize(BannerSize.BANNER);
        bannerView.loadAd(banner_request);
    }

    public void loadNativeAd(final FrameLayout adFrameLayout, final CardView advertize_layout, final int layout) {
//        AdLoader.Builder builder = new AdLoader.Builder(this, getString(R.string.native_app_id));
        AdLoader.Builder builder = new AdLoader.Builder(context, PreferencesUtility.getNativeAdId(context));

        builder.forNativeAd(unifiedNativeAd -> {
            if (nativeAd != null) {
                nativeAd.destroy();
            }

            try {
                nativeAd = unifiedNativeAd;
                LayoutInflater inflater = LayoutInflater.from(context);
                com.google.android.gms.ads.nativead.NativeAdView adView = (com.google.android.gms.ads.nativead.NativeAdView) inflater
                        .inflate(layout, null);
                populateUnifiedNativeAdView(unifiedNativeAd, adView);
                adFrameLayout.removeAllViews();
                adFrameLayout.addView(adView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (advertize_layout != null)
                    advertize_layout.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (advertize_layout != null)
                    advertize_layout.setVisibility(View.VISIBLE);
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateUnifiedNativeAdView(com.google.android.gms.ads.nativead.NativeAd nativeAd, com.google.android.gms.ads.nativead.NativeAdView adView) {

        MediaView mediaView = adView.findViewById(R.id.ad_media);
        mediaView.setMediaContent(nativeAd.getMediaContent());
        mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
//            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }


        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }
        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }


        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);
//
//        VideoController vc = nativeAd.getVideoController();
//
//        // Updates the UI to say whether or not this ad has a video asset.
//        if (vc.hasVideoContent()) {
//            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
//                @Override
//                public void onVideoEnd() {
//
//                    super.onVideoEnd();
//                }
//            });
//        } else {
//        }
    }

    public void loadAppNextNativeAd(final FrameLayout adFrameLayout, final CardView advertize_layout, final int layout) {
        appNextNativeAd = new NativeAd(context, PreferencesUtility.getNativeAdId(context));
        appNextNativeAd.setAdListener(new NativeAdListener() {
            @Override
            public void onAdLoaded(final NativeAd nativeAd) {
                super.onAdLoaded(nativeAd);
                try {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    NativeAdView adView = (NativeAdView) inflater
                            .inflate(layout, null);
                    setAppNextViews(appNextNativeAd, adView);
                    adFrameLayout.removeAllViews();
                    adFrameLayout.addView(adView);
                    advertize_layout.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //Ad clicked callback
            @Override
            public void onAdClicked(NativeAd nativeAd) {
                super.onAdClicked(nativeAd);
            }

            //Ad error callback
            @Override
            public void onError(NativeAd nativeAd, AppnextError appnextError) {
                super.onError(nativeAd, appnextError);
                advertize_layout.setVisibility(View.GONE);
            }

            //Ad impression callback
            @Override
            public void adImpression(NativeAd nativeAd) {
                super.adImpression(nativeAd);
            }
        });

//The native ad request object
        appNextNativeAd.loadAd(new NativeAdRequest()
                // optional - config your ad request:
                .setCachingPolicy(NativeAdRequest.CachingPolicy.STATIC_ONLY)
                .setCreativeType(NativeAdRequest.CreativeType.ALL)
                .setVideoLength(NativeAdRequest.VideoLength.SHORT)
                .setVideoQuality(NativeAdRequest.VideoQuality.LOW)
        );
    }

    private void setAppNextViews(NativeAd appNextNativeAd, NativeAdView nativeAdView) {
        ImageView imageView = (ImageView) nativeAdView.findViewById(R.id.na_icon);
        TextView textView = (TextView) nativeAdView.findViewById(R.id.na_title);
        com.appnext.nativeads.MediaView mediaView = (com.appnext.nativeads.MediaView) nativeAdView.findViewById(R.id.na_media);
        ProgressBar progressBar = (ProgressBar) nativeAdView.findViewById(R.id.progressBar);
        TextView button = (TextView) nativeAdView.findViewById(R.id.install);
        TextView rating = (TextView) nativeAdView.findViewById(R.id.rating);
        TextView description = (TextView) nativeAdView.findViewById(R.id.description);
        ArrayList<View> viewArrayList = new ArrayList<>();
        viewArrayList.add(button);
        viewArrayList.add(mediaView);

        progressBar.setVisibility(View.GONE);

        //The ad Icon
        appNextNativeAd.downloadAndDisplayImage(imageView, appNextNativeAd.getIconURL());

        //The ad title
        textView.setText(appNextNativeAd.getAdTitle());

        //Setting up the Appnext MediaView
        appNextNativeAd.setMediaView(mediaView);

        //The ad rating
        rating.setText(appNextNativeAd.getStoreRating());

        //The ad description
//        description.setText(appNextNativeAd.getAdDescription());

        //Registering the clickable areas - see the array object in `setViews()` function
        appNextNativeAd.registerClickableViews(nativeAdView);

        //Setting up the entire native ad view
        appNextNativeAd.setNativeAdView(nativeAdView);
    }

    public void loadfacebookNativeAd(final FrameLayout adFrameLayout, final CardView advertize_layout, final int layout) {
        facebookNativeAd = new com.facebook.ads.NativeAd(context, PreferencesUtility.getNativeAdId(context));

        facebookNativeAd.setAdListener(new com.facebook.ads.NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                advertize_layout.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                try {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    NativeAdLayout adView = (NativeAdLayout) inflater
                            .inflate(layout, null);
                    setFacebookViews(facebookNativeAd, adView);
                    adFrameLayout.removeAllViews();
                    adFrameLayout.addView(adView);
                    advertize_layout.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Native ad clicked
                Log.d(TAG, "Native ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
                Log.d(TAG, "Native ad impression logged!");
            }
        });

        // Request an ad
        facebookNativeAd.loadAd();
    }

    private void setFacebookViews(com.facebook.ads.NativeAd nativeAd, NativeAdLayout adView) {

        nativeAd.unregisterView();

        // Add the Ad view into the ad container.
        com.facebook.ads.MediaView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

        com.facebook.ads.MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);

        // Setting the Text
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        nativeAdCallToAction.setVisibility(
                nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
//        sponsoredLabel.setText(R.string.sponsored);

        // You can use the following to specify the clickable areas.
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdIcon);
        clickableViews.add(nativeAdMedia);
        clickableViews.add(nativeAdCallToAction);
        nativeAd.registerViewForInteraction(
                adView,
                nativeAdMedia,
                nativeAdIcon,
                clickableViews);
    }

}
