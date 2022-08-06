package com.screen.mirroring.casttotv.tv.cast.screencast;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.screen.mirroring.casttotv.tv.cast.screencast.castserver.CastServerService;
import com.screen.mirroring.casttotv.tv.cast.screencast.model.WebVideo;
import com.screen.mirroring.casttotv.tv.cast.screencast.utils.Utils;

import static android.content.Context.WIFI_SERVICE;

public class WebServerController {

    private final Context context;

    public WebServerController(Context context) {
        this.context = context.getApplicationContext();
    }

    public void stopCastServer() {
        Intent castServerService = new Intent(context, CastServerService.class);
        context.stopService(castServerService);
    }

    private void startCastServer(final String ip, final String rootDir) {
        Intent castServerService = new Intent(context, CastServerService.class);
        castServerService.putExtra(CastServerService.IP_ADDRESS, ip);
        castServerService.putExtra(CastServerService.ROOT_DIR, rootDir);
        context.startService(castServerService);
    }

    public MediaInfo getMediaInfo(String mediaName, boolean photoMode) {
        stopCastServer();
        
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        @SuppressWarnings("deprecation")
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        String filename = "";
        String rootDir = ".";
        int slash = mediaName.lastIndexOf('/');
        if (slash > 1) {
            filename = mediaName.substring(slash + 1);
            rootDir = mediaName.substring(0, slash);
        }
        startCastServer(ip, rootDir);
        Log.e("Tag", "Root    :    " + rootDir + "   File name ::::    " +filename);
        String url = "http://" + ip + ":" + CastServerService.SERVER_PORT + "/" + filename;
        String thumnailUrl = "http://" + ip + ":" + CastServerService.SERVER_PORT + "/" + filename;

        Log.e("Tag", "url :    " + url + "   File name ::::    " +filename);
        MediaMetadata mediaMetadata = photoMode ?
                new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO) :
                new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        mediaMetadata.putString(MediaMetadata.KEY_TITLE, filename);
        mediaMetadata.addImage(new WebImage(Uri.parse(url)));
        mediaMetadata.addImage(new WebImage(Uri.parse(url)));

        return photoMode ?
                new MediaInfo.Builder(url)
                        .setContentType(Utils.getMimeType(context, Uri.parse(filename)))
                        .setStreamType(MediaInfo.STREAM_TYPE_NONE)
                        .setMetadata(mediaMetadata)
                        .build() :
                new MediaInfo.Builder(url)
                        .setContentType(Utils.getMimeType(context, Uri.parse(filename)))
                        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                        .setMetadata(mediaMetadata)
//                    .setStreamDuration(selectedMedia.getDuration() * 1000)
                        .build();
    }

    public MediaInfo getMediaQueueInfo(String mediaName, boolean photoMode) {
        stopCastServer();
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        @SuppressWarnings("deprecation")
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        String filename = "";
        String rootDir = ".";
        int slash = mediaName.lastIndexOf('/');
        if (slash > 1) {
            filename = mediaName.substring(slash + 1);
            rootDir = mediaName.substring(0, slash);
        }
        startCastServer(ip, rootDir);

        String url = "http://" + ip + ":" + CastServerService.SERVER_PORT + "/" + filename;
        String thumnailUrl = "http://" + ip + ":" + CastServerService.SERVER_PORT + "/" + filename;

        MediaMetadata mediaMetadata = photoMode ?
                new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE) :
                new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        mediaMetadata.putString(MediaMetadata.KEY_TITLE, filename);
        mediaMetadata.addImage(new WebImage(Uri.parse(url)));
        mediaMetadata.addImage(new WebImage(Uri.parse(url)));

        return photoMode ?
                new MediaInfo.Builder(url)
                        .setContentType(Utils.getMimeType(context, Uri.parse(filename)))
                        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                        .setMetadata(mediaMetadata)
                        .setStreamDuration(2 * 1000)
                        .build() :
                new MediaInfo.Builder(url)
                        .setContentType(Utils.getMimeType(context, Uri.parse(filename)))
                        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                        .setMetadata(mediaMetadata)
//                    .setStreamDuration(selectedMedia.getDuration() * 1000)
                        .build();
    }

    public MediaInfo getAudioMediaInfo(String mediaName) {
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        @SuppressWarnings("deprecation")
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        String filename = "";
        String rootDir = ".";
        int slash = mediaName.lastIndexOf('/');
        if (slash > 1) {
            filename = mediaName.substring(slash + 1);
            rootDir = mediaName.substring(0, slash);
        }
        startCastServer(ip, rootDir);

        String url = "http://" + ip + ":" + CastServerService.SERVER_PORT + "/" + filename;

        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);

        mediaMetadata.putString(MediaMetadata.KEY_TITLE, filename);
        mediaMetadata.addImage(new WebImage(Uri.parse(url)));
        mediaMetadata.addImage(new WebImage(Uri.parse(url)));

        return new MediaInfo.Builder(url)
                .setContentType(Utils.getMimeType(context, Uri.parse(filename)))
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();
    }

    public MediaInfo getUrlMediaInfo(WebVideo webVideo) {
//        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
//        @SuppressWarnings("deprecation")
//        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
//
//        String filename = "";
//        String rootDir = ".";
//        int slash = mediaName.lastIndexOf('/');
//        if (slash > 1) {
//            filename = mediaName.substring(slash + 1);
//            rootDir = mediaName.substring(0, slash);
//        }
//        startCastServer(ip, rootDir);
//
//        String url = "http://" + ip + ":" + CastServerService.SERVER_PORT + "/" + filename;

        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        mediaMetadata.putString(MediaMetadata.KEY_TITLE, webVideo.getName());
//        mediaMetadata.addImage(new WebImage(Uri.parse(webVideo.getThumbnail_url())));
//        mediaMetadata.addImage(new WebImage(Uri.parse(webVideo.getThumbnail_url())));

        mediaMetadata.addImage(new WebImage(Uri.parse(webVideo.getLink())));
        mediaMetadata.addImage(new WebImage(Uri.parse(webVideo.getLink())));

        return new MediaInfo.Builder(webVideo.getLink())
                .setContentType(webVideo.getType())
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();
    }

}
