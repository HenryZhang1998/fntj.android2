package com.fntj.lib.zb.manager;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.fntj.lib.zb.model.APIResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebClient {

    //private static final Logger logger = LoggerFactory.getLogger(WebClient.class);

    private String encode = "utf-8";
    private Map<String, String> headers = new HashMap<>();

    public WebClient setEncode(String encode) {
        this.encode = encode;
        return this;
    }

    public WebClient setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    //同步get请求
    public APIResult<String> get(String url) {

        OkHttpClient okHttpClient = new OkHttpClient();

        Request.Builder builder = new Request.Builder().url(url);
        for (Map.Entry<String, String> kv : headers.entrySet()) {
            builder.addHeader(kv.getKey(), kv.getValue());
        }

        final Request request = builder.build();

        final Call call = okHttpClient.newCall(request);
        String rsp = null;
        try {
            Response response = call.execute();

            if (!response.isSuccessful()) {

                String msg = response.message();

                if (response.body() != null) {
                    String s = response.body().string();
                    if (s.startsWith("{")) {
                        msg = s;
                    }
                }

                return APIResult.Fail(msg, response.code() + "");
            }

            rsp = response.body().string();
            Log.d("rsp", rsp);

            return APIResult.Success(rsp);
        } catch (IOException e) {

            e.printStackTrace();

            Log.e("", e.getMessage());

            return APIResult.Fail(e.getMessage(), e.getClass().getSimpleName());
        }
    }


    public APIResult<String> post(String url, Map<String, String> values) {
        OkHttpClient okHttpClient = new OkHttpClient();

        FormBody.Builder bb = new FormBody.Builder();
        if (values != null && values.size() > 0) {
            for (Map.Entry<String, String> kv : values.entrySet()) {
                bb.addEncoded(kv.getKey(), kv.getValue());
            }
        }
        RequestBody body = bb.build();

        Request.Builder builder = new Request.Builder().post(body).url(url);
        for (Map.Entry<String, String> kv : headers.entrySet()) {
            builder.addHeader(kv.getKey(), kv.getValue());
        }

        final Request request = builder.build();

        final Call call = okHttpClient.newCall(request);
        String rsp = null;
        try {
            Response response = call.execute();

            if (!response.isSuccessful()) {
                String msg = response.message();

                if (response.body() != null) {
                    msg = response.body().string();
                }

                return APIResult.Fail(msg, response.code() + "");
            }

            rsp = response.body().string();
            Log.d("rsp", rsp);

            return APIResult.Success(rsp);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("", e.getMessage());

            return APIResult.Fail(e.getMessage(), e.getClass().getSimpleName());
        }
    }

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    //private static final String JSON = "application/json; charset=utf-8";

    public APIResult<String> postJson(String url, JSONObject j) {

        String json = j.toString();

        return postJson(url, json);
    }

    public APIResult<String> postJson(String url, String json) {

        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON, json);

        Request.Builder builder = new Request.Builder().post(body).url(url);
        for (Map.Entry<String, String> kv : headers.entrySet()) {
            builder.addHeader(kv.getKey(), kv.getValue());
        }

        final Request request = builder.build();

        final Call call = okHttpClient.newCall(request);
        String rsp = null;
        try {
            Response response = call.execute();

            if (!response.isSuccessful()) {
                return APIResult.Fail(response.message(), response.code() + "");
            }

            rsp = response.body().string();
            Log.d("rsp", rsp);

            return APIResult.Success(rsp);
        } catch (IOException e) {

            e.printStackTrace();
            try {
                Log.e("WebClient", e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return APIResult.Fail(e.getMessage(), e.getClass().getSimpleName());
        }
    }
}
