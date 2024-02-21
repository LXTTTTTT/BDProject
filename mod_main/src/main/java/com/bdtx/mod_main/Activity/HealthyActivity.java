package com.bdtx.mod_main.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_data.Global.Variable;
import com.bdtx.mod_data.ViewModel.MainVM;
import com.bdtx.mod_main.Base.BaseMVVMActivity;
import com.bdtx.mod_main.Base.BaseViewBindingActivity;
import com.bdtx.mod_main.R;
import com.bdtx.mod_main.databinding.ActivityHealthyBinding;
import com.bdtx.mod_main.databinding.ActivityStateBinding;
import com.bdtx.mod_util.Utils.SendMessageUtils;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

@Route(path = Constant.HEALTHY_ACTIVITY)
public class HealthyActivity extends BaseMVVMActivity<ActivityHealthyBinding,MainVM> {

    private String TAG = "HealthyActivity";
    public HealthyActivity(){super(true);}
    @Override public void beforeSetLayout() {}

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        init_control();
        getHeartRateAndOxygen();
        getStep();
    }

    @Override
    public void initData() {
        super.initData();
        init_view_model();
    }

    private void init_control(){

        setTitle("健康数据");
        viewBinding.send.setOnClickListener((View view) ->{
            String heartRate = viewBinding.heartRate.getText().toString();
            String bloodOxygen = viewBinding.bloodOxygen.getText().toString();
            String step = viewBinding.step.getText().toString();
            String content = "心率:"+heartRate+"bpm"+"\n 血氧:"+bloodOxygen+"\n 步数:"+step;
            SendMessageUtils.INSTANCE.send_text(Constant.PLATFORM_IDENTIFIER,content,true);
        });

    }

    private void init_view_model(){

        viewModel.isConnectDevice().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnect) {
                if(isConnect){
                    viewBinding.send.setClickable(true);
                    viewBinding.send.setBackgroundResource(R.drawable.half_oval_up_blue_1_ripple);
                    viewBinding.sendImage.setVisibility(View.VISIBLE);
                    viewBinding.countDown.setVisibility(View.GONE);
                }
                else {
                    viewBinding.send.setClickable(false);
                    viewBinding.send.setBackgroundResource(R.drawable.half_oval_up_gray_3_ripple);
                    viewBinding.sendImage.setVisibility(View.GONE);
                    viewBinding.countDown.setVisibility(View.VISIBLE);
                    viewBinding.countDown.setText("未连接");
                }
            }
        });

        viewModel.getWaitTime().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                if(!viewModel.isConnectDevice().getValue()){return;}
                if(count>0){
                    viewBinding.send.setClickable(false);
                    viewBinding.send.setBackgroundResource(R.drawable.half_oval_up_gray_3_ripple);
                    viewBinding.sendImage.setVisibility(View.GONE);
                    viewBinding.countDown.setVisibility(View.VISIBLE);
                    viewBinding.countDown.setText(""+count);
                }
                else {
                    viewBinding.send.setClickable(true);
                    viewBinding.send.setBackgroundResource(R.drawable.half_oval_up_blue_1_ripple);
                    viewBinding.sendImage.setVisibility(View.VISIBLE);
                    viewBinding.countDown.setVisibility(View.GONE);
                }
            }
        });
    }


    // 无法同时开启心率和血氧传感器，循环获取数据，先开启心率传感器获取到数据之后关闭心率传感器再开启血氧传感器
    private void getHeartRateAndOxygen(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.ly.RECEIVER_OPEN_HEARTRATE_MEASURE");
        filter.addAction("com.ly.RECEIVER_OPEN_BLOODOXYGEN_MEASURE");
        filter.addAction("com.ly.ACTION_SEND_JSON_TO_APP");
        registerReceiver(HealthyReceiver, filter);

        // 开启心率
        Intent in = new Intent("com.ly.ACTION_OPEN_HEARTRATE_MEASURE");
        in.setPackage("com.android.systemui");
        sendBroadcast(in);

        // 开启血氧
//        Intent in2 = new Intent("com.ly.ACTION_OPEN_BLOODOXYGEN_MEASURE");
//        in2.setPackage("com.android.systemui");
//        sendBroadcast(in2);
    }

    private Timer stepTimer = null;
    // 这个系统以这种方式获取步数
    private void getStep(){
        stopStep();
        stepTimer = new Timer();
        stepTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent in = new Intent("com.ly.ACTION_APP_GET_STEP");
                in.setComponent(new ComponentName("com.wear.launcher", "com.wear.launcher.receiver.HealthReceiver"));
                sendBroadcast(in);
            }
        },0,5000);  // 5秒一次获取步数步数数据
    }

    private void stopStep(){
        if(stepTimer!=null){
            stepTimer.cancel();
            stepTimer=null;
        }
    }

    private void stopSensor(){
        // 关闭心率血氧传感器
        Intent in = new Intent("com.ly.ACTION_CLOSE_HEARTRATE_MEASURE");
        in.setPackage("com.android.systemui");
        sendBroadcast(in);
        Intent in2 = new Intent("com.ly.ACTION_CLOSE_BLOODOXYGEN_MEASURE");
        in2.setPackage("com.android.systemui");
        sendBroadcast(in2);
        // 停止步数
        stopStep();
        // 注销广播
        try {unregisterReceiver(HealthyReceiver);}catch (Exception e){e.printStackTrace();}
    }

    private BroadcastReceiver HealthyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String identify = intent.getAction();
            Log.e(TAG, "收到广播: "+identify );
            // 心率
            if(identify.equals("com.ly.RECEIVER_OPEN_HEARTRATE_MEASURE")){
                String value  = intent.getStringExtra("heart_rate");
                Log.e(TAG, "收到心率："+value);
                viewBinding.heartRate.setText(value);
                // 关闭心率传感器，开启血氧传感器
                Intent in = new Intent("com.ly.ACTION_CLOSE_HEARTRATE_MEASURE");
                in.setPackage("com.android.systemui");
                sendBroadcast(in);
                Intent in2 = new Intent("com.ly.ACTION_OPEN_BLOODOXYGEN_MEASURE");
                in2.setPackage("com.android.systemui");
                sendBroadcast(in2);
            }
            // 血氧
            else if(identify.equals("com.ly.RECEIVER_OPEN_BLOODOXYGEN_MEASURE")){
                String value  = intent.getStringExtra("blood_oxygen");
                Log.e(TAG, "收到血氧："+value);
                viewBinding.bloodOxygen.setText(value);
                // 关闭血氧传感器，开启心率传感器
                Intent in = new Intent("com.ly.ACTION_CLOSE_BLOODOXYGEN_MEASURE");
                in.setPackage("com.android.systemui");
                sendBroadcast(in);
                Intent in2 = new Intent("com.ly.ACTION_OPEN_HEARTRATE_MEASURE");
                in2.setPackage("com.android.systemui");
                sendBroadcast(in2);
            }
            // 步数
            else if(identify.equals("com.ly.ACTION_SEND_JSON_TO_APP")){
                int step = intent.getIntExtra("step_number" ,0);
                Log.i(TAG, "收到步数："+step);
                viewBinding.step.setText(""+step);
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        stopSensor();
    }
}
