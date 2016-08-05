package com.mudah.my.models;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.chatcafe.sdk.core.CCMessage;
import com.chatcafe.sdk.core.CCRoom;
import com.chatcafe.sdk.core.Cafe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.activities.InboxActivity;
import com.mudah.my.activities.SignInActivity;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;

import org.json.JSONObject;

public class ChatCafe {

    public static void logInChatCafe() {
        try {
            if (Config.userAccount.isLogin()) {
                Cafe.logIn(Config.userAccount.getUserId()
                        , Config.userAccount.getUsername()
                        , Config.userAccount.getToken()
                        , null);
            }
        } catch (Exception e) {
            Log.e(" error = " + e.getMessage());
        }
    }

    public static String getLastMessage(Context context, CCMessage ccMessage) {
        if (ccMessage == null) {
            return "";
        }
        return isImageMessage(ccMessage) ? getLastImageMessage(context, ccMessage) : ccMessage.getText();
    }


    @NonNull
    private static String getLastImageMessage(Context context, CCMessage ccMessage) {
        return !isSender(ccMessage) ? context.getString(R.string.chat_list_text_case_retrieve_image) :
                context.getString(R.string.chat_list_text_case_send_image);
    }

    public static String getLastMessage(Context context, JSONObject jsonObject) {
        Gson gson = new GsonBuilder().create();
        CCMessage ccMessage = gson.fromJson(jsonObject.toString(), CCMessage.class);
        return getLastMessage(context, ccMessage);
    }

    public static boolean isImageMessage(CCMessage ccMessage) {
        return ccMessage.getData() != null && ccMessage.getData().getAttachments() != null
                && !ccMessage.getData().getAttachments().isEmpty();
    }

    public static boolean isSender(CCMessage ccMessage) {
        if (!ACUtils.isEmpty(Config.userAccount.getUserId()))
            return Config.userAccount.getUserId().equals(ccMessage.getSenderId());
        else
            return false;
    }

    public static boolean isBuyer(CCRoom ccRoom) {
        if (!ACUtils.isEmpty(Config.userAccount.getUserId()))
            return Config.userAccount.getUserId().equals(ccRoom.getBuyerId());
        else
            return false;
    }

    public static Intent redirectIntentToInBox(Activity activity) {
        //if user has not logged in - > Go to Sign In page
        //If user has logged in, go to inbox
        Intent intent;
        if (!Config.userAccount.isLogin()) {
            intent = new Intent(activity, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra(Constants.CHAT_INBOX, true);
        } else {
            intent = new Intent(activity, InboxActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }
        return intent;
    }

}
