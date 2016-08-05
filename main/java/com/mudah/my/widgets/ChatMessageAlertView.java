package com.mudah.my.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lib701.utils.ACUtils;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.models.ChatCafe;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

public class ChatMessageAlertView extends RelativeLayout {
    private TextView senderName;
    private TextView chatMessage;
    private ImageView chatImage;

    private final int AD_WIDTH = 94;
    private final int AD_HEIGHT = 70;

    public ChatMessageAlertView(Context context) {
        super(context);
        initInflate();
    }

    public ChatMessageAlertView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initInflate();
    }

    public ChatMessageAlertView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initInflate();
    }

    public void initInflate(){
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.chat_message_alert, this);
        init();
    }

    private void init() {
        senderName = (TextView) findViewById(R.id.senderName);
        chatMessage = (TextView) findViewById(R.id.chatMessage);
        chatImage = (ImageView) findViewById(R.id.chatImage);
    }

    public void setCCMessage(JSONObject ccMessage) {
        if(!ccMessage.optString("sender_id", "{\"item_image\": \"\", \"\"}").equals(Config.userAccount.getUserId())){
            senderName.setText(ccMessage.optString("sender_name", Constants.EMPTY_STRING)+":");
        }else{
            senderName.setText(Constants.EMPTY_STRING);
        }
        String message = ChatCafe.getLastMessage(getContext(), ccMessage);
        chatMessage.setText(message);

        String imagePath = ccMessage.optString("item_image", Constants.EMPTY_STRING);
        if(ACUtils.isEmpty(imagePath)){
            Picasso.with(getContext())
                    .load(R.drawable.cat_others)
                    .resize(AD_WIDTH, AD_HEIGHT)
                    .centerCrop()
                    .into(chatImage);
        }else {
            Picasso.with(getContext())
                    .load(imagePath)
                    .resize(AD_WIDTH, AD_HEIGHT)
                    .centerCrop()
                    .placeholder(R.drawable.loading_image_bg)
                    .into(chatImage);
        }

    }
}
