package com.bdtx.mod_util.Utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.pancoit.compression.ZDCompression;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

// 文件处理
public class FileUtils2 {

    //将字节数转化为MB
    @SuppressLint("DefaultLocale")
    public static String byteToMBStr(long size){
        long kb = 1024;
        long mb = kb*1024;


        float f = (float) size/mb;
        return String.format(f > 100 ?"%.2f":"%.2f",f);

    }
    @SuppressLint("DefaultLocale")
    public static String byteToMB(long size){
        long kb = 1024;
        long mb = kb*1024;
        long gb = mb*1024;
        if (size >= gb){
            return String.format("%.1f GB",(float)size/gb);
        }else if (size >= mb){
            float f = (float) size/mb;
            return String.format(f > 100 ?"%.0f MB":"%.1f MB",f);
        }else if (size > kb){
            float f = (float) size / kb;
            return String.format(f>100?"%.0f KB":"%.1f KB",f);
        }else {
            return String.format("%d B",size);
        }
    }
    public static String getFileMbSize(String filePath){
        String mb="0kb";
        try{
            mb= byteToMB(new File(filePath).length());

        }catch (Exception e){
            e.printStackTrace();
        }
        return mb;
    }
    public static String getFileMbSizeStr(String filePath){
        String mb="0kb";
        try{
            mb= byteToMBStr(new File(filePath).length());
        }catch (Exception e){
            e.printStackTrace();
        }
        return mb;
    }
    public static void saveBitmapFile(Bitmap bitmap, String path){
        File file=new File(path);//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取指定文件大小
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    private static int getFileSize(String filePath) throws Exception {
        int size = 0;
        File file=new File(filePath);
        if (file.exists()) {
            FileInputStream fis =new FileInputStream(file);
            size = fis.available();
        } else {
            Log.e("获取文件大小","文件不存在!");
        }
        return size;
    }
    public static int getPcmSeconds(String filePath){
        long seconds=0;
        try {
            long kb = new File(filePath).length();
            seconds=kb/16000;
        }catch (Exception e){
            e.printStackTrace();
        }
        return (int)seconds;
    }

    /**
     * 公网对讲时长获取，向上取整
     * @param filePath
     * @return
     */
    public static int getVoicePcmSeconds(String filePath){
        int seconds=0;
        try {
            long kb = new File(filePath).length();
            seconds = (int)Math.ceil((double) kb / 16000);
        }catch (Exception e){
            e.printStackTrace();
        }
        return seconds;
    }


    //质量压缩
    public static  Bitmap compressBmpFromBmp(Bitmap image,int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        image.compress(Bitmap.CompressFormat.JPEG, quality, baos);

        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());

        return BitmapFactory.decodeStream(isBm, null, null);
    }
    //高宽裁剪
    public static Bitmap createScaleBitmap(Bitmap bmpSrc, int targetWidth, int targetHeight) {
        //Bitmap bmpSrc = BitmapFactory.decodeFile(path);
        int srcWidth = bmpSrc.getWidth();
        int srcHeight = bmpSrc.getHeight();
        float scale = 1;
        float widthScale = targetWidth * 1.0f / srcWidth;
        float heightScale = targetHeight * 1.0f / srcHeight;
        if(widthScale < heightScale){
            scale = widthScale;
        }else{
            scale = heightScale;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale, 0, 0);
        // 如需要可自行设置 Bitmap.Config.RGB_8888 等等
        Bitmap bmpRet = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmpRet);
        // 如需要可自行设置 Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG 等等
        Paint paint = new Paint();
        canvas.drawBitmap(bmpSrc, matrix, paint);
        return bmpRet;
    }
    public static Bitmap compressPx(Bitmap bitmap, int radio) {
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth() / radio, bitmap.getHeight() / radio, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        RectF rectF = new RectF(0, 0, bitmap.getWidth()/radio, bitmap.getHeight()/radio);//将原图画在缩放之后的矩形上
        canvas.drawBitmap(bitmap, null, rectF, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        ByteArrayInputStream isBm = new ByteArrayInputStream(bos.toByteArray());
        return BitmapFactory.decodeStream(isBm, null, null);
    }
    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param delFile 要删除的文件夹或文件名
     * @return 删除成功返回true，否则返回false
     */
    public static boolean delete(String delFile) {
        File file = new File(delFile);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile())
                return deleteSingleFile(delFile);
            else
                return deleteDirectory(delFile);
        }
    }
    /**
     * 删除目录及目录下的文件
     *
     * @param filePath 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    private static boolean deleteDirectory(String filePath) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator))
            filePath = filePath + File.separator;
        File dirFile = new File(filePath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (File file : files) {
            // 删除子文件
            if (file.isFile()) {
                flag = deleteSingleFile(file.getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (file.isDirectory()) {
                flag = deleteDirectory(file
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            Log.e("--Method--", "Copy_Delete.deleteDirectory: 删除目录" + filePath + "成功！");
            return true;
        } else {
            return false;
        }
    }
    /**
     * 删除单个文件
     *
     * @param filePath$Name 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    private static boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.e("--Method--", "Copy_Delete.deleteSingleFile: 删除单个文件" + filePath$Name + "成功！");
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    /**
     * 保存图片
     *
     * @param bitmap      图片
     * @param fileName    文件名
     * @param fileDirName 文件夹名称
     * @throws IOException
     */
    public static void saveFile(final Bitmap bitmap, final String fileName, final String fileDirName) throws IOException {
        new Thread(() -> {
            try {
                if (bitmap != null) {
                    File dirFile = new File(fileDirName);
                    Log.e("fileDirName:",fileDirName);
                    if (!dirFile.exists()) {
                        dirFile.mkdir();
                    }
                    File myCaptureFile = new File(fileDirName + fileName);
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                    bos.flush();
                    bos.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    /**
     * 瓦片坐标转换
     *
     * @param tileX         x轴
     * @param tileY         y轴
     * @param levelOfDetail 缩放等级
     * @return
     */
    public static String TileXYToQuadKey(int tileX, int tileY, int levelOfDetail) {
        StringBuilder quadKey = new StringBuilder();
        for (int i = levelOfDetail; i > 0; i--) {
            char digit = '0';
            int mask = 1 << (i - 1);
            if ((tileX & mask) != 0) {
                digit++;
            }
            if ((tileY & mask) != 0) {
                digit++;
                digit++;
            }
            quadKey.append(digit);
        }
        return quadKey.toString();
    }
    public static  Bitmap compressImageFromFile(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;//只读边,不读内容
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = 1080f;//
        float ww = 640f;//
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置采样率

        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;//该模式是默认的,可不设
        newOpts.inPurgeable = true;// 同时设置才会有效
        newOpts.inInputShareable = true;//。当系统内存不够时候图片自动被回收

        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        //其实是无效的,大家尽管尝试
        return bitmap;
    }
    /**
     * 根据分辨率压缩图片比例
     *
     * @param imgPath
     * @param w
     * @param h
     * @return
     */
    public static Bitmap compressByResolution(String imgPath, int w, int h) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, opts);

        int width = opts.outWidth;
        int height = opts.outHeight;
        int widthScale = width / w;
        int heightScale = height / h;

        int scale;
        if (widthScale < heightScale) { //保留压缩比例小的
            scale = widthScale;
        } else {
            scale = heightScale;
        }

        if (scale < 1) {
            scale = 1;
        }
        Log.e("compressByResolution:","图片分辨率压缩比例：" + scale);

        opts.inSampleSize = scale;

        opts.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(imgPath, opts);
    }
    public static void saveFile(Bitmap bmp, String otherPath){
        try{
            BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(new File(otherPath)));
            bmp.compress(Bitmap.CompressFormat.JPEG,80,bos);
            bos.flush();
            bos.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
   /* public static String zipImageByQuality(String imgUrl, Context context,String destinationDirectoryPat) {
        try {
            return   new Compressor(context)
                    .setMaxWidth(1280)
                    .setMaxHeight(800)
                    .setQuality(80)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setDestinationDirectoryPath(destinationDirectoryPat)
                    .compressToFile(new File(imgUrl)).getPath();

        } catch (Exception e) {
            e.printStackTrace();

        }
        return imgUrl;
    }*/
    /**
     * Uri转File
     *
     * @param uri
     * @param context
     * @return
     */
    public static File getFileByUri(Uri uri, Context context) {
        String path = null;
        if ("file".equals(uri.getScheme())) {
            path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = context.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=").append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA}, buff.toString(), null, null);
                int index = 0;
                int dataIdx = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    index = cur.getInt(index);
                    dataIdx = cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    path = cur.getString(dataIdx);
                }
                cur.close();
                if (index == 0) {
                } else {
                    Uri u = Uri.parse("content://media/external/images/media/" + index);
                    //Log.i("InfoMessage", "temp uri is :" + u);
                }
            }
            if (path != null) {
                return new File(path);
            }
        } else if ("content".equals(uri.getScheme())) {
            // 4.2.2以后
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
            cursor.close();

            return new File(path);
        } else {
            //Log.i(TAG, "Uri Scheme:" + uri.getScheme());
        }
        return null;
    }
    /**
     * 文件转byte
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] InputStream2ByteArray(File file) throws IOException {

        InputStream in = new FileInputStream(file);
        byte[] data = toByteArray(in);
        in.close();
        return data;
    }
    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n = 0;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }
    /**
     * Bitmap转换成byte[]并且进行压缩,压缩到不大于maxkb
     * @param bitmap
     * @param maxkb
     * @return
     */
    public static byte[] bmpToByteArray(Bitmap bitmap, int maxkb) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        int options = 100;
        while (output.toByteArray().length > maxkb&& options != 10) {
            output.reset(); //清空output
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, output);//这里压缩options%，把压缩后的数据存放到output中
            options -= 10;
        }
        return output.toByteArray();
    }

    public static Bitmap revitionImageSize(String path) throws IOException {

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(path)));
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, options);
        in.close();
