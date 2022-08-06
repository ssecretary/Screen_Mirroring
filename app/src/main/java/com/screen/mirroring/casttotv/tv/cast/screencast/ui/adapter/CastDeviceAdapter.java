package com.screen.mirroring.casttotv.tv.cast.screencast.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.mediarouter.media.MediaRouter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.cast.CastDevice;
import com.screen.mirroring.casttotv.tv.cast.screencast.dlna.model.upnp.IUpnpDevice;
import com.screen.mirroring.casttotv.tv.cast.screencast.ui.interfaces.OnItemClickListner;
import com.screen.mirroring.casttotv.tv.cast.screencast.databinding.ListItemCastDeviceBinding;

import java.util.ArrayList;
import java.util.List;

import static com.screen.mirroring.casttotv.tv.cast.screencast.Constant.SELECTED_DEVICE_POSITION;

public class CastDeviceAdapter extends RecyclerView.Adapter<CastDeviceAdapter.CastDeviceViewHolder> {

    Context mContext;
    List<Object> castDeviceList;
    OnItemClickListner onItemClickListner;

    public CastDeviceAdapter(Context mContext) {
        this.mContext = mContext;
        castDeviceList = new ArrayList<>();
    }

    public void setFolderDataList(List<Object> castDeviceList) {
        this.castDeviceList.clear();
        this.castDeviceList = castDeviceList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListner(OnItemClickListner onItemClickListner) {
        this.onItemClickListner = onItemClickListner;
    }

    @NonNull
    @Override
    public CastDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CastDeviceViewHolder(ListItemCastDeviceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CastDeviceViewHolder holder, int position) {
        Object object = castDeviceList.get(position);

        if (object instanceof MediaRouter.RouteInfo) {
            MediaRouter.RouteInfo castDevice = (MediaRouter.RouteInfo) object;
            CastDevice device = CastDevice.getFromBundle(castDevice.getExtras());
            holder.binding.deviceName.setText(castDevice.getName() + " (" + device.getInetAddress().getHostAddress() + ")");
            holder.binding.subTitle.setText("Chromecast");
            if (SELECTED_DEVICE_POSITION instanceof MediaRouter.RouteInfo) {
                MediaRouter.RouteInfo castDevice1 = (MediaRouter.RouteInfo) SELECTED_DEVICE_POSITION;
                CastDevice device1 = CastDevice.getFromBundle(castDevice1.getExtras());
                if (device.getFriendlyName().equals(device1.getFriendlyName()))
                    holder.binding.selectedDevice.setVisibility(View.VISIBLE);
            } else {
                holder.binding.selectedDevice.setVisibility(View.GONE);
            }
        } else if (object instanceof IUpnpDevice) {
            IUpnpDevice upnpDevice = (IUpnpDevice) object;
            if (SELECTED_DEVICE_POSITION instanceof IUpnpDevice) {
                IUpnpDevice upnpDevice1 = (IUpnpDevice) SELECTED_DEVICE_POSITION;
                if (upnpDevice.getFriendlyName().equals(upnpDevice1.getFriendlyName()))
                    holder.binding.selectedDevice.setVisibility(View.VISIBLE);
            } else {
                holder.binding.selectedDevice.setVisibility(View.GONE);
            }
            holder.binding.deviceName.setText(upnpDevice.getFriendlyName());
            holder.binding.subTitle.setText("DLNA");
        }
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return castDeviceList.size();
    }

    public class CastDeviceViewHolder extends RecyclerView.ViewHolder {

        ListItemCastDeviceBinding binding;

        public CastDeviceViewHolder(ListItemCastDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                if (onItemClickListner != null) {
                    Object object = castDeviceList.get(getAdapterPosition());
                    if (SELECTED_DEVICE_POSITION instanceof IUpnpDevice) {
                        IUpnpDevice upnpDevice = (IUpnpDevice) object;
                        IUpnpDevice upnpDevice1 = (IUpnpDevice) SELECTED_DEVICE_POSITION;
                        if (upnpDevice.getFriendlyName().equals(upnpDevice1.getFriendlyName()))
                            return;
                    } else if (SELECTED_DEVICE_POSITION instanceof MediaRouter.RouteInfo) {
                        MediaRouter.RouteInfo castDevice = (MediaRouter.RouteInfo) object;
                        CastDevice device = CastDevice.getFromBundle(castDevice.getExtras());

                        MediaRouter.RouteInfo castDevice1 = (MediaRouter.RouteInfo) SELECTED_DEVICE_POSITION;
                        CastDevice device1 = CastDevice.getFromBundle(castDevice1.getExtras());
                        if (device.getFriendlyName().equals(device1.getFriendlyName()))
                            return;
                    }
                    SELECTED_DEVICE_POSITION = castDeviceList.get(getAdapterPosition());
                    onItemClickListner.onItemClick(getAdapterPosition());
                    notifyDataSetChanged();
                }
            });
        }
    }
}
