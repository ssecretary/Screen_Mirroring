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

package com.screen.mirroring.casttotv.tv.cast.screencast.dlna.controller;

import android.content.Context;

import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.RendererState;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.ARendererState;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IContentDirectoryCommand;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IRendererCommand;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IRendererState;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IFactory;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity.CastDeviceListActivity;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ControlPoint;

public class Factory implements IFactory {

    @Override
    public IContentDirectoryCommand createContentDirectoryCommand() {
        AndroidUpnpService aus = ((ServiceListener) CastDeviceListActivity.upnpServiceController.getServiceListener()).getUpnpService();
        ControlPoint cp = null;
        if (aus != null)
            cp = aus.getControlPoint();
        if (cp != null)
            return new ContentDirectoryCommand(cp);

        return null;
    }

    @Override
    public IRendererCommand createRendererCommand(IRendererState rs) {
        AndroidUpnpService aus = ((ServiceListener) CastDeviceListActivity.upnpServiceController.getServiceListener()).getUpnpService();
        ControlPoint cp = null;
        if (aus != null)
            cp = aus.getControlPoint();
        if (cp != null)
            return new RendererCommand(cp, (RendererState) rs);

        return null;
    }

    @Override
    public IUpnpServiceController createUpnpServiceController(Context ctx) {
        return new ServiceController(ctx);
    }

    @Override
    public ARendererState createRendererState() {
        return new RendererState();
    }
}
