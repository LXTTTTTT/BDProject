package com.bdtx.mod_util.Utils.Transfer.USB;

import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import com.bdtx.mod_util.Utils.ApplicationUtils;
import com.bdtx.mod_util.Utils.DataUtils;
import com.bdtx.mod_util.Utils.GlobalControlUtils;
import com.bdtx.mod_util.Utils.Protocol.BDProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// USB附件连接工具
public class USBAccessoryTransferUtils {

    String TAG = "USBAccessoryTransferUtil";
    private Application APP = ApplicationUtils.INSTANCE.getApplication();  // 主程序
    public UsbManager usbManager = (UsbManager) APP.getSystemService(Context.USB_SERVICE);

    private BroadcastReceiver usbAccessoryReceiver = null;  // 广播监听：判断设备授权操作
    public UsbAccessory usbAccessory = null;  // 当前连接的 USB附件 对象
    public ParcelFileDescriptor fileDescriptor = null;
    public FileInputStream inputStream = null;  // 输入流
    public FileOutputStream outputStream = null;  // 输出流
    public ReadThread readThread = null;  // 接收数据线程

    private final String ACTION_USB_PERMISSION = "com.bdtx.main.INTENT_ACTION_GRANT_USB_ACCESSORY";  // usb权限请求标识
    private final String IDENTIFICATION = "WCHAccessory1";  // 目标设备的序列号标识

// 单例 -------------------------------------------------------------------
    private static USBAccessoryTransferUtils usbAccessoryTransferUtil;
    public static USBAccessoryTransferUtils getInstance() {
        if(usbAccessoryTransferUtil == null){
            usbAccessoryTransferUtil = new USBAccessoryTransferUtils();
        }
        return usbAccessoryTransferUtil;
    }


