package com.fntj.app.net;

import android.content.Context;
import android.os.AsyncTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fntj.app.BuildConfig;
import com.fntj.app.manager.DataManager;
import com.fntj.app.model.KnowledgePackage;
import com.fntj.app.model.KnowledgePackageDetail;
import com.fntj.app.model.KnowledgePackageDetailItem;
import com.fntj.app.model.QueueInfo;
import com.fntj.app.util.AppUtils;
import com.fntj.lib.zb.manager.WebClient;
import com.fntj.lib.zb.model.APIResult;
import com.fntj.lib.zb.util.StringUtil;
import com.reeman.nerves.RobotActionProvider;

import java.util.ArrayList;
import java.util.List;

public class RobotApi {

    public static String buildType = BuildConfig.BUILD_TYPE; //"debug";
    public static String api = BuildConfig.SERVER_URL;

    public static void onLaunch(Context context, final MyResponseListener<JSONObject> listener) {

        simpleGet(context, String.class, api + "/app/onLaunch", new MyResponseListener<String>() {
            @Override
            public void onSuccess(String data) {

                JSONObject j = JSONObject.parseObject(data);

                listener.onSuccess(j);
            }

            @Override
            public void onError(String message, String code) {
                listener.onError(message, code);
            }
        });
    }

    private static <T> void simpleGet(Context context, Class<T> tClass, String url, final MyResponseListener<T> listener) {

        new AsyncTask<Void, Void, Exception>() {

            @Override
            protected Exception doInBackground(Void... voids) {

                WebClient client = buildWebClient(context);

                APIResult<String> result = client.get(url);

                if (!result.isSuccess()) {
                    listener.onError(result.getMessage(), result.getCode());
                } else {
                    String json = result.getData();

                    JSONObject j = JSONObject.parseObject(json);

                    Boolean success = j.getBoolean("success");
                    String code = j.getString("code");
                    String msg = j.getString("message");

                    if (success) {
                        T data;
                        if (tClass.equals(String.class)) {
                            data = (T) j.getString("data");
                        } else {
                            JSONObject _data = j.getJSONObject("data");
                            data = toBean(tClass, _data);
                        }

                        listener.onSuccess(data);

//                        if (_data != null) {
//                            T data = toBean(tClass, _data);
//                            listener.onSuccess(data);
//                        } else {
//                            listener.onError("data empty", code + "");
//                        }
                    } else {
                        listener.onError(msg, code);
                    }
                }

                return null;
            }
        }.execute();
    }

    private static <T> void simplePost( Context context, Class<T> tClass, String url, JSONObject params, final MyResponseListener listener) {

        new AsyncTask<Void, Void, Exception>() {

            @Override
            protected Exception doInBackground(Void... voids) {

                WebClient client = buildWebClient(context);

                APIResult<String> result = client.postJson(url, params == null ? "{}" : params.toString());

                if (!result.isSuccess()) {
                    listener.onError(result.getMessage(), result.getCode());
                } else {
                    String json = result.getData();

                    JSONObject j = JSONObject.parseObject(json);

                    Boolean success = j.getBoolean("success");
                    String code = j.getString("code");
                    String msg = j.getString("message");

                    if (success) {
                        JSONObject _data = j.getJSONObject("data");

                        if (_data != null) {
                            T data = toBean(tClass, _data);
                            listener.onSuccess(data);
                        } else {
                            listener.onError("data empty", code + "");
                        }
                    } else {
                        listener.onError(msg, code);
                    }
                }

                return null;
            }
        }.execute();
    }

    public static void getKnowledgePackageList(Context context,final MyListResponseListener<KnowledgePackage> listener) {

        JSONObject params = new JSONObject();
        params.put("name", "");

        proxyPostList(context, api + "/knowledge/packageList", params, "packageList", KnowledgePackage.class, listener);
    }

    public static void getKnowledgePackageDetail(Context context, String packageId, final MyResponseListener<KnowledgePackageDetail> listener) {

        JSONObject params = new JSONObject();
        params.put("packageId", packageId);

        proxyPost( context, api + "/knowledge/packageDetail", params, "packageEntity", KnowledgePackageDetail.class, listener);
    }

    public static void getKnowledgePackageItemList(Context context, String itemName, final MyListResponseListener<KnowledgePackageDetailItem> listener) {

        JSONObject params = new JSONObject();
        params.put("itemName", itemName);

        proxyPostList(context,api + "/knowledge/itemList", params, "itemList", KnowledgePackageDetailItem.class, listener);
    }

