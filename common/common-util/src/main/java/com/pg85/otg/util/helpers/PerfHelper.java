package com.pg85.otg.util.helpers;

public final class PerfHelper {
    public static boolean stringIsEmpty(String str) {
        if (str == null)
            return true;
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.indexOf(i)))
                return false;
        }
        return true;
    }
}
