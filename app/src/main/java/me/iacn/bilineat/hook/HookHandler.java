package me.iacn.bilineat.hook;

import me.iacn.bilineat.bean.HookBean;

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

    private static HookBean getHookBean(String currentVersion) {
        HookBean bean = new HookBean();

        // 根据当前版本决定要Hook的类和方法名
        switch (currentVersion) {
            case "5.1.2":
            case "5.1.1":
                bean.onlineHelper = "dgn";
                bean.onlineCategoryGame = "d";
                bean.onlineToolbarGame = "e";
                bean.onlineFoundGame = "f";
                bean.onlineGameCenter = "g";

                bean.themeClass = "eqe";
                bean.foundMall = "dym";
                bean.indexInnerClass = "dom$5";
                break;
        }

        return bean;
    }
}