    // 注册usb授权监听广播
    public void registerReceiver(){
        usbAccessoryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.e(TAG, "onReceive: "+action);
                // 收到 ACTION_USB_PERMISSION 请求权限广播
                if (ACTION_USB_PERMISSION.equals(action)) {
                    // 确保只有一个线程执行里面的任务，不与其他应用冲突
                    synchronized (this) {
                        usbAccessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                        if(usbAccessory==null){Log.e(TAG, "usbAccessory 对象为空" );return;}
                        // 判断是否授予了权限
                        boolean havePermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                        if (havePermission) {
                            GlobalControlUtils.INSTANCE.showToast("授予 USB 权限", Toast.LENGTH_SHORT);
                        }
                        else {
                            GlobalControlUtils.INSTANCE.showToast("拒绝 USB 权限", Toast.LENGTH_SHORT);
                        }
                    }
                }
                // 收到 USB附件 拔出的广播
                else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {  // android.hardware.usb.action.USB_ACCESSORY_DETACHED
                    // 断开连接
                    disconnect();
                }
                else {
                    Log.e(TAG, "registerReceiver/onReceive其它："+action);
                }
            }
        };
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);  // 当收到 usb附件 拔出广播动作
        APP.registerReceiver(usbAccessoryReceiver, filter);  // 注册
    }

    public List<UsbAccessory> refreshDevice(){
        UsbAccessory[] accessories = usbManager.getAccessoryList();
        if(accessories==null || accessories.length<1){
            return new ArrayList<UsbAccessory>();
        }else {
            Log.e(TAG, "获取 UsbAccessory 数量: "+accessories.length );
            return Arrays.asList(accessories);
        }
    }

    // 连接设备
    public void connectDevice(UsbAccessory usbAccessory){
//        if(!checkDevice(usbAccessory)){return;}
        this.usbAccessory = usbAccessory;
        fileDescriptor = usbManager.openAccessory(usbAccessory);
        if(fileDescriptor != null){
            FileDescriptor fd = fileDescriptor.getFileDescriptor();
            // 拿到输入/输出流
            inputStream = new FileInputStream(fd);
            outputStream = new FileOutputStream(fd);
            // 开启接收数据线程
            readThread = new ReadThread();
            readThread.start();
        }
    }

    public boolean checkPermission(UsbAccessory usbAccessory){
        boolean hasPermission = usbManager.hasPermission(usbAccessory);
        if(!hasPermission){
            synchronized (usbAccessoryReceiver) {
                GlobalControlUtils.INSTANCE.showToast("请授予 USB 权限", Toast.LENGTH_SHORT);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(APP, 0, new Intent(ACTION_USB_PERMISSION), 0);
                usbManager.requestPermission(usbAccessory,pendingIntent);
            }
        }
        return hasPermission;
    }

    public String ManufacturerString = "mManufacturer=WCH";
    public String ModelString1 = "mModel=WCHUARTDemo";
    public String VersionString = "mVersion=1.0";
    public boolean checkDevice(UsbAccessory usbAccessory){
        if( -1 == usbAccessory.toString().indexOf(ManufacturerString)) {
            GlobalControlUtils.INSTANCE.showToast("Manufacturer is not matched!", Toast.LENGTH_SHORT);
            return false;
        }
        if( -1 == usbAccessory.toString().indexOf(ModelString1) ) {
            GlobalControlUtils.INSTANCE.showToast("Model is not matched!", Toast.LENGTH_SHORT);
            return false;
        }
        if( -1 == usbAccessory.toString().indexOf(VersionString)) {
            GlobalControlUtils.INSTANCE.showToast("Version is not matched!", Toast.LENGTH_SHORT);
            return false;
        }
        GlobalControlUtils.INSTANCE.showToast("制造商、型号和版本匹配", Toast.LENGTH_SHORT);
        return true;
    }


    // 下发数据（16进制字符串）
    public void write(String data_hex) {
        if(outputStream==null){return;}
        try {
            byte[] data_bytes = DataUtils.hex2bytes(data_hex);
            this.outputStream.write(data_bytes);
            Log.e(TAG, "write 下发的指令是: " + DataUtils.hex2String(data_hex) );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // 下发初始化指令
    public void init_device(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SetConfig(19200,(byte)8,(byte)1,(byte)0,(byte)0);

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                write(BDProtocolUtils.CCRMO("PWI",2,9));  // 设置pwi信号输出频度

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                write(BDProtocolUtils.CCRMO("MCH",1,0));  // 关闭设备的HCM指令输出

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                write(BDProtocolUtils.CCRNS(5,5,5,5,5,5));  // 设置rn指令输出频度

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                write(BDProtocolUtils.CCICR(0,"00"));

            }
        }).start();

    }



    // 断开连接
    public void disconnect(){
        try {
            // 停止数据监听
            if(readThread != null){
                readThread.close();
                readThread = null;
            }
            // 关闭输入输出流
            if(inputStream != null){
                inputStream.close();
                inputStream = null;
            }
            if(outputStream != null){
                outputStream.close();
                outputStream = null;
            }
            if(fileDescriptor != null){
                fileDescriptor.close();
                fileDescriptor = null;
            }
            // 清除设备
            if(usbAccessory != null){
                usbAccessory = null;
            }
            // 注销广播
            if(usbAccessoryReceiver != null){
                APP.unregisterReceiver(usbAccessoryReceiver);
                usbAccessoryReceiver = null;
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    // 读取 USB附件 数据线程
    private byte[] readBuffer = new byte[1024 * 2];  // 缓冲区
    private class ReadThread  extends Thread {
        boolean alive = true;
        ReadThread(){
            this.setPriority(Thread.MAX_PRIORITY);  // 设置线程的优先级：最高级
        }

        byte[] buf = new byte[2048];  // 每次从输入流读取的最大数据量：这个大小直接影响接收数据的速率，根据需求修改
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        public void run() {
            if(inputStream == null){return;}
            init_device();  // 下发初始化指令，根据自己的设备修改或直接删掉
            Log.e(TAG, "开启数据监听");
            while(alive) {
                try {
                    int size = inputStream.read(buf);
                    if(size>0){
                        baos.write(buf,0,size);
                        readBuffer = baos.toByteArray();
                        // 根据需求设置停止位：由于我需要接收的是北斗指令，指令格式最后两位为 “回车换行(\r\n)” 所以只需要判断数据末尾两位
                        // 设置停止位，当最后两位为 \r\n 时就传出去
                        if (readBuffer.length >= 2 && readBuffer[readBuffer.length - 2] == (byte)'\r' && readBuffer[readBuffer.length - 1] == (byte)'\n') {
                            String data_str = DataUtils.bytes2string(readBuffer);
                            String data_hex = DataUtils.bytes2Hex(readBuffer);
                            Log.i(TAG, "收到 usb_accessory 数据: " + data_str);
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
                    sleep(10);  // 设置循环间隔
                } catch (Throwable var3) {
                    if(var3.getMessage() != null){
                        Log.e(TAG, "ReadThread：" + var3.getMessage());
                    }
                    return;
                }
            }
        }

        public void close(){
            alive = false;
            this.interrupt();
        }
    }


    // 沁恒设备设置波特率等参数方法
    public void SetConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        Log.e(TAG, "设置参数: " + baud + "/"  + dataBits + "/" + stopBits + "/" + parity + "/" + flowControl);
        byte tmp = 0x00;
        byte baudRate_byte = 0x00;
        byte []	writeusbdata = new byte[5];
        writeusbdata[0] = 0x30;
        switch(baud) {
            case 300:baudRate_byte = 0x00;break;
            case 600:baudRate_byte = 0x01;break;
            case 1200:baudRate_byte = 0x02;break;
            case 2400:baudRate_byte = 0x03;break;
            case 4800:baudRate_byte = 0x04;break;
            case 9600:baudRate_byte = 0x05;break;
            case 19200:baudRate_byte = 0x06;break;
            case 38400:baudRate_byte = 0x07;break;
            case 57600:baudRate_byte = 0x08;break;
            case 115200:baudRate_byte = 0x09;break;
            case 230400:baudRate_byte = 0x0A;break;
            case 460800:baudRate_byte = 0x0B;break;
            case 921600:baudRate_byte = 0x0C;break;
            default:baudRate_byte = 0x05;break; // default baudRate "9600"
        }
        // prepare the baud rate buffer
        writeusbdata[1] = baudRate_byte;

        switch(dataBits){
            case 5:tmp |= 0x00;break;  //reserve
            case 6:tmp |= 0x01;break;  //reserve
            case 7:tmp |= 0x02;break;
            case 8:tmp |= 0x03;break;
            default:tmp |= 0x03;break; // default data bit "8"
        }

        switch(stopBits){
            case 1:tmp &= ~(1 << 2);break;
            case 2:tmp |= (1 << 2);break;
            default:tmp &= ~(1 << 2);break; // default stop bit "1"
        }

        switch(parity){
            case 0:tmp &= ~( (1 << 3) | (1 << 4) | (1 << 5) );break; //none
            case 1:tmp |= (1 << 3);break; //odd
            case 2:tmp |= ( (1 << 3) | (1 << 4) );break; //event
            case 3:tmp |= ( (1 << 3) | (1 << 5) );break; //mark
            case 4:tmp |= ( (1 << 3) | (1 << 4) | (1 << 5) );break; //space
            default:tmp &= ~( (1 << 3) | (1 << 4) | (1 << 5));break;//default parity "NONE"
        }

        switch(flowControl){
            case 0:tmp &= ~(1 << 6);break;
            case 1:tmp |= (1 << 6);break;
            default:tmp &= ~(1 << 6);break; //default flowControl "NONE"
        }
        // dataBits, stopBits, parity, flowControl
        writeusbdata[2] = tmp;
        writeusbdata[3] = 0x00;
        writeusbdata[4] = 0x00;
        write(DataUtils.bytes2Hex(writeusbdata));
        writeusbdata = null;
    }


}
