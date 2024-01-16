package com.bdtx.mod_util.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.bdtx.mod_util.View.AuthInfoDialog;
import com.pancoit.compression.CompressionInterface;
import com.pancoit.compression.ZDCompression;
import com.tencent.mmkv.MMKV;


// 授权信息管理类
public class ZDCompressionUtil {
    public static final String PIC_ONLINE_ACTIVATION_KEY="pic_online_activation_key";//图片在线激活key
    public static final String VO_ONLINE_ACTIVATION_KEY="vo_online_activation_key";//语音在线激活key
    public static final  String PIC_OFF_ACTIVATION_VALUE="A90A411BDBF02DBEBP";//离线图片key
    public static final  String VO_OFF_ACTIVATION_VALUE="A90A411BDBF02DBEBV";//离线语音key

    // dialog 操作对象 -------------------------
    public AuthInfoDialog authPicInfoDialog;
    public AuthInfoDialog authVoiceInfoDialog;
    public AuthInfoDialog authPicInfoDialog_normal;
    public AuthInfoDialog authVoiceInfoDialog_normal;

    public static final int PIC_SC_CODE=20010;
    public static final int VO_SC_CODE=20011;

    public Context mContext;

    @SuppressLint("StaticFieldLeak")
    private static ZDCompressionUtil zdCompressionUtil;
    public static ZDCompressionUtil getInstance(){
        if(zdCompressionUtil==null){
            zdCompressionUtil=new ZDCompressionUtil();
        }
        return zdCompressionUtil;
    }

    private ZDCompressionUtil(){}

    public void init(Context context){
        this.mContext=context;
    }
    public boolean isPicOnline(){
        String picKey= MMKV.defaultMMKV().decodeString(PIC_ONLINE_ACTIVATION_KEY,"");
        return !"".equals(picKey);
    }
    public boolean isVoiceOnline(){
        String voKey=MMKV.defaultMMKV().decodeString(VO_ONLINE_ACTIVATION_KEY,"");
        return !"".equals(voKey);
    }
    public void initVoOnline(String str){
        MMKV.defaultMMKV().encode(VO_ONLINE_ACTIVATION_KEY,str);
    }
    public void initPicOnline(String str){
        MMKV.defaultMMKV().encode(PIC_ONLINE_ACTIVATION_KEY,str);
    }

    public void initZipSdk(){
        String picKey=MMKV.defaultMMKV().decodeString(PIC_ONLINE_ACTIVATION_KEY,"");
        if("".equals(picKey)){
            ZDCompression.getInstance().off_img_init(mContext,PIC_OFF_ACTIVATION_VALUE,picCOInterface);
        }else{
            ZDCompression.getInstance().initImgZip(mContext,picKey,picCOInterface);
        }
        String voKey=MMKV.defaultMMKV().decodeString(VO_ONLINE_ACTIVATION_KEY,"");
        if("".equals(voKey)){
            ZDCompression.getInstance().off_voice_init(mContext,VO_OFF_ACTIVATION_VALUE,voCOInterface);
        }else{
            ZDCompression.getInstance().initVoiceZip(mContext,voKey,voCOInterface);
        }
    }
// 这里展示的是 过期 dialog ------------------------------ mainactivity 里面会自动展示，不用处理
    public void showDialogType(boolean isPic){
        String str;
        if(isPic){
            str=ZDCompression.getInstance().getImgOffInfo();
        }else{
            str=ZDCompression.getInstance().getVOffInfo();
        }
        if(str==null||"".equals(str)){
            return;
        }
        String[] array = str.split(",");
        int usageCount=Integer.parseInt(array[0]);
//        String startDate= DataUtil.getTimeSecond();
        String endDate=array[2];
//        int code= DateUtils.dataContrast(startDate, endDate);
        if (code==-1||code==0||0>=usageCount) {
            if(isPic){
                AuthInfoDialog.show(authPicInfoDialog);
            }else{
                AuthInfoDialog.show(authVoiceInfoDialog);
            }
        }
    }