//      int i = 0;
        Bitmap bitmap = null;

        int wRatio = (int) Math.ceil(options.outWidth / (float) 720);
        int hRatio = (int) Math.ceil(options.outHeight / (float) 1280);

        if (wRatio > 1 && hRatio > 1) {
            if (wRatio > hRatio) {
                options.inSampleSize = wRatio;
            } else {
                options.inSampleSize = hRatio;
            }
        }

        in = new BufferedInputStream(new FileInputStream(new File(path)));
        options.inJustDecodeBounds = false;

        // 杯具的老戳手机-1G以下的内存的某些手机无法加载高清图片大于1M以上，只能加大压缩力度
        try{
            bitmap = BitmapFactory.decodeStream(in, null, options);
        }catch (Exception e) {
            e.printStackTrace();

            wRatio = (int) Math.ceil(options.outWidth / (float) 480);
            hRatio = (int) Math.ceil(options.outHeight / (float) 800);

            if (wRatio > 1 && hRatio > 1) {
                if (wRatio > hRatio) {
                    options.inSampleSize = wRatio;
                } else {
                    options.inSampleSize = hRatio;
                }
            }

            in = new BufferedInputStream(new FileInputStream(new File(path)));
            options.inJustDecodeBounds = false;

            bitmap = BitmapFactory.decodeStream(in, null, options);
        }
        return bitmap;
    }
}
