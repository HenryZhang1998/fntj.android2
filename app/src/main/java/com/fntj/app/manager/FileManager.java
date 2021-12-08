package com.fntj.app.manager;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.fntj.lib.zb.model.APIResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;

public class FileManager {

    public static String read(Context context, String fileName) {

        String pkg = context.getPackageName();
        String filePath = null;
        boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (hasSDCard) {
            filePath = Environment.getExternalStorageDirectory().toString();
        } else {
            filePath = Environment.getDownloadCacheDirectory().toString();
        }

        filePath += File.separator + pkg + File.separator + fileName;

        try {
            File file = new File(filePath);

            if (!file.exists()) {
                return null;
            }

            RandomAccessFile acc = new RandomAccessFile(file, "rwd");
            String text = acc.readUTF();
            acc.close();

            return text;

        } catch (Exception e) {
            Log.e("FileManager", "Error on read File:" + e);

            return null;
        }
    }

    // 将字符串写入到文本文件中
    public static void write(Context context, String fileName, String text) {

        String pkg = context.getPackageName();
        String filePath = null;
        boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (hasSDCard) {
            filePath = Environment.getExternalStorageDirectory().toString();
        } else {
            filePath = Environment.getDownloadCacheDirectory().toString();
        }

        filePath += File.separator + pkg + File.separator + fileName;

        try {
            File file = new File(filePath);

            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + filePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(text.getBytes());
            raf.close();

        } catch (Exception e) {
            Log.e("FileManager", "Error on write File:" + e);
        }
    }

    public static APIResult<String> readAsset(Context context, String fileName) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();

            Reader reader = new InputStreamReader(assetManager.open(fileName));

            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(reader);
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
            bf.close();
            reader.close();
            //assetManager.close();

            String text = stringBuilder.toString();
            return APIResult.Success(text);
        } catch (Exception ex) {
            return APIResult.Fail(ex.getMessage(), ex.getClass().getSimpleName());
        }
    }
}
