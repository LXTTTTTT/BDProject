package com.bdtx.mod_main.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bdtx.mod_main.databinding.AdapterUsbItemBinding;
import com.bdtx.mod_util.Utils.Transfer.USB.USBSerial.driver.UsbSerialDriver;

public class USBHostListAdapter extends BaseRecyclerViewAdapter<UsbSerialDriver, AdapterUsbItemBinding> {

    @NonNull
    @Override
    public AdapterUsbItemBinding getViewBinding(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent, int viewType) {
        return AdapterUsbItemBinding.inflate(layoutInflater,parent,false);
    }

    @Override
    protected void onBindDefViewHolder(@NonNull BaseBindViewHolder<AdapterUsbItemBinding> holder, @Nullable UsbSerialDriver item, int position) {
        if(item==null){return;}
        AdapterUsbItemBinding binding = holder.getBinding();
        if(binding!=null){
            try{
                binding.deviceName.setText(item.getDevice().getDeviceName());
                binding.deviceProductName.setText(item.getDevice().getProductName());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
