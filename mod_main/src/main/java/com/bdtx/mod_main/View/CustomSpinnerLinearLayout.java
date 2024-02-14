package com.bdtx.mod_main.View;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bdtx.mod_main.R;

import java.util.ArrayList;
import java.util.List;

// 自定义 Spinner LinearLayout
public class CustomSpinnerLinearLayout extends LinearLayout {

    private String TAG = "CustomSpinnerLinearLayout";

    private View spinner_header;  // spinner 头部
    private TextView spinner_text;  // spinner 头部的文本
    private ImageView spinner_arrow;  // spinner 头部的收缩/展开箭头

    private PopupWindow spinner_body;  // spinner 身体（显示的选项列表弹窗视图）
    private View spinner_body_view;  // 填充身体用的 view

    private LayoutInflater layoutInflater;
    private CustomSpinnerListAdapter my_adapter;  // 选项列表 adapter
    private List<String> list_items = new ArrayList<>();  // 选项列表数据
    private boolean isListShow = false;  // 列表的显示状态标识
    private int spinner_body_height = 0;  // 展示的列表高度
    private int spinner_item_count = 3;  // 展示的子项数目，默认3条（高度/数目二选一）
    private int selected_index = 0;  // 当前选中项的索引

    public CustomSpinnerLinearLayout(Context context) {
        super(context);
        initView(context);
    }

