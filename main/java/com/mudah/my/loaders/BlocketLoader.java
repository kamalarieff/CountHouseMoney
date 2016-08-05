package com.mudah.my.loaders;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import com.lib701.connection.ACRESTClientAuth;
import com.lib701.utils.Log;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BlocketLoader extends AsyncTaskLoader<JSONObject> {
    private final String resource;
    private final Map<String, Object> params;
    private volatile boolean dataIsReady;
    private volatile JSONObject data;
    private Method method;
    private String baseUrl;

    public BlocketLoader(Context context, String baseUrl, Method method, String resource, Map<String, Object> params) {
        super(context);
        if (method == null || resource == null || params == null || context == null) {
            throw new IllegalArgumentException();
        }
        this.method = method;
        this.resource = resource;
        this.params = params;
        this.baseUrl = baseUrl;
    }

    public BlocketLoader(Context context, Method method, String resource, Map<String, Object> params) {
        this(context, null, method, resource, params);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (dataIsReady) {
            deliverResult(data);
        } else {
            forceLoad();
        }
    }

    @Override
    public JSONObject loadInBackground() {
        Log.d();
        dataIsReady = false;
        //params.put("key",Config.key);
        ACRESTClientAuth a = new ACRESTClientAuth();
        if (baseUrl != null) {
            a.setBaseUrl(baseUrl);

        }
        a.setMethod(method.name());
        a.setResource(resource);
        if (method == Method.GET) {
            a.setGetParameters(params);
        } else if (method == Method.POST) {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            a.setHeaders(headers);
            a.setPostParameters(params);
        }

        data = a.makeSynchronousRESTCallWithError();
        dataIsReady = true;
        return data;
    }

    public Method getMethod() {
        return method;
    }

    public Map<String, Object> getParams() {
        return Collections.unmodifiableMap(params);
    }

    public String getResource() {
        return resource;
    }

    public JSONObject getData() {
        return data;
    }

    public static interface OnLoadCompleteListener {
        public void onLoadComplete(BlocketLoader loader, JSONObject data);

        public void onLoadError(BlocketLoader loader, JSONObject data);
    }

    public static abstract class Callbacks implements LoaderCallbacks<JSONObject>, OnLoadCompleteListener {
        private Method method;
        private String resource;
        private Map<String, Object> params;
        private Context context;
        private String baseUrl;

        public Callbacks(String baseUrl, Method method, String resource, Map<String, Object> params, Context context) {
            this.baseUrl = baseUrl;
            this.method = method;
            this.resource = resource;
            this.params = params;
            this.context = context;
        }

        public Callbacks(Method method, String resource, Map<String, Object> params, Context context) {
            this(null, method, resource, params, context);

        }

        @Override
        public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
            if (baseUrl == null) {
                return new BlocketLoader(context, method, resource, params);
            } else {
                return new BlocketLoader(context, baseUrl, method, resource, params);
            }
        }

        @Override
        public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
            //data response could be very big. Check if isDebug mode is on first.
            //Otherwise this could lead to OOM error because StringBuilder will try to append the Json data into the log.
            if (Log.isDebug)
                Log.d(loader.getId() + ": " + data);
            if (data == null) {
                onLoadError((BlocketLoader) loader, data);
            } else {
                onLoadComplete((BlocketLoader) loader, data);

            }
        }

        @Override
        public void onLoaderReset(Loader<JSONObject> loader) {
            // do nothing
        }

        @Override
        public abstract void onLoadComplete(BlocketLoader loader, JSONObject data);

        @Override
        public abstract void onLoadError(BlocketLoader loader, JSONObject data);
    }
}
