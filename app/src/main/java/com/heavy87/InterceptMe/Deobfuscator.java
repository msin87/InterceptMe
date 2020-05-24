package com.heavy87.InterceptMe;

import java.lang.reflect.Method;

class Deobfuscator {
    static Class<?> getClassFromParamClass(Class<?> TargetClass, String methodName, int countParams, int paramIndex) {
        Method[] methods = TargetClass.getMethods();
        Class<?> FoundClass;
        for (Method m : methods) {
            if (!m.getName().equals(methodName)) {
                continue;
            }
            Class<?>[] pType = m.getParameterTypes();
            if (pType.length == countParams) {
                try {
                    FoundClass = Class.forName(pType[paramIndex].getName());
                    return FoundClass;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }

    static Method getMethodFromClass(Class<?> TargetClass, String name, int countParams) {
        Method[] methods = TargetClass.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(name) && (m.getParameterTypes().length == countParams))
                return m;
        }
        return null;
    }

    static Method getMethodByParamsClass(Class<?> TargetClass, Class<?>... parameterTypes) {
        Method[] methods = TargetClass.getMethods();
        Method foundMethod = null;
        int matchCount = 0;
        for (Method m : methods) {
            for (Class<?> pType : m.getParameterTypes()) {
                if (pType.getName().equals(parameterTypes[matchCount].getName())) {
                    matchCount++;
                    foundMethod = m;
                }
            }
            if (matchCount == parameterTypes.length) {
                return foundMethod;
            }
        }
        return null;
    }
}
