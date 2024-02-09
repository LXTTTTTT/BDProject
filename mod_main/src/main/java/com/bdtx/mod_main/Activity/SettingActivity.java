package com.bdtx.mod_main.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableLinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_main.Adapter.SettingListAdapter;
import com.bdtx.mod_main.Base.BaseViewBindingActivity;
import com.bdtx.mod_main.R;
import com.bdtx.mod_main.databinding.ActivitySettingBinding;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

@Route(path = Constant.SETTING_ACTIVITY)
public class SettingActivity extends BaseViewBindingActivity<ActivitySettingBinding> {

    List<List<Object>> settings = new ArrayList<>();  // 列表数据

    private SettingListAdapter settingListAdapter;

    @Override public void beforeSetLayout() {}

    @Override public void initView(@Nullable Bundle savedInstanceState) {
        setTitle("设置");
        settingListAdapter = new SettingListAdapter();
        settingListAdapter.setOnItemClickListener(new Function2<View, Integer, Unit>() {
            @Override
            public Unit invoke(View view, Integer position) {
                loge("点击了："+position);
                switch (position){
                    case 0:
                        ARouter.getInstance().build(Constant.VOICE_AUTH_ACTIVITY).navigation();
                        break;
                    case 1:
                        ARouter.getInstance().build(Constant.COMPRESSION_RATE_ACTIVITY).navigation();
                        break;
                    case 2:
                        ARouter.getInstance().build(Constant.PLATFORM_SETTING_ACTIVITY).navigation();
                        break;
                    case 3:
                        ARouter.getInstance().build(Constant.MESSAGE_TYPE_ACTIVITY).navigation();
                        break;
                    case 4:
                        ARouter.getInstance().build(Constant.ABOUT_US_ACTIVITY).navigation();
                        break;

                    default:break;
                }
                return null;
            }
        });

        viewBinding.settingList.setLayoutManager(new LinearLayoutManager(this));
        //第一个列表项和最后一个列表项在屏幕上垂直居中对齐
        viewBinding.settingList.setEdgeItemsCenteringEnabled(true);
        //是否可以使用圆形滚动手势，不要使用否则会手势错乱
        viewBinding.settingList.setCircularScrollingGestureEnabled(false);
        //靠近屏幕边缘的虚拟“屏幕边框”（在此区域内能够识别出手势）的宽度
        viewBinding.settingList.setBezelFraction(0.5f);
        //用户的手指必须旋转多少度才能滚过一个屏幕高度
        viewBinding.settingList.setScrollDegreesPerScreen(180f);
        viewBinding.settingList.setLayoutManager(new WearableLinearLayoutManager(this, new CustomScrollingLayoutCallback()));
        viewBinding.settingList.setAdapter(settingListAdapter);
    }

    @Override
    public void initData() {
        List<Object> a = new ArrayList<>();
        a.add(R.mipmap.voice_auth);
        a.add("语音压缩库授权");
        List<Object> c = new ArrayList<>();
        c.add(R.mipmap.voice_encode);
        c.add("北斗语音码率设置");
        List<Object> d = new ArrayList<>();
        d.add(R.mipmap.plat_setting);
        d.add("北斗通道设置");
        List<Object> e = new ArrayList<>();
        e.add(R.mipmap.msg_type_setting);
        e.add("北斗报文类型设置");
        List<Object> f = new ArrayList<>();
        f.add(R.mipmap.about_us);
        f.add("关于我们");

        settings.add(a);
        settings.add(c);
        settings.add(d);
        settings.add(e);
        settings.add(f);
        settingListAdapter.setData(settings);
    }

    public class CustomScrollingLayoutCallback extends WearableLinearLayoutManager.LayoutCallback {
        private static final float MAX_ICON_PROGRESS = 0.65f;
        @Override
        public void onLayoutFinished(View child, RecyclerView parent) {
            try {
//                float centerOffset = ((float) child.getHeight() / 2.0f) / (float) parent.getHeight();  // 写死就好
//                float yRelativeToCenterOffset = (child.getY() / parent.getHeight()) + centerOffset;
//                // 中心规格化，调整到最大刻度
//                float progressToCenter = Math.min(Math.abs(0.5f - yRelativeToCenterOffset), MAX_ICON_PROGRESS);  // 写死就好
//                // 沿着弯曲的路径，而不是三角形
//                progressToCenter = (float)(Math.cos(progressToCenter * Math.PI * 0.70f));  // 这个是列表变化时缩小的倍数，后面的数字越大缩得越小
//                child.setScaleX (progressToCenter);
//                child.setScaleY (progressToCenter);
                float childHeight = (float) child.getHeight();
                float parentHeight = (float) parent.getHeight();
                float centerOffset = childHeight / 2.0f / parentHeight;
                float yRelativeToCenterOffset = (child.getY() / parentHeight) + centerOffset;
                loge("childHeight: " + childHeight + "/parentHeight: " + parentHeight + "/yRelativeToCenterOffset: " + yRelativeToCenterOffset);
                float progressToCenter = Math.min(Math.abs(0.5f - yRelativeToCenterOffset), MAX_ICON_PROGRESS);
                // 使用余弦函数创建缩放曲线
                progressToCenter = (float) Math.cos(progressToCenter * Math.PI * 0.70f);
                // 缩放子项
                child.setScaleX(progressToCenter);
                child.setScaleY(progressToCenter);
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }
}
