package com.bdtx.mod_util.Utils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 高位在前地位在后
 */
// 数据处理工具
public class DataUtils {

    public static final String GB18030 = "GB18030";
    /**
     * bytes 转十六进制字符串
     * @param bytes
     * @return
     */
    public static String bytes2Hex(byte[] bytes){
        String hex = "";
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xff;
            String hexVaule = Integer.toHexString(value);
            if (hexVaule.length() < 2) {
                hexVaule = "0" + hexVaule;
            }
            hex += hexVaule;
        }
        return hex;
    }

    public static String byte2hex(byte b) {
        int value = b & 0xff;
        String hexVaule = Integer.toHexString(value);
        if (hexVaule.length() < 2) {
            hexVaule = "0" + hexVaule;
        }
        return hexVaule;
    }
    /**
     * 16进制字符串转int
     *
     * @param hex 16进制字符串
     * @return
     */
    public static int hex2Int(String hex) {
        byte[] bytes = hex2bytes(hex);
        return bytes2int(bytes);
    }
    public static String hex2IntString(String hex) {
        byte[] bytes = hex2bytes(hex);
        return String.valueOf(bytes2int(bytes));
    }
    /**
     * 16进制转换成Long类型
     *
     * @param hex
     * @return
     */
    public static long hex2Long(String hex) {
        byte[] bytes = hex2bytes(hex);
        return bytes2long(bytes);
    }
    /**
     * 16进制转String
     *
     * @param hex
     * @return
     */
    public static String hex2String(String hex) {
        byte[] bytes = hex2bytes(hex);
        return bytes2string(bytes);
    }

    /**
     * string转16进制
     *
     * @return
     */
    public static String string2Hex(String str) {
        String hex;
        try {
            byte[] bytes = string2bytes(str, GB18030);
            hex = bytes2Hex(bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return hex.toUpperCase();
    }
    /**
     * String转bytes
     *
     * @param str
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     */
    public static byte[] string2bytes(String str, String charset) throws UnsupportedEncodingException {
        if (str == null) {
            return null;
        }
        return str.getBytes(charset);
    }

    /**
     * 16进制转bytes
     *
     * @param hex
     * @return
     */
    public static byte[] hex2bytes(String hex) {
        if (hex == null || hex.length() < 1) {
            return null;
        }
        // 如果长度不是偶数，则前面补0
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        byte[] bytes = new byte[(hex.length() + 1) / 2];
        try {
            for (int i = 0, j = 0; i < hex.length(); i += 2) {
                byte hight = (byte) (Character.digit(hex.charAt(i), 16) & 0xff);
                byte low = (byte) (Character.digit(hex.charAt(i + 1), 16) & 0xff);
                bytes[j++] = (byte) (hight << 4 | low);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytes;
    }

    /**
     * bytes转int
     *
     * @param bytes
     * @return int
     */
    public static int bytes2int(byte[] bytes) {
        int intData = 0;
        int len = bytes.length <= 4 ? bytes.length : 4;
        for (int i = 0; i < len; i++) {
            intData <<= 8;
            intData |= (bytes[i] & 0xff);
        }
        return intData;
    }

    /**
     * bytes转long
     *
     * @param bytes 数组从左到右，依次是高位到低位
     * @return long
     */
    public static long bytes2long(byte[] bytes) {
        long longData = 0;
        int len = bytes.length <= 8 ? bytes.length : 8;
        for (int i = 0; i < len; i++) {
            longData <<= 8;
            longData |= (bytes[i] & 0xff);
        }
        return longData;
    }

    /**
     * bytes转String
     *
     * @param bytes
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String bytes2string(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        String newStr = null;
        try {
            newStr = new String(bytes, GB18030).trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return newStr;
    }

    /**
     * int转16进制
     *
     * @param i
     * @return
     */
    public static String int2Hex(int i) {
        String hex;
        byte[] bytes = int2bytes(i);
        hex = bytes2Hex(bytes);
        return hex;
    }

    /**
     * long转16进制
     *
     * @param l
     * @return
     */
    public static String long2Hex(long l) {
        String hex;
        byte[] bytes = long2bytes(l);
        hex = bytes2Hex(bytes);
        return hex;
    }

    /**
     * long转byte[],数组从左到右，数组从左到右，高位到低位
     *
     * @param longData
     * @return byte[]:byteData
     */
    public static byte[] long2bytes(long longData) {
        byte[] byteData = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = i * 8;
            byteData[i] = (byte) (longData >> offset);
        }
        reversalBytes(byteData);
        return byteData;
    }

    /**
     * int转byte[],数组从左到右，数组从左到右，高位到低位
     *
     * @param intData
     * @return byte[]:byteData
     */
    public static byte[] int2bytes(int intData) {
        byte[] byteData = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = i * 8;
            byteData[i] = (byte) (intData >> offset);
        }
        reversalBytes(byteData);
        return byteData;
    }

    /**
     * 翻转byte数组
     *
     * @param bytes
     */
    public static void reversalBytes(byte[] bytes) {
        byte b = 0;
        int length = bytes.length;
        int half = length / 2;
        for (int i = 0; i < half; i++) {
            b = bytes[i];
            bytes[i] = bytes[length - 1 - i];
            bytes[length - 1 - i] = b;
        }
    }
    /**
     * 将char转换为HexByte
     *
     * @param chr
     * @return
     */
    public static byte char2HexByte(char chr) {
        byte chrRet = -1;
        if (chr >= '0' && chr <= '9') {
            chrRet = (byte) chr;
        } else if (chr >= 'A' && chr <= 'F') {
            chrRet = (byte) (chr - 65 + 10);
        } else if (chr >= 'a' && chr <= 'f') {
            chrRet = (byte) (chr - 97 + 10);
        }
        return chrRet;
    }

    /**
     * 截取对应长度--不足前面补0
     * @param value
     * @param len
     * @return
     */
    public static String toLength(String value,int len){
        String str = value;
        if (str.length() > len){
            return str.substring(str.length()-len);
        }else {
            while (str.length() != len){
                str = "0"+str;
            }
        }
        return str;
    }


    /**
     * 加密算法
     *
     * @param password 密码
     * @return 加密密码
     */
    public static String encryption(String password) {
        //随机产生6个字节位加密因子
        Date d = new Date();
        SimpleDateFormat sd = new SimpleDateFormat("yyMMddhhmmss");
        String jmiy = sd.format(d);//加密因子
        byte[] key = password.getBytes();//原密码字节
        byte[] A = DataUtils.hex2bytes(jmiy);//加密因子字节
        byte[] Xkey = new byte[6];
        //利用加密因子对key[]逐字节一一进行异或运算
        for (int i = 0; i < 6; i++) {
            Xkey[i] = (byte) (key[i] ^ A[i]);
        }
        //再将每个字节的高4位和低四位进行交换， 得到新密文Xkey[K6,K5,K4,K3,K2,K1].
        for (int i = 0; i < 6; i++) {
            //高四位
            int h4 = (Xkey[i] << 4) & 0xF0;
            //低四位
            int l4 = (Xkey[i] >> 4) & 0x0F;
            Xkey[i] = (byte) (l4 + h4);
        }
        //对加密因子A[]进行取反, 得到新的加密因子数组XA[A6,A5,A4,A3,A2,A1]。
        byte[] XA = new byte[6];
        for (int i = 0; i < 6; i++) {
            XA[i] = (byte) (~A[i]);
        }
        //将密码和加密因子按照如下规则排列,得到新的总体密文 SecretData[  K6,A1,  K5,A2,  K4,A3, K3,A4,  K2,A5  K1,A6 ]
        byte[] SecretData = new byte[12];
        for (int i = 0; i < 6; i++) {
            SecretData[i * 2] = Xkey[i];
            SecretData[i * 2 + 1] = XA[5 - i];
        }
        return DataUtils.bytes2Hex(SecretData);
    }

    /**
     * 获取检验和
     *
     * @param strProtocol
     * @return
     */
    public static String getCheckCode0007(String strProtocol) {
        strProtocol.replace(" ",  "");
        byte chrCheckCode = 0;
        for (int i = 0; i < strProtocol.length(); i += 2) {
            char chrTmp ;
            chrTmp = strProtocol.charAt(i);
            if (chrTmp == ' ') continue;
            byte chTmp1 = (byte) (DataUtils.char2HexByte(chrTmp) << 4);
            chrTmp = strProtocol.charAt(i + 1);
            byte chTmp2 = (byte) (chTmp1 + (DataUtils.char2HexByte(chrTmp) & 15));
            chrCheckCode = i == 0 ? chTmp2 : (byte) (chrCheckCode ^ chTmp2);
        }
        String strHexCheckCode = String.format("%x", Byte.valueOf(chrCheckCode));
        if ((strHexCheckCode = strHexCheckCode.toUpperCase()).length() != 2) {
            if (strHexCheckCode.length() > 2) {
                strHexCheckCode = strHexCheckCode.substring(strHexCheckCode.length() - 2);
            } else if (strHexCheckCode.length() < 2 && strHexCheckCode.length() > 0) {
                strHexCheckCode = "0" + strHexCheckCode;
            }
        }
        return strHexCheckCode;
    }

    /**
     * 解析时间 从2000年开始
     * @param hex 12位，每2位代表 年 月 日 时 分 秒
     * @return
     */
    public static String parsingDate(String hex){
        // yyyy-MM-dd HH:mm:ss
        int yy = DataUtils.hex2Int(hex.substring(0,2));
        int MM = DataUtils.hex2Int(hex.substring(2,4));
        int dd = DataUtils.hex2Int(hex.substring(4,6));
        int HH = DataUtils.hex2Int(hex.substring(6,8));
        int mm = DataUtils.hex2Int(hex.substring(8,10));
        int ss = DataUtils.hex2Int(hex.substring(10,12));
        String itmer = String.valueOf(2000+yy) + "-" + MM + "-" + dd + " " + HH +":"+mm+":"+ss;
        return itmer;
    }
    /**
     * 解析经/纬度
     * @param hex 8位，每2位代表 度 分 秒 小秒
     * @return
     */
    public static double parsingLnt(String hex){
        int d = DataUtils.hex2Int(hex.substring(0,2));
        int m = DataUtils.hex2Int(hex.substring(2,4));
        int s = DataUtils.hex2Int(hex.substring(4,6));
        int ds = DataUtils.hex2Int(hex.substring(6,8));
        String ss = String.format("%d.%d",s,ds);
        double lnt = d + (m/60.0)+(Double.valueOf(ss)/3600.0);
        return lnt;
    }

    /**
     * 经纬度单位换算  - 度 转 分
     * @param degree
     * @return
     */
    public static String degree2Points(double degree){
        int vlaues[] = degreeBreakup(degree);
        int dd = vlaues[0];
        int mm = vlaues[1];
        int ss = vlaues[2];
        int ms = vlaues[3];
        return String.format("%d°.%d′%d.%d″",dd,mm,ss,ms);
    }

    public static int[] degreeBreakup(double degree){
        int dd = (int)Math.abs(degree);
        double v = (Math.abs(degree) - dd)* 60;
        int mm = (int)v;
        double sv = (v - mm)*60;
        int ss = (int)sv;
        int ms = (int)(sv-ss);
        int values[] = {dd,mm,ss,ms};
        return values;
    }

    /**
     * 格式 dddmm.mmmmmm 转 dd.dddddddd
     * @param value dddmm.mmmmmm
     * @return
     */
    public static double analysisLonlat(String value){
        double lonlat = Double.valueOf(value);
        int dd = (int)lonlat / 100;
        int mm = (int)lonlat % 100;
        double ms = lonlat - (int)lonlat;
        return dd+((mm+ms)/60.0);
    }

// -----------------------------------------

    public static final String FORMAT_DATE_YMDHMS = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_DATE_SERIAL = "yyMMddHHmmss";
    private static final String FORMAT_DATE_YMD = "yyyy-MM-dd";
    private static final String FORMAT_DATE_YMDHM = "yyyy-MM-dd HH:mm";

    // 时间戳转字符串
    public static String timeStampToString(long seconds) {
        return timeStampToString(seconds,FORMAT_DATE_YMDHMS);
    }
    public static String timeStampToString(long seconds,String forma) {
        SimpleDateFormat sdf = new SimpleDateFormat(forma);
        return sdf.format(new Date(seconds*1000));
    }

    // 获取秒时间戳
    public static long getTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }
    public static long getTimeMillis() {
        return System.currentTimeMillis();
    }

    // yyyy-MM-dd HH:mm:ss
    public static String getTimeString(){
        return timeStampToString(getTimeSeconds());
    }

    public static String getTimeSerial() {
        return timeStampToString(getTimeSeconds(),FORMAT_DATE_SERIAL);
    }

    public static long stringToTimeStamp(String dateStr, boolean isPreciseTime) {
        return stringToTimeStamp(dateStr, getFormatPattern(isPreciseTime));
    }

    private static long stringToTimeStamp(String dateStr, String pattern) {
        try {
            return new SimpleDateFormat(pattern, Locale.CHINA).parse(dateStr).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    private static String getFormatPattern(boolean showSpecificTime) {
        if (showSpecificTime) {
            return FORMAT_DATE_YMDHM;
        } else {
            return FORMAT_DATE_YMD;
        }
    }

    // 高位补0
    public static String padWithZeros(String input, int desiredLength) {
        int inputLength = input.length();
        if (inputLength >= desiredLength) {
            return input; // 如果输入长度大于等于指定长度，直接返回输入字符串
        }
        int zerosToAdd = desiredLength - inputLength; // 需要补充的0的个数
        StringBuilder paddedString = new StringBuilder();
        // 在前面添加补充的0
        for (int i = 0; i < zerosToAdd; i++) {
            paddedString.append('0');
        }
        paddedString.append(input); // 添加原始字符串
        return paddedString.toString();
    }
}
