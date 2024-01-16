package com.bdtx.mod_main.Activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.launcher.ARouter
import com.bdtx.mod_data.Database.DaoUtil
import com.bdtx.mod_data.Database.Entity.Message
import com.bdtx.mod_data.Global.Constant
import com.bdtx.mod_data.ViewModel.MainVM
import com.bdtx.mod_main.Base.BaseMVVMActivity
import com.bdtx.mod_main.databinding.ActivityMainBinding
import com.bdtx.mod_util.Util.ApplicationUtil
import com.bdtx.mod_util.Util.BluetoothTransferUtil
import com.bdtx.mod_util.Util.DataUtil
import com.bdtx.mod_util.Util.GlobalControlUtil

// 用不上 ViewModel
class MainActivity : BaseMVVMActivity<ActivityMainBinding,MainVM>(true) {


    override fun beforeSetLayout() {}

    override fun initView(savedInstanceState: Bundle?) {


        viewBinding.messagePage.setOnClickListener {
            ARouter.getInstance().build(Constant.MESSAGE_ACTIVITY).navigation()
        }

        viewBinding.statePage.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
//                startActivity(Intent(my_context,StateActivity::class.java))
                ARouter.getInstance().build(Constant.STATE_ACTIVITY).navigation()
            }
        })

        viewBinding.mapPage.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                ARouter.getInstance().build(Constant.MAP_ACTIVITY).navigation()
            }
        })

        viewBinding.sosPage.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                ARouter.getInstance().build(Constant.SOS_ACTIVITY).navigation()
            }
        })

        viewBinding.settingPage.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                ARouter.getInstance().build(Constant.SETTING_ACTIVITY).navigation()
            }
        })

        viewBinding.test1.setOnClickListener {
            var message = Message()
            message.content = "测试发送"
            message.messageType = Constant.MESSAGE_TEXT
            message.ioType = Constant.TYPE_SEND
            message.number = "666666"
            message.state = Constant.STATE_SENDING
            message.time = DataUtil.getTimeSecond()
            DaoUtil.getInstance().addMessage(message)

            var message2 = Message()
            message2.content = "测试接收"
            message2.messageType = Constant.MESSAGE_TEXT
            message2.ioType = Constant.TYPE_RECEIVE
            message2.number = "666666"
            message2.state = Constant.STATE_SUCCESS
            message2.time = DataUtil.getTimeSecond()
            DaoUtil.getInstance().addMessage(message2)
        }

    }

    override fun initData() {
        super.initData()  // 在父类初始化 viewModel
        // 全局数据变化监听
        viewModel.isConnectDevice.observe(this,object : Observer<Boolean?>{
            override fun onChanged(isConnect: Boolean?) {
                // 文字变化
                if(isConnect==true){
                    viewBinding.connectBluetooth.text = "断开连接"
                }else{
                    viewBinding.connectBluetooth.text = "点击连接北斗"
                }
                // 点击事件
                viewBinding.connectBluetoothGroup.setOnClickListener {
                    if(isConnect==true){
                        GlobalControlUtil.showAlertDialog("断开蓝牙？","当前已连接蓝牙，是否需要断开蓝牙",
                            onYesClick = {
                                BluetoothTransferUtil.getInstance().disconnectDevice();
                            }
                        )
                    }
                    else{
                        ARouter.getInstance().build(Constant.CONNECT_BLUETOOTH_ACTIVITY).navigation()  // 页面跳转
                    }
                }
            }
        })
        viewModel.deviceCardID.observe(this,{
            it?.let { loge("监听到卡号变化 $it") }
        })

        viewModel.getUnreadMessageCount().observe(this,{
            it?.let {
                viewBinding.unreadCount.text = it.toString()
            }
        })


    }



}