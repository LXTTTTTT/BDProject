package com.bdtx.mod_main.Adapter;

import android.hardware.usb.UsbAccessory;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bdtx.mod_main.databinding.AdapterUsbItemBinding;

public class USBAccessoryListAdapter extends BaseRecyclerViewAdapter<UsbAccessory, AdapterUsbItemBinding> {

    @NonNull
    @Override
    public AdapterUsbItemBinding getViewBinding(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent, int viewType) {
        return AdapterUsbItemBinding.inflate(layoutInflater,parent,false);
    }

    @Override
    protected void onBindDefViewHolder(@NonNull BaseBindViewHolder<AdapterUsbItemBinding> holder, @Nullable UsbAccessory item, int position) {
        if(item==null){return;}
        AdapterUsbItemBinding binding = holder.getBinding();
        if(binding!=null){
            try{
                binding.deviceName.setText(item.getSerial());
                binding.deviceProductName.setText(item.getDescription());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
