package com.bdtx.mod_main.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bdtx.mod_main.databinding.AdapterSerialPortItemBinding;
import com.sum.framework.adapter.BaseBindViewHolder;
import com.sum.framework.adapter.BaseRecyclerViewAdapter;

public class SerialPortListAdapter extends BaseRecyclerViewAdapter<String, AdapterSerialPortItemBinding> {

    @NonNull
    @Override
    public AdapterSerialPortItemBinding getViewBinding(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent, int viewType) {
        return AdapterSerialPortItemBinding.inflate(layoutInflater,parent,false);
    }

    @Override
    protected void onBindDefViewHolder(@NonNull BaseBindViewHolder<AdapterSerialPortItemBinding> holder, @Nullable String item, int position) {
        if(item==null){return;}
        AdapterSerialPortItemBinding binding = holder.getBinding();
        if(binding!=null){
            try{
                binding.path.setText(item);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
