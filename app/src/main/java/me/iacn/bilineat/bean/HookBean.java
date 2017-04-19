package me.iacn.bilineat.bean;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by iAcn on 2017/3/27
 * Emali iAcn0301@foxmail.com
 */

public class HookBean implements Serializable {

    public String officialVersion;

    public String onlineHelper;
    public String onlineCategoryGame;
    public String onlineToolbarGame;
    public String onlineUnicomSim;
    public String onlineFoundGame;
    public String onlineGameCenter;

    public String themeClass;
    public String foundMall;
    public String indexInnerClass;

    public boolean isEmpty() {
        return TextUtils.isEmpty(onlineHelper) ||
                TextUtils.isEmpty(onlineCategoryGame) ||
                TextUtils.isEmpty(onlineToolbarGame) ||
                TextUtils.isEmpty(onlineUnicomSim) ||
                TextUtils.isEmpty(onlineFoundGame) ||
                TextUtils.isEmpty(onlineGameCenter) ||
                TextUtils.isEmpty(themeClass) ||
                TextUtils.isEmpty(foundMall) ||
                TextUtils.isEmpty(indexInnerClass);
    }
}