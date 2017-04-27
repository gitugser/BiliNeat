package me.iacn.bilineat.hook;

import android.content.Context;
import android.content.res.XResources;
import android.os.Bundle;
import android.widget.Toast;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import me.iacn.bilineat.bean.HookBean;
import me.iacn.bilineat.ui.StateFragment;
import me.iacn.bilineat.util.HookBuilder;

/**
 * Created by iAcn on 2017/3/27
 * Emali iAcn0301@foxmail.com
 */

public class HookHandler {

    public static void methodHook(ClassLoader loader, String currentVersion) {
        HookBean bean = getHookBean(currentVersion);

        if (!bean.isEmpty()) {
            OnlineHook.doHook(loader, bean);
            HomeHook.doHook(loader, bean);
            OtherHook.doHook(loader, bean);
        }
    }

    public static void layoutHook(XResources res) {
        LayoutHook.doHook(res);
    }

    /**
     * 在哔哩哔哩启动时显示 Toast
     */
    public static void showStartToast(ClassLoader loader, final String message) {
        HookBuilder.create(loader)
                .setClass("tv.danmaku.bili.ui.splash.SplashActivity")
                .setMethod("onCreate")
                .setParamTypes(Bundle.class)
                .setHookCallBack(new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        Toast.makeText((Context) param.thisObject, message, Toast.LENGTH_LONG).show();
                    }
                }).hook();
    }

    public static void hookSelf(ClassLoader loader) {
        Class<?> clazz = XposedHelpers.findClass(StateFragment.class.getName(), loader);
        XposedHelpers.setStaticBooleanField(clazz, "sXposedRunning", true);
    }

    private static HookBean getHookBean(String currentVersion) {
        HookBean bean = new HookBean();

        // 根据当前版本决定要Hook的类和方法名
        switch (currentVersion) {
            case "5.4.0":
                bean.onlineHelper = "dvp";
                bean.onlineCategoryGame = "h";
                bean.onlineToolbarGame = "i";
                bean.onlineUnicomSim = "j";
                bean.onlineFoundGame = "k";
                bean.onlineGameCenter = "l";

                bean.themeClass = "exm";
                bean.foundMall = "eiy";
                bean.indexInnerClass = "ecp$5";
                break;
        }

        return bean;
    }
}