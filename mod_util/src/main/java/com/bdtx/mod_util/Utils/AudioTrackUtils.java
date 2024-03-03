package com.bdtx.mod_util.Utils;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.util.Log;


import com.bdtx.mod_data.Global.Constant;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;


public class AudioTrackUtils {

    private static final String TAG = "AudioTrackUtils";
// 单例 --------------------------------------
    private volatile static AudioTrackUtils instance;
    public static AudioTrackUtils getInstance() {
        if (instance == null) {
            synchronized (AudioTrackUtils.class) {
                if (instance == null) {
                    instance = new AudioTrackUtils();
                }
            }
        }
        return instance;
    }

    public AudioTrackUtils() {
        initData();
    }
    private AudioTrack mAudioTrack;
    private DataInputStream mDis;//播放文件的数据流
    private Thread mRecordThread;
    private boolean isStart = false;

    private PlayListener playListener;
    private static final int mStreamType = AudioManager.STREAM_MUSIC;  // 音频流类型
    private static final int mSampleRateInHz= Constant.sampleRateInHz;  // 指定采样率（MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
    private static final int mChannelConfig= AudioFormat.CHANNEL_CONFIGURATION_MONO;  // 单声道，指定捕获音频的声道数目。在AudioFormat类中指定用于此的常量
    // 指定音频量化位数 ,在 AudioFormaat 类中指定了以下各种可能的常量。通常我们选择 ENCODING_PCM_16BIT 和 ENCODING_PCM_8BIT PCM 代表的是脉冲编码调制，它实际上是原始音频样本。
    // 因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。
    private static final int mAudioFormat= AudioFormat.ENCODING_PCM_16BIT;
    private int mMinBufferSize;  // 指定缓冲区大小。调用AudioRecord类的getMinBufferSize方法可以获得。
    private static int mMode = AudioTrack.MODE_STREAM;  // STREAM 的意思是由用户在应用程序通过write方式把数据一次一次得写到 audiotrack 中。这个和我们在 socket 中发送数据一样，应用层从某个地方获取数据，例如通过编解码得到PCM数据，然后 write 到 audiotrack。


    private void initData(){
        // 根据采样率，采样精度，单双声道来得到 frame 的大小。
        mMinBufferSize = AudioTrack.getMinBufferSize(mSampleRateInHz,mChannelConfig, mAudioFormat);  // 计算最小缓冲区
        mAudioTrack = new AudioTrack(mStreamType, mSampleRateInHz,mChannelConfig, mAudioFormat,mMinBufferSize,mMode);
    }

    private void destroyThread() {
        try {
            if (null != mRecordThread && Thread.State.RUNNABLE == mRecordThread.getState()) {
                try {
                    mRecordThread.interrupt();
                } catch (Exception e) {
                    mRecordThread = null;
                }
            }
            mRecordThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mRecordThread = null;
        }
    }

    // 启动播放线程
    private void startThread() {
        isStart = true;
        setRecordRunnable();
    }

    @SuppressLint("CheckResult")
    private void setRecordRunnable(){
        RXObservableUtils.observe(Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                if (mAudioTrack == null)
                    return;
                try {
                    Log.e(TAG,"recordRunnable:start"+Thread.currentThread());
                    //设置线程的优先级
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                    playListener.start();
                    byte[] tempBuffer = new byte[3200];
                    int readCount;
                    if(mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED){
                        initData();
                    }
                    if (mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                        //IllegalStateException 播放失败后stopPlay复位
                        try {
                            mAudioTrack.play();
                        } catch (IllegalStateException e) {
                            stopPlay();
                            Log.w(TAG, "could not start audio track");
                        }
                    }
                    while (mDis.available() > 0&&isStart) {
                        readCount= mDis.read(tempBuffer);
                        if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                            continue;
                        }
                        // 一次一次的写入 pcm 数据到 audioTrack.由于是在子线程中进行write，快速连续点击可能主线程触发了 stop 或者 release，导致子线程 write异常：IllegalStateException: Unable to retrieve AudioTrack pointer for send_text()
                        // 所以加 playstate 的判断
                        if (readCount > 0 && mAudioTrack != null && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING && mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                            mAudioTrack.write(tempBuffer, 0, readCount);
                        }
                    }
                    stopPlay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })).subscribe(waitSendNum -> Log.e(TAG, "accept: recordRunnable" ), throwable -> Log.e(TAG, "accept: recordRunnable"+throwable.toString() ));

    }


    // 设置播放文件
    private void setPath(String path) throws Exception {
        File file = new File(path);
        mDis = new DataInputStream(new FileInputStream(file));
    }


    public void startPlay(String path, PlayListener playListener) {
        try {
            this.playListener=playListener;
            setPath(path);
            startThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopPlay() {
        isStart=false;
        try {
            if (mAudioTrack != null) {
                if (mAudioTrack.getState() == AudioRecord.STATE_INITIALIZED) {  // 初始化成功
                    mAudioTrack.stop();  // 停止播放
                    mAudioTrack.release();  // 释放audioTrack资源
                }
            }
            if(playListener!=null) {
                playListener.stop();
            }
            if (mDis != null) {
                mDis.close();//关闭数据输入流
            }
            destroyThread();//销毁线程
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isStart() {
        return isStart;
    }


    public interface PlayListener{
        void start();
        void stop();
    }
}
