package me.iacn.bilineat.hook;

import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
    private Object[] mParamTypes;

    private XC_MethodHook mHookCallBack;

    private HookInfo() {
    }

    public void hook() {
        if (mLoader == null) return;
        if (mClass == null && TextUtils.isEmpty(mClassName)) return;
        if (mMethod == null && TextUtils.isEmpty(mMethodName)) return;

        if (mClass == null) {
            mClass = findClass(mClassName, mLoader);
        }

        if (mMethod == null) {
            if (mReturnType != null) {
                mMethod = getMethodByReturnType();
            } else {
                mMethod = getMethodByReflect();
            }
        }

        if (mMethod == null) return;

        XposedBridge.hookMethod(mMethod, mHookCallBack);
    }

    private Method getMethodByReturnType() {
        for (Method method : mClass.getDeclaredMethods()) {
            if (mMethodName.equals(method.getName()) &&
                    mReturnType == method.getReturnType()) {
                return method;
            }
        }

        return null;
    }

    private Method getMethodByReflect() {
        try {
            return mClass.getDeclaredMethod(mMethodName, objectsToClasses());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Class<?>[] objectsToClasses() {
        List<Class<?>> paramTypeList = new ArrayList();

        for (Object obj : mParamTypes) {
            if (obj instanceof String) {
                Class<?> clazz = findClass((String) obj, mLoader);
                paramTypeList.add(clazz);

            } else if (obj instanceof Class) {
                paramTypeList.add((Class<?>) obj);
            }
        }

        Class<?>[] temp = new Class<?>[paramTypeList.size()];
        return paramTypeList.toArray(temp);
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

        public Builder setHookCallBack(XC_MethodHook callback) {
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