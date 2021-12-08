package com.fntj.app.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

import com.fntj.lib.zb.util.JSON;

import java.util.HashMap;
import java.util.Map;

public class DeviceUtils {

    public static void init(Context context) {
        screenInfo = _getScreenInfo(context);
    }

    private static ScreenInfo screenInfo;

    public static ScreenInfo getScreenInfo(Context context) {
        if(screenInfo == null){
            init(context);
        }
        return screenInfo;
    }

    private static Map<Integer, String> densityDpiNames = new HashMap<Integer, String>() {{
        put(120, "ldpi");//240x320
        put(160, "mdpi");//320x480
        put(240, "hdpi");//480x800
        put(320, "xhdpi");//720x1280
        put(480, "xxhdpi"); //1920x1080
    }};

    /**
     * @param context
     * @return
     */
    //该类用于描述有关显示器的一般信息，例如大小，密度和字体缩放。它定义了许多变量来描述显示区域的宽高、dp、dpi和sp等信息
    private static ScreenInfo _getScreenInfo(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        ScreenInfo screenInfo = new ScreenInfo();
        //密度
        Log.e("display", "density:" + metrics.density);
        screenInfo.setDensity(metrics.density);

        // 每英寸的像素点数，屏幕密度的另一种表示。densityDpi = density * 160.
        Log.e("display", "densityDpi:" + metrics.densityDpi);
        screenInfo.setDensityDpi(metrics.densityDpi);

        if (densityDpiNames.containsKey(metrics.densityDpi)) {
            String densityDpiName = densityDpiNames.get(metrics.densityDpi);

            Log.e("display", "densityDpiName:" + densityDpiName);
            screenInfo.setDensityDpiName(densityDpiName);
        }

        //屏幕的绝对宽度（像素）
        Log.e("display", "widthPixels:" + metrics.widthPixels);
        screenInfo.setWidthPixels(metrics.widthPixels);

        // 屏幕的绝对高度（像素）
        Log.e("display", "heightPixels:" + metrics.heightPixels);
        screenInfo.setHeightPixels(metrics.heightPixels);

        //  屏幕上字体显示的缩放因子，一般与density值相同，除非在程序运行中，用户根据喜好调整了显示字体的大小时，会有微小的增加。
        Log.e("display", "scaledDensity:" + metrics.scaledDensity);
        screenInfo.setScaledDensity(metrics.scaledDensity);

        // X轴方向上屏幕每英寸的物理像素数。
        Log.e("display", "xdpi:" + metrics.xdpi);
        screenInfo.setXdpi(metrics.xdpi);

        // Y轴方向上屏幕每英寸的物理像素数。
        Log.e("display", "ydpi:" + metrics.ydpi);
        screenInfo.setYdpi(metrics.ydpi);

        //获取屏幕相关信息的集中方式
        Log.e("display", "fontScale = " + context.getResources().getConfiguration().fontScale);
        screenInfo.setFontScale(context.getResources().getConfiguration().fontScale);

        Log.e("display", screenInfo.toString());

        /**
         * DisplayMetrics metric = new DisplayMetrics();
         * getWindowManager().getDefaultDisplay().getMetrics(metric);
         * int width = metric.widthPixels;  // 屏幕宽度（像素）
         * int height = metric.heightPixels;  // 屏幕高度（像素）
         * float density = metric.density;  // 屏幕密度（0.75 / 1.0 / 1.5）
         * int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
         */
        return screenInfo;
    }

    public static class ScreenInfo {

        private float density;
        private int densityDpi;
        private String densityDpiName;
        private int widthPixels;
        private int heightPixels;
        private float scaledDensity;
        private float xdpi;
        private float ydpi;
        private float fontScale;

        public String getDensityDpiName() {
            return densityDpiName;
        }

        public void setDensityDpiName(String densityDpiName) {
            this.densityDpiName = densityDpiName;
        }

        public float getDensity() {
            return density;
        }

        public void setDensity(float density) {
            this.density = density;
        }

        public int getDensityDpi() {
            return densityDpi;
        }

        public void setDensityDpi(int densityDpi) {
            this.densityDpi = densityDpi;
        }

        public int getWidthPixels() {
            return widthPixels;
        }

        public void setWidthPixels(int widthPixels) {
            this.widthPixels = widthPixels;
        }

        public int getHeightPixels() {
            return heightPixels;
        }

        public void setHeightPixels(int heightPixels) {
            this.heightPixels = heightPixels;
        }

        public float getScaledDensity() {
            return scaledDensity;
        }

        public void setScaledDensity(float scaledDensity) {
            this.scaledDensity = scaledDensity;
        }

        public float getXdpi() {
            return xdpi;
        }

        public void setXdpi(float xdpi) {
            this.xdpi = xdpi;
        }

        public float getYdpi() {
            return ydpi;
        }

        public void setYdpi(float ydpi) {
            this.ydpi = ydpi;
        }

        public float getFontScale() {
            return fontScale;
        }

        public void setFontScale(float fontScale) {
            this.fontScale = fontScale;
        }

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }
    }

}
