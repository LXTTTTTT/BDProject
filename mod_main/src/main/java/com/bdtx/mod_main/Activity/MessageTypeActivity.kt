package com.bdtx.mod_main.Activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_main.Base.BaseViewBindingActivity
import com.bdtx.mod_main.databinding.ActivityMessageTypeBinding
import com.bdtx.mod_main.databinding.ActivityVoiceAuthBinding

@Route(path = Constant.MESSAGE_TYPE_ACTIVITY)
class MessageTypeActivity : BaseViewBindingActivity<ActivityMessageTypeBinding>() {


    override fun beforeSetLayout() {}

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData() {

    }
}