package com.heavy87.InterceptMe;

import java.lang.reflect.Method;

import okhttp3.RequestBody;

public class Utils {
    static boolean isOldOkHttpLibrary (){
        boolean isOldOkHttpVersion = false;
        Method[] methods = RequestBody.class.getMethods();
        RequestBody requestBody;
        for (Method m : methods) {
            if (!m.getName().equals("create")) {
                continue;
            }
            Class<?>[] pType = m.getParameterTypes();
            if (pType.length == 2 && pType[0].toString().contains("MediaType") && pType[1].toString().contains("String")) {
                isOldOkHttpVersion = !isOldOkHttpVersion;
            }
        }
        return isOldOkHttpVersion;
    }
}
