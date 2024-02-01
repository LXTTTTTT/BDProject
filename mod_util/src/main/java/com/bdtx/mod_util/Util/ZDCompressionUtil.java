package com.bdtx.mod_util.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bdtx.mod_data.EventBus.AuthMsg;
import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_util.View.AuthInfoDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.pancoit.compression.CompressionInterface;
import com.pancoit.compression.ZDCompression;
import com.tencent.mmkv.MMKV;

import org.greenrobot.eventbus.EventBus;


// 授权信息管理类
public class ZDCompressionUtil {

    private final String TAG = "ZDCompressionUtil";
    public AuthInfoDialog authInfoDialog;  // 激活弹窗
    public static final int SCAN_ACTIVITY_CODE = 201;
    public String voice_key = "";
    public String picture_key = "";

// 单例 --------------------------------------------------------
    private static ZDCompressionUtil zdCompressionUtil;
    public static ZDCompressionUtil getInstance(){
        if(zdCompressionUtil==null){
            zdCompressionUtil=new ZDCompressionUtil();
        }
        return zdCompressionUtil;
    }

    public boolean isVoiceOnline(){
        String voKey= MMKVUtil.INSTANCE.getString(Constant.VO_ONLINE_ACTIVATION_KEY,"");
        return !"".equals(voKey);
    }
    public void saveVoiceKey(String str){
        MMKVUtil.INSTANCE.put(Constant.VO_ONLINE_ACTIVATION_KEY,str);
    }
    public boolean isPicOnline(){
        String picKey= MMKVUtil.INSTANCE.getString(Constant.PIC_ONLINE_ACTIVATION_KEY,"");
        return !"".equals(picKey);
    }
    public void savePicKey(String str){
        MMKVUtil.INSTANCE.put(Constant.PIC_ONLINE_ACTIVATION_KEY,str);
    }

    public void initZipSdk(){
        String voKey = MMKVUtil.INSTANCE.getString(Constant.VO_ONLINE_ACTIVATION_KEY,"");
        if("".equals(voKey)){
            ZDCompression.getInstance().off_voice_init(ApplicationUtil.INSTANCE.getApplication(),Constant.VO_OFF_ACTIVATION_VALUE,offlineCompressionInterface);
        }else{
            ZDCompression.getInstance().initVoiceZip(ApplicationUtil.INSTANCE.getApplication(),voKey,onlineCompressionInterface);
        }

        String picKey = MMKVUtil.INSTANCE.getString(Constant.PIC_ONLINE_ACTIVATION_KEY,"");
        if("".equals(picKey)){
            ZDCompression.getInstance().off_img_init(ApplicationUtil.INSTANCE.getApplication(),Constant.PIC_OFF_ACTIVATION_VALUE,offlineCompressionInterface);
        }else{
            ZDCompression.getInstance().initImgZip(ApplicationUtil.INSTANCE.getApplication(),picKey,onlineCompressionInterface);
        }
    }



    public void showAuthDialog(Activity activity){
        hideAuthDialog();
        authInfoDialog = new AuthInfoDialog(activity, "压缩库激活", "请输入授权码激活压缩库，无授权码请联系商务人员", new AuthInfoDialog.OnItemClickListener() {
            @Override
            public void onOK(String str) {
                GlobalControlUtil.INSTANCE.showLoadingDialog("正在激活中");
                voice_key = str;
//                ZDCompression.getInstance().initVoiceZip(ApplicationUtil.INSTANCE.getApplication(),str,onlineCompressionInterface);
                // 模拟
                if(str.equals("00000")){
                    GlobalControlUtil.INSTANCE.showToast("初始化成功",0);
                    hideAuthDialog();
                    EventBus.getDefault().post(new AuthMsg(AuthMsg.AUTH_SUCCESS));
                    saveVoiceKey(voice_key);
                }else {
                    GlobalControlUtil.INSTANCE.showToast("初始化失败！",Toast.LENGTH_SHORT);
                }
                GlobalControlUtil.INSTANCE.hideLoadingDialog();
            }

            @Override
            public void onScanning() {
                // 开启扫码摄像头
                initScan(activity,SCAN_ACTIVITY_CODE);
            }
        });
        authInfoDialog.show();
    }

    public void hideAuthDialog(){
        if(authInfoDialog!=null){
            authInfoDialog.dismiss();
            authInfoDialog = null;
        }
    }

    public void setDialogKey(String key){
        if(authInfoDialog!=null){
            authInfoDialog.setKey(key);
        }
    }


    public void initScan(Activity activity,int code) {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        // 设置要扫描的条码类型，ONE_D_CODE_TYPES：一维码，QR_CODE_TYPES-二维码
        integrator.setDesiredBarcodeFormats();
        integrator.setCaptureActivity(CaptureActivity.class); //设置打开摄像头的Activity
        integrator.setRequestCode(code);
        integrator.setPrompt("扫码二维码"); //底部的提示文字，设为""可以置空
        integrator.setCameraId(0); //前置或者后置摄像头
        integrator.setBeepEnabled(false); //扫描成功的「哔哔」声，默认开启
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }


// 离线和在线的回调要分开设置 ------------------------------------------
    public CompressionInterface offlineCompressionInterface = new CompressionInterface() {
        @Override
        public void initCallback(int code) {
            Log.e("压缩库离线初始化状态码:",""+code);
            if(code!=20000){GlobalControlUtil.INSTANCE.showToast("压缩库初始化失败！:"+code,Toast.LENGTH_SHORT);}
        }
        @Override
        public void zipCallback(int code) {
            Log.e("压缩状态码",":"+code);
            if(code!=20000){GlobalControlUtil.INSTANCE.showToast("压缩失败！:"+code,Toast.LENGTH_SHORT);}
        }
    };
    public CompressionInterface onlineCompressionInterface = new CompressionInterface() {
        @Override
        public void initCallback(int code) {
            Log.e("压缩库初始化状态码:",""+code);
            if(code==20000){
                GlobalControlUtil.INSTANCE.showToast("初始化成功",0);
                hideAuthDialog();
                EventBus.getDefault().post(new AuthMsg(AuthMsg.AUTH_SUCCESS));
                // 成功了，保存 key
                saveVoiceKey(voice_key);
            }
            else {
                voice_key = "";
                GlobalControlUtil.INSTANCE.showToast("缩库初始化失败！:"+code,Toast.LENGTH_SHORT);
            }
            GlobalControlUtil.INSTANCE.hideLoadingDialog();
        }
        @Override
        public void zipCallback(int code) {
            Log.e("压缩状态码",":"+code);
            if(code!=20000){GlobalControlUtil.INSTANCE.showToast("压缩失败！:"+code,Toast.LENGTH_SHORT);}
        }
    };

}
