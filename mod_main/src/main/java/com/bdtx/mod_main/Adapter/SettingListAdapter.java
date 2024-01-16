package com.bdtx.mod_main.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bdtx.mod_main.databinding.AdapterSettingItemBinding;
import com.sum.framework.adapter.BaseBindViewHolder;
import com.sum.framework.adapter.BaseRecyclerViewAdapter;

import java.util.List;

public class SettingListAdapter extends BaseRecyclerViewAdapter<List<Object>, AdapterSettingItemBinding> {

    @NonNull
    @Override
    public AdapterSettingItemBinding getViewBinding(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent, int viewType) {
        return AdapterSettingItemBinding.inflate(layoutInflater,parent,false);
    }

    @Override
    protected void onBindDefViewHolder(@NonNull BaseBindViewHolder<AdapterSettingItemBinding> holder, @Nullable List<Object> item, int position) {
        if(item==null) return;
        holder.getBinding().image.setImageResource((Integer) (item.get(0)));
        holder.getBinding().text.setText((String) (item.get(1)));
    }


}
