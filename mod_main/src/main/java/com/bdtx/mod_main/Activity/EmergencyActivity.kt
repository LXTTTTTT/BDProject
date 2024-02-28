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
import com.bdtx.mod_main.databinding.ActivityEmergencyBinding

class EmergencyActivity:BaseViewBindingActivity<ActivityEmergencyBinding>() {

    var status:String = Constant.SOS_STATUS_OTHER

    companion object{
        fun start(context: Context, sos_status: String?) {
            val intent = Intent(context, EmergencyActivity::class.java)
            intent.putExtra(Constant.SOS_STATUS, sos_status)
            (context as Activity).startActivityForResult(intent,Constant.RESULT_CODE_EMERGENCY)
        }
    }
    override fun beforeSetLayout() {}

    override fun initView(savedInstanceState: Bundle?) {
        status = intent.getStringExtra(Constant.SOS_STATUS).toString()
        select_img = viewBinding.checked1
        select_text = viewBinding.text1
        init_control()
    }

    override fun initData() {}
    override suspend fun initDataSuspend() {}

    fun init_control(){
        when(status){
            Constant.SOS_STATUS_OTHER -> {
                selectImage(viewBinding.checked1)
                selectText(viewBinding.text1)
            }
            Constant.SOS_STATUS_LOST -> {
                selectImage(viewBinding.checked2)
                selectText(viewBinding.text2)
            }
            Constant.SOS_STATUS_FLOOD -> {
                selectImage(viewBinding.checked3)
                selectText(viewBinding.text3)
            }
            Constant.SOS_STATUS_FALL -> {
                selectImage(viewBinding.checked4)
                selectText(viewBinding.text4)
            }
            Constant.SOS_STATUS_DAMAGED -> {
                selectImage(viewBinding.checked5)
                selectText(viewBinding.text5)
            }
            Constant.SOS_STATUS_ROCKFALL -> {
                selectImage(viewBinding.checked6)
                selectText(viewBinding.text6)
            }
            Constant.SOS_STATUS_ACCIDENT -> {
                selectImage(viewBinding.checked7)
                selectText(viewBinding.text7)
            }
            Constant.SOS_STATUS_HYPOTHERMIA -> {
                selectImage(viewBinding.checked8)
                selectText(viewBinding.text8)
            }
            Constant.SOS_STATUS_HEATSTROKE -> {
                selectImage(viewBinding.checked9)
                selectText(viewBinding.text9)
            }
            Constant.SOS_STATUS_SICKNESS -> {
                selectImage(viewBinding.checked10)
                selectText(viewBinding.text10)
            }
            Constant.SOS_STATUS_HEARTATTACK -> {
                selectImage(viewBinding.checked11)
                selectText(viewBinding.text11)
            }
            Constant.SOS_STATUS_POISON -> {
                selectImage(viewBinding.checked12)
                selectText(viewBinding.text12)
            }

        }

        viewBinding.cancel.setOnClickListener {
            finish()
        }
        viewBinding.qt.setOnClickListener {
            status=Constant.SOS_STATUS_OTHER
            back()
        }
        viewBinding.ml.setOnClickListener {
            status=Constant.SOS_STATUS_LOST
            back()
        }
        viewBinding.sh.setOnClickListener {
            status=Constant.SOS_STATUS_FLOOD
            back()
        }
        viewBinding.hz.setOnClickListener {
            status=Constant.SOS_STATUS_FALL
            back()
        }
        viewBinding.xlss.setOnClickListener {
            status=Constant.SOS_STATUS_DAMAGED
            back()
        }
        viewBinding.ls.setOnClickListener {
            status=Constant.SOS_STATUS_ROCKFALL
            back()
        }
        viewBinding.jtsg.setOnClickListener {
            status=Constant.SOS_STATUS_ACCIDENT
            back()
        }
        viewBinding.sw.setOnClickListener {
            status=Constant.SOS_STATUS_HYPOTHERMIA
            back()
        }
        viewBinding.zs.setOnClickListener {
            status=Constant.SOS_STATUS_HEATSTROKE
            back()
        }
        viewBinding.gsb.setOnClickListener {
            status=Constant.SOS_STATUS_SICKNESS
            back()
        }
        viewBinding.xzb.setOnClickListener {
            status=Constant.SOS_STATUS_HEARTATTACK
            back()
        }
        viewBinding.zd.setOnClickListener {
            status=Constant.SOS_STATUS_POISON
            back()
        }
    }

    fun back(){
//        val intent = Intent()
        intent.putExtra(Constant.SOS_STATUS,status)
        setResult(Constant.RESULT_CODE_EMERGENCY,intent)
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