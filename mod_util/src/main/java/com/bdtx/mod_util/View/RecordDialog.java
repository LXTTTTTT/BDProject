package com.bdtx.mod_util.View;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bdtx.mod_data.Global.Variable;
import com.bdtx.mod_data.ViewModel.MainVM;
import com.bdtx.mod_util.R;
import com.bdtx.mod_util.Utils.ApplicationUtils;
import com.bdtx.mod_util.Utils.AudioRecordUtils;
import com.bdtx.mod_util.Utils.AudioTrackUtils;
import com.bdtx.mod_util.Utils.DataUtils;
import com.bdtx.mod_util.Utils.FileUtils;
import com.pancoit.compression.ZDCompression;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class RecordDialog extends Dialog{
    public String TAG = "VoiceDialog";
    public Context mContext;
    public AnimationDrawable anim;
    public ImageView voiceAnim;
    public LinearLayout microphoneBg;
    public Drawable cancelSendingBg;
    public Drawable sendBg;
    public int voiceBtnBgHeight = 0;
    int index = -1;  // 计时器用的录音时长
    public int seconds=0;  // 要传出去录音的时长
    private MediaRecorder mr = null;
    private String suffix=".pcm";
    public String audioFilePath;

    private int max=30;  // 最长录音时长，初始化时根据卡的等级决定
    private int progress=12;  // 进度条，这里没用到
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1){

            }
            // 录音时间达到最大值
            else if(msg.what==2){
                stopRecord();  // 停止录音
                listener.onClick(RecordDialog.this,audioFilePath,seconds);  // 把录音文件路径、录音时长传出去
            }
        }

    };

