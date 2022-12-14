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

package com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.didl;

import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IUpnpDevice;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.view.DeviceDisplay;
import com.screen.mirroring.casttotv.tv.cast.screencast.R;

public class DIDLDevice implements IDIDLObject {

	IUpnpDevice device;

	public DIDLDevice(IUpnpDevice device)
	{
		this.device = device;
	}

	public IUpnpDevice getDevice()
	{
		return device;
	}

	@Override
	public String getDataType()
	{
		return "";
	}

	@Override
	public String getTitle() {
		return (new DeviceDisplay(device)).toString();
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getCount() {
		return "";
	}

	@Override
	public int getIcon() {
		return R.drawable.ic_images;
	}

	@Override
	public String getParentID() {
		return "";
	}

	@Override
	public String getId() {
		return "";
	}
}
