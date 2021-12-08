/*Copyright ©2015 TommyLemon(https://github.com/TommyLemon)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package com.fntj.app.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.fntj.app.MyApplication;
import com.fntj.lib.zb.util.MD5Util;
import com.fntj.lib.zb.util.StringUtil;

/**
 * 数据工具类
 *
 * @author Lemon
 */
public class DataManager {
    private final String TAG = "DataManager";
    //private final String demoClientId = BuildConfig.Demo_ClientId; //"demo202103010001";
    //private final String demoAppKey = BuildConfig.Demo_AppKey; // "456456"

    private Context context;

    private DataManager(Context context) {
        this.context = context;
    }

    private static DataManager instance;

    public static DataManager getInstance() {
        if (instance == null) {
            synchronized (DataManager.class) {
                if (instance == null) {
                    instance = new DataManager(MyApplication.getInstance());
                }
            }
        }
        return instance;
    }

    //用户 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    private String PATH_USER = "PATH_USER";

    public final String KEY_USER = "KEY_USER";
    public final String KEY_USER_ID = "KEY_USER_ID";
    public final String KEY_USER_NAME = "KEY_USER_NAME";
    public final String KEY_USER_PHONE = "KEY_USER_PHONE";

    public final String KEY_CURRENT_USER_ID = "KEY_CURRENT_USER_ID";
    public final String KEY_LAST_USER_ID = "KEY_LAST_USER_ID";
    public final String KEY_CLIENT_ID = "KEY_CLIENT_ID";
    public final String KEY_APP_KEY = "KEY_APP_KEY";
    public final String KEY_TOKEN = "KEY_TOKEN";

    public String getClientId() {

//        if (!StringUtil.isEmpty(demoClientId)) {
//            return demoClientId;
//        }

        SharedPreferences sdf = context.getSharedPreferences(PATH_USER, Context.MODE_PRIVATE);
        String clientId = sdf == null ? "" : sdf.getString(KEY_CLIENT_ID, "");

        if (StringUtil.isEmpty(clientId)) {

            if (StringUtil.isEmpty(clientId)) {
                clientId = FileManager.read(context, "client-id.txt");
            }

            if(StringUtil.isEmpty(clientId)) {
                try {
                    clientId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (StringUtil.isEmpty(clientId)) {

                clientId = MD5Util.MD5(System.currentTimeMillis() + "");

                SharedPreferences.Editor editor = sdf.edit();
                editor.remove(KEY_TOKEN).putString(KEY_CLIENT_ID, clientId);
                editor.commit();

                FileManager.write(context, "client-id.txt", clientId);
            }

            return clientId;
        }

        return clientId;
    }

    public String getAppKey() {

//        if (!StringUtil.isEmpty(demoAppKey)) {
//            return demoAppKey;
//        }

        SharedPreferences sdf = context.getSharedPreferences(PATH_USER, Context.MODE_PRIVATE);
        return sdf == null ? "" : sdf.getString(KEY_APP_KEY, "");
    }

    public void setAppKey(String appKey) {

        SharedPreferences sdf = context.getSharedPreferences(PATH_USER, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sdf.edit();
        editor.remove(KEY_TOKEN).putString(KEY_APP_KEY, appKey);
        editor.commit();
    }

    public String getToken() {
        SharedPreferences sdf = context.getSharedPreferences(PATH_USER, Context.MODE_PRIVATE);
        return sdf == null ? "" : sdf.getString(KEY_TOKEN, "");
    }

    public void setToken(String token) {

        SharedPreferences sdf = context.getSharedPreferences(PATH_USER, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sdf.edit();
        editor.remove(KEY_TOKEN).putString(KEY_TOKEN, token);
        editor.commit();
    }
}
