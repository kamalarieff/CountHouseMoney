/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mudah.my.services;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.utils.MudahUtil;
import com.mudah.my.utils.ServerUtils;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // [START register_for_gcm]
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                // [START get_token]
                // Chotot sender Id 1045502190564
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_sender_id),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                // [END get_token]
                Log.d("GCM Sender: " + getString(R.string.gcm_sender_id));
                Log.d("GCM Registration Token: " + token);

                // TODO: Implement this method to send any registration to your app's servers.
                Config.gcmToken = token;
                sendRegistrationToServer();
            }
        } catch (Exception ignore) {
            Log.e("GCM is not available on this device!");
        }
    }

    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.

     */
    private void sendRegistrationToServer() {
        if (Config.enableChat && !Config.isRegisterWithGcm) {
            ServerUtils.postDevice(Config.deviceId);
        }
    }


}
