package me.iacn.bilineat.hook;

import me.iacn.bilineat.bean.HookBean;

/**
 * Created by iAcn on 2017/3/27
 * Emali iAcn0301@foxmail.com
 */

public class HomeHook {

    public static void methodHook(ClassLoader loader) {
        HookBean bean = getHookBean("5.1.2");

        OnlineHook.doHook(loader, bean);
    }
}
