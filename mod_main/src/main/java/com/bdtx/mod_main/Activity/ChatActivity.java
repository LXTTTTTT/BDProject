package com.bdtx.mod_main.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bdtx.mod_data.Database.DaoUtils;
import com.bdtx.mod_data.Database.Entity.Message;
import com.bdtx.mod_data.EventBus.BaseMsg;
import com.bdtx.mod_data.EventBus.UpdateMessageMsg;
import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_data.Global.Variable;
import com.bdtx.mod_data.ViewModel.CommunicationVM;
import com.bdtx.mod_data.ViewModel.MainVM;
import com.bdtx.mod_main.Adapter.ChatListAdapter;
import com.bdtx.mod_main.Adapter.SwiftListAdapter;
import com.bdtx.mod_main.Base.BaseMVVMActivity;
import com.bdtx.mod_main.R;
import com.bdtx.mod_main.databinding.ActivityChatBinding;
import com.bdtx.mod_util.Utils.ApplicationUtils;
import com.bdtx.mod_util.Utils.GlobalControlUtils;
import com.bdtx.mod_util.Utils.SendMessageUtils;
import com.bdtx.mod_util.View.RecordDialog;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class ChatActivity extends BaseMVVMActivity<ActivityChatBinding, CommunicationVM> {

    private final String TAG = "ChatActivity";

    public ChatActivity(){}
    public ChatActivity(boolean global_model) {super(false);}  // 声明全局 viewModel

    String target_number = "";  // 目标卡号
    private ChatListAdapter chatListAdapter;
    private SwiftListAdapter swiftListAdapter;
    private List<String> swiftMessages = new ArrayList<>();
    private GlobalControlUtils globalControl = GlobalControlUtils.INSTANCE;
    private SendMessageUtils sendMessageUtils = SendMessageUtils.INSTANCE;
    private RecordDialog recordDialog;  // 录音dialog
    private MainVM mainVM;  // 全局变量

    private Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    if(chatListAdapter.getItemCount()>1){viewBinding.chatList.smoothScrollToPosition(chatListAdapter.getItemCount() - 1);}
                    break;
            }
        }
    };
    @Override public void beforeSetLayout() {}
    @Override public boolean enableEventBus() {return true;}

    public static void start(Context context, String card_id){
        Intent intent = new Intent(context,ChatActivity.class);
        intent.putExtra(Constant.CONTACT_ID,card_id);
        context.startActivity(intent);
    }


    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        target_number = getIntent().getStringExtra(Constant.CONTACT_ID);
        loge("进入聊天："+target_number);
        if(target_number.equals("")){globalControl.showToast("页面出错了",0);finish();}
        if(target_number.equals(Constant.NEW_CHAT)){viewBinding.targetNumber.setVisibility(View.VISIBLE);}
        // 初始化动画效果
        alphaAnimation();
        scaleAnimation();
        translateAnimation();

        init_chat_list();
        init_swift_list();
        init_control();

    }

    @Override
    public void initData() {
        super.initData();
        init_view_model();
    }

    public void init_chat_list(){
        chatListAdapter = new ChatListAdapter();
        chatListAdapter.setOnMessageClick(new ChatListAdapter.OnMessageClick() {
            @Override
            public void onLocationClick(double longitude, double latitude) {
                loge("消息位置："+longitude+"/"+latitude);
                MapActivity.start(ChatActivity.this,longitude,latitude);
            }

            @Override
            public void onResendClick(@NonNull Message message) {
                // 重发消息
                loge("重发消息类型："+message.getMessageType());
            }
        });

        viewBinding.chatList.setLayoutManager(new LinearLayoutManager(my_context,LinearLayoutManager.VERTICAL,false));
        viewBinding.chatList.setAdapter(chatListAdapter);
    }

    public void init_swift_list(){
        // 初始化数据
        swiftMessages.add("我在这里，一切正常");
        swiftMessages.add("麻烦各位队友报一下自己的位置");
        swiftMessages.add("我已安全到达目的地");
        swiftMessages.add("任务已完成");
        String commands_str = Variable.getSwiftMsg();
        Log.e(TAG, "拿到自定义快捷消息: "+commands_str);
        if(!commands_str.equals("")){
            String[] commands_arr = commands_str.split(Constant.SWIFT_MESSAGE_SYMBOL);
            List<String> commands_list = Arrays.asList(commands_arr);
            swiftMessages.addAll(commands_list);
        }

        swiftListAdapter = new SwiftListAdapter();
        swiftListAdapter.setOnItemClickListener(new Function2<View, Integer, Unit>() {
            @Override
            public Unit invoke(View view, Integer position) {
                if(isAnimating){return null;}
//                globalControl.showToast("点击 "+swiftMessages.get(position),0);
                viewBinding.content.setText(swiftMessages.get(position));
                viewBinding.swiftList.setVisibility(View.GONE);
                return null;
            }
        });
        swiftListAdapter.setData(swiftMessages);
        viewBinding.swiftList.setLayoutManager(new LinearLayoutManager(my_context,LinearLayoutManager.VERTICAL,false));
        viewBinding.swiftList.setAdapter(swiftListAdapter);

    }

    public void init_control(){
        // 设置标题
        if(target_number.equals(Constant.PLATFORM_IDENTIFIER)){setTitle("指挥中心");}
        else if(target_number.equals(Constant.NEW_CHAT)){setTitle("新消息");}
        else{setTitle(target_number);}
        // 发送文本按键
        viewBinding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = viewBinding.content.getText().toString();
                if(content.isEmpty()) {globalControl.showToast("请输入内容消息",0);return;}
                loge("发送消息");
                sendMessageUtils.send_text(target_number,content,true);
                viewBinding.content.setText("");  // 清空输入栏
                // 不需要在这里刷新消息列表，发送完后会发广播
            }
        });

        // 切换语音
        viewBinding.voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewBinding.textGroup.setVisibility(View.GONE);
                viewBinding.voiceGroup.setVisibility(View.VISIBLE);
                viewBinding.swiftList.setVisibility(View.GONE);  // 隐藏快捷消息
            }
        });

        // 切换文本
        viewBinding.keyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewBinding.voiceGroup.setVisibility(View.GONE);
                viewBinding.textGroup.setVisibility(View.VISIBLE);
            }
        });

        // 快捷消息
        viewBinding.swiftMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isAnimating){return;}  // 正在加载中不做操作
                if(viewBinding.swiftList.getVisibility()==View.GONE){
                    viewBinding.swiftList.startAnimation(translateAniShow);
                    viewBinding.swiftList.setVisibility(View.VISIBLE);
                }else{
                    viewBinding.swiftList.startAnimation(translateAniHide);
                }
            }
        });

        // 解决键盘弹起时遮挡 recyclerview 问题
        viewBinding.content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){handler.sendEmptyMessageDelayed(0,250);}
            }
        });

        // 目标卡号修改监听（刷新卡号）
        viewBinding.targetNumber.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                handler.removeCallbacksAndMessages(null);  // 取消上一次
                if(editable.toString().length()<6){return;}  // 卡号太短
                // 0.5秒后修改目标卡号并获取对应的消息记录
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        target_number = editable.toString();
                        viewModel.upDateMessage(target_number);
                    }
                },500);
            }
        });

        // 初始化录音窗口
        recordDialog =new RecordDialog(my_context);
        recordDialog.setOnCloseListener(new RecordDialog.OnCloseListener() {
            @Override
            public void onSend(String file, int seconds) {
                loge("录音文件："+file+"/时长："+seconds);
                sendMessageUtils.send_voice(target_number,file,seconds);
            }
        });
        // 录音
        viewBinding.voiceButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                // 刚按下时那一瞬间，显示 dialog
                if (action == MotionEvent.ACTION_DOWN) {
                    viewBinding.voiceButton.setSelected(true);
                    if(!recordDialog.isShowing()){
                        recordDialog.show();
                    }
                }
                // 过了刚按下时的那一瞬间，这里只把 ACTION_DOWN 和 ACTION_UP 传进去
                if (recordDialog.isShowing()) {
                    recordDialog.dialogTouch(motionEvent);
                }
                // 点击事件被dialog遮挡了，手动换背景
                if (action == MotionEvent.ACTION_UP) {
                    viewBinding.voiceButton.setSelected(false);
                }
                return true;
            }
        });

    }

    // 数据变化监听
    public void init_view_model(){
        mainVM = ApplicationUtils.INSTANCE.getGlobalViewModel(MainVM.class);
        // 消息变化监听
        viewModel.getMessage(target_number).observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                chatListAdapter.setData(messages);
                handler.sendEmptyMessageDelayed(0,100);  // 滚动到底部
            }
        });

        if(mainVM!=null){
            // 设备连接监听
            mainVM.isConnectDevice().observe(this, isConnect -> {
                if(isConnect){
                    enableSendGroup();
                }
                else {
                    disableSendGroup("未连接");
                }
            });

            // 倒计时监听
            mainVM.getWaitTime().observe(this, new Observer<Integer>() {
                @Override
                public void onChanged(Integer countDown) {
                    if(!mainVM.isConnectDevice().getValue()){return;}
                    Log.i(TAG, "当前倒计时："+countDown);
                    if(countDown==0){
                        enableSendGroup();
                    } else {
                        disableSendGroup(countDown+"");
                    }
                }
            });
        }

    }


    // 开启发送功能
    private void enableSendGroup(){
        // 发送文字按键
        viewBinding.send.setClickable(true);
        viewBinding.send.setBackgroundResource(R.drawable.fill_blue_1_ripple);
        viewBinding.send.setText("发送");
        // 发送语音按键
        viewBinding.voiceButton.setEnabled(true);
        viewBinding.voiceButton.setText("按住说话");
        viewBinding.voiceButton.setBackgroundResource(R.drawable.selector_button_bg);
    }

    private void disableSendGroup(String tips){
        // 发送文字按键
        viewBinding.send.setClickable(false);
        viewBinding.send.setBackgroundResource(R.drawable.corner_fill_gray_3);
        viewBinding.send.setText(tips);
        // 发送语音按键
        viewBinding.voiceButton.setEnabled(false);
        viewBinding.voiceButton.setText(tips);
        viewBinding.voiceButton.setBackgroundResource(R.drawable.corner_fill_gray_3);
    }

    private AlphaAnimation alphaAniShow, alphaAniHide;
    private TranslateAnimation translateAniShow, translateAniHide;
    private Animation bigAnimation, smallAnimation;
    private boolean isAnimating = false;
    private void translateAnimation() {
        //向上位移显示动画  从自身位置的最下端向上滑动了自身的高度
        translateAniShow = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF,//RELATIVE_TO_SELF表示操作自身
                0,//fromXValue表示开始的X轴位置
                Animation.RELATIVE_TO_SELF,
                0,//fromXValue表示结束的X轴位置
                Animation.RELATIVE_TO_SELF,
                1,//fromXValue表示开始的Y轴位置
                Animation.RELATIVE_TO_SELF,
                0);//fromXValue表示结束的Y轴位置
        translateAniShow.setRepeatMode(Animation.REVERSE);
        translateAniShow.setDuration(1000);
        translateAniShow.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {isAnimating = true;}
            @Override public void onAnimationEnd(Animation animation) {isAnimating = false;}
            @Override public void onAnimationRepeat(Animation animation) {}
        });

        //向下位移隐藏动画  从自身位置的最上端向下滑动了自身的高度
        translateAniHide = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF,//RELATIVE_TO_SELF表示操作自身
                0,//fromXValue表示开始的X轴位置
                Animation.RELATIVE_TO_SELF,
                0,//fromXValue表示结束的X轴位置
                Animation.RELATIVE_TO_SELF,
                0,//fromXValue表示开始的Y轴位置
                Animation.RELATIVE_TO_SELF,
                1);//fromXValue表示结束的Y轴位置
        translateAniHide.setRepeatMode(Animation.REVERSE);
        translateAniHide.setDuration(1000);
        translateAniHide.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {isAnimating = true;}
            @Override
            public void onAnimationEnd(Animation animation) {
                viewBinding.swiftList.setVisibility(View.GONE);isAnimating =false;
            }
            @Override public void onAnimationRepeat(Animation animation) {}
        });
    }

    //缩放动画
    private void scaleAnimation() {
        //放大
        bigAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_big);
        //缩小
        smallAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_small);

    }

    //透明度动画
    private void alphaAnimation() {
        //显示
        alphaAniShow = new AlphaAnimation(0, 1);//百分比透明度，从0%到100%显示
        alphaAniShow.setDuration(1000);//一秒
        //隐藏
        alphaAniHide = new AlphaAnimation(1, 0);
        alphaAniHide.setDuration(1000);
    }

    // 广播事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onBroadcast(BaseMsg message){
        if(message.getType()==BaseMsg.Companion.getMSG_UPDATE_MESSAGE()){
            UpdateMessageMsg msg = (UpdateMessageMsg) message.getMessage();
            if(msg.number.equals(target_number)){
                viewModel.upDateMessage(target_number);
            }
        }
    }


// 暂时不触发这个 ------------------------------------
    @Override
    protected void onNewIntent(Intent intent) {
        loge("新 intent");
        super.onNewIntent(intent);
        if(intent!=null){
            target_number = intent.getStringExtra(Constant.CONTACT_ID);
            loge("目标卡号："+target_number);
        }
    }



}
