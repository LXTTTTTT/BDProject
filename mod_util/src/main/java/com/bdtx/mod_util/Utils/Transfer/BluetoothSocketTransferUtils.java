package com.bdtx.mod_util.Utils.Transfer;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.util.Log;

import com.bdtx.mod_util.Utils.ApplicationUtils;
import com.bdtx.mod_util.Utils.DataUtils;
import com.bdtx.mod_util.Utils.FileUtils;
import com.bdtx.mod_util.Utils.GlobalControlUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

// 蓝牙 Socket 数据传输工具
public class BluetoothSocketTransferUtils {

    private static String TAG = "BluetoothSocketTransferUtils";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothServerSocket bluetoothServerSocket;
    private BluetoothDevice nowDevice;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ReceiveDataThread receiveDataThread;  // 接收数据线程
    private ListenThread listenThread;  // 监听连接线程

    private final UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private int state = 0;
    private final int STATE_DISCONNECT = 0;
    private final int STATE_CONNECTING = 1;
    private final int STATE_CONNECTED = 2;

    public boolean isConnectedDevice = false;
    public boolean isSendFile = false;
    public static ExecutorService executorService;
// 单例 ----------------------------------------------------------------
    private static BluetoothSocketTransferUtils bluetoothSocketUtils;

    public static BluetoothSocketTransferUtils getInstance() {
        if (bluetoothSocketUtils == null) {
            bluetoothSocketUtils = new BluetoothSocketTransferUtils();
        }
        return bluetoothSocketUtils;
    }