    public CompressionInterface picCOInterface = new CompressionInterface() {
        @Override
        public void initCallback(int code) {
            Log.e("initCallback:",""+code);
            if(code!=20000){
                //MainApp.getInstance().showMsg("图片压缩库初始化失败！:"+code);
                Toast.makeText(mContext,"图片压缩库初始化失败！:"+code ,Toast.LENGTH_SHORT).show();
            }
            if(!isPicOnline()){
                showDialogType(true);
            }
        }

        @Override
        public void zipCallback(int code) {
            Log.e("zipCallback",":"+code);
            if(code!=20000){
                //   MainApp.getInstance().showMsg("图片压缩库压缩失败！:"+code);
                Toast.makeText(mContext,"图片压缩库压缩失败！:"+code,Toast.LENGTH_SHORT).show();
            }

        }
    };
    public CompressionInterface voCOInterface=new CompressionInterface() {
        @Override
        public void initCallback(int code) {
            Log.e("initCallback:",""+code);
            if(code!=20000){
                //MainApp.getInstance().showMsg("语音压缩库初始化失败！:"+code);
                Toast.makeText(mContext,"语音压缩库初始化失败！:"+code,Toast.LENGTH_SHORT).show();
            }
            if(!isVoiceOnline()){
                showDialogType(false);
            }
        }

        @Override
        public void zipCallback(int code) {
            Log.e("zipCallback",":"+code);
            if(code!=20000){
                //   MainApp.getInstance().showMsg("语音压缩库压缩失败！:"+code);
                Toast.makeText(mContext,"语音压缩库压缩失败！:"+code,Toast.LENGTH_SHORT).show();
            }
        }
    };
// 展示普通图片 dialog ------------------
    public void showPicInfoDialog(){
        AuthInfoDialog.show(authPicInfoDialog_normal);
    }
// 展示普通语音 dialog ------------------
    public void showVoiceInfoDialog(){
        AuthInfoDialog.show(authVoiceInfoDialog_normal);
    }

// 初始化要用到的几个 dialog ----------------------------------------------
    public void initDialog(Activity activity){
// 过期 图片授权 dialog -------------
        authPicInfoDialog=new AuthInfoDialog(activity, "图像授权过期", "图像压缩库激活,请输入授权码。无授权码请联系销售人员", new AuthInfoDialog.OnItemClickListener() {
            @Override
            public void onOK(String str, AuthInfoDialog authInfoDialog) {
                //MainApp.getInstance().showLoading("加载中");
                CustomDialog dialog = new CustomDialog(mContext);
                dialog.show();
                ZDCompression.getInstance().initImgZip(mContext,str, new CompressionInterface() {
                    @Override
                    public void initCallback(int code) {
                        activity.runOnUiThread(() -> {
                            dialog.dismiss();
                            //MainApp.getInstance().hideLoading();
                            if(code!=20000){
                                if(20001==code||20100==code){
                                    //MainApp.getInstance().showMsg("key错误！:" + code);
                                    Toast.makeText(mContext,"key错误" + code,Toast.LENGTH_SHORT).show();
                                }else if(20010==code){
                                    //MainApp.getInstance().showMsg("没有网络！:" + code);
                                    Toast.makeText(mContext,"没有网络" + code,Toast.LENGTH_SHORT).show();
                                }else{
                                    //MainApp.getInstance().showMsg("图片压缩库错误-其他原因:" + code);
                                    Toast.makeText(mContext,"图片压缩库错误-其他原因:" + code,Toast.LENGTH_SHORT).show();
                                }
                            } else{
                                //MainApp.getInstance().showMsg("图片压缩库授权成功");
                                Toast.makeText(mContext,"图片压缩库授权成功:",Toast.LENGTH_SHORT).show();
                                initPicOnline(str);
                            }
                        });

                    }
                    @Override
                    public void zipCallback(int code) {
                        Log.e("zipCallback",":"+code);
                        if(code!=20000){
                            //MainApp.getInstance().showMsg("图像压缩库压缩失败！:" + code);
                            Toast.makeText(mContext,"图像压缩库压缩失败！:" + code,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                AuthInfoDialog.shutDown(authInfoDialog);
            }

            @Override
            public void onScanning() {
                initScan(activity,PIC_SC_CODE);
            }

            @Override
            public void onUpdata() {

            }
        });
// 普通 图片授权 dialog -------------
        authPicInfoDialog_normal=new AuthInfoDialog(activity, "图像授权", "图像压缩库激活,请输入授权码。无授权码请联系销售人员", new AuthInfoDialog.OnItemClickListener() {
            @Override
            public void onOK(String str, AuthInfoDialog authInfoDialog) {
                //MainApp.getInstance().showLoading("加载中");
                CustomDialog dialog = new CustomDialog(mContext);
                dialog.show();
                ZDCompression.getInstance().initImgZip(mContext,str, new CompressionInterface() {
                    @Override
                    public void initCallback(int code) {
                        activity.runOnUiThread(() -> {
                            dialog.dismiss();
                            //MainApp.getInstance().hideLoading();
                            if(code!=20000){
                                if(20001==code||20100==code){
                                    //MainApp.getInstance().showMsg("key错误！:" + code);
                                    Toast.makeText(mContext,"key错误" + code,Toast.LENGTH_SHORT).show();
                                }else if(20010==code){
                                    //MainApp.getInstance().showMsg("没有网络！:" + code);
                                    Toast.makeText(mContext,"没有网络" + code,Toast.LENGTH_SHORT).show();
                                }else{
                                    //MainApp.getInstance().showMsg("图片压缩库错误-其他原因:" + code);
                                    Toast.makeText(mContext,"图片压缩库错误-其他原因:" + code,Toast.LENGTH_SHORT).show();
                                }
                            } else{
                                //MainApp.getInstance().showMsg("图片压缩库授权成功");
                                Toast.makeText(mContext,"图片压缩库授权成功:",Toast.LENGTH_SHORT).show();
                                initPicOnline(str);
                            }
                        });

                    }
                    @Override
                    public void zipCallback(int code) {
                        Log.e("zipCallback",":"+code);
                        if(code!=20000){
                            //MainApp.getInstance().showMsg("图像压缩库压缩失败！:" + code);
                            Toast.makeText(mContext,"图像压缩库压缩失败！:" + code,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                AuthInfoDialog.shutDown(authInfoDialog);
            }

            @Override
            public void onScanning() {
                initScan(activity,PIC_SC_CODE);
            }

            @Override
            public void onUpdata() {

            }
        });


// 过期 语音授权 dialog -------------
        authVoiceInfoDialog=new AuthInfoDialog(activity, "语音授权过期", "语音压缩库激活,请输入授权码。无授权码请联系销售人员", new AuthInfoDialog.OnItemClickListener() {
            @Override
            public void onOK(String str, AuthInfoDialog authInfoDialog) {
                //MainApp.getInstance().showLoading("加载中");
                CustomDialog dialog = new CustomDialog(mContext);
                dialog.show();
                ZDCompression.getInstance().initVoiceZip(mContext,str, new CompressionInterface() {
                    @Override
                    public void initCallback(int code) {
                        activity.runOnUiThread(() -> {
                            //MainApp.getInstance().hideLoading();
                            dialog.dismiss();
                            if (code != 20000) {
                                if(20001==code||20100==code){
                                    //MainApp.getInstance().showMsg("key错误！:" + code);
                                    Toast.makeText(mContext,"key错误！:" + code,Toast.LENGTH_SHORT).show();
                                }else if(20010==code){
                                    //MainApp.getInstance().showMsg("没有网络！:" + code);
                                    Toast.makeText(mContext,"没有网络！:" + code,Toast.LENGTH_SHORT).show();
                                }else{
                                    //MainApp.getInstance().showMsg("语音压缩库错误-其他原因:" + code);
                                    Toast.makeText(mContext,"语音压缩库错误-其他原因:" + code,Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                //MainApp.getInstance().showMsg("语音压缩库授权成功");
                                Toast.makeText(mContext,"语音压缩库授权成功",Toast.LENGTH_SHORT).show();

                                // 添加一个授权成功标识
                                SharedPreferencesUtil.set(Constant.IS_VOICE_AUTH,Constant.VOICE_AUTH_YES);

                                initVoOnline(str);  // 授权成功，加个语音压缩库在线标识
                            }
                        });
                    }
                    @Override
                    public void zipCallback(int code) {
                        if (code != 20000) {
                            //MainApp.getInstance().showMsg("语音压缩库压缩失败！:" + code);
                            Toast.makeText(mContext,"语音压缩库压缩失败！:" + code,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                AuthInfoDialog.shutDown(authInfoDialog);
            }
            @Override
            public void onScanning() {
                initScan(activity,VO_SC_CODE);
            }

            @Override
            public void onUpdata() {
                if(updataView!= null){
                    updataView.onUpdata();
                }
            }
        });

// 普通 语音授权 dialog -------------
        authVoiceInfoDialog_normal=new AuthInfoDialog(activity, "语音授权", "语音压缩库激活,请输入授权码。无授权码请联系销售人员", new AuthInfoDialog.OnItemClickListener() {
            @Override
            public void onOK(String str, AuthInfoDialog authInfoDialog) {
                //MainApp.getInstance().showLoading("加载中");
                CustomDialog dialog = new CustomDialog(mContext);
                dialog.show();
                ZDCompression.getInstance().initVoiceZip(mContext,str, new CompressionInterface() {
                    @Override
                    public void initCallback(int code) {
                        activity.runOnUiThread(() -> {
                            //MainApp.getInstance().hideLoading();
                            dialog.dismiss();
                            if (code != 20000) {
                                if(20001==code||20100==code){
                                    //MainApp.getInstance().showMsg("key错误！:" + code);
                                    Toast.makeText(mContext,"key错误！:" + code,Toast.LENGTH_SHORT).show();
                                }else if(20010==code){
                                    //MainApp.getInstance().showMsg("没有网络！:" + code);
                                    Toast.makeText(mContext,"没有网络！:" + code,Toast.LENGTH_SHORT).show();
                                }else{
                                    //MainApp.getInstance().showMsg("语音压缩库错误-其他原因:" + code);
                                    Toast.makeText(mContext,"语音压缩库错误-其他原因:" + code,Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                //MainApp.getInstance().showMsg("语音压缩库授权成功");
                                Toast.makeText(mContext,"语音压缩库授权成功",Toast.LENGTH_SHORT).show();

                                // 添加一个授权成功标识
                                SharedPreferencesUtil.set(Constant.IS_VOICE_AUTH,Constant.VOICE_AUTH_YES);
                                // 发送通知让授权页面更新
                                NotificationCenter.standard().postNotification(Constant.UPDATA_VOICE_AUTH_ACTIVITY);

                                initVoOnline(str);  // 授权成功，加个语音压缩库在线标识
                            }
                        });
                    }
                    @Override
                    public void zipCallback(int code) {
                        if (code != 20000) {
                            //MainApp.getInstance().showMsg("语音压缩库压缩失败！:" + code);
                            Toast.makeText(mContext,"语音压缩库压缩失败！:" + code,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                AuthInfoDialog.shutDown(authInfoDialog);
            }
            @Override
            public void onScanning() {
                initScan(activity,VO_SC_CODE);
            }

            @Override
            public void onUpdata() {
                if(updataView!= null){
                    updataView.onUpdata();
                }
            }
        });










    }

    public void initScan(Activity activity,int code) {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        // 设置要扫描的条码类型，ONE_D_CODE_TYPES：一维码，QR_CODE_TYPES-二维码
        integrator.setDesiredBarcodeFormats();
        integrator.setCaptureActivity(CaptureActivity.class); //设置打开摄像头的Activity
        integrator.setRequestCode(code);
        integrator.setPrompt("请对准二维码"); //底部的提示文字，设为""可以置空
        integrator.setCameraId(0); //前置或者后置摄像头
        integrator.setBeepEnabled(false); //扫描成功的「哔哔」声，默认开启
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }
    public void setResult(int requestCode, int resultCode, Intent data){
        IntentResult scanResult = IntentIntegrator.parseActivityResult(resultCode, data);
        if (scanResult.getContents() != null) {
            String result = scanResult.getContents();
            if(requestCode ==PIC_SC_CODE){
                authPicInfoDialog.setKeyEt(result);
            }else{
                authVoiceInfoDialog.setKeyEt(result);
            }
        }
    }

    public UpdataView updataView;
    public void setUpdataView(UpdataView updataView){
        this.updataView = updataView;
    }
    public interface UpdataView {
        // 更新页面状态
        void onUpdata();
    }
}
