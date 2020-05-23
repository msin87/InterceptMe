package com.heavy87.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.heavy87.InterceptMe.InterceptMe;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String string = "https://www.avito.ru/api/9/items?page=1&display=grid&categoryId=24&locationId=639300&sort=default&params%5B568-to-int%5D=50&params%5B201%5D=1060&params%5B504%5D=5257&params%5B550%5D%5B0%5D=5702&params%5B550%5D%5B1%5D=5703&viewPort%5Bwidth%5D=392&viewPort%5Bheight%5D=774&searchArea%5BlatTop%5D=55.75955550605182&searchArea%5BlonLeft%5D=37.86852852204905&searchArea%5BlatBottom%5D=55.74314761607052&searchArea%5BlonRight%5D=37.88553160345069";
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", -123123);
        body.put("text", "asdasdasdasd");
        body.put("parse_mode", "HTML");
        JSONObject jsonRequest = new JSONObject(body);
        RequestBody requestBody = RequestBody.create(jsonRequest.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(string)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:2.1) Gecko/20110318 Firefox/4.0b13pre Fennec/4.0")
                .post(requestBody)
                .build();
        InterceptMe.send(request);
    }
}
