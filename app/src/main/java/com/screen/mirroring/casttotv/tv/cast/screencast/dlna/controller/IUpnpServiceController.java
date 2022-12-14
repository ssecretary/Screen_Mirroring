/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien@chabot.fr>
 * 
 * This file is part of DroidUPNP.
 * 
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.screen.mirroring.casttotv.tv.cast.screencast.dlna.controller;

import android.app.Activity;

import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.ContentDirectoryDiscovery;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IServiceListener;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IUpnpDevice;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.RendererDiscovery;

import org.fourthline.cling.model.meta.LocalDevice;

import java.util.Observer;

public interface IUpnpServiceController {

	public void setSelectedRenderer(IUpnpDevice renderer);

	public void setSelectedRenderer(IUpnpDevice renderer, boolean force);

	public void setSelectedContentDirectory(IUpnpDevice contentDirectory);

	public void setSelectedContentDirectory(IUpnpDevice contentDirectory, boolean force);

	public IUpnpDevice getSelectedRenderer();

	public IUpnpDevice getSelectedContentDirectory();

	public void addSelectedRendererObserver(Observer o);

	public void delSelectedRendererObserver(Observer o);

	public void addSelectedContentDirectoryObserver(Observer o);

	public void delSelectedContentDirectoryObserver(Observer o);

	public IServiceListener getServiceListener();

	public ContentDirectoryDiscovery getContentDirectoryDiscovery();

	public RendererDiscovery getRendererDiscovery();

	// Pause the service
	public void pause();

	// Resume the service
	public void resume(Activity activity);

	public void addDevice(LocalDevice localDevice);
	public void removeDevice(LocalDevice localDevice);

}
