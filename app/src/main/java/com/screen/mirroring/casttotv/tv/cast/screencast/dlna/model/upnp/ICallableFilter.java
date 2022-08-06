package com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp;

import java.util.concurrent.Callable;

public interface ICallableFilter extends Callable<Boolean> {
	public void setDevice(IUpnpDevice device);
}
