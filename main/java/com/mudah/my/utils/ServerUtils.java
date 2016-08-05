package com.mudah.my.utils;

/**
 * Created by pin on 30/11/15.
 */

import android.os.AsyncTask;

import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sandy on 8/12/15.
 */
public class ServerUtils {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String STATUS_OK = "ok";
    private static final String ACCOUNT_ID = "account_id";
    private static final String DEVICE_GCM = "device_gcm";
    private static final String DEVICE_TOKEN = "device_token";
    private static final String DEVICE_TYPE = "device_type";
    private static final String DEVICE_ID = "device_id";
    private static final String DEVICE_API_PATH = "/devices";
    private static final OkHttpClient client = new OkHttpClient();

    public static void deleteDevice(final String deviceId) {
        client.setConnectTimeout(Config.TIME_OUT, TimeUnit.SECONDS); // connect timeout
        client.setReadTimeout(Config.TIME_OUT, TimeUnit.SECONDS);
        Log.d("GCM deviceId: " + deviceId + ", account_id: " + Config.userAccount.getUserId());
        DeleteDeviceAsyncTask deleteDeviceAsyncTask = new DeleteDeviceAsyncTask(deviceId);
        deleteDeviceAsyncTask.execute();
    }

    public static void postDevice(final String deviceId) {
        final String gcmId = Config.gcmToken;
        client.setConnectTimeout(Config.TIME_OUT, TimeUnit.SECONDS); // connect timeout
        client.setReadTimeout(Config.TIME_OUT, TimeUnit.SECONDS);
        Log.d("GCM deviceId: " + deviceId + ", account_id: " + Config.userAccount.getUserId());
        PostDeviceAsyncTask postDeviceAsyncTask = new PostDeviceAsyncTask(gcmId, deviceId);
        postDeviceAsyncTask.execute();
    }

    private static class PostDeviceAsyncTask extends AsyncTask<Void, Void, Void> {
        private String gcmId;
        private String deviceId;

        private PostDeviceAsyncTask(String gcmId, String deviceId) {
            this.gcmId = gcmId;
            this.deviceId = deviceId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject jsonObject = new JSONObject();
                if (Config.userAccount.isLogin() && !ACUtils.isEmpty(Config.userAccount.getUserId())) {
                    jsonObject.put(ACCOUNT_ID, Config.userAccount.getUserId());
                }
                jsonObject.put(DEVICE_TOKEN, gcmId);
                jsonObject.put(DEVICE_TYPE, Constants.ANDROID);
                jsonObject.put(DEVICE_ID, deviceId);
                jsonObject.put(DEVICE_GCM, Constants.USER_ACCOUNT);

                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                Request request = new Request.Builder()
                        .url(Config.hydraPushNotificationRoot + DEVICE_API_PATH)
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(Request request, IOException e) {
                        Config.isRegisterWithGcm = false;
                        Log.e("GCM Failed to register: ", e);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        try {
                            String body = response.body().string();
                            JSONObject bodyJson = new JSONObject(body);
                            String status = bodyJson.getString("status");
                            Log.d("GCM deviceId: " + deviceId + ", bodyJson: " + bodyJson);
                            Log.d("GCM gcmId: " + gcmId);
                            if (STATUS_OK.equalsIgnoreCase(status)) {
                                Config.isRegisterWithGcm = true;
                            }
                        } catch (Exception exp) {
                        }
                    }
                });
            } catch (Exception exp) {
            }
            return null;
        }
    }

    private static class DeleteDeviceAsyncTask extends AsyncTask<Void, Void, Void> {
        private String deviceId;

        private DeleteDeviceAsyncTask(String deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(ACCOUNT_ID, Config.userAccount.getUserId());
                jsonObject.put(DEVICE_GCM, Constants.USER_ACCOUNT);

                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                Request request = new Request.Builder()
                        .url(Config.hydraPushNotificationRoot + DEVICE_API_PATH + Constants.SLASH + deviceId)
                        .delete(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(Request request, IOException e) {
                        Config.isRegisterWithGcm = false;
                        Log.e("Failed to DeleteDeviceAsyncTask: ", e);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        try {
                            String body = response.body().string();
                            JSONObject bodyJson = new JSONObject(body);
                            //String status = bodyJson.getString("status");
                            Log.d("GCM deviceId: " + deviceId + ", bodyJson: " + bodyJson);

                        } catch (Exception exp) {
                        }
                    }
                });
            } catch (Exception exp) {
            }
            return null;
        }
    }
}