package com.bdtx.mod_util.Utils;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.bdtx.mod_data.Global.Constant;

import java.io.File;
import java.io.FileOutputStream;

// 录音工具：初始化录音参数/工具 - 启动录音 - 获取输出流存储文件
public class AudioRecordUtils {

    private String TAG = "AudioRecordUtils";
    // 录制状态
    private boolean isRecording = true;
    int recordMinBufferSize;
    private byte[] buffer;
    private AudioRecord audioRecord;

    private AudioRecordUtils() {
        recordMinBufferSize = AudioRecord.getMinBufferSize(Constant.sampleRateInHz, Constant.channelConfig, Constant.audioFormat);
        buffer = new byte[recordMinBufferSize];  // 指定 AudioRecord 缓冲区大小
    }


    private static AudioRecordUtils audioRecordUtils = new AudioRecordUtils();

    public static AudioRecordUtils getInstance() {
        return audioRecordUtils;
    }

    public void init() {
        Log.e(TAG, "初始话语音工具");
        //  根据录音参数构造AudioRecord实体对象
        if (audioRecord != null) {
            stop();
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, Constant.sampleRateInHz, Constant.channelConfig, Constant.audioFormat, recordMinBufferSize);
    }


    // pcm 录音
    private FileOutputStream bos;  // 文件输出流
    public void startPCM(String filePath) {
        init();
        Log.e(TAG, "开始录音");
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            bos = new FileOutputStream(file);
            isRecording = true;
            audioRecord.startRecording();
            new RecordThread().start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stop() {
        isRecording = false;
        if(audioRecord != null){
            if (audioRecord.getState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop();
            }
            audioRecord.release();
            audioRecord=null;
        }
        if(bos!=null){
            try {
                bos.close();bos=null;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        Log.e(TAG,"停止录音");
    }


    private class RecordThread extends Thread {
        @Override
        public void run() {
            while (isRecording) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read>0) {
                    try {
                        Log.i(TAG,"正在录音-----------"+read);
                        bos.write(buffer);
                    } catch (Exception e) {e.printStackTrace();}
                }
            }
        }
    }





}
