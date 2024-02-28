package com.bdtx.mod_main.Activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_main.Base.BaseViewBindingActivity;
import com.bdtx.mod_main.databinding.ActivityAboutUsBinding;

import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Route(path = Constant.ABOUT_US_ACTIVITY)
public class AboutUsActivity extends BaseViewBindingActivity<ActivityAboutUsBinding> {

    @Override public void beforeSetLayout() {}

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        setTitle("关于");
        viewBinding.back.setOnClickListener((view)->{
            finish();
        });
    }

    @Override
    public void initData() {

    }

    @Nullable
    @Override
    public Object initDataSuspend(@NonNull Continuation<? super Unit> $completion) {
        return null;
    }
}