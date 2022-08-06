package com.screen.mirroring.casttotv.tv.cast.screencast.model;

import java.util.List;

public class MediaFolderData {
    String folderName;
    String folderPath;
    List<MediaFileModel> mediaFileList;
    double length;
    long lastModified;

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public List<MediaFileModel> getMediaFileList() {
        return mediaFileList;
    }

    public void setMediaFileList(List<MediaFileModel> mediaFileList) {
        this.mediaFileList = mediaFileList;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}
