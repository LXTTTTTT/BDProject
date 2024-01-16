package com.bdtx.mod_main.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_main.Adapter.BluetoothListAdapter;
import com.bdtx.mod_main.Base.BaseViewBindingActivity;
import com.bdtx.mod_main.databinding.ActivityConnectBluetoothBinding;
import com.bdtx.mod_util.Util.BluetoothTransferUtil;
import com.bdtx.mod_util.Util.GlobalControlUtil;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

@Route(path = Constant.CONNECT_BLUETOOTH_ACTIVITY)
public class ConnectBluetoothActivity extends BaseViewBindingActivity<ActivityConnectBluetoothBinding> {

    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private BluetoothListAdapter bluetoothListAdapter;

    @Override public void beforeSetLayout() {}

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        init_bluetooth_list();
        BluetoothTransferUtil.getInstance().setOnBluetoothWork(new BluetoothTransferUtil.onBluetoothWork() {
            @Override public void onScanningResult(List<BluetoothDevice> devices, BluetoothDevice new_device) {}
            @Override public void onConnectSucceed() {
                GlobalControlUtil.INSTANCE.hideLoadingDialog();
                GlobalControlUtil.INSTANCE.showToast("连接成功",0);
                finish();
            }
            @Override public void onConnectError() {
                GlobalControlUtil.INSTANCE.hideLoadingDialog();
                GlobalControlUtil.INSTANCE.showToast("连接错误",0);
            }
            @Override public void onDisconnect() {
                GlobalControlUtil.INSTANCE.hideLoadingDialog();
                GlobalControlUtil.INSTANCE.showToast("断开连接",0);
            }
            @Override public void sendDataCallback(int var1) {}
            @Override public void onReceiveData(String data_hex) {}
        });
    }

    @Override
    public void initData() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()){
            bluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int i, byte[] bytes) {
//                    loge("搜索到设备");
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
            });
        }else {
            Toast.makeText(this, "请打开蓝牙", Toast.LENGTH_SHORT).show();
            Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            in.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200);
            startActivityForResult(in,1);
        }
    }

    private void init_bluetooth_list(){
        bluetoothListAdapter = new BluetoothListAdapter();
        // 列表点击
        bluetoothListAdapter.setOnItemClickListener(new Function2<View, Integer, Unit>() {
            @Override
            public Unit invoke(View view, Integer integer) {
                BluetoothTransferUtil.getInstance().connectDevice(bluetoothListAdapter.getItem(integer));
                GlobalControlUtil.INSTANCE.showLoadingDialog("正在连接");
//                finish();
                return null;
            }
        });
        getViewBinding().bluetoothList.setLayoutManager(new LinearLayoutManager(my_context, LinearLayoutManager.VERTICAL, false));
        getViewBinding().bluetoothList.setAdapter(bluetoothListAdapter);
    }

}
