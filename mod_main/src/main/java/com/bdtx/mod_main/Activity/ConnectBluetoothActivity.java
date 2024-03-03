package com.bdtx.mod_main.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_main.Adapter.BluetoothListAdapter;
import com.bdtx.mod_main.Base.BaseViewBindingActivity;
import com.bdtx.mod_main.databinding.ActivityConnectBluetoothBinding;
import com.bdtx.mod_util.Utils.Connection.BaseConnector;
import com.bdtx.mod_util.Utils.Transfer.BluetoothTransferUtils;
import com.bdtx.mod_util.Utils.GlobalControlUtils;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function2;

@Route(path = Constant.CONNECT_BLUETOOTH_ACTIVITY)
public class ConnectBluetoothActivity extends BaseViewBindingActivity<ActivityConnectBluetoothBinding> {

    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private BluetoothListAdapter bluetoothListAdapter;
    public static final int MODE_BLE = 0;
    public static final int MODE_CLSB = 1;
    public static final String CONNECTION_MODE = "connection_mode";
    private int connection_mode = MODE_BLE;

    @Override public void beforeSetLayout() {}
    @Nullable @Override public Object initDataSuspend(@NonNull Continuation<? super Unit> $completion) {return null;}

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        connection_mode = getIntent().getIntExtra(CONNECTION_MODE,MODE_BLE);
        init_bluetooth_list();
        if(connection_mode==MODE_BLE){
            BluetoothTransferUtils.getInstance().setOnBluetoothWork(new BluetoothTransferUtils.onBluetoothWork() {
                @Override public void onScanningResult(List<BluetoothDevice> devices, BluetoothDevice new_device) {

                }
                @Override public void onConnectSucceed() {
                    GlobalControlUtils.INSTANCE.hideLoadingDialog();
                    GlobalControlUtils.INSTANCE.showToast("连接成功",0);
                    finish();
                }
                @Override public void onConnectError() {
                    GlobalControlUtils.INSTANCE.hideLoadingDialog();
                    GlobalControlUtils.INSTANCE.showToast("连接错误",0);
                }
                @Override public void onDisconnect() {
                    GlobalControlUtils.INSTANCE.hideLoadingDialog();
                    GlobalControlUtils.INSTANCE.showToast("断开连接",0);
                }
                @Override public void sendDataCallback(int var1) {}
                @Override public void onReceiveData(String data_hex) {}
            });
        }else{
            registerBroadcast();
        }

    }

    @Override
    public void initData() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()){
            if(connection_mode==MODE_BLE){
                // 扫描低功耗蓝牙设备
                bluetoothAdapter.startLeScan(BLECallback);
                loge("扫描低功耗蓝牙设备");
            }else{
                // 扫描所有蓝牙设备
                bluetoothAdapter.startDiscovery();
                loge("扫描所有蓝牙设备");
            }
        } else {
            Toast.makeText(this, "请打开蓝牙", Toast.LENGTH_SHORT).show();
//            Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            in.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200);
//            startActivityForResult(in,1);
        }
    }

    // 蓝牙扫描回调
    BluetoothAdapter.LeScanCallback BLECallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int i, byte[] bytes) {
//            loge("搜索到低功耗设备");
            if (device.getName() == null || device.getName().equals("")) return;
            if (devices.contains(device)){
                return;
            }
            devices.add(device);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bluetoothListAdapter.setData(devices);
                }
            });
        }
    };

    private void init_bluetooth_list(){
        bluetoothListAdapter = new BluetoothListAdapter();
        // 列表点击
        bluetoothListAdapter.setOnItemClickListener(new Function2<View, Integer, Unit>() {
            @Override
            public Unit invoke(View view, Integer integer) {
//                BluetoothTransferUtils.getInstance().connectDevice(bluetoothListAdapter.getItem(integer));
                BaseConnector.Companion.getConnector().connect(bluetoothListAdapter.getItem(integer));
                return null;
            }
        });
        getViewBinding().bluetoothList.setLayoutManager(new LinearLayoutManager(my_context, LinearLayoutManager.VERTICAL, false));
        getViewBinding().bluetoothList.setAdapter(bluetoothListAdapter);
    }

    public void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.device.action.FOUND");
        filter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
        filter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
        filter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        this.registerReceiver(receiver, filter);
        loge("广播注册成功");
    }

    // 蓝牙连接监听广播
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.device.action.FOUND".equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                if (device.getName() == null) {return;}
                if (!devices.contains(device)) {
                    devices.add(device);
                    bluetoothListAdapter.setData(devices);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        if(connection_mode==MODE_BLE){
            bluetoothAdapter.stopLeScan(BLECallback);
        }else{
            if(bluetoothAdapter.isDiscovering()){bluetoothAdapter.cancelDiscovery();}
            this.unregisterReceiver(receiver);
        }
        super.onDestroy();
    }
}
