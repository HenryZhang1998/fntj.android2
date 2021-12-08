package com.fntj.app.util;

import java.util.regex.Pattern;

public class SU {
    public static boolean isNumber(String str) {
        Pattern pattern = Pattern.compile("^(\\d+)$");
        return pattern.matcher(str).matches();
    }
}
