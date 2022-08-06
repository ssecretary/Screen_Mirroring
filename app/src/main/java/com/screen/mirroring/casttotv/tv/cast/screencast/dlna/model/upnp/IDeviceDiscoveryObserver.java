package com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp;

public interface IDeviceDiscoveryObserver {

	public void addedDevice(IUpnpDevice device);

	public void removedDevice(IUpnpDevice device);
}
