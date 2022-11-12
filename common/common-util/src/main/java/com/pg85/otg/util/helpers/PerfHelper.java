package com.pg85.otg.util.helpers;

import com.pg85.otg.constants.Constants;

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

    public static boolean isYOutOfWorldBounds(int y) {
        return y >= Constants.WORLD_HEIGHT || y < Constants.WORLD_DEPTH;
    }
}
