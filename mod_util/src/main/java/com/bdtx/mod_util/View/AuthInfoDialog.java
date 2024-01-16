package com.bdtx.mod_util.View;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bdtx.mod_util.R;


public class AuthInfoDialog extends Dialog {

    public TextView contentTV;
    public Button cancelBtn,okBtn;
    public ImageView scanningImg;
    public EditText keyEt;
    public TextView titleTv;
    private String title;
    private String content;
    private Context mContext;
    private OnItemClickListener onItemClickListener;
    public AuthInfoDialog(Context context, String title, String content, OnItemClickListener onItemClickListener ) {
        super(context);
        this.content=content;
        this.mContext=context;
        this.title=title;
        this.onItemClickListener=onItemClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_auth_info);
        // 设置触摸外部界面使dialog消失
        setCanceledOnTouchOutside(true);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        init();
    }
    public void init() {
        scanningImg=findViewById(R.id.code_scanning_img);
        contentTV=findViewById(R.id.contentTV);
        keyEt=findViewById(R.id.key_et);
        titleTv=findViewById(R.id.title);
        cancelBtn=findViewById(R.id.cancelBtn);
        okBtn=findViewById(R.id.okBtn);

        // 取消按键
        cancelBtn.setOnClickListener(view -> {
            dismiss();
        });

        contentTV.setText(content);
        titleTv.setText(title);
        okBtn.setOnClickListener(view -> {
            String str=keyEt.getText().toString().trim();
            if("".equals(str)){
                Toast.makeText(mContext,"key不能为空",Toast.LENGTH_SHORT).show();
                return;
            }
            onItemClickListener.onOK(str,this);

            onItemClickListener.onUpdata();
        });
        scanningImg.setOnClickListener(view -> onItemClickListener.onScanning());
    }
    public void setKeyEt(String str){

        if(keyEt == null){
            Log.e("未初始化", "setKeyEt: ");
            return;
        }
        if(str == null){
            Log.e("无文本", "setKeyEt: ");
            return;
        }
        keyEt.setText(str);
    }



    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onOK(String str,AuthInfoDialog authInfoDialog);
        void onScanning();

        // 更新页面状态
        void onUpdata();
    }
    public static void show(Dialog dialog) {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public static void shutDown(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

}
