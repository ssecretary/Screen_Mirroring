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

import android.util.Log;

import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.CDevice;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.didl.ClingAudioItem;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.didl.ClingDIDLContainer;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.didl.ClingDIDLParentContainer;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.didl.ClingImageItem;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IContentDirectoryCommand;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.didl.ClingDIDLItem;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.didl.ClingVideoItem;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.view.DIDLObjectDisplay;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.activity.CastDeviceListActivity;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.VideoItem;

import java.util.ArrayList;

@SuppressWarnings("rawtypes")
public class ContentDirectoryCommand implements IContentDirectoryCommand {
    private static final String TAG = "ContentDirectoryCommand";

    private final ControlPoint controlPoint;

    public ContentDirectoryCommand(ControlPoint controlPoint) {
        this.controlPoint = controlPoint;
    }

    @SuppressWarnings("unused")
    private Service getMediaReceiverRegistarService() {
        if (CastDeviceListActivity.upnpServiceController.getSelectedContentDirectory() == null)
            return null;

        return ((CDevice) CastDeviceListActivity.upnpServiceController.getSelectedContentDirectory()).getDevice().findService(
                new UDAServiceType("X_MS_MediaReceiverRegistar"));
    }

    private Service getContentDirectoryService() {
        if (CastDeviceListActivity.upnpServiceController.getSelectedContentDirectory() == null)
            return null;

        return ((CDevice) CastDeviceListActivity.upnpServiceController.getSelectedContentDirectory()).getDevice().findService(
                new UDAServiceType("ContentDirectory"));
    }

    private ArrayList<DIDLObjectDisplay> buildContentList(String parent, DIDLContent didl) {
        ArrayList<DIDLObjectDisplay> list = new ArrayList<DIDLObjectDisplay>();

        if (parent != null)
            list.add(new DIDLObjectDisplay(new ClingDIDLParentContainer(parent)));

        for (Container item : didl.getContainers()) {
            list.add(new DIDLObjectDisplay(new ClingDIDLContainer(item)));
            Log.v(TAG, "Add container : " + item.getTitle());
        }

        for (Item item : didl.getItems()) {
            ClingDIDLItem clingItem = null;
            if (item instanceof VideoItem)
                clingItem = new ClingVideoItem((VideoItem) item);
            else if (item instanceof AudioItem)
                clingItem = new ClingAudioItem((AudioItem) item);
            else if (item instanceof ImageItem)
                clingItem = new ClingImageItem((ImageItem) item);
            else
                clingItem = new ClingDIDLItem(item);

            list.add(new DIDLObjectDisplay(clingItem));
            Log.v(TAG, "Add item : " + item.getTitle());

            for (DIDLObject.Property p : item.getProperties())
                Log.v(TAG, p.getDescriptorName() + " " + p.toString());
        }

        return list;
    }

    public boolean isSearchAvailable() {
        if (getContentDirectoryService() == null)
            return false;

        return false;
    }
}
