package me.iacn.bilineat.hook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import me.iacn.bilineat.BuildConfig;
import me.iacn.bilineat.Constant;
import me.iacn.bilineat.bean.HookBean;
import me.iacn.bilineat.util.HookBuilder;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType;
import static de.robv.android.xposed.XposedHelpers.findMethodsByExactParameters;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setIntField;

/**
 * Created by iAcn on 2017/3/27
 * Emali iAcn0301@foxmail.com
 */

class OtherHook {

    private static ClassLoader mClassLoader;

    static void doHook(ClassLoader loader, HookBean bean) {
        mClassLoader = loader;

        freeTheme(bean.themeClass);
        disableThemeDialog();
        downloadBangumi();
        downloadMovie();
        addNeatEntrance();
    }

    private static void freeTheme(String className) {
        HookBuilder.create(mClassLoader)
                .setClass("bl." + className)
                .setMethod("a")
                .setParamTypes(Constant.biliPackageName + ".ui.theme.api.BiliSkinList", boolean.class)
                .setHookCallBack(new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args[0] == null) return;

                        List list = (List) getObjectField(param.args[0], "mList");

                        if (list != null) {
                            for (Object theme : list) {
                                setBooleanField(theme, "mIsFree", true);
                                setIntField(theme, "mPrice", 0);
                            }
                        }
                    }
                }).hook();
    }


    private static void disableThemeDialog() {
        HookBuilder.create(mClassLoader)
                .setClass("tv.danmaku.bili.MainActivity")
                .setMethod("e")
                .setHookCallBack(XC_MethodReplacement.DO_NOTHING)
                .hook();
    }

    private static void downloadMovie() {
        HookBuilder.create(mClassLoader)
                .setClass("com.bilibili.api.BiliVideoDetail")
                .setMethod("c")
                .setReturnType(boolean.class)
                .setHookCallBack(XC_MethodReplacement.returnConstant(true))
                .hook();
    }

    private static void downloadBangumi() {
        Method[] methods = findMethodsByExactParameters(
                findClass("com.bilibili.api.bangumi.BiliBangumiSeason", mClassLoader), boolean.class);

        XC_MethodHook downloadableHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                setBooleanField(param.thisObject, "mDownloadable", true);
            }
        };

        for (Method method : methods) {
            String name = method.getName();
            if ("a".equals(name) || "d".equals(name)) {
                XposedBridge.hookMethod(method, downloadableHook);
            }
        }
    }

    private static void addNeatEntrance() {
        HookBuilder.create(mClassLoader)
                .setClass("tv.danmaku.bili.preferences.BiliPreferencesActivity$a")
                .setMethod("onCreate")
                .setParamTypes(Bundle.class)
                .setHookCallBack(new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        final Activity activity = (Activity) callMethod(param.thisObject, "getActivity");

                        Preference preference = new Preference(activity);
                        preference.setTitle("净化设置");
                        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName(BuildConfig.APPLICATION_ID,
                                        BuildConfig.APPLICATION_ID + ".ui.MainActivity"));
                                intent.putExtra("color", getBiliThemeColor(activity));

                                activity.startActivity(intent);
                                activity.overridePendingTransition(0, 0);

                                return true;
                            }
                        });

                        PreferenceScreen screen = (PreferenceScreen)
                                callMethod(param.thisObject, "getPreferenceScreen");

                        PreferenceCategory category = (PreferenceCategory)
                                ((List) getObjectField(screen, "mPreferenceList")).get(1);

                        category.addPreference(preference);
                    }
                }).hook();
    }

    private static int getBiliThemeColor(Activity externalActivity) {
        Class<?> toolbarClass = findClass("com.bilibili.magicasakura.widgets.TintToolbar", mClassLoader);
        Field field = findFirstFieldByExactType(externalActivity.getClass(), toolbarClass);
        field.setAccessible(true);

        try {
            Object obj = field.get(externalActivity);
            ColorDrawable drawable = (ColorDrawable) callMethod(obj, "getBackground");
            return drawable.getColor();

        } catch (IllegalAccessException e) {
            e.printStackTrace();
            // 返回默认的粉色
            return -298343;
        }
    }
}