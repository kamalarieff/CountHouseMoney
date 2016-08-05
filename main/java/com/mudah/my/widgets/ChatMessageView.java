package com.mudah.my.widgets;

import android.app.Activity;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chatcafe.sdk.Validate;
import com.chatcafe.sdk.core.CCMessage;
import com.chatcafe.sdk.tool.MessageBaseAdapter;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.activities.ChatActivity;
import com.mudah.my.configs.Constants;
import com.mudah.my.models.ChatImageItem;
import com.mudah.my.utils.MudahUtil;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatMessageView extends RelativeLayout{
    private final static String MY_MSG_TAG = "txtMyMessage";
    private final static String OTHER_MSG_TAG ="txtOtherMessage";
    private final String FORMAT_TIME_STAMP = "HH:mm";
    private final int STATUS_CHAT_READ = 0;
    private final int FRAME_LONG_SIDE = 130;
    private final int FRAME_SHORT_SIDE = 110;
    private boolean isMyMessage;
    private TextView textMy;
    private TextView textOther;
    private int direction;
    private LinearLayout viewGroupOtherMessage;
    private LinearLayout viewGroupMyMessage;
    private TextView textMyTime;
    private TextView textOtherTime;
    private TextView textMyStatus;
    private ImageView imageMy;
    private ImageView imageOther;
    private String messageTime;
    private View itemView;
    private ProgressBar pbUploadMyImage;
    private Context mContext;
    private FrameLayout imageMyFrame;
    private FrameLayout imageOtherFrame;
    public ImageView btnRetry;

    public ChatMessageView(Context context) {
        super(context);
        init(context);
    }

    public ChatMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChatMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        itemView = inflater.inflate(R.layout.chat_room_view_messages_cell_text, this);
        mContext = context;
        initInstance();

    }

    private void initInstance() {
        viewGroupOtherMessage = (LinearLayout) findViewById(R.id.viewGroupOtherMessage);
        viewGroupMyMessage = (LinearLayout) findViewById(R.id.viewGroupMyMessage);
        textMy = (TextView) findViewWithTag(MY_MSG_TAG);
        textOther = (TextView) findViewWithTag(OTHER_MSG_TAG);
        textMyTime = (TextView) findViewById(R.id.txtMyTime);
        textOtherTime = (TextView) findViewById(R.id.txtOtherTime);
        textMyStatus = (TextView) findViewById(R.id.txtMyStatus);
        imageMy = (ImageView) findViewById(R.id.imgMyMessage);
        imageOther = (ImageView) findViewById(R.id.imgOtherMessage);
        pbUploadMyImage = (ProgressBar) findViewById(R.id.pbUploadMyImage);
        imageMyFrame = (FrameLayout) findViewById(R.id.imageMyFrame);
        imageOtherFrame = (FrameLayout) findViewById(R.id.imageOtherFrame);
        btnRetry = (ImageView) findViewById(R.id.btnRetry);
    }

    public void setView() {
        if (direction == MessageBaseAdapter.CELL_TYPE_TEXT_DIRECTION_OUTGOING) {
            setViewMyMessage();
        } else if (direction == MessageBaseAdapter.CELL_TYPE_TEXT_DIRECTION_INCOMING) {
            setViewOtherMessage();
        } else if (isTypeImageDirectionOutgoing()) {
            setViewMyImage();
        } else if (direction == MessageBaseAdapter.CELL_TYPE_IMAGE_DIRECTION_INCOMING) {
            setViewOtherImage();
        }
    }

    public void setImageDisplayFullScreen(String imgUrl){
        ChatImageItem dfImageItem = new ChatImageItem();
        dfImageItem.setLargeUrl(imgUrl);
        ArrayList<ChatImageItem> mImages = new ArrayList<>();
        mImages.add(dfImageItem);

        if(!mImages.isEmpty()) {
            ChatActivity.start(mContext, mImages);
        }
    }

    private void setViewMyMessage(){
        textMy.setVisibility(View.VISIBLE);
        textMyTime.setVisibility(VISIBLE);
        textMyStatus.setVisibility(VISIBLE);
        imageMyFrame.setVisibility(GONE);

        textOther.setVisibility(GONE);
        textOtherTime.setVisibility(GONE);
        imageOtherFrame.setVisibility(GONE);

        viewGroupOtherMessage.setVisibility(GONE);
        viewGroupMyMessage.setVisibility(VISIBLE);
    }

    private void setViewMyImage(){
        textMy.setVisibility(View.GONE);
        textMyTime.setVisibility(VISIBLE);
        textMyStatus.setVisibility(VISIBLE);
        imageMyFrame.setVisibility(VISIBLE);


        textOther.setVisibility(GONE);
        textOtherTime.setVisibility(GONE);
        imageOtherFrame.setVisibility(GONE);
        viewGroupOtherMessage.setVisibility(GONE);
        viewGroupMyMessage.setVisibility(VISIBLE);
    }

    private void setViewOtherMessage(){
        textMy.setVisibility(View.GONE);
        textMyTime.setVisibility(textMyTime.isShown() ? VISIBLE : GONE);
        textMyStatus.setVisibility(textMyStatus.isShown() ? VISIBLE : GONE);
        imageMyFrame.setVisibility(GONE);

        textOther.setVisibility(VISIBLE);
        textOtherTime.setVisibility(VISIBLE);
        imageOtherFrame.setVisibility(GONE);

        textOtherTime.setText(messageTime);
        viewGroupOtherMessage.setVisibility(VISIBLE);
        viewGroupMyMessage.setVisibility(GONE);
    }

    private void setViewOtherImage(){
        textMy.setVisibility(View.GONE);
        textMyTime.setVisibility(GONE);
        textMyStatus.setVisibility(GONE);
        imageMyFrame.setVisibility(GONE);

        textOther.setVisibility(GONE);
        textOtherTime.setVisibility(VISIBLE);
        imageOtherFrame.setVisibility(VISIBLE);

        textOtherTime.setText(messageTime);
        viewGroupOtherMessage.setVisibility(VISIBLE);
        viewGroupMyMessage.setVisibility(GONE);
    }

    private void setViewDate(){
        textMy.setVisibility(View.GONE);
        textMyTime.setVisibility(GONE);
        textMyStatus.setVisibility(GONE);
        imageMyFrame.setVisibility(GONE);
        textOther.setVisibility(GONE);
        textOtherTime.setVisibility(GONE);
        imageOtherFrame.setVisibility(GONE);
        textOtherTime.setText(messageTime);
        viewGroupOtherMessage.setVisibility(GONE);
        viewGroupMyMessage.setVisibility(GONE);
    }

    private void setStatusReadMessage(){
        textMyStatus.setText(getContext().getString(R.string.chat_room_read));
        textMyStatus.setVisibility(VISIBLE);
    }

    private void setStatusUnReadMessage(){
        textMyStatus.setText(Constants.EMPTY_STRING);
    }

    private LayoutParams getLayoutParams(int height, int width){

        LayoutParams lp = null;
        if (width > height){
            lp = new LayoutParams(MudahUtil.dpToPx(FRAME_LONG_SIDE,getContext()), MudahUtil.dpToPx(FRAME_SHORT_SIDE, getContext()));
        }else{
            lp = new LayoutParams(MudahUtil.dpToPx(FRAME_SHORT_SIDE, getContext()), MudahUtil.dpToPx(FRAME_LONG_SIDE, getContext()));
        }

        return lp;
    }

    public ChatMessageView setMessage(String message) {
        if(isMyMessage){
            textMy.setText(message);
            textMy.setMovementMethod(LinkMovementMethod.getInstance());
            textOther.setText(Constants.EMPTY_STRING);
        }else{
            textMy.setText(Constants.EMPTY_STRING);
            textOther.setText(message);
            textOther.setMovementMethod(LinkMovementMethod.getInstance());
        }
        return this;
    }

    public ChatMessageView setImage(CCMessage.Data data) {
        if (data != null && data.getImage() != null) {
            if (isMyMessage) {
                imageOther.setImageBitmap(null);
                imageMyFrame.setLayoutParams(getLayoutParams(data.getImage().getHeight(), data.getImage().getWidth()));
                if(data.getImage().getThumbnailImage() != null){
                    imageMy.setImageBitmap(data.getImage().getThumbnailImage());
                }else if(!Validate.isNullOrEmpty(data.getImage().getThumbnailUrl())){
                    Picasso.with(mContext)
                            .load(data.getImage().getThumbnailUrl())
                            .fit()
                            .centerInside()
                            .placeholder(R.drawable.cat_others)
                            .into(imageMy);
                }
            } else {
                imageMy.setImageBitmap(null);
                imageOtherFrame.setLayoutParams(getLayoutParams(data.getImage().getHeight(), data.getImage().getWidth()));

                Picasso.with(mContext)
                        .load(data.getImage().getThumbnailUrl())
                        .fit()
                        .centerInside()
                        .placeholder(R.drawable.cat_others)
                        .into(imageOther);
            }
            setImageUrl(data.getImage().getFullUrl());
        }else{
            imageMy.setImageBitmap(null);
            imageOther.setImageBitmap(null);
            setImageUrl(Constants.EMPTY_STRING);
        }
        return this;
    }

    public ChatMessageView setDirection(int direction) {
        this.direction = direction;

        if (direction == MessageBaseAdapter.CELL_TYPE_TEXT_DIRECTION_OUTGOING) {
            isMyMessage = true;
            setViewMyMessage();
        } else if (direction == MessageBaseAdapter.CELL_TYPE_TEXT_DIRECTION_INCOMING) {
            isMyMessage = false;
            setViewOtherMessage();
        } else if (direction == MessageBaseAdapter.CELL_TYPE_IMAGE_DIRECTION_OUTGOING) {
            isMyMessage = true;
            setViewMyImage();
        } else if (direction == MessageBaseAdapter.CELL_TYPE_IMAGE_DIRECTION_INCOMING) {
            isMyMessage = false;
            setViewOtherImage();
        }
        return this;
    }

    public ChatMessageView setTime(Long timeStamp){
        try{
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_TIME_STAMP, Locale.getDefault());
            this.messageTime = sdf.format(new Date(timeStamp));
        }catch (Exception ignored){
            this.messageTime = Constants.EMPTY_STRING;
        }
        if(isMyMessage){
            textMyTime.setText(messageTime);
        }else{
            textOtherTime.setText(messageTime);
        }

        return this;
    }

    public ChatMessageView setStatusMessage(int statusReadCount) {
        if(statusReadCount == STATUS_CHAT_READ){
            setStatusUnReadMessage();
        }else{
            setStatusReadMessage();
        }

        return this;
    }

    public ChatMessageView setProgress(int progress){
        if( pbUploadMyImage != null ) {
            pbUploadMyImage.setProgress(progress);
        }
        return this;
    }

    public ChatMessageView setAlphaOnProcessSending(boolean statusComplete, boolean fail) {
        if(statusComplete){
            if (isTypeImageDirectionOutgoing()){
                setVisibilityMyUploadImage(GONE);
            }
        }else{
            if (isTypeImageDirectionOutgoing()){
                if(fail){
                    setVisibilityMyUploadImage(GONE);
                }else{
                    setVisibilityMyUploadImage(VISIBLE);
                }
            }

        }

        return this;
    }

    public ChatMessageView setImageUrl(String url){
        if(!Validate.isNullOrEmpty(url)) {
            if(isMyMessage){
                setImageOnclick(imageMy, url);
            }else{
                setImageOnclick(imageOther, url);
            }
        }else{
            if(isMyMessage){
                imageMy.setOnClickListener(null);
            }else{
                imageOther.setOnClickListener(null);
            }
        }
        return this;
    }

    private void setImageOnclick(ImageView view, final String url) {
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setImageDisplayFullScreen(url);
            }
        });
    }

    private boolean isTypeImageDirectionOutgoing() {
        return direction == MessageBaseAdapter.CELL_TYPE_IMAGE_DIRECTION_OUTGOING;
    }

    private void setAlphaItemView(float alpha) {
        this.itemView.setAlpha(alpha);
    }

    private void setVisibilityMyUploadImage(int gone) {
        if (pbUploadMyImage != null) this.pbUploadMyImage.setVisibility(gone);
    }

    private void setAlphaMyImage(float alpha) {
        if (imageMy != null) this.imageMy.setAlpha(alpha);
    }

}
