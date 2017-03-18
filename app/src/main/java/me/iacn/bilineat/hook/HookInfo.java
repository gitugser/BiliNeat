package me.iacn.bilineat.hook;

import android.text.TextUtils;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Created by iAcn on 2017/3/18
 * Emali iAcn0301@foxmail.com
 */

public class HookInfo {

    private ClassLoader mLoader;

    private String mClassName;
    private String mMethodName;

    private Class<?> mClass;
    private Method mMethod;
    private Class<?> mReturnType;
    private Class<?>[] mParamTypes;

    private XC_MethodHook mHookCallBack;

    private HookInfo() {
    }

    public void hook() {
        System.out.println("-------------------------------");
        System.out.println(mLoader);
        System.out.println(mClassName);
        System.out.println(mMethodName);
        System.out.println(mClass);
        System.out.println(mMethod);
        System.out.println(mReturnType);
        System.out.println(mParamTypes);
        System.out.println(mHookCallBack);
        System.out.println("-------------------------------");



        if (mLoader == null) return;
        if (mClass == null && TextUtils.isEmpty(mClassName)) return;
        if (mMethod == null && TextUtils.isEmpty(mMethodName)) return;

        if (mClass == null) {
            mClass = findClass(mClassName, mLoader);
        }

        if (mMethod == null) {
            if (mReturnType != null) {
                for (Method method : mClass.getDeclaredMethods()) {
                    if (mMethodName.equals(method.getName()) &&
                            mReturnType == method.getReturnType()) {
                        mMethod = method;
                    }
                }
            } else {
                try {
                    mMethod = mClass.getDeclaredMethod(mMethodName, mParamTypes);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        System.out.println("-------------------------------");
        System.out.println(mLoader);
        System.out.println(mClassName);
        System.out.println(mMethodName);
        System.out.println(mClass);
        System.out.println(mMethod);
        System.out.println(mReturnType);
        System.out.println(mParamTypes);
        System.out.println(mHookCallBack);
        System.out.println("-------------------------------");

        XposedBridge.hookMethod(mMethod, mHookCallBack);
    }

    public static class Builder {

        private HookInfo mInfo;

        public Builder(ClassLoader loader) {
            mInfo = new HookInfo();
            mInfo.mLoader = loader;
        }

        public Builder setClass(String clazz) {
            mInfo.mClassName = clazz;
            return this;
        }

        public Builder setClass(Class<?> clazz) {
            mInfo.mClass = clazz;
            return this;
        }

        public Builder setMethod(String method) {
            mInfo.mMethodName = method;
            return this;
        }

        public Builder setMethod(Method method) {
            mInfo.mMethod = method;
            return this;
        }

        public Builder setReturnType(Class<?> returnType) {
            mInfo.mReturnType = returnType;
            return this;
        }

        public Builder setParamTypes(Class<?>... paramTypes) {
            mInfo.mParamTypes = paramTypes;
            return this;
        }

        public Builder setMethodHook(XC_MethodHook callback) {
            mInfo.mHookCallBack = callback;
            return this;
        }

        public HookInfo build() {
            return mInfo;
        }

        public void hook() {
            mInfo.hook();
        }
    }
}