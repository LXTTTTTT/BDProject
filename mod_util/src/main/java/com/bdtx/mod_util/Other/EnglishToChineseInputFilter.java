package com.bdtx.mod_util.Other;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

public class EnglishToChineseInputFilter implements InputFilter {

    private final String TAG = "EnglishToChineseInputFilter";
    private boolean enable = false;
    public void enableFiltering(boolean enable){
        Log.e(TAG, "启动过滤规则: "+enable );
        this.enable = enable;
    }
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        if(!enable){return source;}

        StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; i++) {
            char currentChar = source.charAt(i);

            // 检查是否为英文字符（包括大小写字母和空格）
            // 48-57 数字 65-90 97-122 大小写字母 13回车 32空格 10换行
            if (currentChar >= 32 && currentChar <= 126) {
                if((currentChar>=48 && currentChar<=57) || (currentChar>=65 && currentChar<=90) || (currentChar>=97 && currentChar<=122)){
                    Log.i(TAG, "filter: 英文/数字");
                    builder.append("");
                } else {
                    Log.i(TAG, "filter: 符号" );
                    // 替换为中文字符，例如逗号替换为逗号，其他替换为空
                    switch (currentChar) {
                        case ' ':
                            builder.append("");
                            break;
                        case ',':
                            builder.append('，');
                            break;
                        case '?':
                            builder.append('？');
                            break;
                        case '!':
                            builder.append('！');
                            break;
                        case ';':
                            builder.append('；');
                            break;
                        case '"':
                            builder.append('“');
                            break;
                        case '.':
                            builder.append('。');
                            break;
                        default:
                            // 如果不需要替换为其他中文字符，则将其替换为空字符串
                            builder.append("");
                            break;
                    }
                }
            }
            // 13回车 10换行
            else if(currentChar == 10 || currentChar == 13){
                Log.e(TAG, "filter: 回车/换行" );
                builder.append("");
            } else {
                Log.e(TAG, "filter: 中文" );
                // 如果不是英文字符，则保留原字符
                builder.append(currentChar);
            }
        }

        // 返回处理后的字符序列
        return builder.toString();
    }
}
