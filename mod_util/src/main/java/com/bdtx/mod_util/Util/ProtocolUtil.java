package com.bdtx.mod_util.Util;

import android.util.Log;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

// 协议工具：解析/封装
public class ProtocolUtil {

    public static String TAG = "ProtocolUtil";
// 单例 -------------------------
    private static ProtocolUtil protocolUtil;
    public static ProtocolUtil getInstance() {
        if(protocolUtil == null){
            protocolUtil = new ProtocolUtil();
        }
        return protocolUtil;
    }

    private static String datas = "";  // 需要拼接的总数据

// USB 串口专用接收数据解析方法：每次传过来的都是碎片数据，需要拼接
// $CCIC     A,1595   0044,0,1,33    3211*99
    public static void receiveData_fragment(String str){
//        Log.e(TAG, "receiveData_fragment: "+str );

        // 拼接数据
        datas +=  str;
        // 找到 $ 符，如果没有的话，这条数据就丢弃，重置数据，如果有的话就从 $ 开始截取到后面的所有字符
        int startIndex = datas.indexOf("$");
        if (startIndex < 0){
            datas = "";
            return;
        }
        if (startIndex > 0) {
            datas = datas.substring(startIndex);
        }
        // 如果长度太短？
        if (datas.length() < 6) return;

        // 找到 * 符，如果没有代表接收的是中间的数据，退出接收下一串，如果有代表这是这条指令的最后一段 *+校验
        int endIndex = datas.indexOf("*");
        if( endIndex < 1 ) return;

        String intactData;
        // 先判断一下长度，不然的话长度不足会断掉
        if(datas.length() < endIndex+3){
            intactData = datas;
        }else {
            intactData = datas.substring(0,endIndex+3);  // 截出 $---*66
            datas = datas.substring(endIndex+3);  // 多出来的是下一条指令，保留到下一次用
        }

        parseData(intactData);

    }

// RS232 串口专用接收数据解析方法：每次传过来的都是整块的数据，直接解析
// $CCICA,15950044,0,1,333211*99
    public static void receiveData_monoblock(String str){
        parseData(str);
    }



    // 解析数据
    public static void parseData(String intactData){
//        Log.i(TAG, "收到数据 parseData: "+ intactData);
        // 拆分数据
        int xorIndex = intactData.indexOf("*");
        if(xorIndex == -1){
            return;
        }
        String data_str = intactData.substring(0, xorIndex);  // 截取到 * 之前，例：$CCICR,0,00
        if(!data_str.contains(",")){
            return;
        }
        String[] values = data_str.split(",", -1);  // , 分割，例：["$CCICR","0","00"]
        Log.e(TAG, "在解析的数据 parseData: "+ Arrays.toString(values));
        if (data_str.contains("FKI")) {  // 反馈信息
            BDFKI(values);
        }else if (data_str.contains("ICP")) {  // IC 信息
            BDICP(values);
        } else if (data_str.contains("PWI")) {  // 波束信息
            BDPWI(values);
        } else if (data_str.contains("TCI")){  // 北斗三代通信信息
            BDTCI(values);
        } else if (data_str.contains("ZDX")){  // 盒子信息
            BDZDX(values);
        }

    // 其余类型的应该都是 RNSS 定位数据
        else {
            parseRNSS(values,intactData);
        }


    }

    // 解析 RNSS 位置数据
    public static void parseRNSS(String[] values , String raw_str){
        try {
            // 拆分数据
            String head = values[0];

            if (head.contains("GGA") && values.length > 9){
                if (values[6].equals("0")) return;  // 0 就是无效定位，不要
                if(values[9] != null || !values[9].equals("")){
                }else {
                }
            }else if (head.contains("GLL") && values.length > 6){
                if (values[6].equals("V")) return;  // V - 无效  A - 有效

            }else if (head.contains("GNS")){

                // GNS消息，这里转发给接收机

            }else if (head.contains("VTG")){

                // VTG消息，这里转发给接收机

            }else if (head.contains("ZDA")){

                // ZDA消息，这里转发给接收机

            }
            else if (head.contains("RMC")){


            }else if (head.contains("BAX")){
                String power = values[1];  // 拿到电量
                // 设置参数
            }else {
                return;
            }
        }catch (Exception e){
            Log.e(TAG, "parseRNSS: 解析错误" + e.toString());
            e.printStackTrace();
            return;
        }

    }

// 解析协议 -----------------------------------------

