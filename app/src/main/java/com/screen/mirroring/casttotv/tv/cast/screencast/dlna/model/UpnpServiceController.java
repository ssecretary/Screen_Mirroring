/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien@chabot.fr>
 * <p>
 * This file is part of DroidUPNP.
 * <p>
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model;

import android.app.Activity;
import android.util.Log;

import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.controller.IUpnpServiceController;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IUpnpDevice;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.RendererDiscovery;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.ContentDirectoryDiscovery;

import java.util.Observer;

public abstract class UpnpServiceController implements IUpnpServiceController {

    private static final String TAG = "UpnpServiceController";

    protected IUpnpDevice renderer;
    protected IUpnpDevice contentDirectory;

    protected CObservable rendererObservable;
    protected CObservable contentDirectoryObservable;

    private final ContentDirectoryDiscovery contentDirectoryDiscovery;
    private final RendererDiscovery rendererDiscovery;

    @Override
    public ContentDirectoryDiscovery getContentDirectoryDiscovery() {
        return contentDirectoryDiscovery;
    }

    @Override
    public RendererDiscovery getRendererDiscovery() {
        return rendererDiscovery;
    }

    protected UpnpServiceController() {
        rendererObservable = new CObservable();
        contentDirectoryObservable = new CObservable();

        contentDirectoryDiscovery = new ContentDirectoryDiscovery(getServiceListener());
        rendererDiscovery = new RendererDiscovery(getServiceListener());
    }

    @Override
    public void setSelectedRenderer(IUpnpDevice renderer) {
        setSelectedRenderer(renderer, false);
    }

    @Override
    public void setSelectedRenderer(IUpnpDevice renderer, boolean force) {
        // Skip if no change and no force
        if (!force && renderer != null && this.renderer != null && this.renderer.equals(renderer))
            return;

        this.renderer = renderer;
        rendererObservable.notifyAllObservers();
    }

    @Override
    public void setSelectedContentDirectory(IUpnpDevice contentDirectory) {
        setSelectedContentDirectory(contentDirectory, false);
    }

    @Override
    public void setSelectedContentDirectory(IUpnpDevice contentDirectory, boolean force) {
        // Skip if no change and no force
        if (!force && contentDirectory != null && this.contentDirectory != null
                && this.contentDirectory.equals(contentDirectory))
            return;

        this.contentDirectory = contentDirectory;
        contentDirectoryObservable.notifyAllObservers();
    }

    @Override
    public IUpnpDevice getSelectedRenderer() {
        return renderer;
    }

    @Override
    public IUpnpDevice getSelectedContentDirectory() {
        return contentDirectory;
    }

    @Override
    public void addSelectedRendererObserver(Observer o) {
        Log.i(TAG, "New SelectedRendererObserver");
        rendererObservable.addObserver(o);
    }

    @Override
    public void delSelectedRendererObserver(Observer o) {
        rendererObservable.deleteObserver(o);
    }

    @Override
    public void addSelectedContentDirectoryObserver(Observer o) {
        contentDirectoryObservable.addObserver(o);
    }

    @Override
    public void delSelectedContentDirectoryObserver(Observer o) {
        contentDirectoryObservable.deleteObserver(o);
    }

    // Pause the service
    @Override
    public void pause() {
        rendererDiscovery.pause(getServiceListener());
        contentDirectoryDiscovery.pause(getServiceListener());
    }

    // Resume the service
    @Override
    public void resume(Activity activity) {
        rendererDiscovery.resume(getServiceListener());
        contentDirectoryDiscovery.resume(getServiceListener());
    }

}