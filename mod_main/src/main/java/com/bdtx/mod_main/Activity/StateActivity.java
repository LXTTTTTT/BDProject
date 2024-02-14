package com.bdtx.mod_main.Activity;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_data.ViewModel.MainVM;
import com.bdtx.mod_main.Base.BaseMVVMActivity;
import com.bdtx.mod_main.Base.BaseViewBindingActivity;
import com.bdtx.mod_main.R;
import com.bdtx.mod_main.databinding.ActivityStateBinding;

import java.util.Arrays;
import java.util.Collections;

@Route(path = Constant.STATE_ACTIVITY)
public class StateActivity extends BaseMVVMActivity<ActivityStateBinding,MainVM> {

    public StateActivity() {super(true);}  // 声明使用全局 viewModel
//    public StateActivity(boolean global_model) {super(true);}
    @Override public void beforeSetLayout() {}

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        setTitle("设备状态");
    }

    @Override
    public void initData() {
        super.initData();

        // 卡号
        viewModel.getDeviceCardID().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                viewBinding.cardId.setText(s);
            }
        });
        // 频度
        viewModel.getDeviceCardFrequency().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer frequency) {
                if(frequency==-1){viewBinding.cardFrequency.setText("-");}
                else {viewBinding.cardFrequency.setText(frequency+"");}

            }
        });
        // 等级
        viewModel.getDeviceCardLevel().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer level) {
                if(level==-1){viewBinding.cardLevel.setText("-");}
                else viewBinding.cardLevel.setText(level+"级");
            }
        });
        // 电量
        viewModel.getDeviceBatteryLevel().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer batteryLevel) {
                if(batteryLevel==-1){viewBinding.batteryLevel.setText("-");}
                else viewBinding.batteryLevel.setText(batteryLevel+" %");
            }
        });
        // 信号
        viewModel.getSignal().observe(this, new Observer<int[]>() {
            @Override
            public void onChanged(int[] signals) {
                int max_single = Arrays.stream(signals).max().orElse(0);  // 拿到所有信号中的最大值
                viewBinding.maxSignal.setText(""+max_single);  // 理论最大：65 只显示 60
                // 根据信号状况决定要显示哪格信号
                if(max_single>=0 && max_single<=6){
                    init_single();
                    viewBinding.signal1.setImageResource(R.mipmap.xhqd_1);
                }else if(max_single>6 && max_single<=12){
                    init_single();
                    viewBinding.signal1.setImageResource(R.mipmap.xhqd_1);
                    viewBinding.signal2.setImageResource(R.mipmap.xhqd_2);
                }else if(max_single>12 && max_single<=18){
                    init_single();
                    viewBinding.signal1.setImageResource(R.mipmap.xhqd_1);
                    viewBinding.signal2.setImageResource(R.mipmap.xhqd_2);
                    viewBinding.signal3.setImageResource(R.mipmap.xhqd_3);
                }else if(max_single>18 && max_single<=24){
                    init_single();
                    viewBinding.signal1.setImageResource(R.mipmap.xhqd_1);
                    viewBinding.signal2.setImageResource(R.mipmap.xhqd_2);
                    viewBinding.signal3.setImageResource(R.mipmap.xhqd_3);
                    viewBinding.signal4.setImageResource(R.mipmap.xhqd_4);
                }else if(max_single>24 && max_single<=36){
                    init_single();
                    viewBinding.signal1.setImageResource(R.mipmap.xhqd_1);
                    viewBinding.signal2.setImageResource(R.mipmap.xhqd_2);
                    viewBinding.signal3.setImageResource(R.mipmap.xhqd_3);
                    viewBinding.signal4.setImageResource(R.mipmap.xhqd_4);
                    viewBinding.signal5.setImageResource(R.mipmap.xhqd_5);
                }else if(max_single>36 && max_single<=42){
                    init_single();
                    viewBinding.signal1.setImageResource(R.mipmap.xhqd_1);
                    viewBinding.signal2.setImageResource(R.mipmap.xhqd_2);
                    viewBinding.signal3.setImageResource(R.mipmap.xhqd_3);
                    viewBinding.signal4.setImageResource(R.mipmap.xhqd_4);
                    viewBinding.signal5.setImageResource(R.mipmap.xhqd_5);
                    viewBinding.signal6.setImageResource(R.mipmap.xhqd_6);
                }else if(max_single>42 && max_single<=48){
                    init_single();
                    viewBinding.signal1.setImageResource(R.mipmap.xhqd_1);
                    viewBinding.signal2.setImageResource(R.mipmap.xhqd_2);
                    viewBinding.signal3.setImageResource(R.mipmap.xhqd_3);
                    viewBinding.signal4.setImageResource(R.mipmap.xhqd_4);
                    viewBinding.signal5.setImageResource(R.mipmap.xhqd_5);
                    viewBinding.signal6.setImageResource(R.mipmap.xhqd_6);
                    viewBinding.signal7.setImageResource(R.mipmap.xhqd_7);
                }else if(max_single>48 && max_single<=54){
                    init_single();
                    viewBinding.signal1.setImageResource(R.mipmap.xhqd_1);
                    viewBinding.signal2.setImageResource(R.mipmap.xhqd_2);
                    viewBinding.signal3.setImageResource(R.mipmap.xhqd_3);
                    viewBinding.signal4.setImageResource(R.mipmap.xhqd_4);
                    viewBinding.signal5.setImageResource(R.mipmap.xhqd_5);
                    viewBinding.signal6.setImageResource(R.mipmap.xhqd_6);
                    viewBinding.signal7.setImageResource(R.mipmap.xhqd_7);
                    viewBinding.signal8.setImageResource(R.mipmap.xhqd_8);
                }else if(max_single>54 && max_single<=60){
                    init_single();
                    viewBinding.signal1.setImageResource(R.mipmap.xhqd_1);
                    viewBinding.signal2.setImageResource(R.mipmap.xhqd_2);
                    viewBinding.signal3.setImageResource(R.mipmap.xhqd_3);
                    viewBinding.signal4.setImageResource(R.mipmap.xhqd_4);
                    viewBinding.signal5.setImageResource(R.mipmap.xhqd_5);
                    viewBinding.signal6.setImageResource(R.mipmap.xhqd_6);
                    viewBinding.signal7.setImageResource(R.mipmap.xhqd_7);
                    viewBinding.signal8.setImageResource(R.mipmap.xhqd_8);
                    viewBinding.signal9.setImageResource(R.mipmap.xhqd_8);
                }else if(max_single>60){
                    init_single();
                    viewBinding.signal1.setImageResource(R.mipmap.xhqd_1);
                    viewBinding.signal2.setImageResource(R.mipmap.xhqd_2);
                    viewBinding.signal3.setImageResource(R.mipmap.xhqd_3);
                    viewBinding.signal4.setImageResource(R.mipmap.xhqd_4);
                    viewBinding.signal5.setImageResource(R.mipmap.xhqd_5);
                    viewBinding.signal6.setImageResource(R.mipmap.xhqd_6);
                    viewBinding.signal7.setImageResource(R.mipmap.xhqd_7);
                    viewBinding.signal8.setImageResource(R.mipmap.xhqd_8);
                    viewBinding.signal9.setImageResource(R.mipmap.xhqd_8);
                    viewBinding.signal10.setImageResource(R.mipmap.xhqd_8);
                }
            }
        });
    }

    public void init_single(){
        viewBinding.signal1.setImageResource(R.mipmap.xhqd_1);
        viewBinding.signal2.setImageResource(R.mipmap.xhqd_10);
        viewBinding.signal3.setImageResource(R.mipmap.xhqd_10);
        viewBinding.signal4.setImageResource(R.mipmap.xhqd_10);
        viewBinding.signal5.setImageResource(R.mipmap.xhqd_10);
        viewBinding.signal6.setImageResource(R.mipmap.xhqd_10);
        viewBinding.signal7.setImageResource(R.mipmap.xhqd_10);
        viewBinding.signal8.setImageResource(R.mipmap.xhqd_10);
        viewBinding.signal9.setImageResource(R.mipmap.xhqd_10);
        viewBinding.signal10.setImageResource(R.mipmap.xhqd_10);
    }

}