    // 北三 --------------------------------------
    // ICP 卡号、频度等
    public  static void BDICP(String[] value){

        try {

        }catch (Exception e){
            Log.e(TAG, "BDICP: 解析错误" + e.toString());
            e.printStackTrace();
            return;
        }

    }

    // PWI 功率信息
    public static void BDPWI(String[] values){
        // 尽量用 try 避免线程中断
        try {
            if(values.length < 3){
                return;
            }
            int rdss2Count1 = Integer.parseInt(values[2]);  // 0
            int index = 2 + (rdss2Count1*3) + 1;  // 3
            int rdss3Count = Integer.parseInt(values[index]);  // 3
            index++;  // 4
            int s21[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            for (int i = 0 ;i < rdss3Count; i++) {  // 0,1,2,3
                if(values.length < index+2){
                    return;  // 越界检测
                }
                int id = Integer.parseInt(values[index]);  // 18
                if (id > 21 || id <= 0) continue;
                int number = Integer.parseInt(values[index+1]);
                s21[id-1] = number;
                index += 4;
            }
            Log.e(TAG, "当前信号状况: "+Arrays.toString(s21) );
        }catch (Exception e){
            Log.e(TAG, "BDPWI: 解析错误" + e.toString());
            e.printStackTrace();
            return;
        }


    }

    // 通信申请后的反馈信息
    // [$BDFKI, TXA, Y, Y, 0, 0000]
    public static void BDFKI(String[] values){
        try {
            boolean message_result = false;
            String type;  // 反馈的指令类型 ：只用 TCQ
            String result;  // 反馈结果 ： Y / N
            String reason;  // 失败原因
            type = values[2];  // 反馈的指令类型 ：只用 TCQ
            result = values[3];  // 反馈结果 ： Y / N
            reason = values[4];  // 失败原因
            if(result.equals("Y")){
                GlobalControlUtil.INSTANCE.showToast("发送成功",0);
            }else {
                switch ( reason ){
                    case "1":
                        GlobalControlUtil.INSTANCE.showToast("频度未到，发射被抑制",0);
                        break;
                    case "2":
                        GlobalControlUtil.INSTANCE.showToast("接收到系统的抑制指令，发射被抑制",0);
                        break;
                    case "3":
                        GlobalControlUtil.INSTANCE.showToast("当前设置为无线电静默状态，发射被抑制",0);
                        break;
                    case "4":
                        GlobalControlUtil.INSTANCE.showToast("功率未锁定",0);
                        break;
                    case "5":
                        GlobalControlUtil.INSTANCE.showToast("未检测到IC模块信息",0);
                        break;
                    default:
                        GlobalControlUtil.INSTANCE.showToast("发射失败，原因码是：" + reason,0);
                        break;
                }
            }
            final boolean result_b = result.equals("Y");
            Log.e(TAG, "通信成功: "+result_b );

        }catch (Exception e){
            Log.e(TAG, "BDFKI: 解析错误" + e.toString());
            e.printStackTrace();
            return;
        }


    }


    // 收到了 TCI 通信信息
    public static void BDTCI(String[] values){
        try {

        }catch (Exception  e){
            Log.e(TAG, "BDTCI: 解析错误" + e.toString());
            e.printStackTrace();
            return;
        }
    }

    // 收到了 ZDX 盒子信息
    public static void BDZDX(String[] values){
        try {

        }catch (Exception  e){
            Log.e(TAG, "BDTCI: 解析错误" + e.toString());
            e.printStackTrace();
            return;
        }
    }


// 协议封装 --------------------------------------

    // 北三 ----------------------------------------
    // 开关指令：instruce - 目标语句
    // mode -  1关闭指定语句，2打开指定语句，3关闭全部语句，4打开全部语句  3-4目标语句为空
    // fre - 输出频度
    public static String CCRMO(String instruce, int mode, int fre) {
        String command;
        if(mode == 1 || mode == 3){
            command = "CCRMO,"+instruce+"," + mode + ",";
        }else {
            command = "CCRMO,"+instruce+"," + mode + "," + fre;
        }
        return packaging(command);
    }

    // RNSS输出频度指令：星宇设备
    // 共有 6 个，每个代表对应指令的输出频度，最大为 9
    public static String  CCRNS(int GGA, int GSV,int GLL,int GSA,int RMC,int ZDA){
        String command = "CCRNS,"+GGA+","+GSV+","+GLL+","+GSA+","+RMC+","+ZDA;
//        String command = "CCRNS,"+GGA+","+GSV+","+GLL+","+GSA+","+RMC+","+ZDA+",0";  // 最后一个是时区，不填
        return packaging(command);
    }

    // RNSS输出频度指令：星宇设备
    // 共有 6 个，每个代表对应指令的输出频度，最大为 9
    public static String PCAS03(int GGA, int GLL,int GSA,int GSV,int RMC,int VTG,int ZDA,int GRS){
        String command = "PCAS03,"+GGA+","+GLL+","+GSA+","+GSV+","+RMC+","+VTG+","+ZDA+","+GRS;
        return packaging(command);
    }


    // 查询 IC 信息：type - 0检测本机IC信息，1检测本机编组信息，2检测下属用户，3检测IC模块工作模式
    public static String CCICR(int type, String info) {
        Log.e("获取IC信息","CCICR");
        String command = "CCICR," + type + "," + info;
        return packaging(command);
    }

    // 通信申请
    // type - 发送模式：1、汉字 2、代码 3、混合 4、压缩汉字 5、压缩代码
    public static String CCTCQ(int type , String cardNumber , String hexMsg){
        String command = "CCTCQ," + cardNumber + ",2,1," + type + "," + hexMsg + ",0";
        return packaging(command);
    }

    // 登录
    public static String CCPWD() {
        String command = "CCPWD,5,000020";
//        String command = "CCPWD,5,000000";
        return packaging(command);
    }

    // 关闭盒子自带的上报
    public static String CCPRS() {
        String command = "CCPRS,15950044,3,60,0";
        return packaging(command);
    }

    // 设置输出终端信息频率
    public static String CCZDC(int frequency) {
        String command = "CCZDC,"+frequency;
        return packaging(command);
    }


// 倒计时 -------------------------------------------------------
    public int countdown = -1;
    //计时器
    private Timer timer;
    // 默认倒计时方法：倒计时60秒
    public void startCountdown(){
        int count = 60;
        startCountdown(count);
    }

    // 自定义倒计时方法：传入一个计时数值
    public void startCountdown(int count){
        this.countdown = count+1;
        cancelCountdown();
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                countdown--;
                // 开启线程消息中心发送广播进入倒计时
                if (countdown < 0){
                    cancelCountdown();
                    return;
                }
            }
        };
        timer.schedule(task, 0, 1005);
    }

    public void cancelCountdown(){
        if (timer != null){
            timer.cancel();
            timer = null;
        }
    }

// -----------------------------------------------------------------------
    // 打包，加上 $ 和 * 和 校验和，输出 hex_str
    public static String packaging(String tmp){
        String hexCommand = DataUtil.string2Hex(tmp);
        String hh = DataUtil.getCheckCode0007(hexCommand).toUpperCase();  // 检验和
        return "24"+hexCommand+"2A"+DataUtil.string2Hex(hh)+"0D0A";
    }


}