    public CustomSpinnerLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CustomSpinnerLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public CustomSpinnerLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(final Context context) {

        layoutInflater = LayoutInflater.from(context);
        spinner_header = layoutInflater.inflate(R.layout.custom_spinner_header, null);  // 初始化spinner头部
        spinner_header.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        spinner_body_view = layoutInflater.inflate(R.layout.custom_spinner_body, null);  // 初始化spinner身体


        spinner_text = spinner_header.findViewById(R.id.text);
        spinner_arrow = spinner_header.findViewById(R.id.arrow);

        // 头部点击事件：收缩/展开 选项列表
        spinner_header.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "当前列表展开状态: " + isListShow );
                if (list_items != null){

                    // 首次点击初始化
                    if(spinner_body == null){
                        // 初始化列表 --------------------------------------
                        RecyclerView spinner_list = spinner_body_view.findViewById(R.id.spinner_list);
                        my_adapter = new CustomSpinnerListAdapter(context,list_items);
                        // 选项列表点击事件
                        my_adapter.setOnListItemClickListener(new onListItemClickListener() {
                            @Override
                            public void onItemClick(Object position) {
                                int index = (int) position;
                                Log.e(TAG, "列表点击项数: " + index );
                                selected_index = index;
                                my_adapter.notifyDataSetChanged();  // 更新列表状态（改变选中项背景）
                                spinner_text.setText(list_items.get(index));  // 修改头部文本
                                spinner_arrow.setImageResource(R.mipmap.sos_down);  // 修改头部箭头为展开（下），这里放入自己的资源文件
                                spinner_body.dismiss();  // 隐藏列表
                                isListShow = false;  // 修改标识
                                if(onSpinnerItemClickListener!=null){onSpinnerItemClickListener.onSpinnerItemClick(position);}  // 传到外部
                            }
                        });
                        spinner_list.setAdapter(my_adapter);
                        spinner_list.setLayoutManager(new LinearLayoutManager(context));

                        // 初始化列表高度 --------------------------------------
                        // 如果设置了展示数目就显示数目，否则显示高度
                        int body_height = spinner_item_count != 0 ? (my_adapter.getItemHeight()*spinner_item_count) : spinner_body_height;
                        spinner_body = new PopupWindow(spinner_body_view, spinner_header.getWidth(),body_height , true);
                        spinner_body.setFocusable(false);  // 设置不可取消
                        spinner_body.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);  // 设置不遮挡软键盘
                        spinner_body.setSoftInputMode(PopupWindow.INPUT_METHOD_FROM_FOCUSABLE);  // 设置不遮挡软键盘
                        spinner_body.showAsDropDown(view,0, 0);  // 显示列表
                        // 初始化小箭头 --------------------------------------
                        spinner_arrow.setImageResource(R.mipmap.sos_down);  // 收缩（上），这里放入自己的资源文件
                        // 初始化列表显示状态 --------------------------------------
                        isListShow = true;
                    }else{

                        // 后续点击收缩/展开 列表
                        if(!isListShow){
                            spinner_arrow.setImageResource(R.mipmap.sos_down);
                            spinner_body.showAsDropDown(view, 0, 0);
                            isListShow = true;
                        }else{
                            spinner_arrow.setImageResource(R.mipmap.sos_down);
                            spinner_body.dismiss();
                            isListShow = false;
                        }
                    }
                }
            }
        });
        // 初始化头部文本
        if (list_items == null || list_items.size() == 0){
            spinner_text.setText("");
        }else{
            spinner_text.setText(list_items.get(0));
        }

        addView(spinner_header);
    }


    public void setData(List<String> list){
        this.list_items = list;
        spinner_text.setText(list.get(0));
    }

    // 设置列表弹窗总高度
    public void setBodyHeiht(int height){
        if(height<=0){return;}
        this.spinner_body_height = height;
        this.spinner_item_count = 0;
    }

    // 设置列表项显示数目
    public void setItemCount(int count){
        if(count<1){return;}
        this.spinner_item_count = count;
        this.spinner_body_height = 0;
    }

    // 设置选中项
    public void setSelectedItem(int position) {
        if (position >= 0 && position < list_items.size()) {
            selected_index = position;
            spinner_text.setText(list_items.get(selected_index));
            if (my_adapter != null) {
                my_adapter.notifyDataSetChanged();
            }
        }
    }

    // 列表适配器
    private class CustomSpinnerListAdapter extends RecyclerView.Adapter<CustomSpinnerListAdapter.MyViewHolder> {

        Context my_context;
        List<String> items = new ArrayList<>();  // 用到的操作名称


        public CustomSpinnerListAdapter(Context context, List<String> items){
            my_context = context;
            this.items = items;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(my_context).inflate(R.layout.custom_spinner_item, parent, false);
            MyViewHolder holder = new MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            String item_str = items.get(position);
            // 如果项数是选中项则修改背景颜色
            holder.item.setText(item_str);
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onListItemClickListener!=null){onListItemClickListener.onItemClick(position);}
                }
            });

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        // 获取列表子项的高度，当spinner设置为显示项数时需要用到
        public int getItemHeight() {
            // 获取子视图
            View itemView = layoutInflater.inflate(R.layout.custom_spinner_item, null);
            MyViewHolder holder = new MyViewHolder(itemView);
            onBindViewHolder(holder, 0);
            // 测量高度
            itemView.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            );
            return itemView.getMeasuredHeight();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{
            private TextView item;
            public MyViewHolder(View itemView) {
                super(itemView);
                item = itemView.findViewById(R.id.text);
            }

        }
        // 接口 ------------------------------------------
        public onListItemClickListener onListItemClickListener;
        public void setOnListItemClickListener(onListItemClickListener onListItemClickListener){
            this.onListItemClickListener = onListItemClickListener;
        }

    }

    @Override
    public void destroyDrawingCache() {
        if (spinner_body != null && spinner_body.isShowing()){
            spinner_body.dismiss();
        }
        super.destroyDrawingCache();
    }

// 接口 ----------------------------------------------------------
    private interface onListItemClickListener<T>{
        void onItemClick(T position);
    }
    public interface onSpinnerItemClickListener<T>{
        void onSpinnerItemClick(T position);
    }
    public onSpinnerItemClickListener onSpinnerItemClickListener;
    public void setOnSpinnerItemClickListener(onSpinnerItemClickListener onSpinnerItemClickListener){
        this.onSpinnerItemClickListener = onSpinnerItemClickListener;
    }
}
