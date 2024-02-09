package com.bdtx.mod_util.Utils.Protocol;

import android.util.Log;

import com.bdtx.mod_data.Global.Constant;
import com.bdtx.mod_data.Global.Variable;
import com.bdtx.mod_data.ViewModel.MainVM;
import com.bdtx.mod_util.Utils.ApplicationUtils;
import com.bdtx.mod_util.Utils.DataUtils;
import com.bdtx.mod_util.Utils.GlobalControlUtils;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

// 协议工具：解析/封装
public class BDProtocolUtils {

    public static String TAG = "ProtocolUtil";
// 单例 -------------------------
    private static BDProtocolUtils bdProtocolUtils;
    public static BDProtocolUtils getInstance() {
        if(bdProtocolUtils == null){
            bdProtocolUtils = new BDProtocolUtils();
            bdProtocolUtils.mainVM = ApplicationUtils.INSTANCE.getGlobalViewModel(MainVM.class);
        }
        return bdProtocolUtils;
    }
    private MainVM mainVM;

// USB 串口专用接收数据解析方法：每次传过来的都是碎片数据，需要拼接
// $CCIC     A,1595   0044,0,1,33    3211*99
    private String datas = "";  // 需要拼接的总数据
    public void receiveData_fragment(String str){
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
    // 解析数据
    public void parseData(String intactData){
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
        } else if (data_str.contains("TCI")||data_str.contains("TXR")){  // 北斗三代通信信息
            BDMessage(values);
        } else if (data_str.contains("ZDX")){  // 盒子信息
            BDZDX(values);
        }

    // 其余类型的应该都是 RNSS 定位数据
        else {
            parseRNSS(values);
        }


    }

    // 解析 RNSS 位置数据
    public void parseRNSS(String[] values){
        try {
            // 拆分数据
            String head = values[0];
            if (head.contains("GGA")){
                if (values[6].equals("0")) return;  // 0 就是无效定位，不要
                double longitude = DataUtils.analysisLonlat(values[4]);
                double latitude = DataUtils.analysisLonlat(values[2]);
                double altitude = Double.parseDouble(values[9]);
                mainVM.getDeviceLatitude().postValue(latitude);
                mainVM.getDeviceLongitude().postValue(longitude);
                mainVM.getDeviceAltitude().postValue(altitude);
                Log.e(TAG, "解析 GGA 位置: "+longitude+"/"+latitude+"/"+altitude );
            }else if (head.contains("GLL") && values.length > 6){
                if (values[6].equals("V")) return;  // V - 无效  A - 有效
                double longitude = DataUtils.analysisLonlat(values[3]);
                double latitude = DataUtils.analysisLonlat(values[1]);
                mainVM.getDeviceLongitude().postValue(longitude);
                mainVM.getDeviceLatitude().postValue(latitude);
                Log.e(TAG, "解析 GLL 位置: "+longitude+"/"+latitude );
            }else if (head.contains("ZDA")){
                // ZDA消息，这里转发给接收机
            }
            else if (head.contains("RMC")){

            }else {
                Log.e(TAG, "收到其他RN指令: "+ Arrays.toString(values));
                return;
            }
        }catch (Exception e){
            Log.e(TAG, "parseRNSS: 解析错误");
            e.printStackTrace();
            return;
        }

    }

// 解析协议 -----------------------------------------

    // 北三 --------------------------------------
    // ICP 卡号、频度等
    public void BDICP(String[] value){
        try {
            String cardId = value[1];
            int cardFre = Integer.parseInt(value[14]);
            int cardLevel = -1;
            mainVM.getDeviceCardID().postValue(cardId);
            mainVM.getDeviceCardFrequency().postValue(cardFre);
            if(Integer.parseInt(value[15]) == 0){
                cardLevel = 5;  // 0就是5级卡
            }else {
                cardLevel = Integer.parseInt(value[15]);
            }
            mainVM.getDeviceCardLevel().postValue(cardLevel);
            Log.e(TAG, "BDICP 解析设备信息: 卡号-"+cardId+" 频度-"+cardFre+" 等级-"+cardLevel );
        }catch (Exception e){
            Log.e(TAG, "BDICP: 解析错误" + e.toString());
            e.printStackTrace();
            return;
        }

    }

