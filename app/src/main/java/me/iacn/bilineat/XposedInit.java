package me.iacn.bilineat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.iacn.bilineat.hook.HookHandler;
import me.iacn.bilineat.util.HookBuilder;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Created by iAcn on 2016/10/5
 * Emali iAcn0301@foxmail.com
 */

public class XposedInit implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {

    public static XSharedPreferences xSharedPref;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        xSharedPref = new XSharedPreferences(BuildConfig.APPLICATION_ID, "setting");
        xSharedPref.makeWorldReadable();
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (BuildConfig.APPLICATION_ID.equals(loadPackageParam.packageName)) {
            // Hook 自身
            HookHandler.hookSelf(loadPackageParam.classLoader);
            return;

        } else if (!Constant.biliPackageName.equals(loadPackageParam.packageName)) {
            // Hook 哔哩哔哩
            return;
        }

        Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
        Context context = (Context) callMethod(activityThread, "getSystemContext");

        final String currentVersion = context.getPackageManager()
                .getPackageInfo(Constant.biliPackageName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT).versionName;

        // 判断插件是否支持当前哔哩哔哩版本
        Set<String> set = new HashSet<>();
        Collections.addAll(set, Constant.supportVersions);

        if (set.contains(currentVersion)) {
            xSharedPref.reload();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                HookHandler.methodHook(loadPackageParam.classLoader, currentVersion);
            } else {
                // Lollipop 以下在 Application 初始化完成后再进行 Hook
                HookBuilder.create(loadPackageParam.classLoader)
                        .setClass("tv.danmaku.bili.MainApplication")
                        .setMethod("onCreate")
                        .setHookCallBack(new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                HookHandler.methodHook(loadPackageParam.classLoader, currentVersion);
                            }
                        }).hook();
            }
        } else {
            // 如果不支持当前哔哩哔哩版本,弹出 Toast 提示
            HookHandler.showStartToast(loadPackageParam.classLoader, "哔哩净化暂不支持你的版本哦~");
        }
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resParam) throws Throwable {
        if (!Constant.biliPackageName.equals(resParam.packageName)) return;

        xSharedPref.reload();
        HookHandler.layoutHook(resParam.res);
    }
}