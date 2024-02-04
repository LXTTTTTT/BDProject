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
    private AudioTrack mAudioTrack;
    private DataInputStream mDis;//播放文件的数据流
    private Thread mRecordThread;
    private boolean isStart = false;
    private volatile static AudioTrackUtils mInstance;
    private PlayListener playListener;
    //音频流类型
    private static final int mStreamType = AudioManager.STREAM_MUSIC;
    //指定采样率 （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
    private static final int mSampleRateInHz= Constant.sampleRateInHz;
    //指定捕获音频的声道数目。在AudioFormat类中指定用于此的常量
    private static final int mChannelConfig= AudioFormat.CHANNEL_CONFIGURATION_MONO; //单声道
    //指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
    //因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。
    private static final int mAudioFormat= AudioFormat.ENCODING_PCM_16BIT;
    //指定缓冲区大小。调用AudioRecord类的getMinBufferSize方法可以获得。
    private int mMinBufferSize;
    //STREAM的意思是由用户在应用程序通过write方式把数据一次一次得写到audiotrack中。这个和我们在socket中发送数据一样，
    // 应用层从某个地方获取数据，例如通过编解码得到PCM数据，然后write到audiotrack。
    private static int mMode = AudioTrack.MODE_STREAM;


    public AudioTrackUtils() {
        initData();
    }

    /**
     * 采样率，现在能够保证在所有设备上使用的采样率是44100Hz, 但是其他的采样率（22050, 16000, 11025）在一些设备上也可以使用。
     */
    public static final int SAMPLE_RATE_INHZ = 44100;

    /**
     * 声道数。CHANNEL_IN_MONO and CHANNEL_IN_STEREO. 其中CHANNEL_IN_MONO是可以保证在所有设备能够使用的。
     */
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    /**
     * 返回的音频数据的格式。 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT, and ENCODING_PCM_FLOAT.
     */
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
    private MediaCodec audioDecoder;  // 音频解码器
    MediaCodec mediaCodec;

    private void initData(){
        //根据采样率，采样精度，单双声道来得到frame的大小。
        mMinBufferSize = AudioTrack.getMinBufferSize(mSampleRateInHz,mChannelConfig, mAudioFormat);//计算最小缓冲区
        mAudioTrack = new AudioTrack(mStreamType, mSampleRateInHz,mChannelConfig, mAudioFormat,mMinBufferSize,mMode);


        try {
            audioDecoder = MediaCodec.createDecoderByType("audio/mp4a-latm");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static AudioTrackUtils getInstance() {
        if (mInstance == null) {
            synchronized (AudioTrackUtils.class) {
                if (mInstance == null) {
                    mInstance = new AudioTrackUtils();
                }
            }
        }
        return mInstance;
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

    /**
     * 启动播放线程
     */
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
                    Log.e("recordRunnable:","start"+Thread.currentThread());
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
                        //一次一次的写入pcm数据到audioTrack.由于是在子线程中进行write，快速连续点击可能主线程触发了stop或者release，导致子线程write异常：IllegalStateException: Unable to retrieve AudioTrack pointer for send_text()
                        //所以加playstate的判断
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




    private static final String TAG = "AudioTrackManager";

    /**
     * 播放线程
     */
    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Log.e("recordRunnable:","start");
                //设置线程的优先级
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                playListener.start();
                byte[] tempBuffer = new byte[mMinBufferSize];
                int readCount;
                while (mDis.available() > 0&&isStart) {
                    readCount= mDis.read(tempBuffer);
                    if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                        continue;
                    }
                    if (readCount != 0 && readCount != -1) {//一边播放一边写入语音数据
                        //判断AudioTrack未初始化，停止播放的时候释放了，状态就为STATE_UNINITIALIZED
                        if(mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED){
                            initData();
                        }
                        mAudioTrack.play();
                        mAudioTrack.write(tempBuffer, 0, readCount);
                    }
                }
                stopPlay();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Runnable recordRunnable2 = new Runnable() {
        @Override
        public void run() {
            if (mAudioTrack == null)
                return;
            try {
                Log.e("recordRunnable:","start"+Thread.currentThread());

                //设置线程的优先级
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                playListener.start();
//                byte[] tempBuffer = new byte[mMinBufferSize];
                byte[] tempBuffer = new byte[3200];
//                byte[] tempBuffer = new byte[1024];
                int readCount;
                if(mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED){
                    initData();
                }
                mAudioTrack.play();

                while (mDis.available() > 0&&isStart) {
                    readCount= mDis.read(tempBuffer);
                    if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                        continue;
                    }
//                    if (readCount != 0 && readCount != -1) {//一边播放一边写入语音数据
                        //判断AudioTrack未初始化，停止播放的时候释放了，状态就为STATE_UNINITIALIZED
//                        if(mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED){
//                            initData();
//                        }
//                        mAudioTrack.play();
                    //一次一次的写入pcm数据到audioTrack.由于是在子线程中进行write，快速连续点击可能主线程触发了stop或者release，导致子线程write异常：IllegalStateException: Unable to retrieve AudioTrack pointer for send_text()
                    //所以加playstate的判断
                    if (readCount > 0 && mAudioTrack != null && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING && mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
//                        audioTrack.send_text(buffer, 0, readCount);

                        /*AudioCodec audioCodec=AudioCodec.newInstance();
                        audioCodec.setEncodeType(MediaFormat.MIMETYPE_AUDIO_MPEG);
                        audioCodec.setIOPath(path + "/codec.aac", path + "/encode.mp3");
                        audioCodec.prepare();
                        audioCodec.startAsync();*/

                        mAudioTrack.write(
                                tempBuffer, 0,
                                readCount);
//                        decode(tempBuffer,readCount,mAudioTrack);
                    }

//                        mAudioTrack.send_text(
//                                tempBuffer, 0,
//                                readCount);
//                    }
                }
                stopPlay();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Runnable recordRunnable1 = new Runnable() {
        @Override
        public void run() {
            try {
                Log.e("recordRunnable:","start");
                //设置线程的优先级
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                playListener.start();
                try {
                    if(mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED){
                        initData();
                        mAudioTrack.play();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (mDis!=null && isStart) {
                    try {
//                        if(mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED){
//                            initData();
//                        }
//                        if(getBuffer(mMinBufferSize) == null){
//                            return;
//                        }

                        while (mDis.available()>0){
                            byte[] tempBuffer = new byte[mMinBufferSize];
//                            byte[] data = getBuffer(mMinBufferSize);
//                            mAudioTrack.send_text(data, 0, data.length);

                            int size = mDis.read(tempBuffer);
                            mAudioTrack.write(tempBuffer, 0, size);//不断播放数据
                        }


                    } catch (Exception e) {
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void decode(byte[] buf, int length, AudioTrack audioTrack) {
        //输入ByteBuffer
        ByteBuffer[] codecInputBuffers = audioDecoder.getInputBuffers();
        //输出ByteBuffer
        ByteBuffer[] codecOutputBuffers = audioDecoder.getOutputBuffers();
        //等待时间
        long kTimeOutUs = 1000;
        try {
            //返回一个包含有效数据的input buffer的index,-1->不存在
            int inputBufIndex = audioDecoder.dequeueInputBuffer(kTimeOutUs);
            if (inputBufIndex >= 0) {
                //获取当前的ByteBuffer
                ByteBuffer currentBuf = codecInputBuffers[inputBufIndex];
                currentBuf.clear();
                currentBuf.put(buf, 0, length);
                //将指定index的input buffer提交给解码器
                audioDecoder.queueInputBuffer(inputBufIndex, 0, length, getPTSUs(), 0);
            }
            //编解码器缓冲区
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            int outputBufferIndex = audioDecoder.dequeueOutputBuffer(info, kTimeOutUs);
            ByteBuffer outputBuffer;
            while (outputBufferIndex >= 0) {
                //获取解码后的ByteBuffer
                outputBuffer = codecOutputBuffers[outputBufferIndex];
                //用来保存解码后的数据
                byte[] outData = new byte[info.size];
                outputBuffer.get(outData);
                //清空缓存
                outputBuffer.clear();

                //播放解码后的数据
                audioTrack.write(outData, 0, outData.length);
                //释放已经解码的buffer
                audioDecoder.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = audioDecoder.dequeueOutputBuffer(info, kTimeOutUs);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }


    /**
     * 获取当前的时间戳
     *
     * @return
     */
    final static int prevPresentationTimes = 1000;
    private long getPTSUs() {
        long result = System.nanoTime() / 1000;
        if (result < prevPresentationTimes) {
            result = (prevPresentationTimes - result) + result;
        }
        return result;
    }


    public void setOutputStream(DataInputStream path) {
//        File file = new File(path);
        mDis = path;
    }


    /**
     * 播放文件
     * @param path
     * @throws Exception
     */
    private void setPath(String path) throws Exception {
        File file = new File(path);
        mDis = new DataInputStream(new FileInputStream(file));
    }

    /**
     * 启动播放
     *
     * @param path
     */
    public void startPlay(String path, PlayListener playListener) {
        try {
            this.playListener=playListener;
            setPath(path);
            startThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void startPlay(DataInputStream dataInputStream, PlayListener playListener) {
        try {
            this.playListener=playListener;
//            setPath(path);
            mDis = dataInputStream;
            startThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        isStart=false;
        try {
            if (mAudioTrack != null) {
                if (mAudioTrack.getState() == AudioRecord.STATE_INITIALIZED) {//初始化成功
                    mAudioTrack.stop();//停止播放
                    mAudioTrack.release();//释放audioTrack资源
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
