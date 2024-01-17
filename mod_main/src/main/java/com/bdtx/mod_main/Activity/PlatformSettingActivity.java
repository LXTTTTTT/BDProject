package com.bdtx.mod_main.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_data.Global.Variable;
import com.bdtx.mod_main.Base.BaseViewBindingActivity;
import com.bdtx.mod_main.databinding.ActivityPlatformSettingBinding;
import com.bdtx.mod_util.Util.GlobalControlUtil;
import com.tencent.mmkv.MMKV;

@Route(path = Constant.PLATFORM_SETTING_ACTIVITY)
public class PlatformSettingActivity extends BaseViewBindingActivity<ActivityPlatformSettingBinding> {

    int change_platform = Constant.default_platform_number;  // 要改的平台号码

    @Override public void beforeSetLayout() {}

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
//        change_platform = Variable.systemNumber;
        change_platform = MMKV.defaultMMKV().decodeInt(Constant.SYSTEM_NUMBER,Constant.default_platform_number);
        Log.e("当前平台号码是：", ""+change_platform);
        // 初始化平台通道设置
        if(change_platform == Constant.default_platform_number){
            viewBinding.autoChecked.setVisibility(View.VISIBLE);
            viewBinding.checked1.setVisibility(View.INVISIBLE);
            viewBinding.editText.setText("");
        }else {
            viewBinding.checked1.setVisibility(View.VISIBLE);
            viewBinding.autoChecked.setVisibility(View.INVISIBLE);
            viewBinding.editText.setText(""+change_platform);
        }

        // 平台默认号码
        viewBinding.autoGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewBinding.autoChecked.setVisibility(View.VISIBLE);
                viewBinding.checked1.setVisibility(View.INVISIBLE);
                viewBinding.editText.setText("");
                change_platform = Constant.default_platform_number;
            }
        });

        viewBinding.sendGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!viewBinding.editText.getText().toString().equals("")){
                    change_platform = Integer.parseInt(viewBinding.editText.getText().toString().trim());
                }

                if(viewBinding.editText.getText().toString().length() >= 6){
//                    Variable.setSystemNumber(change_platform);
                    MMKV.defaultMMKV().encode(Constant.SYSTEM_NUMBER,change_platform);
                    GlobalControlUtil.INSTANCE.showToast("保存成功",0);
                    finish();
                }else if(viewBinding.editText.getText().toString().equals("")){
//                    Variable.setSystemNumber(Constant.default_platform_number);
                    MMKV.defaultMMKV().encode(Constant.SYSTEM_NUMBER,Constant.default_platform_number);
                    GlobalControlUtil.INSTANCE.showToast("保存成功",0);
                    finish();
                }else {
                    GlobalControlUtil.INSTANCE.showToast("平台号码至少为6位",0);
                }
            }
        });
    }

    @Override
    public void initData() {

    }
}