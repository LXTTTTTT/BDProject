package com.bdtx.mod_util.Utils.Transfer.USB;

import static java.lang.Thread.sleep;

import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;


import com.bdtx.mod_data.ViewModel.MainVM;
import com.bdtx.mod_util.Utils.ApplicationUtils;
import com.bdtx.mod_util.Utils.DataUtils;
import com.bdtx.mod_util.Utils.DispatcherExecutor;
import com.bdtx.mod_util.Utils.GlobalControlUtils;
import com.bdtx.mod_util.Utils.Protocol.BDProtocolUtils;
import com.bdtx.mod_util.Utils.Transfer.USB.USBSerial.driver.UsbSerialDriver;
import com.bdtx.mod_util.Utils.Transfer.USB.USBSerial.driver.UsbSerialPort;
import com.bdtx.mod_util.Utils.Transfer.USB.USBSerial.driver.UsbSerialProber;
import com.bdtx.mod_util.Utils.Transfer.USB.USBSerial.util.SerialInputOutputManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// usb 数据传输工具
public class USBHostTransferUtils {

    private String TAG = "USBHostTransferUtil";
    private Application APP = ApplicationUtils.INSTANCE.getApplication();
    private UsbManager manager = (UsbManager) APP.getSystemService(Context.USB_SERVICE);  // usb管理器

    private BroadcastReceiver usbReceiver;  // 广播监听：判断usb设备授权操作
    private static final String INTENT_ACTION_GRANT_USB = "com.bdtx.main.INTENT_ACTION_GRANT_USB";  // usb权限请求标识
    private final String IDENTIFICATION = " USB-Serial Controller D";  // 目标设备标识

    private List<UsbSerialDriver> availableDrivers = new ArrayList<>();  // 所有可用设备
    private UsbSerialDriver usbSerialDriver;  // 当前连接的设备
    private UsbDeviceConnection usbDeviceConnection;  // 连接对象
    private UsbSerialPort usbSerialPort;  // 设备端口对象，通过这个读写数据
    private SerialInputOutputManager inputOutputManager;  // 数据输入输出流管理器

// 连接参数，按需求自行修改 ---------------------
    private int baudRate = 115200;  // 波特率
    private int dataBits = 8;  // 数据位
    private int stopBits = UsbSerialPort.STOPBITS_1;  // 停止位
    private int parity = UsbSerialPort.PARITY_NONE;// 奇偶校验

// 单例 -------------------------
    private static USBHostTransferUtils usbHostTransferUtil;
    public static USBHostTransferUtils getInstance() {
        if(usbHostTransferUtil == null){
            usbHostTransferUtil = new USBHostTransferUtils();
        }
        return usbHostTransferUtil;
    }


