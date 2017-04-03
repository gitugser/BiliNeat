package me.iacn.bilineat.util;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;

/**
 * Created by iAcn on 2017/4/3
 * Emali iAcn0301@foxmail.com
 */

public class ThemeHelper {

    private static int mThemeColor;

    public static void init(Intent intent) {
        mThemeColor = intent.getIntExtra("color", -298343);
    }

    public static int getPrimaryColor() {
        if (mThemeColor != 0) {
            return mThemeColor;
        }

        return -298343;
    }

    public static ColorStateList getCheckedColorList() {
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_checked};
        states[1] = new int[]{-android.R.attr.state_checked};

        int[] colors = new int[2];
        colors[0] = mThemeColor;
        colors[1] = Color.parseColor("#6b6b6b"); // 取色得到的未勾选颜色

        return new ColorStateList(states, colors);
    }
}