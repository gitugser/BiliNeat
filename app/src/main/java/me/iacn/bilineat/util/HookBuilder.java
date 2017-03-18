package me.iacn.bilineat.util;

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

public class HookBuilder {

    private static ClassLoader mLoader;

    private String mClassName;
    private String mMethodName;

    private Class<?> mClass;
    private Method mMethod;
    private Class<?> mReturnType;
    private Object[] mParamTypes;

    private XC_MethodHook mHookCallBack;

    private HookBuilder() {
    }

    public static HookBuilder create(ClassLoader loader) {
        mLoader = loader;
        return new HookBuilder();
    }

    public HookBuilder setClass(String clazz) {
        mClassName = clazz;
        return this;
    }

    public HookBuilder setClass(Class<?> clazz) {
        mClass = clazz;
        return this;
    }

    public HookBuilder setMethod(String method) {
        mMethodName = method;
        return this;
    }

    public HookBuilder setMethod(Method method) {
        mMethod = method;
        return this;
    }

    public HookBuilder setReturnType(Class<?> returnType) {
        mReturnType = returnType;
        return this;
    }

    public HookBuilder setParamTypes(Object... paramTypes) {
        mParamTypes = paramTypes;
        return this;
    }

    public HookBuilder setHookCallBack(XC_MethodHook callback) {
        mHookCallBack = callback;
        return this;
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
            if (mParamTypes != null) {
                return mClass.getDeclaredMethod(mMethodName, objectsToClasses());
            } else {
                return mClass.getDeclaredMethod(mMethodName);
            }
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
}