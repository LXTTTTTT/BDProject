package com.bdtx.mod_main.Activity;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_main.Base.BaseViewBindingActivity;
import com.bdtx.mod_main.databinding.ActivityAboutUsBinding;
import com.bdtx.mod_main.databinding.ActivityPlatformSettingBinding;

@Route(path = Constant.ABOUT_US_ACTIVITY)
public class AboutUsActivity extends BaseViewBindingActivity<ActivityAboutUsBinding> {

    @Override public void beforeSetLayout() {}

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        setTitle("关于");
    }

    @Override
    public void initData() {

    }
}