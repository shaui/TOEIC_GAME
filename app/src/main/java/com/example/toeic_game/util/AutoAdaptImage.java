package com.example.toeic_game.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AutoAdaptImage {

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){

        /*取得圖片width,heigth*/
        int imgWidth = options.outWidth;
        int imgHeight = options.outHeight;
        int inSampleSize = 1;

        if(imgWidth > reqWidth || imgHeight > reqHeight){
            int widthRatio = Math.round( (float)imgWidth / (float)reqWidth  );
            int heightRatio = Math.round( (float)imgHeight / (float)reqHeight  );
            inSampleSize = widthRatio > heightRatio ? widthRatio : heightRatio;
        }
        System.out.println("size"+inSampleSize);
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resImg,
                                                         int reqWidth, int reqHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        //防止bitmap佔用內存分配，為了先去取得長寬比，重新調整size
        options.inJustDecodeBounds = true;
        //计算inSampleSize值
        BitmapFactory.decodeResource(res, resImg, options);
        //重新設定inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        /*要設定回false，才能使用內存*/
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(res, resImg, options);
    }
}
