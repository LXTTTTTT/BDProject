package com.bdtx.mod_main.Activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_data.Global.Variable
import com.bdtx.mod_main.Adapter.SwiftListAdapter2
import com.bdtx.mod_main.Base.BaseViewBindingActivity
import com.bdtx.mod_main.databinding.ActivityCommunicationLinkBinding
import com.bdtx.mod_main.databinding.ActivityMessageTypeBinding
import com.bdtx.mod_util.Utils.GlobalControlUtils

// 兼容所有连接方式（画大饼）
@Route(path = Constant.COMMUNICATION_LINK_ACTIVITY)
class CommunicationLinkActivity : BaseViewBindingActivity<ActivityCommunicationLinkBinding>() {

    override fun beforeSetLayout() {}

    override fun initView(savedInstanceState: Bundle?) {
        setTitle("通信链路");
        init_control()
    }

    override fun initData() {

    }

    fun init_control(){
        viewBinding.bleConnection.setOnClickListener {
            ARouter.getInstance().build(Constant.CONNECT_BLUETOOTH_ACTIVITY).navigation()  // 页面跳转
        }
        viewBinding.back.setOnClickListener {
            finish()
        }
    }

}