    // PWI 功率信息
    public void BDPWI(String[] values){
        // 尽量用 try 避免线程中断
        try {
            int rdss2Count1 = Integer.parseInt(values[2]);
            int index = 2+rdss2Count1*3+1;
            int rdss3Count = Integer.parseInt(values[index]);
            index++;
            int s21[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            for (int i =0 ;i < rdss3Count; i++){
                int id = Integer.parseInt(values[index]);
                if (id > 21) continue;
                int number = Integer.parseInt(values[index+1]);
                s21[id-1] = number;
//                index += 3;  // 正常数据只要+3，这个版本是正常的
                if(values.length>index+3 && (values[index+3].equals("0") || values[index+3].equals(""))){
                    index += 4;
                } else {
                    index += 3;
                }
            }
            Log.e(TAG, "BDPWI 当前信号状况: "+Arrays.toString(s21) );
            Arrays.sort(s21);  // 排序
            int[] topTen = Arrays.copyOfRange(s21, s21.length - 10, s21.length);  // 取出 10 个最大信号
            mainVM.getSignal().postValue(topTen);
        }catch (Exception e){
            Log.e(TAG, "BDPWI: 解析错误" + e.toString());
            e.printStackTrace();
            return;
        }


    }

    // 通信申请后的反馈信息
    // [$BDFKI, TXA, Y, Y, 0, 0000]
    // [$BDFKI, 080432, TCQ, Y, 0, 0]  北三
    public void BDFKI(String[] values){
        try {
            boolean message_result = false;
            String type;  // 反馈的指令类型 ：只用 TCQ
            String result;  // 反馈结果 ： Y / N
            String reason;  // 失败原因
            type = values[2];  // 反馈的指令类型 ：只用 TCQ
            result = values[3];  // 反馈结果 ： Y / N
            reason = values[4];  // 失败原因
            if(result.equals("Y")){
                if(Variable.lastSendMsg!=null){Variable.lastSendMsg.setState(Constant.STATE_SUCCESS);}  // 修改状态
                GlobalControlUtils.INSTANCE.showToast("发送成功",0);
            }else {
                if(Variable.lastSendMsg!=null){Variable.lastSendMsg.setState(Constant.STATE_FAILURE);}
                String reason_str = "";
                switch ( reason ){
                    case "1":
                        reason_str = "频度未到，发射被抑制";
                        break;
                    case "2":
                        reason_str = "接收到系统的抑制指令，发射被抑制";break;
                    case "3":
                        reason_str = "当前设置为无线电静默状态，发射被抑制";break;
                    case "4":
                        reason_str = "功率未锁定";break;
                    case "5":
                        reason_str = "未检测到IC模块信息";break;
                    default:
                        reason_str = "发射失败，原因码是："+reason;
                        break;
                }
                GlobalControlUtils.INSTANCE.showToast(reason_str,0);
            }
        }catch (Exception e){
            Log.e(TAG, "BDFKI: 解析错误" + e.toString());
            e.printStackTrace();
            return;
        }


    }


    // 收到了 TCI 通信信息
    // [$BDTCI, 04207733, 4207733, 2, 023242, 2, 0, 90000000000065BEF749B2E2CAD4]
    // $BDTXR,1,4207733,1,2337,90000000000065C1FAF4B2E2CAD4*3F
    public String lastMsgFrom = "";
    public String lastMsgTime = "";
    public void BDMessage(String[] values){
        try {
            int from = 0;  // 带了个0，先转化为int
            int frequency_point = 0;
            String content = "000000";
            String time = "000000";
            String decode_type = "0";
            if(values[0].contains("TCI")){
                from = Integer.parseInt(values[1]);  // 带了个0，先转化为int
                frequency_point = Integer.parseInt(values[3]);
                content = values[7];
                time = values[4];
                decode_type = values[5];
            }else if(values[0].contains("TXR")){
                from = Integer.parseInt(values[2]);
                frequency_point = Integer.parseInt(values[3]);
                content = values[5];
                time = values[4];
                decode_type = values[3];
            }else {
                Log.e(TAG, "其他北三内容协议");
            }
            Log.i(TAG, "BDTCI: "+from+"/"+frequency_point+"/"+time+"/"+decode_type+"/"+content);
            if(lastMsgFrom.equals(values[1]) && lastMsgTime.equals(time)){Log.i(TAG, "重复消息，舍弃");return;}
            lastMsgFrom=values[1];lastMsgTime=time;  // 解决消息重复问题（同一秒内收到同一个号码的第二条消息）
            // 处理内容数据
            String header = content.substring(0,2);
            switch (header){
                case "90":
                    TDWTUtils.resolve90(from+"",content);
                    break;
                case "91":
                    TDWTUtils.resolve91(from+"",content);
                    break;
                case "92":
                    TDWTUtils.resolve92(from+"",content);
                    break;
                case "93":
                    TDWTUtils.resolve93(from+"",content);
                    break;
                case "A7":
                    TDWTUtils.resolveA7(from+"",content);
                    break;
                default:GlobalControlUtils.INSTANCE.showToast("收到其它消息："+content,0);
            }
        }catch (Exception  e){
            Log.e(TAG, "BDTCI: 解析错误" + e.toString());
            e.printStackTrace();
            return;
        }
    }

    // 收到了 ZDX 盒子信息
    public void BDZDX(String[] values){
        try {
            String cardId = values[1];
            int cardFre = Integer.parseInt(values[24]);
            int cardLevel = Integer.parseInt(values[25]);
            int batteryLevel = Integer.parseInt(values[2]);
            mainVM.getDeviceCardID().postValue(cardId);
            mainVM.getDeviceCardFrequency().postValue(cardFre);
            mainVM.getDeviceCardLevel().postValue(cardLevel);
            mainVM.getDeviceBatteryLevel().postValue(batteryLevel);
            Log.e(TAG, "BDZDX 解析设备信息: 卡号-"+cardId+" 频度-"+cardFre+" 等级-"+cardLevel+" 电量-"+batteryLevel );

            int s21[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            s21[0] = Integer.parseInt(values[3]);
            s21[1] = Integer.parseInt(values[4]);
            s21[2] = Integer.parseInt(values[5]);
            s21[3] = Integer.parseInt(values[6]);
            s21[4] = Integer.parseInt(values[7]);
            s21[5] = Integer.parseInt(values[8]);
            s21[6] = Integer.parseInt(values[9]);
            s21[7] = Integer.parseInt(values[10]);
            s21[8] = Integer.parseInt(values[11]);
            s21[9] = Integer.parseInt(values[12]);
            s21[10] = Integer.parseInt(values[13]);
            s21[11] = Integer.parseInt(values[14]);
            s21[12] = Integer.parseInt(values[15]);
            s21[13] = Integer.parseInt(values[16]);
            s21[14] = Integer.parseInt(values[17]);
            s21[15] = Integer.parseInt(values[18]);
            s21[16] = Integer.parseInt(values[19]);
            s21[17] = Integer.parseInt(values[20]);
            s21[18] = Integer.parseInt(values[21]);
            s21[19] = Integer.parseInt(values[22]);
            s21[20] = Integer.parseInt(values[23]);
            Arrays.sort(s21);  // 排序
            int[] topTen = Arrays.copyOfRange(s21, s21.length - 10, s21.length);  // 取出 10 个最大信号
            mainVM.getSignal().postValue(topTen);
            Log.e(TAG, "BDZDX 解析: "+Arrays.toString(s21) );
        }catch(Exception  e){
            Log.e(TAG, "BDZDX: 解析错误" + e.toString());
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


    // 查询 IC 信息：type - 0检测本机IC信息，1检测本机编组信息，2检测下属用户，3检测IC模块工作模式
    public static String CCICR(int type, String info) {
        Log.e("获取IC信息","CCICR");
        String command = "CCICR," + type + "," + info;
        return packaging(command);
    }

    // 通信申请
    // type - 发送模式：1、汉字 2、代码 3、混合 4、压缩汉字 5、压缩代码
    public static String CCTCQ( String cardNumber, int type, String hexMsg){
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



    // 打包，加上 $ 和 * 和 校验和，输出 hex_str
    public static String packaging(String tmp){
        String hexCommand = DataUtils.string2Hex(tmp);
        String hh = DataUtils.getCheckCode0007(hexCommand).toUpperCase();  // 检验和
        return "24"+hexCommand+"2A"+ DataUtils.string2Hex(hh)+"0D0A";
    }


}