    public BluetoothSocketTransferUtils() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        executorService = Executors.newFixedThreadPool(100);
    }

    public Set<BluetoothDevice> getPairedDeviceList(){
        return null;
    }


    public synchronized void connect(BluetoothDevice device) {
        Log.e(TAG, "连接设备: " + device.getName()+"/"+state);
        if (state == STATE_CONNECTING || state == STATE_CONNECTED) {
            GlobalControlUtils.INSTANCE.showToast("正在连接设备",0);return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                    state = STATE_CONNECTING;
                    if(onBluetoothSocketWork!=null){onBluetoothSocketWork.onConnecting();}
                    bluetoothSocket.connect();
                    inputStream = bluetoothSocket.getInputStream();
                    outputStream = bluetoothSocket.getOutputStream();
                    state = STATE_CONNECTED;
                    isConnectedDevice = true;
                    nowDevice = device;
                    receiveDataThread = new ReceiveDataThread();
                    receiveDataThread.start();  // 开启读数据线程
                    if(onBluetoothSocketWork!=null){onBluetoothSocketWork.onConnected(device.getName());}
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(TAG, "连接失败了" );
                    disconnect();
                }
            }
        }).start();
    }

    private byte[] readBuffer = new byte[1024*1024];
    private class ReceiveDataThread extends Thread{
        private boolean receive = false;
        byte[] buffer = new byte[1024*1024];
        @Override
        public void run() {
            if(inputStream==null){return;}
            receive = true;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (receive){
                try{
                    int size = inputStream.read(buffer);
                    if(size>0){
                        baos.write(buffer, 0, size);
                        readBuffer = baos.toByteArray();
                        executorService.execute(new Runnable() {@Override public void run() {
                            receiveData(readBuffer);
                        }});
                        baos.reset();
                    }else if(size==-1){
                        Log.e(TAG, "BluetoothSocket: 断开了");
                        cancel();
                        disconnect();
                        break;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    // 断开连接了
                    Log.e(TAG, "BluetoothSocket: 读取数据错误，断开连接");
                    cancel();
                    disconnect();
                }
            }
        }

        public void cancel(){
            receive = false;
        }
    }

    public void listen(){
        if(state!=STATE_DISCONNECT){return;}
        if(listenThread!=null){
            listenThread.cancel();
            listenThread = null;
        }
        listenThread = new ListenThread();
        listenThread.start();
    }

    private class ListenThread extends Thread{
        private boolean listen = false;
        public ListenThread(){
            try {
                bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("name", MY_UUID_SECURE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            listen = true;
            Log.e(TAG, "开启设备连接监听"+listen+"/"+(state==STATE_DISCONNECT) );
            while (listen && state==STATE_DISCONNECT){
                try {
                    if(bluetoothSocket==null){
                        bluetoothSocket = bluetoothServerSocket.accept();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (bluetoothSocket != null) {
                    try {
                        Log.e(TAG, "监听到设备连接" );
                        state = STATE_CONNECTING;
                        if(onBluetoothSocketWork!=null){onBluetoothSocketWork.onConnecting();}
                        inputStream = bluetoothSocket.getInputStream();
                        outputStream = bluetoothSocket.getOutputStream();
                        nowDevice = bluetoothSocket.getRemoteDevice();
                        receiveDataThread = new ReceiveDataThread();
                        receiveDataThread.start();  // 开启读数据线程
                        state = STATE_CONNECTED;
                        isConnectedDevice = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void cancel(){
            listen = false;
            try {
                if (bluetoothServerSocket != null) {
                    bluetoothServerSocket.close();
                    bluetoothServerSocket = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void send_text(String data_str){
        if(outputStream==null){return;}
        if(isSendFile){return;}
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] data_bytes = data_str.getBytes("GB18030");
                    String head = "$*1*$"+String.format("%08X", data_bytes.length);
                    outputStream.write(head.getBytes(StandardCharsets.UTF_8));
                    outputStream.write(data_bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public final int SEND_FILE_AUDIO = 0;
    public final int SEND_FILE_PICTURE = 1;
    public void send_file(String path,int type){
        if(outputStream==null){return;}
        if(isSendFile){return;}
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(path);
                if (!file.exists() || !file.isFile()) {
                    Log.e(TAG, "文件不存在");
                    return;
                }else {
                    GlobalControlUtils.INSTANCE.showToast("开始发送文件",0);
                    Log.e(TAG, "开始发送文件");
                    isSendFile = true;
                }

                byte[] file_byte = fileToBytes(path);
                try {
                    Thread.sleep(500);
                    int bytesRead;
                    // 写入文件内容到输出流
                    String head;
                    if(type==SEND_FILE_AUDIO){head = "$*2*$"+String.format("%08X", file_byte.length);}
                    else {head = "$*3*$"+String.format("%08X", file_byte.length);}
                    Log.e(TAG, "head: "+head );
                    outputStream.write(head.getBytes(StandardCharsets.UTF_8));
                    outputStream.write(file_byte);
                    isSendFile = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "文件发送失败", e);
                    isSendFile = false;
                }
            }
        }).start();
    }

    public void disconnect(){
        try {
            if(receiveDataThread!=null){
                receiveDataThread.cancel();
                receiveDataThread = null;
            }
            if(inputStream!=null){
                inputStream.close();
                inputStream=null;
            }
            if(outputStream!=null){
                outputStream.close();
                outputStream=null;
            }
            if(bluetoothSocket!=null){
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
            if(onBluetoothSocketWork!=null){onBluetoothSocketWork.onDisconnecting();}
            state = STATE_DISCONNECT;
            isConnectedDevice = false;
            nowDevice = null;
            initReceiveParameter();  // 初始化接收数据参数
//            listen();  // 断开后重新开启设备连接监听
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isStart = false;
    private ByteArrayOutputStream file_bytes_baos = new ByteArrayOutputStream();
    private long file_length = 0;  // 文件数据长度
    private int message_type = -1;  // 消息类型：-1-未知 0-设备指令 1-文本 2-语音 3-图片
    private Timer resetTimmer = null;
    private void initReceiveParameter(){
        isStart = false;
        file_bytes_baos.reset();
        file_length = 0;
        message_type = -1;
    }
    private synchronized void receiveData(byte[] data_bytes) {
        Log.e(TAG, "处理数据: "+data_bytes.length );
        if(!isStart){
            try{
                // $*1*$00339433
                String data_str = new String(data_bytes,StandardCharsets.UTF_8);
                int head_index = data_str.indexOf("$*");
                // 有头，开始接收
                if(head_index>=0){
                    isStart = true;
                    String head = data_str.substring(head_index,head_index+13);
                    String msg_type = head.substring(0,5);
                    if(msg_type.contains("1")){
                        message_type = 1;
                    }else if(msg_type.contains("2")){
                        message_type = 2;
                    }else if(msg_type.contains("3")){
                        message_type = 3;
                    }else if(msg_type.contains("0")){
                        message_type = 0;
                    }else {
                        message_type = -1;
                    }
                    String length_hex = head.substring(5);
                    file_length = Long.parseLong(length_hex,16);
                    Log.e(TAG, "收到头消息 head: "+head+" 文件数据长度："+file_length);

                    file_bytes_baos.write(data_bytes,13,data_bytes.length-13);
                    // 如果是单次数据：文本
                    if(data_bytes.length==file_length+13){
                        parseData();
                    }
                }else {
                    Log.e(TAG, "receiveData: 没有头"+data_str );
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {

            try {
                file_bytes_baos.write(data_bytes);
                Log.e(TAG, "总长度: "+file_length+" /已接收长度："+file_bytes_baos.size());

            } catch (IOException e) {
                e.printStackTrace();
            }
            if(file_bytes_baos.size()>=file_length){
                parseData();
            }
        }

        // 如果发送文件途中丢包或者发送文件时读取长度太长导致收到的数据一直达不到长度
        if(resetTimmer!=null){
            resetTimmer.cancel();
            resetTimmer = null;
        }
        resetTimmer = new Timer();
        resetTimmer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.e(TAG, "重置接收文件状态" );
                if(isStart){isStart = false;}
            }
        },3000);
    }

    public void parseData(){
        if(message_type==-1){
            initReceiveParameter();
            return;
        }
        if(message_type==1){
            String content = "";
            try {
                content = new String(file_bytes_baos.toByteArray(),"GB18030");
                Log.e(TAG, "数据接收完毕，文本："+content);
            } catch (Exception e) {
                e.printStackTrace();
            }
            initReceiveParameter();
        }else if(message_type==2){
            Log.e(TAG, "数据接收完毕，语音" );
            // 保存语音数据
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String imgFilePath= FileUtils.getTransferImgFile()+"transfer" + DataUtils.getTimeSerial()+".pcm";
                        File imageFile = new File(imgFilePath);
                        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                            fos.write(file_bytes_baos.toByteArray());
                        }
                        initReceiveParameter();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }else if(message_type==3){
            Log.e(TAG, "数据接收完毕，图片" );
            executorService.execute(new Runnable() {
                @Override public void run() {
                    try {
                        String imgFilePath= FileUtils.getTransferImgFile()+"transfer" + DataUtils.getTimeSerial()+".jpg";
                        File imageFile = new File(imgFilePath);
                        try (FileOutputStream fos = new FileOutputStream(imageFile); FileChannel channel = fos.getChannel()) {
                            ByteBuffer buffer = ByteBuffer.wrap(file_bytes_baos.toByteArray());
                            channel.write(buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        forceFilesystemCache(imgFilePath);
                        initReceiveParameter();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } else if(message_type==0){
            Log.e(TAG, "数据接收完毕，指令" );
            String content = "";
            try {
                content = new String(file_bytes_baos.toByteArray(),"GB18030");
                Log.e(TAG, "数据接收完毕，指令："+content);
            } catch (Exception e) {
                e.printStackTrace();
            }
            initReceiveParameter();
            // 解析指令
            JsonElement jsonElement = new JsonParser().parse(content);
            // 检查JSON元素是否是JsonObject
            if (jsonElement.isJsonObject()) {
                // 将JsonElement转换为JsonObject
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                // 获取特定键的值
                String number = jsonObject.get("targetNumber").getAsString();
                if(number!=null&&!number.equals("")){
                    Log.e(TAG, "收到指令，修改目标地址："+number);
                }
            }

        }

    }
    private static void forceFilesystemCache(String filePath) {
        try (FileOutputStream fos = new FileOutputStream(new File(filePath), true);
             FileChannel channel = fos.getChannel()) {
            // 强制刷新文件系统缓存
            channel.force(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static byte[] fileToBytes(String filePath){
        File file = new File(filePath);
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024*1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


// 接口 ---------------------------------------------
    public interface OnBluetoothSocketWork{
        void onConnecting();
        void onConnected(String device_name);
        void onDisconnecting();
        void onDiscoverNewDevice(List<BluetoothDevice> devices);
        void receiveData(byte[] data);
    }
    public OnBluetoothSocketWork onBluetoothSocketWork;
    public void setOnBluetoothSocketWork(OnBluetoothSocketWork onBluetoothSocketWork){
        this.onBluetoothSocketWork = onBluetoothSocketWork;
    }

}
