package me.iacn.bilineat.hook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import me.iacn.bilineat.BuildConfig;
import me.iacn.bilineat.Constant;
import me.iacn.bilineat.XposedInit;
import me.iacn.bilineat.util.HookBuilder;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType;
import static de.robv.android.xposed.XposedHelpers.findMethodsByExactParameters;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setIntField;

/**
 * Created by iAcn on 2016/10/5
 * Emali iAcn0301@foxmail.com
 */

public class MethodHook {

    private ClassLoader mClassLoader;

    /**
     * Method Hook的具体实现方法
     *
     * @param classLoader    类加载器
     * @param currentVersion 当前版本
     */
    public void doHook(ClassLoader classLoader, String currentVersion) {
        mClassLoader = classLoader;

        boolean isCategoryGame = !XposedInit.xSharedPref.getBoolean("category_game", true);
        boolean isFoundGame = !XposedInit.xSharedPref.getBoolean("found_game", true);
        boolean isToolbarGame = !XposedInit.xSharedPref.getBoolean("toolbar_game", true);
        boolean isDrawerPromote = !XposedInit.xSharedPref.getBoolean("drawer_promote", true);

        int homeIndex = Integer.parseInt(XposedInit.xSharedPref.getString("default_page", "1"));

        // 根据当前版本决定要Hook的类和方法名
        switch (currentVersion) {
            case "5.1.2":
            case "5.1.1":
                hookResult("dgn", "d", isCategoryGame);
                hookResult("dgn", "e", isToolbarGame);
                hookResult("dgn", "f", isFoundGame);
                hookResult("dgn", "g", isCategoryGame);
                freeTheme("eqe");
                removeFoundMall("dym");
                break;
        }

        removeDrawerVip();
        removePromoBanner();

        downloadBangumi();
        downloadMovie();

        disableThemeDialog();
        setHomePage(homeIndex);

        addNeatEntrance();
    }

    /**
     * 使用返回值类型查找并处理侧滑菜单、发现、Toolbar、分类里的广告开关
     */
    private void hookResult(String clazz, String method, boolean state) {
        HookBuilder.create(mClassLoader)
                .setClass("bl." + clazz)
                .setMethod(method)
                .setReturnType(boolean.class)
                .setHookCallBack(XC_MethodReplacement.returnConstant(state))
                .hook();
    }

    private void freeTheme(String className) {
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

    /**
     * 去除发现里的周边商城
     */
    private void removeFoundMall(String className) {
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
     * 因官方在此处有较多变动
     * 暂不做处理
     */
    private void removePromoBanner() {
        if (!XposedInit.xSharedPref.getBoolean("promo_banner", false)) return;

        HookBuilder.create(mClassLoader)
                .setClass("tv.danmaku.bili.ui.category.api.CategoryIndex")
                .setMethod("haveBanners")
                .setHookCallBack(new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Object banner = getObjectField(param.thisObject, "banner");
                        List bottomBanners = (List) getObjectField(banner, "bottomBanners");

                        for (Object obj : new ArrayList(bottomBanners)) {
                            boolean isAd = getBooleanField(obj, "isAd");

                            // 去除标识为 isAd 的 Banner
                            if (isAd) {
                                bottomBanners.remove(obj);
                            }
                        }
                    }
                }).hook();
    }

    private void removeIndexDataStreamAd() {
        // 妈蛋这个也不调用
        // 官方这是挖了多少坑？
        //
        // Notes：
        // IndexFeedFragment 是推荐页的，其中有个 List 存放着 BasicIndexItem

        HookBuilder.create(mClassLoader)
                .setClass("bl.dmt")
                .setMethod("onCreate")
                .setParamTypes(Bundle.class)
                .setHookCallBack(new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        System.out.println("----------------------");

                        Field field = findFirstFieldByExactType(param.thisObject.getClass(), List.class);
                        field.setAccessible(true);
                        System.out.println(field.getName());
                        List list = (List) field.get(param.thisObject);

                        System.out.println(list.size());

                        for (Object obj : list) {
                            System.out.println(obj);
                        }
                    }
                }).hook();
    }

    /**
     * 去除侧边栏我的大会员
     */
    private void removeDrawerVip() {
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

                        // 取得第九个 Menu，为 B币钱包
                        menu.getItem(8).setVisible(bcoin);
                    }
                }).hook();
    }

    private void disableThemeDialog() {
        HookBuilder.create(mClassLoader)
                .setClass("tv.danmaku.bili.MainActivity")
                .setMethod("c")
                .setHookCallBack(XC_MethodReplacement.DO_NOTHING)
                .hook();
    }

    private void downloadMovie() {
        HookBuilder.create(mClassLoader)
                .setClass("com.bilibili.api.BiliVideoDetail")
                .setMethod("c")
                .setReturnType(boolean.class)
                .setHookCallBack(XC_MethodReplacement.returnConstant(true))
                .hook();
    }

    private void downloadBangumi() {
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

    private void setHomePage(final int pageIndex) {
        // 默认为 「推荐」
        if (pageIndex == 1) return;

        HookBuilder.create(mClassLoader)
                .setClass("tv.danmaku.bili.ui.main.HomeFragment")
                .setMethod("onViewCreated")
                .setParamTypes(View.class, Bundle.class)
                .setHookCallBack(new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        setIntField(param.thisObject, "c", pageIndex);
                    }
                }).hook();
    }

    private void addNeatEntrance() {
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
}