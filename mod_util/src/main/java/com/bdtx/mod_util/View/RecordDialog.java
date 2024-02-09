package com.bdtx.mod_util.View;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bdtx.mod_data.Global.Variable;
import com.bdtx.mod_data.ViewModel.MainVM;
import com.bdtx.mod_util.R;
import com.bdtx.mod_util.Utils.ApplicationUtils;
import com.bdtx.mod_util.Utils.AudioRecordUtils;
import com.bdtx.mod_util.Utils.DataUtils;
import com.bdtx.mod_util.Utils.FileUtils;
import com.bdtx.mod_util.Utils.GlobalControlUtils;
import com.pancoit.compression.ZDCompression;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class RecordDialog extends Dialog{

    private String TAG = "RecordDialog";
    private AnimationDrawable anim;
    private ImageView voiceAnim;
    private TextView recordCount;
    private LinearLayout microphoneBg;
    private Drawable cancelRecordingBg, recordingBg;
    private int seconds = 0;  // 要传出去录音的时长
    private long countDownMillions = 0;
    private String audioFilePath;

    private int max=5;  // 最长录音时长，初始化时根据卡的等级决定
    float x1,x2,y1,y2 = 0;

    public RecordDialog(Context context) {
        super(context);
        cancelRecordingBg = context.getDrawable(R.drawable.microphone_bg_2);
        recordingBg = context.getDrawable(R.drawable.microphone_bg_1);
        // 初始化最大录音长度
        int compression_rate = Variable.getCompressRate(); // 设置的语音压缩模式，默认是自动压缩
        int card_level = ApplicationUtils.INSTANCE.getGlobalViewModel(MainVM.class).getDeviceCardLevel().getValue();
        max = getMaxTimeLimit(compression_rate,card_level);
    }

    // 计时器
    public CountDownTimer countDownTimer = null;
    public void startTimer(){
        stopTimer();
        countDownTimer = new CountDownTimer(max*1000,1000) {
            @Override
            public void onTick(long l) {
                countDownMillions = max*1000L - l;
                Log.e(TAG, "倒计时剩余: "+l);
                seconds = max - (int)(l/1000);
                recordCount.setText(""+seconds);  // 在主线程，可以直接更新ui
            }

            @Override
            public void onFinish() {
                Log.e(TAG, "录音时长达到最大限制" );
                stopRecord();  // 停止录音
                if(listener!=null){listener.onSend(audioFilePath,seconds);}  // 把录音文件路径、录音时长传出去
                seconds = 0;countDownTimer=null;
                dismiss();
            }
        };
        countDownTimer.start();
    }
    public void stopTimer(){
        if(countDownTimer!=null){
            countDownTimer.cancel();
            countDownTimer=null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_record);
        setCanceledOnTouchOutside(true);
        setCancelable(true);  // 不可取消
        Window window = getWindow();
        if(window != null){
            window.setGravity(Gravity.BOTTOM);  // 底部出现
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);  // 设置横向全屏
        }
    }

    // 打开时
    @Override
    public void show() {
        super.show();
        initView();
    }

    // dialog 消失时就结束计时
    @Override
    public void dismiss(){
        stopTimer();
        super.dismiss();
    }


    private void initView(){
        voiceAnim=findViewById(R.id.voiceAnim);  // 动画效果
        microphoneBg=findViewById(R.id.microphoneBg);  // 背景
        recordCount=findViewById(R.id.record_count);
        anim = (AnimationDrawable) voiceAnim.getBackground();
        anim.start();
    }

    public int getMaxTimeLimit(int compressionRate, int cardLevel) {
        Map<Integer, Map<Integer, Integer>> maxTimeLimits = new HashMap<>();
        // 设置不同压缩码率和卡级别对应的最大时间限制
        maxTimeLimits.put(666, createCardLevelMap(30, 17, 8,3));
        maxTimeLimits.put(ZDCompression.bitRate_450, createCardLevelMap(30, 17, 8,2));
        maxTimeLimits.put(ZDCompression.bitRate_700, createCardLevelMap(19, 11, 5,2));
        maxTimeLimits.put(ZDCompression.bitRate_1200, createCardLevelMap(11, 6, 3,2));
        maxTimeLimits.put(ZDCompression.bitRate_2400, createCardLevelMap(5, 3, 1,2));
        // 检查是否存在对应的压缩码率和卡级别，如果存在则返回最大时间限制，否则返回默认值
        int maxLimits = maxTimeLimits.containsKey(compressionRate) ? maxTimeLimits.get(compressionRate).getOrDefault(cardLevel, 0) : 0;
        Log.e(TAG, "压缩率: "+compressionRate+"/卡等级: "+cardLevel+"/最大长度限制: "+maxLimits);
        return maxLimits;
    }

    private Map<Integer, Integer> createCardLevelMap(int level5, int level4, int level3, int level2) {
        Map<Integer, Integer> cardLevelMap = new HashMap<>();
        cardLevelMap.put(5, level5);
        cardLevelMap.put(4, level4);
        cardLevelMap.put(3, level3);
        cardLevelMap.put(2, level2);
        return cardLevelMap;
    }

    // 开始录音，音频文件在这一步创建
    public void startRecord(){
        audioFilePath = FileUtils.getAudioPcmFile() + DataUtils.getTimeSerial() + "_send.pcm";  // 保存音频文件路径
        AudioRecordUtils.getInstance().startPCM(audioFilePath);  // 开始录音并保存文件
    }

    // 停止录制，资源释放
    public void stopRecord(){
        AudioRecordUtils.getInstance().stop();
    }


    public void dialogTouch(MotionEvent motionEvent){
        if(max==0){Log.e(TAG, "无最大长度限制");return;}
        int action = motionEvent.getAction();
        // 按下时
        if (action == MotionEvent.ACTION_DOWN) {
            // 拿到按下去时的 x，y 坐标
            x1 = motionEvent.getX();
            y1 = motionEvent.getY();
            startTimer();  // 开始计时
            startRecord();  // 开始录音
        }
        // 抬起时
        else if (action == MotionEvent.ACTION_UP) {
            // 拿到抬起时的 x，y 坐标
            x2 = motionEvent.getX();
            y2 = motionEvent.getY();
            stopRecord();  // 结束录音
            stopTimer();  // 停止计时
            // 手指向上移动的距离 大于150 时取消
            if(y1 - y2 > 150) {
                File file=new File(audioFilePath);  // 取消录音，删除文件
                if(file.exists()){
                    file.delete();
                }
            }
            // 录音结束
            else{
                if(countDownMillions<1000){
                    GlobalControlUtils.INSTANCE.showToast("录音时间太短",0);
                    File file=new File(audioFilePath);  // 取消录音，删除文件
                    if(file.exists()){
                        file.delete();
                    }
                }
                else {if(listener!=null){listener.onSend(audioFilePath,seconds);}}
            }
            dismiss();
        }

        // 移动时，这里只做录音背景的颜色变化操作
        else if(action== MotionEvent.ACTION_MOVE){
            x2 = motionEvent.getX();
            y2 = motionEvent.getY();
            if(y1 - y2 > 150) {
                microphoneBg.setBackground(cancelRecordingBg);
            }else{
                microphoneBg.setBackground(recordingBg);
            }
        }
    }


// 接口 ----------------------------------
    public OnCloseListener listener;
    public void setOnCloseListener(OnCloseListener listener){this.listener = listener;}
    public interface OnCloseListener{
        // 把录音之后生成的 音频文件、音频长度传出去，在外面压缩/发送
        void onSend(String file, int seconds);
    }

}
