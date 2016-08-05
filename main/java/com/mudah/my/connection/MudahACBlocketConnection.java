package com.mudah.my.connection;

import android.content.Context;

import com.lib701.connection.ACBlocketConnection;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.models.UserAccountModel;

import org.json.JSONObject;

/**
 * Created by w_ongkl on 10/17/14.
 */
public class MudahACBlocketConnection extends ACBlocketConnection {

    public MudahACBlocketConnection(Context context) {
        super(context);
    }

    public static ImageInfoModel newAdInsertImageForImageInfo() {
        JSONObject responseJSON = ACBlocketConnection.newAdInsertImage();
        ImageInfoModel imageInfoModel = new ImageInfoModel();

        if (responseJSON != null && responseJSON.has("newad")) {
            JSONObject newAd = responseJSON.optJSONObject("newad");
            if (newAd != null) {
                String status = newAd.optString("status");
                imageInfoModel.status = status;
                if (status.equals(Constants.OK)) {

                    String imageUrl = newAd.optString("image_url");
                    String imageDigest = newAd.optString("image_digest");

                    if (!ACUtils.isEmpty(imageUrl)) {
                        imageInfoModel.imageUrl = imageUrl;
                    } else {
                        imageInfoModel.imageUrl = null;
                    }

                    if (!ACUtils.isEmpty(imageDigest)) {
                        imageInfoModel.imageDigest = imageDigest;
                    } else {
                        imageInfoModel.imageDigest = null;
                    }
                } else {
                    imageInfoModel.errorMessage = newAd.optString("message");
                }
            }
        }

        return imageInfoModel;
    }

    public static ImageInfoModel profileImageinfo() {
        JSONObject responseJSON = ACBlocketConnection.profileImage(Config.userAccount.getToken());
        Log.d("response: "+responseJSON);
        ImageInfoModel imageInfoModel = new ImageInfoModel();
        if (responseJSON != null) {
            String status = responseJSON.optString(Constants.STATUS);
            imageInfoModel.status = status;
            if (status.equals(Constants.OK)) {
                JSONObject files = responseJSON.optJSONObject("files");
                String imageUrl = responseJSON.optString(UserAccountModel.AVATAR_URL);
                String fileName = files.optString("filename");

                if (!ACUtils.isEmpty(imageUrl)) {
                    imageInfoModel.imageUrl = imageUrl;
                } else {
                    imageInfoModel.imageUrl = null;
                }
                if (!ACUtils.isEmpty(fileName)) {
                    imageInfoModel.imageDigest = fileName;
                } else {
                    imageInfoModel.imageDigest = null;
                }
            } else {
                imageInfoModel.errorMessage = responseJSON.optString(Constants.MSG);
            }
        }
        return imageInfoModel;
    }

    public static class ImageInfoModel {
        public String status = null;
        public String errorMessage = null;

        public String imageUrl = null;
        public String imageDigest = null;
    }

}
