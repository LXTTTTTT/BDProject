package com.bdtx.mod_main.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bdtx.mod_data.Database.Entity.Message;
import com.bdtx.mod_data.Global.Constant;
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

import java.util.ArrayList;
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
    private MainVM mainVM;

    @Override public void beforeSetLayout() {}

    public static void start(Context context,String card_id){
        Intent intent = new Intent(context,ChatActivity.class);
        intent.putExtra(Constant.CONTACT_ID,card_id);
        context.startActivity(intent);
    }


    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        target_number = getIntent().getStringExtra(Constant.CONTACT_ID);
        loge("进入聊天："+target_number);
        if(target_number.equals("")){globalControl.showToast("页面出错了",0);finish();}
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
        chatListAdapter.setOnItemClickListener(new Function2<View, Integer, Unit>() {
            @Override
            public Unit invoke(View view, Integer position) {
                loge("点击了 "+position);
                return null;
            }
        });
        viewBinding.chatList.setLayoutManager(new LinearLayoutManager(my_context,LinearLayoutManager.VERTICAL,false));
        viewBinding.chatList.setAdapter(chatListAdapter);
    }

    public void init_swift_list(){
        // 初始化数据
        swiftMessages.add("快捷1");
        swiftMessages.add("快捷2");
        swiftMessages.add("快捷3");
        swiftMessages.add("快捷4");
        swiftMessages.add("快捷5");

        swiftListAdapter = new SwiftListAdapter();
        swiftListAdapter.setOnItemClickListener(new Function2<View, Integer, Unit>() {
            @Override
            public Unit invoke(View view, Integer position) {
                globalControl.showToast("点击 "+swiftMessages.get(position),0);
                viewBinding.swiftList.startAnimation(translateAniHide);
                return null;
            }
        });
        swiftListAdapter.setData(swiftMessages);
        viewBinding.swiftList.setLayoutManager(new LinearLayoutManager(my_context,LinearLayoutManager.VERTICAL,false));
        viewBinding.swiftList.setAdapter(swiftListAdapter);

    }

    public void init_control(){
        // 设置标题
        if(target_number.equals(Constant.platform_identifier)){setTitle("指挥中心");}else {setTitle(target_number);}
        // 发送文本按键
        viewBinding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = viewBinding.content.getText().toString();
                if(content.isEmpty()) {globalControl.showToast("请输入内容消息",0);return;}
                loge("发送消息");
                sendMessageUtils.send_text(target_number,content);
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

    }

    // 数据变化监听
    public void init_view_model(){
        mainVM = ApplicationUtils.INSTANCE.getGlobalViewModel(MainVM.class);
        // 消息变化监听
        viewModel.getMessage(target_number).observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                chatListAdapter.setData(messages);
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
        viewBinding.send.setBackgroundResource(R.drawable.corner_fill_blue_1_ripple);
        viewBinding.send.setText("发送");
        // 发送语音按键
        viewBinding.voiceButton.setSelected(true);
        viewBinding.voiceButton.setText("按住说话");
    }

    private void disableSendGroup(String tips){
        // 发送文字按键
        viewBinding.send.setClickable(false);
        viewBinding.send.setBackgroundResource(R.drawable.corner_fill_gray_3);
        viewBinding.send.setText(tips);
        // 发送语音按键
        viewBinding.voiceButton.setSelected(false);
        viewBinding.voiceButton.setText(tips);
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
