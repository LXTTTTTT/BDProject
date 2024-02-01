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
import com.bdtx.mod_util.Util.GlobalControlUtil;


public class AuthInfoDialog extends Dialog {

    public TextView tips,title;
    public Button cancel, yes;
    public ImageView scan;
    public EditText key;
    private String title_str;
    private String tips_str;
    private Context my_context;
    public AuthInfoDialog(Context context, String title_str, String tips_str, OnItemClickListener onItemClickListener ) {
        super(context);
        this.my_context =context;
        this.title_str=title_str;
        this.tips_str =tips_str;
        this.onItemClickListener=onItemClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_auth_info);
        // 设置触摸外部界面使dialog消失
        setCanceledOnTouchOutside(true);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        init();
    }
    public void init() {

        scan=findViewById(R.id.scan);
        tips=findViewById(R.id.tips);
        key =findViewById(R.id.key);
        title=findViewById(R.id.title);
        cancel=findViewById(R.id.cancel);
        yes =findViewById(R.id.yes);

        tips.setText(tips_str);
        title.setText(title_str);
        scan.setOnClickListener(view -> onItemClickListener.onScanning());

        // 取消
        cancel.setOnClickListener(view -> {dismiss();});
        // 确定
        yes.setOnClickListener(view -> {
            String str = key.getText().toString().trim();
            if("".equals(str)){GlobalControlUtil.INSTANCE.showToast("请输入key",0);return;}
            onItemClickListener.onOK(str);
        });

    }
    public void setKey(String str){
        if(key == null){return;}
        if(str == null || str.equals("")){return;}
        key.setText(str);
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

// 接口 -----------------------------------------------
    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {this.onItemClickListener = onItemClickListener;}
    public interface OnItemClickListener {
        void onOK(String str);
        void onScanning();
    }
}
