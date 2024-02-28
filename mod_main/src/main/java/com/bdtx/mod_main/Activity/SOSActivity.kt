package com.bdtx.mod_main.Activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_data.ViewModel.CommunicationVM
import com.bdtx.mod_data.ViewModel.MainVM
import com.bdtx.mod_main.Base.BaseMVVMActivity
import com.bdtx.mod_main.Base.BaseViewBindingActivity
import com.bdtx.mod_main.R
import com.bdtx.mod_main.View.CustomSpinnerLinearLayout
import com.bdtx.mod_main.databinding.ActivitySosBinding
import com.bdtx.mod_util.Utils.ApplicationUtils
import com.bdtx.mod_util.Utils.GlobalControlUtils
import com.bdtx.mod_util.Utils.SendMessageUtils

@Route(path = Constant.SOS_ACTIVITY)
class SOSActivity:BaseMVVMActivity<ActivitySosBinding,CommunicationVM>() {

    var now_count = 1;
    var status = Constant.SOS_STATUS_OTHER
    var body = Constant.BODY_STATUS_GREAT
    val PLATFORM = "指挥中心"
    var content = PLATFORM
    var listData = mutableListOf<String>()
    override fun beforeSetLayout() {}

    override fun initView(savedInstanceState: Bundle?) {
        init_control()
    }
    override suspend fun initDataSuspend() {}
    override fun initData() {
        super.initData()
        init_view_model()
    }

    fun init_control(){
        // 求救内容
//        listData.add("请求支援")
//        listData.add("遇到自然灾害")
//        listData.add("有人受伤了")
//        listData.add("迷路了")
//        listData.add("遇到其它状况")

//        viewBinding.sosContent.setData(listData) // 设置选项
        listData.add(PLATFORM)
        viewBinding.sosContent.setItemCount(3) // 设置项数
        viewBinding.sosContent.setOnSpinnerItemClickListener(object :CustomSpinnerLinearLayout.onSpinnerItemClickListener<Int>{
            override fun onSpinnerItemClick(position: Int?) {
                position?.let {
                    content = listData[it]
                    loge("点击项数：$content")
                }
            }
        })
        // 紧急情况
        viewBinding.statusButton.setOnClickListener {
            EmergencyActivity.start(this@SOSActivity,viewBinding.status.text.toString())
        }

        // 身体情况
        viewBinding.bodyButton.setOnClickListener {
            BodyStatusActivity.start(this@SOSActivity,viewBinding.body.text.toString())
        }

        // 增加/减少人数
        viewBinding.add.setOnClickListener(View.OnClickListener {
            if (now_count < 100) {
                now_count += 1
                viewBinding.peopleCount.setText(now_count.toString())
            } else {
                GlobalControlUtils.showToast("最大人数为99",0)
            }
        })
        viewBinding.reduce.setOnClickListener(View.OnClickListener {
            if (now_count > 1) {
                now_count -= 1
                viewBinding.peopleCount.setText(now_count.toString())
            } else {
                return@OnClickListener
            }
        })

        viewBinding.peopleCount.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                p0?.let {
                    val count_str = it.toString()
                    if(!count_str.isNullOrEmpty()){
                        now_count = p0.toString().toInt()
                    }else{
                        now_count = 0;
                    }
                }
            }

        })

        viewBinding.sosButton.setOnClickListener {
            SendMessageUtils.send_sos(status,body,now_count,content)
        }
    }

    fun init_view_model(){

        viewModel.getContact().observe(this,{
            it?.let {
                listData.clear()
                listData.add(PLATFORM)
                it.forEach {  contact->
                    if(contact.number.length==11){
                        listData.add(contact.number)
                    }
                }
                viewBinding.sosContent.setData(listData)
            }
        })

        val mainVM = ApplicationUtils.getGlobalViewModel(MainVM::class.java)
        if(mainVM!=null){
            // 设备连接监听
            mainVM.isConnectDevice.observe(this,object : Observer<Boolean?>{
                override fun onChanged(t: Boolean?) {
                    t?.let {
                        if(it){
                            viewBinding.sosButton.isClickable = true;
                            viewBinding.sosButton.setBackgroundResource(R.drawable.sos_icon_ripple)
                            viewBinding.countDown.text = ""
                        }else{
                            viewBinding.sosButton.isClickable = false;
                            viewBinding.sosButton.setBackgroundResource(R.mipmap.sos_button)
                            viewBinding.countDown.text = "未连接"
                        }
                    }
                }
            })
            mainVM.waitTime.observe(this,{ count ->
                count?.let {
                    if(mainVM.isConnectDevice.getValue()==false){return@observe}
                    if(count>0){
                        viewBinding.sosButton.isClickable = false;
                        viewBinding.sosButton.setBackgroundResource(R.mipmap.sos_button)
                        viewBinding.countDown.text = count.toString()
                    }else{
                        viewBinding.sosButton.isClickable = true;
                        viewBinding.sosButton.setBackgroundResource(R.drawable.sos_icon_ripple)
                        viewBinding.countDown.text = ""
                    }
                }
            })

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==Constant.RESULT_CODE_EMERGENCY){
            val status = data?.getStringExtra(Constant.SOS_STATUS)
            if(!status.isNullOrEmpty()){
                viewBinding.status.text = status
                this.status = status
                loge("返回状态$status")
            }
        }else if(requestCode==Constant.RESULT_CODE_BODY){
            val status = data?.getStringExtra(Constant.BODY_STATUS)
            if(!status.isNullOrEmpty()){
                viewBinding.body.text = status
                this.body = status
                loge("返回状态$status")
            }
        }
    }
}