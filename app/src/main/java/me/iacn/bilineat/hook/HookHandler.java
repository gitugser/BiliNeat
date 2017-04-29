package me.iacn.bilineat.hook;

import android.content.res.XResources;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import me.iacn.bilineat.bean.HookBean;

/**
 * Created by iAcn on 2017/3/27
 * Emali iAcn0301@foxmail.com
 */

public class HookHandler {

    public static void methodHook(ClassLoader loader, String configPath) {
        HookBean bean = getHookBean(configPath);

        if (!bean.isEmpty()) {
            OnlineHook.doHook(loader, bean);
            HomeHook.doHook(loader, bean);
            OtherHook.doHook(loader, bean);
        }
    }

    public static void layoutHook(XResources res) {
        LayoutHook.doHook(res);
    }

    private static HookBean getHookBean(String configPath) {
        try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(configPath));
            HookBean bean = (HookBean) stream.readObject();
            stream.close();

            return bean;

        } catch (Exception e) {
            e.printStackTrace();
            return new HookBean();
        }
    }
}