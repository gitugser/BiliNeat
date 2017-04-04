package me.iacn.bilineat.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;

import me.iacn.bilineat.R;

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

    public static void updateTaskColor(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;

        String label = activity.getTitle().toString().trim();
        Bitmap icon = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher);

        if (TextUtils.isEmpty(label)) {
            label = activity.getResources().getString(R.string.app_name);
        }

        activity.setTaskDescription(new ActivityManager.TaskDescription(label, icon, getPrimaryColor()));
    }
}