// 计时器
    private  Timer timer = new Timer();
    public void startTimer(){
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();

                index+=1;  // 计时 +1
                seconds=index;

                // 当录音时间达到最大值时
                if(index==max){
//                    index=0;  // 结束计时任务
                    index=-1;  // 结束计时任务
                    timer.cancel();
                    timer=null;
                    message.what = 2;  // 发送达到最大录音消息
                }else{
                    message.what = 1;
                    message.arg1=index;
                }
                handler.sendMessage(message);
            }
        }, 0, 1000);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_record);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
        Window window = getWindow();
        if(window != null){
            window.setGravity(Gravity.BOTTOM);  // 底部出现
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);  // 设置横向全屏
        }
    }
    public RecordDialog(Context context, OnCloseListener listener) {
        super(context);
        this.mContext = context;
        this.listener = listener;
        cancelSendingBg= mContext.getDrawable(R.drawable.microphone_bg_2);
        sendBg= mContext.getDrawable( R.drawable.microphone_bg_1);
        init_max();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView(){
        voiceAnim=findViewById(R.id.voiceAnim);
        microphoneBg=findViewById(R.id.microphoneBg);
        anim = (AnimationDrawable) voiceAnim.getBackground();
        anim.start();
        //voiceBtnBgHeight=voiceBtnBg.getHeight();
        voiceBtnBgHeight=120;
    }

// 接口 ----------------------------------
    public OnCloseListener listener;
    public interface OnCloseListener{
        // 把录音之后生成的 音频文件、音频长度传出去，在外面压缩/发送
        void onClick(Dialog dialog, String file, int seconds);
    }

// 初始化录音时间限制
    public void init_max(){
        // 根据 卡的等级 和 设置好的压缩码率 决定录音时间限制
        int compression_rate = Variable.getCompressRate(); // 设置的语音压缩模式，默认是自动压缩
        int card_level = ApplicationUtils.INSTANCE.getGlobalViewModel(MainVM.class).getDeviceCardLevel().getValue();
        Log.e(TAG + "卡的等级:","" + card_level + " 设置的压缩率：" + compression_rate);
        // 自动压缩率模式 或 450 ：直接按照最大长度设置
        if(compression_rate == 666 || compression_rate == ZDCompression.bitRate_450){
            if(card_level==5){  // 5级卡 在450码率下最多 31 秒
                max=30;
                progress=11;
            }else if(card_level==4){  // 4级卡 在450码率下最多 17.7 秒
                max=17;
                progress=22;
            }else if(card_level==3){  // 3级卡 在450码率下最多 8.6 秒
                max=8;
                progress=45;
            }
        }
        // 700 压缩码率
        else if (compression_rate == ZDCompression.bitRate_700){
            if(card_level==5){  // 5级卡 在700码率下最多 20.0 秒
                max=19;
            }else if(card_level==4){  // 4级卡 在700码率下最多 11.4 秒
                max=11;
            }else if(card_level==3){  // 3级卡 在700码率下最多 5.5 秒
                max=5;
            }
        }
        // 1200 压缩码率
        else if (compression_rate == ZDCompression.bitRate_1200){
            if(card_level==5){  // 5级卡 在700码率下最多 11.7 秒
                max=11;
            }else if(card_level==4){  // 4级卡 在700码率下最多 6.6 秒
                max=6;
            }else if(card_level==3){  // 3级卡 在700码率下最多 3.2 秒
                max=3;
            }
        }
        // 2400 压缩码率
        else if (compression_rate == ZDCompression.bitRate_2400){
            if(card_level==5){  // 5级卡 在700码率下最多 5.8 秒
                max=5;
            }else if(card_level==4){  // 4级卡 在700码率下最多 3.3 秒
                max=3;
            }else if(card_level==3){  // 3级卡 在700码率下最多 1.6 秒
                max=1;
            }
        }

    }

// 开始录音，音频文件是在这一步创建的
    public void startRecord(){
        audioFilePath= FileUtils.getAudioPcmFile() + DataUtils.getTimeSerial() + "_send.pcm";  // 保存音频文件路径
        AudioRecordUtils.getInstance().init();
        AudioRecordUtils.getInstance().startPCM(audioFilePath);  // 开始录音并保存文件
    }

    //停止录制，资源释放
    public void stopRecord(){
        AudioRecordUtils.getInstance().stop();
    }

    // dialog 消失时就结束计时
    @Override
    public void dismiss(){
        super.dismiss();
        stopTimer();
    }

// 停止计时，初始化秒数，取消计时线程
    public void stopTimer(){
//        index=0;
        index=-1;  // 结束计时任务
       /**/
        if(timer!=null) {
            timer.cancel();
            timer = null;
        }
    }

    // 打开时
    @Override
    public void show() {
        super.show();
        initView();
    }

    public void dialogTouch(MotionEvent motionEvent){
        int action = motionEvent.getAction();

        // 按下时
        if (action == MotionEvent.ACTION_DOWN) {
            // 拿到按下去时的 x，y 坐标
            x1 = motionEvent.getX();
            y1 = motionEvent.getY();
//            myProgressBar.setDrawable(voiceDownBtnDrawable);
//            voiceBtnBg.setBackground(voiceDownBgDrawable);

            startRecord();  // 开始录音
            startTimer();  // 开始计时
        }

        // 抬起时
        else if (action == MotionEvent.ACTION_UP) {
            // 拿到抬起时的 x，y 坐标
            x2 = motionEvent.getX();
            y2 = motionEvent.getY();

            stopRecord();  // 结束录音
            stopTimer();  // 停止计时

        // 这里 y1 - y2 的值就是手指移动多少距离触发取消
            // 手指向上移动的距离 大于150 时取消
            if(y1 - y2 > 150) {
                microphoneBg.setBackground(sendBg);  // 修改录音背景颜色
                File file=new File(audioFilePath);  // 取消录音，删除文件
                if(file.exists()){
                    file.delete();
                }
                dismiss();
            }
            // 录音结束
            else{
                listener.onClick(this,audioFilePath,seconds);  // 接口把录音文件的路径传出去
            }
//            myProgressBar.setDrawable(voiceUpBtnDrawable);
//            voiceBtnBg.setBackground(voiceUpBgDrawable);
        }

        // 移动时，这里只做录音背景的颜色变化操作
        else if(action== MotionEvent.ACTION_MOVE){
            x2 = motionEvent.getX();
            y2 = motionEvent.getY();
            if(y1 - y2 > 150) {
                microphoneBg.setBackground(cancelSendingBg);
            }else{
                microphoneBg.setBackground(sendBg);
            }
        }
    }
}
