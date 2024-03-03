package com.bdtx.mod_util.Utils.Transfer;


import static java.lang.Thread.sleep;

import android.util.Log;

import com.bdtx.mod_data.ViewModel.MainVM;
import com.bdtx.mod_util.Utils.ApplicationUtils;
import com.bdtx.mod_util.Utils.DataUtils;
import com.bdtx.mod_util.Utils.DispatcherExecutor;
import com.bdtx.mod_util.Utils.Protocol.BDProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public class SerialPortTransferUtils {

    private String TAG = "SerialPortTransferUtils";
    private String path = "/dev/ttyS1";  // 串口地址
    private int baudrate = 115200;  // 波特率
    private int stopBits = 1;  // 停止位
    private int dataBits = 8;  // 数据位
    private int parity = 0;  // 校验位
    private int flowCon = 0;
    private int flags = 0;
    private SerialPort serialPort = null;
    private OutputStream outputStream;  // 输出流，写入数据
    private InputStream inputStream;  // 输入流，读取数据
    private ReadThread readThread;  // 读数据线程
    // 单例 -------------------------------------------------------------------
    private static SerialPortTransferUtils serialPortTransferUtils;
    public static SerialPortTransferUtils getInstance() {
        if(serialPortTransferUtils == null){
            serialPortTransferUtils = new SerialPortTransferUtils();
        }
        return serialPortTransferUtils;
    }

    // 设置串口参数，一般只有这两个参数需要改变
    public void setSerialPortParameters(String path,int baudrate){
        this.path = path; this.baudrate = baudrate;
    }
    public boolean openSerialPort(){
        boolean result = false;
        try{
            serialPort = new SerialPort(new File(path), baudrate, stopBits, dataBits, parity, flowCon, flags);  // 打开串口
            outputStream = serialPort.getOutputStream();  // 拿到输出流
            inputStream = serialPort.getInputStream();  // 拿到输出流
            readThread = new ReadThread();  // 开启读数据线程
            readThread.start();
            result = true;
            Log.e(TAG, "打开串口成功" );
        }catch (Exception e){
            result = false;
            Log.e(TAG, "打开串口失败" );
            e.printStackTrace();
        }
        return result;
    }

    public List<String> getSerialPortPaths(){
        try{
            SerialPortFinder serialPortFinder = new SerialPortFinder();
            String[] allDevicesPath = serialPortFinder.getAllDevicesPath();
            if(allDevicesPath==null || allDevicesPath.length<1){
                return new ArrayList<String>();
            } else {
                return Arrays.asList(allDevicesPath);
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<String>();
        }
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

    public void init_device(){
        ExecutorService executorService = DispatcherExecutor.INSTANCE.getIOExecutor();
        if(executorService!=null){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        write(BDProtocolUtils.CCPWD());  // 登录
                        sleep(300);
                        write(BDProtocolUtils.CCICR(0,"00"));  // 查询ic信息
                        sleep(300);
                        write(BDProtocolUtils.CCRMO("PWI",2,5));  // 北三信号间隔 5
                        sleep(300);
                        write(BDProtocolUtils.CCRNS(0,0,0,0,0,0));
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                }
            });
        }
    }


    public void close(){
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
            // 关闭串口
            if(serialPort != null){
                serialPort.close();
                serialPort = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private byte[] readBuffer = new byte[1024 * 4];
    private class ReadThread extends Thread {
        boolean alive = true;
        public void run() {
            super.run();
            if(inputStream == null){return;}
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Log.e(TAG, "开启数据监听");
            while(alive) {
                try {
                    if (inputStream.available() > 0) {
                        byte[] buf = new byte[10];  // 原来 2048，这个大小直接影响接收数据的速率，例如：当byte[10]时可能出现很长时间不输出，一次性输出多条指令
                        int size = inputStream.read(buf);
                        baos.write(buf,0,size);
                        readBuffer=baos.toByteArray();
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
                    sleep(10);
                } catch (Throwable var3) {
                    if(var3.getMessage() != null){
                        Log.e("error", var3.getMessage());
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

}
