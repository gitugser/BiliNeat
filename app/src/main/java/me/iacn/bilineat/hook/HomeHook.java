package me.iacn.bilineat.hook;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import me.iacn.bilineat.Constant;
import me.iacn.bilineat.XposedInit;
import me.iacn.bilineat.bean.HookBean;
import me.iacn.bilineat.util.HookBuilder;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setIntField;

/**
 * Created by iAcn on 2017/3/27
 * Emali iAcn0301@foxmail.com
 */

class HomeHook {

    private static ClassLoader mClassLoader;

    static void doHook(ClassLoader loader, HookBean bean) {
        mClassLoader = loader;

        removeIndexDataStreamAd(bean.indexInnerClass);
        removeFoundMall(bean.foundMall);

        removePromoBanner();
        setHomePage();
        removeDrawerVip();
    }

    /**
     * 去除首页推荐数据流中的广告
     */
    private static void removeIndexDataStreamAd(String clazz) {
        if (!XposedInit.xSharedPref.getBoolean("promo_stream", false)) return;

        HookBuilder.create(mClassLoader)
                .setClass("bl." + clazz)
                .setMethod("a")
                .setParamTypes(List.class)
                .setHookCallBack(new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        List list = (List) param.args[0];
                        if (list != null) {
                            deleteAdItemFromList(list);
                        }
                    }
                }).hook();
    }

    /**
     * 去除发现里的周边商城
     */
    private static void removeFoundMall(String className) {
        if (!XposedInit.xSharedPref.getBoolean("found_mall", false)) return;

        HookBuilder.create(mClassLoader)
                .setClass("bl." + className)
                .setMethod("onViewCreated")
                .setParamTypes(View.class, Bundle.class)
                .setHookCallBack(new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        View view = (View) param.args[0];

                        int id = view.getContext().getResources()
                                .getIdentifier("bmall", "id", Constant.biliPackageName);

                        if (id != 0) {
                            view.findViewById(id).setVisibility(View.GONE);
                        }
                    }
                }).hook();
    }

    /**
     * 去除分区选择界面和各分区二级界面 Banner 中的广告
     */
    private static void removePromoBanner() {
        if (!XposedInit.xSharedPref.getBoolean("promo_banner", false)) return;

        // 去除分区页面数据流里 Banner 的广告
        HookBuilder.create(mClassLoader)
                .setClass("tv.danmaku.bili.ui.category.api.CategoryIndex")
                .setMethod("haveBanners")
                .setHookCallBack(new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        boolean haveBanner = (boolean) param.getResult();
                        // 有 Banner 时才 Hook
                        if (!haveBanner) return;

                        Object banner = getObjectField(param.thisObject, "banner");
                        List bottomBanners = (List) getObjectField(banner, "bottomBanners");
                        deleteAdItemFromList(bottomBanners);
                    }
                }).hook();

        // 去除各分区顶部 Banner 的广告
        HookBuilder.create(mClassLoader)
                .setClass("tv.danmaku.bili.ui.category.api.RegionRecommendVideo")
                .setMethod("getBannerList")
                .setHookCallBack(new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        List top = (List) param.getResult();
                        if (top != null) {
                            deleteAdItemFromList(top);
                        }
                    }
                }).hook();
    }

    /**
     * 从 List 里删除标识为 isAd 的 Item
     */
    private static void deleteAdItemFromList(List list) {
        for (Object obj : new ArrayList(list)) {
            boolean isAd = getBooleanField(obj, "isAd");

            // 去除标识为 isAd 的 Banner
            if (isAd) {
                list.remove(obj);
            }
        }
    }

    private static void setHomePage() {
        final int homeIndex = Integer.parseInt(XposedInit.xSharedPref.getString("default_page", "1"));

        // 默认为 「推荐」
        if (homeIndex == 1) return;

        HookBuilder.create(mClassLoader)
                .setClass("tv.danmaku.bili.ui.main.HomeFragment")
                .setMethod("onViewCreated")
                .setParamTypes(View.class, Bundle.class)
                .setHookCallBack(new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        setIntField(param.thisObject, "k", homeIndex);
                    }
                }).hook();
    }

    /**
     * 去除侧边栏我的大会员
     */
    private static void removeDrawerVip() {
        final boolean bcoin = !XposedInit.xSharedPref.getBoolean("drawer_bcoin", false);
        final boolean myVip = !XposedInit.xSharedPref.getBoolean("drawer_my_vip", false);
        final boolean vipPoint = !XposedInit.xSharedPref.getBoolean("drawer_vip_point", false);

        // Q：为什么要使用这种方式来判断？
        // A：因为 Hook 是一项很占资源的操作，这里为了节省资源
        //    只在两个选项中有某个开启时才进行挂钩，而不是每次根据 Value 值去设置
        //    其他地方同理

        if (bcoin && myVip && vipPoint) return;

        HookBuilder.create(mClassLoader)
                .setClass("tv.danmaku.bili.ui.main.NavigationFragment")
                .setMethod("onViewCreated")
                .setParamTypes(View.class, Bundle.class)
                .setHookCallBack(new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Class clazz = findClass("android.support.design.widget.NavigationView", mClassLoader);
                        Field field = findFirstFieldByExactType(param.thisObject.getClass(), clazz);
                        Menu menu = (Menu) callMethod(field.get(param.thisObject), "getMenu");

                        // 取得第二个 Menu，为 我的大会员
                        menu.getItem(1).setVisible(myVip);

                        // 取得第三个 Menu，为 会员积分
                        menu.getItem(2).setVisible(vipPoint);

                        // 取得第十个 Menu，为 B币钱包
                        menu.getItem(9).setVisible(bcoin);
                    }
                }).hook();
    }
}