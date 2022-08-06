package com.screen.mirroring.casttotv.tv.cast.screencast.dlna;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.Channel;

import java.util.logging.Logger;

public abstract class SetPlaybackSpeed extends ActionCallback {

    private static Logger log = Logger.getLogger(SetPlaybackSpeed.class.getName());

    public SetPlaybackSpeed(Service service, String desiredMute) {
        this(new UnsignedIntegerFourBytes(0), service, desiredMute);
    }

    public SetPlaybackSpeed(UnsignedIntegerFourBytes instanceId, Service service, String desiredMute) {
        super(new ActionInvocation(service.getAction("SetCurrentSpeed")));
        getActionInvocation().setInput("InstanceID", instanceId);
        getActionInvocation().setInput("Channel", Channel.Master.toString());
        getActionInvocation().setInput("DesiredCurrentSpeed", desiredMute);
    }

    @Override
    public void success(ActionInvocation invocation) {
        log.fine("Executed successfully");

    }
}