    // 注册usb授权监听广播
    public void registerReceiver(){
        usbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(INTENT_ACTION_GRANT_USB.equals(intent.getAction())) {
                    // 授权操作完成，连接
//                    boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);  // 不知为何获取到的永远都是 false 因此无法判断授权还是拒绝
                    Log.e(TAG, "授予权限");
                }
            }
        };
        APP.registerReceiver(usbReceiver,new IntentFilter(INTENT_ACTION_GRANT_USB));
    }

    public void setConnectionParameters(int baudRate,int dataBits,int stopBits,int parity){
        this.baudRate = baudRate;this.dataBits = dataBits;this.stopBits = stopBits;this.parity = parity;
    }

    // 刷新当前可用 usb设备
    public List<UsbSerialDriver> refreshDevice(){
        availableDrivers.clear();
        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        Log.e(TAG, "当前可用 usb 设备数量: " + availableDrivers.size() );
        // 有设备可以连接
        if(availableDrivers.size() >= 0){
            GlobalControlUtils.INSTANCE.showToast("当前可连接设备："+availableDrivers.size(),0);
            return availableDrivers;
        }
        // 没有设备
        else {
            GlobalControlUtils.INSTANCE.showToast("请先接入设备",0);
            return null;
        }
    }

    // 检查设备权限
    public boolean checkDevicePermission(UsbSerialDriver usbSerialDriver){
        boolean hasPermission = manager.hasPermission(usbSerialDriver.getDevice());
        if(!hasPermission){
            // 申请设备权限
            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(APP, 0, new Intent(INTENT_ACTION_GRANT_USB), flags);
            manager.requestPermission(usbSerialDriver.getDevice(), usbPermissionIntent);
            GlobalControlUtils.INSTANCE.showToast("请先授予连接权限",0);
        }
        return hasPermission;
    }

    // 连接设备
    public boolean connectDevice(UsbSerialDriver usbSerialDriver){
        this.usbSerialDriver = usbSerialDriver;
        usbSerialPort = usbSerialDriver.getPorts().get(0);  // 一般设备的端口都只有一个，具体要参考设备的说明文档
        usbDeviceConnection = manager.openDevice(usbSerialDriver.getDevice());  // 拿到连接对象
        if(usbSerialPort == null){return false;}
        try {
            usbSerialPort.open(usbDeviceConnection);  // 打开串口
            usbSerialPort.setParameters(baudRate, dataBits, stopBits, parity);  // 设置串口参数：波特率 - 115200 ， 数据位 - 8 ， 停止位 - 1 ， 奇偶校验 - 无
            return startReceiveData();  // 开启读数据线程
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "连接错误" );
            return false;
        }
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private byte[] readBuffer = new byte[1024 * 2];  // 缓冲区
    // 开启数据接收监听
    public boolean startReceiveData(){
        if(usbSerialPort == null || !usbSerialPort.isOpen()){return false;}
        inputOutputManager = new SerialInputOutputManager(usbSerialPort, new SerialInputOutputManager.Listener() {
            @Override
            public void onNewData(byte[] data) {
            // 在这里处理接收到的 usb 数据 -------------------------------
                // 拼接处理
                baos.write(data,0,data.length);
                readBuffer = baos.toByteArray();
                if (readBuffer.length >= 2 && readBuffer[readBuffer.length - 2] == (byte)'\r' && readBuffer[readBuffer.length - 1] == (byte)'\n') {
                    String data_str = DataUtils.bytes2string(readBuffer);
                    String data_hex = DataUtils.bytes2Hex(readBuffer);
                    Log.i(TAG, "收到 usb 数据: " + data_str);

                    String[] data_hex_array = data_hex.split("0d0a");  // 分割后处理
                    for (String s : data_hex_array) {
                        String s_str = DataUtils.hex2String(s);
                        Pattern pattern = Pattern.compile("FKI|ICP|ICI|TCI|PWI|SNR|GGA|GLL|PRX|RNX|ZDX|TXR");
                        Matcher matcher = pattern.matcher(s_str);
                        if (matcher.find()) {
                            BDProtocolUtils.getInstance().parseData(s_str);
                        }
                    }
                    baos.reset();  // 重置
                }
            }
            @Override
            public void onRunError(Exception e) {
                Log.e(TAG, "usb 断开了" );
                disconnect();
                e.printStackTrace();
            }
        });
        inputOutputManager.start();
        return true;
    }

    // 下发数据：建议使用线程池
    public void write(String data_hex){
        if(usbSerialPort != null){
            Log.e(TAG, "当前usb状态: isOpen-" + usbSerialPort.isOpen() );
            // 当串口打开时再下发
            if(usbSerialPort.isOpen()){
                byte[] data_bytes = DataUtils.hex2bytes(data_hex);  // 将字符数据转化为 byte[]
                if (data_bytes == null || data_bytes.length == 0) return;
                try {
                    usbSerialPort.write(data_bytes,0);  // 写入数据，延迟设置太大的话如果下发间隔太小可能报错
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                GlobalControlUtils.INSTANCE.showToast("usb 未连接" ,0);
            }
        }
    }

    // 下发北斗消息
    public void sendMessage(String targetCardNumber, int type, String content_str){
        ExecutorService executorService = DispatcherExecutor.INSTANCE.getIOExecutor();
        if(executorService!=null){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    write(BDProtocolUtils.CCTCQ(targetCardNumber,type,content_str));
                    // 开始倒计时
                    ApplicationUtils.INSTANCE.getGlobalViewModel(MainVM.class).startCountDown();
                }
            });
        }
    }

    // 下发初始化指令
    public void init_device(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(300);
                    write(BDProtocolUtils.CCPWD());  // 登录
                    sleep(300);
                    write(BDProtocolUtils.CCICR(0,"00"));  // 查询ic信息
                    sleep(300);
                    write(BDProtocolUtils.CCRMO("PWI",2,5));  // 北三信号间隔 5
                    sleep(300);
                    write(BDProtocolUtils.CCRNS(0,0,0,0,0,0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    // 断开连接
    public void disconnect(){
        try{
            // 停止数据接收监听
            if(inputOutputManager != null){
                inputOutputManager.stop();
                inputOutputManager = null;
            }
            // 关闭端口
            if(usbSerialPort != null){
                usbSerialPort.close();
                usbSerialPort = null;
            }
            // 关闭连接
            if(usbDeviceConnection != null){
                usbDeviceConnection.close();
                usbDeviceConnection = null;
            }
            // 清除设备
            if(usbSerialDriver != null){
                usbSerialDriver = null;
            }
            // 清空设备列表
            availableDrivers.clear();
            // 注销广播监听
            if(usbReceiver != null){
                APP.unregisterReceiver(usbReceiver);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}
