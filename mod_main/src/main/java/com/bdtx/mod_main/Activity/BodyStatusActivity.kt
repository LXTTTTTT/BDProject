package com.bdtx.mod_main.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_main.Base.BaseViewBindingActivity
import com.bdtx.mod_main.databinding.ActivityBodyStatusBinding

class BodyStatusActivity:BaseViewBindingActivity<ActivityBodyStatusBinding>() {

    var status:String = Constant.BODY_STATUS_GREAT

    companion object{
        fun start(context: Context, sos_status: String?) {
            val intent = Intent(context, BodyStatusActivity::class.java)
            intent.putExtra(Constant.BODY_STATUS, sos_status)
            (context as Activity).startActivityForResult(intent,Constant.RESULT_CODE_BODY)
        }
    }
    override fun beforeSetLayout() {}

    override fun initView(savedInstanceState: Bundle?) {
        status = intent.getStringExtra(Constant.BODY_STATUS).toString()
        select_img = viewBinding.checked1
        select_text = viewBinding.text1
        init_control()
    }

    override fun initData() {}
    override suspend fun initDataSuspend() {}

    fun init_control(){
        when(status){
            Constant.BODY_STATUS_GREAT -> {
                selectImage(viewBinding.checked1)
                selectText(viewBinding.text1)
            }
            Constant.BODY_STATUS_WALK -> {
                selectImage(viewBinding.checked2)
                selectText(viewBinding.text2)
            }
            Constant.BODY_STATUS_BLOOD -> {
                selectImage(viewBinding.checked3)
                selectText(viewBinding.text3)
            }
            Constant.BODY_STATUS_HUNGRY -> {
                selectImage(viewBinding.checked4)
                selectText(viewBinding.text4)
            }
            Constant.BODY_STATUS_INJURED -> {
                selectImage(viewBinding.checked5)
                selectText(viewBinding.text5)
            }
        }

        viewBinding.cancel.setOnClickListener {
            finish()
        }
        viewBinding.lh.setOnClickListener {
            status=Constant.BODY_STATUS_GREAT
            back()
        }
        viewBinding.wfxz.setOnClickListener {
            status=Constant.BODY_STATUS_WALK
            back()
        }
        viewBinding.sxgd.setOnClickListener {
            status=Constant.BODY_STATUS_BLOOD
            back()
        }
        viewBinding.jk.setOnClickListener {
            status=Constant.BODY_STATUS_HUNGRY
            back()
        }
        viewBinding.pws.setOnClickListener {
            status=Constant.BODY_STATUS_INJURED
            back()
        }
    }

    fun back(){
//        val intent = Intent()
        intent.putExtra(Constant.BODY_STATUS,status)
        setResult(Constant.RESULT_CODE_BODY,intent)
        finish()
    }


    var select_img: ImageView? = null
    var select_text : TextView? = null
    fun selectImage(view: ImageView) {
        // 如果传入的还是上一个信号的话就不做处理
        if (view.visibility == View.VISIBLE) {
            Log.e("没有执行", "selectImage: ")
            return
        }
        view.visibility = View.VISIBLE
        select_img?.setVisibility(View.INVISIBLE)
        select_img = view
    }

    // 单选文字 -------------------------
    fun selectText(view: TextView) {
        // 如果传入的还是上一个信号的话就不做处理
        if (view.currentTextColor == Color.WHITE) { return }
        view.setTextColor(Color.WHITE)
        select_text?.setTextColor(Color.GRAY)
        select_text = view
    }
}