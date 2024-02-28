package com.bdtx.mod_main.Activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.bdtx.mod_data.EventBus.AuthMsg
import com.bdtx.mod_data.EventBus.BaseMsg
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_data.Global.Variable
import com.bdtx.mod_main.Base.BaseViewBindingActivity
import com.bdtx.mod_main.databinding.ActivityVoiceAuthBinding
import com.bdtx.mod_util.Utils.DataUtils
import com.bdtx.mod_util.Utils.ZDCompressionUtils
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.pancoit.compression.ZDCompression
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = Constant.VOICE_AUTH_ACTIVITY)
class VoiceAuthActivity : BaseViewBindingActivity<ActivityVoiceAuthBinding>() {


    override fun beforeSetLayout() {}
    override fun enableEventBus(): Boolean { return true }  // 使用 eventbus
    override fun initView(savedInstanceState: Bundle?) {
        setTitle("语音压缩授权")
        // 是否显示信息
        val show = Variable.isVoiceOnline()
        loge("当前 $show 激活")
        if(!show){
            try {
                loge("压缩库信息: "+ZDCompression.getInstance().vOffInfo)
                val array = ZDCompression.getInstance().vOffInfo.split(",".toRegex()).toTypedArray()
                val all = 100
                val leave = Integer.valueOf(array[0])
                val use = all - leave
                val start = array[1]
                val end = array[2]
                viewBinding.effectTime.text = start
                viewBinding.expireTime.text = end
                viewBinding.allTimes.text = "" + all
                viewBinding.usedTimes.text = "" + use
                viewBinding.leaveTimes.text = "" + leave

                val a: Long = DataUtils.stringToTimeStamp(end.substring(0, 16),true)
                val c: Long = DataUtils.stringToTimeStamp(start.substring(0, 16),true)
                val b = System.currentTimeMillis()
                if (b - a > 0) {
                    viewBinding.tips.text = "*压缩库已过期！"
                    viewBinding.tips.setTextColor(Color.RED)
                }
                if (c - b > 0) {
                    viewBinding.tips.text = "*压缩库不在使用日期范围内"
                    viewBinding.tips.setTextColor(Color.RED)
                }
                if (leave <= 0) {
                    viewBinding.tips.text = "*压缩库次数不足！"
                    viewBinding.tips.setTextColor(Color.RED)
                }
                viewBinding.authText.text = "开始授权"
                viewBinding.view1.visibility = View.VISIBLE
                viewBinding.view2.visibility = View.GONE
                viewBinding.startAuth.setOnClickListener {
                    ZDCompressionUtils.getInstance().showAuthDialog(this@VoiceAuthActivity)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }else{
            viewBinding.authText.text = "已授权"
            viewBinding.view1.visibility = View.GONE
            viewBinding.view2.visibility = View.VISIBLE
            viewBinding.startAuth.setOnClickListener {
                finish()
            }
        }

    }

    override fun initData() {

    }
    override suspend fun initDataSuspend() {}

    // 认证结果处理
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(eventMsg: BaseMsg<Any>){
        loge("收到广播，类型：${eventMsg.type}")
        if(eventMsg.type==BaseMsg.MSG_AUTH){
            val message = eventMsg.message
            message?.let {
                if((it as AuthMsg).authResult==AuthMsg.AUTH_SUCCESS){
                    ZDCompressionUtils.getInstance().hideAuthDialog()
                    Variable.setKey(it.key)  // 保存key
                    finish()
                    loge("激活成功，关闭页面")
                }
            }
        }
    }


    // 扫码结果监听
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == ZDCompressionUtils.SCAN_ACTIVITY_CODE) {
                val scanResult: IntentResult = IntentIntegrator.parseActivityResult(resultCode, data)
                if (scanResult.getContents() != null) {
                    val result: String = scanResult.getContents()
                    ZDCompressionUtils.getInstance().setDialogKey(result);
                }
            }
        }
    }
}