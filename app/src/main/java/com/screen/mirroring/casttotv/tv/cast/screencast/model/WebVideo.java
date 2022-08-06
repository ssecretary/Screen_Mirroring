package com.screen.mirroring.casttotv.tv.cast.screencast.model;

public class WebVideo {

    String size, type, link, name, page, website, details, width, height;
    boolean chunked = false, checked = false, expanded = false;
    String thumbnail_url;

    public WebVideo(String size, String type, String link, String name, String page, String website, String details, boolean chunked, boolean checked, boolean expanded, String width, String height,  String thumbnail_url) {
        this.size = size;
        this.type = type;
        this.link = link;
        this.name = name;
        this.page = page;
        this.website = website;
        this.details = details;
        this.chunked = chunked;
        this.checked = checked;
        this.expanded = expanded;
        this.width = width;
        this.height = height;
        this.thumbnail_url = thumbnail_url;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean isChunked() {
        return chunked;
    }

    public void setChunked(boolean chunked) {
        this.chunked = chunked;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public void setThumbnail_url(String thumbnail_url) {
        this.thumbnail_url = thumbnail_url;
    }
}
