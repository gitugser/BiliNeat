package me.iacn.bilineat.hook;

import de.robv.android.xposed.XC_MethodReplacement;
import me.iacn.bilineat.XposedInit;
import me.iacn.bilineat.bean.HookBean;
import me.iacn.bilineat.util.HookBuilder;

/**
 * Created by iAcn on 2017/3/27
 * Emali iAcn0301@foxmail.com
 */

class OnlineHook {

    private static ClassLoader mClassLoader;

    static void doHook(ClassLoader loader, HookBean bean) {
        mClassLoader = loader;

        boolean isCategoryGame = !XposedInit.xSharedPref.getBoolean("category_game", true);
        boolean isFoundGame = !XposedInit.xSharedPref.getBoolean("found_game", true);
        boolean isToolbarGame = !XposedInit.xSharedPref.getBoolean("toolbar_game", true);
        boolean isUnicomSim = !XposedInit.xSharedPref.getBoolean("drawer_unicom_sim", false);
//        boolean isDrawerPromote = !XposedInit.xSharedPref.getBoolean("drawer_promote", true);

        hookResult(bean.onlineHelper, bean.onlineCategoryGame, isCategoryGame);
        hookResult(bean.onlineHelper, bean.onlineToolbarGame, isToolbarGame);
        hookResult(bean.onlineHelper, bean.onlineUnicomSim, isUnicomSim);
        hookResult(bean.onlineHelper, bean.onlineFoundGame, isFoundGame);
        hookResult(bean.onlineHelper, bean.onlineGameCenter, isCategoryGame);
    }

    /**
     * 使用返回值类型查找并处理侧滑菜单、发现、Toolbar、分类里的广告开关
     */
    private static void hookResult(String clazz, String method, boolean state) {
        HookBuilder.create(mClassLoader)
                .setClass("bl." + clazz)
                .setMethod(method)
                .setReturnType(boolean.class)
                .setHookCallBack(XC_MethodReplacement.returnConstant(state))
                .hook();
    }
}