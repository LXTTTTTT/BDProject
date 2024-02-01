package com.bdtx.mod_main.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_data.Global.Variable;
import com.bdtx.mod_main.Base.BaseViewBindingActivity;
import com.bdtx.mod_main.databinding.ActivityCompressionRateBinding;
import com.bdtx.mod_util.Util.GlobalControlUtil;
import com.bdtx.mod_util.Util.MMKVUtil;
import com.pancoit.compression.ZDCompression;
import com.tencent.mmkv.MMKV;

@Route(path = Constant.COMPRESSION_RATE_ACTIVITY)
public class CompressionRateActivity extends BaseViewBindingActivity<ActivityCompressionRateBinding> {

    ImageView select_img;  // 单选图片
    TextView select_text;  // 单选文字
    int change_rate = 666;  // 需要改变的码率

    @Override public void beforeSetLayout() {}

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        select_img = viewBinding.autoChecked;
        select_text = viewBinding.textAuto;
        change_rate = Variable.getCompressRate();
        Log.e("当前码率模式是：",""+change_rate);
        // 初始化默认选中项，没用 600 和 800 码率，手表界面小这几个可以满足需求
        switch (change_rate){
            case 666:
                Log.e("点击了自动", "initView: ");
                selectImage(viewBinding.autoChecked);
                selectText(viewBinding.textAuto);
                break;
            case ZDCompression.bitRate_450:
                selectImage(viewBinding.checked1);
                selectText(viewBinding.text1);
                break;
            case ZDCompression.bitRate_700:
                selectImage(viewBinding.checked2);
                selectText(viewBinding.text2);
                break;
            case ZDCompression.bitRate_1200:
                selectImage(viewBinding.checked3);
                selectText(viewBinding.text3);
                break;
            case ZDCompression.bitRate_2400:
                selectImage(viewBinding.checked4);
                selectText(viewBinding.text4);
                break;
        }
        // 初始化按键
        viewBinding.autoGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(viewBinding.autoChecked);
                selectText(viewBinding.textAuto);
                change_rate = 666;  // 自动码率
            }
        });

        viewBinding.group1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(viewBinding.checked1);
                selectText(viewBinding.text1);
                change_rate = ZDCompression.bitRate_450;  // 450码率
            }
        });

        viewBinding.group2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(viewBinding.checked2);
                selectText(viewBinding.text2);
                change_rate = ZDCompression.bitRate_700;  // 700码率
            }
        });

        viewBinding.group3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(viewBinding.checked3);
                selectText(viewBinding.text3);
                change_rate = ZDCompression.bitRate_1200;  // 1200码率
            }
        });

        viewBinding.group4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(viewBinding.checked4);
                selectText(viewBinding.text4);
                change_rate = ZDCompression.bitRate_2400;  // 2400码率
            }
        });

        // 确定
        viewBinding.sendGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MMKVUtil.INSTANCE.put(Constant.VOICE_COMPRESSION_RATE,change_rate);
                GlobalControlUtil.INSTANCE.showToast("保存成功",0);
                finish();
            }
        });
    }

    @Override
    public void initData() {}

    // 单选图片 -----------------------------
    public void selectImage(View view){
        // 如果传入的还是上一个的话就不做处理
        if(view.getVisibility() == View.VISIBLE){
            Log.e("没有执行", "selectImage: ");
            return;
        }
        view.setVisibility(View.VISIBLE);
        select_img.setVisibility(View.INVISIBLE);
        select_img =  (ImageView) view;
        Log.e("执行", "selectImage: ");
    }

    // 单选文字 -------------------------
    public void selectText(View view){
        TextView view2 = (TextView) view;
        // 如果传入的还是上一个的话就不做处理
        if(view2.getCurrentTextColor() == Color.WHITE){
            return;
        }
        view2.setTextColor(Color.WHITE);
        select_text.setTextColor(Color.GRAY);
        select_text = view2;
    }
}
