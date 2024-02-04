package com.bdtx.mod_util.Utils;

import android.media.AudioRecord;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import com.bdtx.mod_data.Global.Constant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioRecordUtils {

    //录制状态
    private boolean recorderState = true;
    private byte[] buffer;
    private AudioRecord audioRecord;
    private FileOutputStream bos;  // 文件输出流
    private AudioRecordUtils() { }


    private static AudioRecordUtils audioRecordUtils = new AudioRecordUtils();
    public static AudioRecordUtils getInstance() {
        return audioRecordUtils;
    }

    public static MediaFormat getFormat(){
        //参数对应-> mime type、采样率、声道数
        MediaFormat encodeFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, Constant.sampleRateInHz, Constant.channelCount);
        //所需比特率（以比特/秒为单位）
        encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, Constant.BIT_RATE);
        //仅编码器，可选，如果内容为AAC音频，则指定所需的配置文件。
        encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        //格式的类型
        encodeFormat.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC);
        // 读取数据的最大字节数
        encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, Constant.KEY_MAX_INPUT_SIZE);
        // 关键帧间隔
        encodeFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,15);
        return encodeFormat;
    }

    public void init() {
        Log.e("初始化语音工具", "init: ");
        int recordMinBufferSize = AudioRecord.getMinBufferSize(Constant.sampleRateInHz, Constant.channelConfig, Constant.audioFormat);
        //指定 AudioRecord 缓冲区大小
        buffer = new byte[recordMinBufferSize];
        //根据录音参数构造AudioRecord实体对象
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, Constant.sampleRateInHz, Constant.channelConfig, Constant.audioFormat, recordMinBufferSize);
    }


    /**
     * 开始ACC录制
     */
    public void startACC(String aacFilePath) {
        try {
            File aacFile = new File(aacFilePath);
            if (!aacFile.exists()) {
                aacFile.createNewFile();
            }
            bos = new FileOutputStream(aacFile);
        }catch (Exception e){
            e.printStackTrace();
        }

        if (audioRecord.getState() == AudioRecord.RECORDSTATE_STOPPED) {
            recorderState = true;
            audioRecord.startRecording();
            new RecordThread().start();
        }
    }

    /**
     * 开始PCM录制
     */
    public void startPCM(String filePath) {
        Log.e("开始录音", "startPCM: ");
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            bos = new FileOutputStream(file);
        }catch (Exception e){
            e.printStackTrace();
        }

        if (audioRecord.getState() == AudioRecord.RECORDSTATE_STOPPED) {
            recorderState = true;
            audioRecord.startRecording();
            new RecordThread().start();
        }
    }
    /**
     * 停止录制
     */
    public void stop() {
        Log.e("停止录音", "startPCM: ");
        recorderState = false;
        if(audioRecord == null){
            Log.e("对象为空", "----");
            return;
        }
        if (audioRecord.getState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop();
        }
        audioRecord.release();
        if(bos!=null){
            try {
                bos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private class RecordThread extends Thread {

        @Override
        public void run() {
            while (recorderState) {
                int read = audioRecord.read(buffer, 0, buffer.length);

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    //获取到的pcm数据就是buffer了
                 //  pcmEncoderAAC.encodeData(buffer);
                    try {
                        Log.e("正在录音", "----");
                        // 进行压缩
                        bos.write(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }





}
