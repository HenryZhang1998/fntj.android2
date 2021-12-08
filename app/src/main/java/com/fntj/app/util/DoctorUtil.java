package com.fntj.app.util;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fntj.app.MyApplication;
import com.fntj.lib.zb.util.StringUtil;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorUtil {
    private static Map<String, List<String>> names = new HashMap<>();
    private static Map<String, String> py2name = new HashMap<>();

    public static void init() {
        AssetManager am = MyApplication.getInstance().getAssets();
        try {
            InputStream inputStream = am.open("doctors.json");

            int size = inputStream.available();
            int len = -1;
            byte[] bytes = new byte[size];
            inputStream.read(bytes);
            inputStream.close();
            String json = new String(bytes);

            JSONObject j = JSON.parseObject(json);
            for (String name : j.keySet()) {
                List<String> py = j.getJSONArray(name).toJavaList(String.class);
                names.put(name, py);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, List<String>> kv : names.entrySet()) {
            for (String py : kv.getValue()) {
                py2name.put(py, kv.getKey());
            }
        }
    }

    public static String findName(String voice) {
        String pyString = toPinyin(voice);
        if (StringUtil.isEmpty(pyString)) {
            return null;
        }

        for (String py : py2name.keySet()) {
            if (pyString.contains(py)) {
                return py2name.get(py);
            }
        }

        return null;
    }

    public static String toPinyin(String input) {
        HanyuPinyinOutputFormat fmt = new HanyuPinyinOutputFormat();
        fmt.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        fmt.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);

        try {
            String py = PinyinHelper.toHanYuPinyinString(input, fmt, "", true);

            return py;
        } catch (BadHanyuPinyinOutputFormatCombination ex) {
            ex.printStackTrace();

            return null;
        }
    }
}