    public static void queueInfo(Context context, int action, String pebillArchiveId, final MyResponseListener<QueueInfo> listener) {

        JSONObject params = new JSONObject();
        params.put("action", action); //类型，1-查询体检者信息，2-过号激活
        params.put("pebillArchiveId", pebillArchiveId);

        proxyPostList(context,api + "/knowledge/queueInfo", params, "data", QueueInfo.class, new MyListResponseListener<QueueInfo>() {
            @Override
            public void onSuccess(List<QueueInfo> data) {
                listener.onSuccess(data.get(0));
            }

            @Override
            public void onError(String message, String code) {
                listener.onError(message, code);
            }
        });
    }

    private static <T> void proxyPostList(Context context, final String url, final JSONObject params, String dataNode, Class<T> tClass, final MyListResponseListener<T> listener) {

        new AsyncTask<Void, Void, Exception>() {

            @Override
            protected Exception doInBackground(Void... voids) {

                WebClient client = buildWebClient(context);

                APIResult<String> result = client.postJson(url, params == null ? "{}" : params.toString());

                if (!result.isSuccess()) {
                    listener.onError(result.getMessage(), result.getCode());
                } else {

                    String json = result.getData();
                    System.out.printf(json);

                    JSONObject j1 = JSONObject.parseObject(json);

                    boolean success = j1.getBoolean("success");

                    if (!success) {

                        String code = j1.getString("code");
                        String message = j1.getString("message");

                        listener.onError(message, code);

                        return null;
                    }

                    String dataJson = j1.getString("data");

                    JSONObject j = JSONObject.parseObject(dataJson);

                    int code = j.getInteger("code");
                    String msg = j.getString("msg");

                    if (code == 0) {

                        JSONArray data = j.getJSONArray(dataNode);
                        List<T> list = new ArrayList<>();

                        for (int i = 0; i < data.size(); i++) {
                            JSONObject jo = data.getJSONObject(i);

                            try {
                                T o = toBean(tClass, jo);

                                list.add(o);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        listener.onSuccess(list);
                    } else {
                        listener.onError(msg, code + "");
                    }
                }

                return null;
            }
        }.execute();

    }

    private static <T> void proxyPost(Context context, final String url, final JSONObject params, final String dataNode, final Class<T> tClass, final MyResponseListener<T> listener) {

        new AsyncTask<Void, Void, Exception>() {

            @Override
            protected Exception doInBackground(Void... voids) {

                WebClient client = buildWebClient(context);

                APIResult<String> result = client.postJson(url, params == null ? "{}" : params.toString());

                if (!result.isSuccess()) {
                    listener.onError(result.getMessage(), result.getCode());
                } else {

                    String json = result.getData();
                    JSONObject j1 = JSONObject.parseObject(json);

                    boolean success = j1.getBoolean("success");

                    if (!success) {

                        String code = j1.getString("code");
                        String message = j1.getString("message");

                        listener.onError(message, code);

                        return null;
                    }

                    String dataJson = j1.getString("data");

                    JSONObject j = JSONObject.parseObject(dataJson);

                    int code = j.getInteger("code");
                    String msg = j.getString("msg");

                    if (code == 0) {
                        JSONObject _data = j.getJSONObject(dataNode);

                        if (_data != null) {
                            T data = toBean(tClass, _data);
                            listener.onSuccess(data);
                        } else {
                            listener.onError("data empty", code + "");
                        }
                    } else {
                        listener.onError(msg, code + "");
                    }
                }

                return null;
            }
        }.execute();
    }

    public static String getClientId() {

        String clientId = "";

        if (StringUtil.isEmpty(clientId)) {
            try {
                clientId = RobotActionProvider.getInstance().getRobotID();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (StringUtil.isEmpty(clientId)) {
            clientId = DataManager.getInstance().getClientId();
        }

        return clientId;
    }


    private static WebClient buildWebClient(Context context) {
        String clientId = getClientId();
        String appKey = DataManager.getInstance().getAppKey();
        String token = DataManager.getInstance().getToken();
        if (!StringUtil.isEmpty(token) && token.startsWith("{")) {
            token = "";
            DataManager.getInstance().setToken(token);
        }

        WebClient client = new WebClient();
        client.setHeader("clientId", clientId);
        client.setHeader("appKey", appKey);
        client.setHeader("token", token);
        client.setHeader("build", buildType);
        client.setHeader("package", AppUtils.getPackageName(context));
        client.setHeader("versionName", AppUtils.getVersionName(context));
        client.setHeader("versionCode", String.valueOf(AppUtils.getVersionCode(context)));

        return client;
    }

    private static <T> T toBean(Class<T> tClass, JSONObject obj) {
        T instance = JSON.toJavaObject(obj, tClass);
        return instance;
    }

    public interface MyListResponseListener<T> extends BaseResponseListener {
        void onSuccess(List<T> list);
    }

    public interface MyResponseListener<T> extends BaseResponseListener {
        void onSuccess(T data);
    }

    public interface BaseResponseListener {
        void onError(String message, String code);
    }
}
