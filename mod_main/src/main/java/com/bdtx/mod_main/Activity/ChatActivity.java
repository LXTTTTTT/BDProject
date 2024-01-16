package com.bdtx.mod_main.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bdtx.mod_data.Database.Entity.Contact;
import com.bdtx.mod_data.Database.Entity.Message;
import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_data.ViewModel.CommunicationVM;
import com.bdtx.mod_main.Adapter.ChatListAdapter;
import com.bdtx.mod_main.Base.BaseMVVMActivity;
import com.bdtx.mod_main.databinding.ActivityChatBinding;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class ChatActivity extends BaseMVVMActivity<ActivityChatBinding, CommunicationVM> {

    public ChatActivity(){}
    public ChatActivity(boolean global_model) {super(false);}  // 声明全局 viewModel

    String target_number = "";  // 目标卡号
    private ChatListAdapter chatListAdapter;

    @Override public void beforeSetLayout() {}

    public static void start(Context context,String card_id){
        Intent intent = new Intent(context,ChatActivity.class);
        intent.putExtra(Constant.CONTACT_ID,card_id);
        context.startActivity(intent);
    }


    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        target_number = getIntent().getStringExtra(Constant.CONTACT_ID);loge("设置目标卡号："+target_number);
        init_chat_list();
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.getMessage(target_number).observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                chatListAdapter.setData(messages);
            }
        });
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
