package com.mudah.my.widgets;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.mudah.my.R;
import com.mudah.my.configs.Constants;
import com.squareup.picasso.Picasso;

public class RoomListView extends FrameLayout {

    private static final String BLOCKING = "BLOCKING";
    private static final String SYSTEM = "SYSTEM";
    private static final String BAN = "BAN";
    private static final String TAG = RoomListView.class.getSimpleName();
    ImageView imgAds;
    ImageView muteImage;
    TextView txtName, txtSubject;
    TextView txtLastMessage;
    TextView txtTime;
    ToggleButton checkBoxDelete;
    String updateTime;
    String name, subject;
    String lastMessage;
    String pathImageAds;
    String status;
    boolean isPushStatus;
    boolean isDeleteRoomStatus;
    boolean isChecked;
    private ChatCafeBadgeView badgeViewLayout;
    private int Xdimen = 94;
    private int Ydimen = 70;

    public RoomListView(Context context) {
        super(context);
        init();
    }

    public RoomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.chat_room_list_row, this);
        initInstance();
    }

    private void initInstance() {
        imgAds = (ImageView) findViewById(R.id.imgAds);
        txtName = (TextView) findViewById(R.id.txtName);
        txtSubject = (TextView) findViewById(R.id.txtSubject);
        txtLastMessage = (TextView) findViewById(R.id.txtLastMessage);
        txtTime = (TextView) findViewById(R.id.txtTime);
        badgeViewLayout = (ChatCafeBadgeView) findViewById(R.id.badgeViewLayout);
        checkBoxDelete = (ToggleButton) findViewById(R.id.checkBoxDelete);
        muteImage = (ImageView) findViewById(R.id.ic_room_mute_dark);

    }

    public RoomListView setUnreadNumber(String number) {
        if (TextUtils.isEmpty(number) || number.equals("0")) {
            badgeViewLayout.setVisibility(GONE);
            txtTime.setTextColor(getResources().getColor(R.color.inbox_message_color));
        } else {
            badgeViewLayout.setNumber(number);
            badgeViewLayout.setVisibility(VISIBLE);
            txtTime.setTextColor(getResources().getColor(R.color.inbox_unread_time_color));
        }
        return this;
    }

    private void setUpdateTime() {
        if (status != null && status.equals(BLOCKING) || status.equals(BAN)) {
            txtTime.setVisibility(View.GONE);
        } else {
            txtTime.setVisibility(View.VISIBLE);
        }
        txtTime.setText(updateTime != null ? updateTime : Constants.EMPTY_STRING);
    }

    private void setName() {
        txtName.setText(name != null ? name : Constants.EMPTY_STRING);
    }

    public void setSubject() {
        txtSubject.setText(subject != null ? subject : Constants.EMPTY_STRING);
    }

    private void setLastMessage() {

        if (lastMessage != null && lastMessage.length() > 0) {
            txtLastMessage.setVisibility(View.VISIBLE);
            txtLastMessage.setText(getLastMessageWithStatusBlocking());
        } else {
            txtLastMessage.setVisibility(View.GONE);
        }
    }

    private String getLastMessageWithStatusBlocking() {
        if (status != null && status.equals(BLOCKING)) {
            return getContext().getString(R.string.chat_status_people_block);
        } else {
            return lastMessage;
        }
    }

    public void setUpdateCheckbox() {
        checkBoxDelete.setChecked(isChecked);
    }

    private void setAdsImage() {
        if (status.equals(SYSTEM)) {
            imgAds.setVisibility(INVISIBLE);
        } else {
            imgAds.setVisibility(VISIBLE);
            if (!TextUtils.isEmpty(pathImageAds)) {

                Picasso.with(getContext())
                        .load(pathImageAds)
                        .centerCrop()
                        .resize(Xdimen, Ydimen)
                        .placeholder(R.drawable.cat_others)
                        .into(imgAds);
            } else {

                Picasso.with(getContext())
                        .load(R.drawable.cat_others)
                        .centerCrop()
                        .resize(Xdimen, Ydimen)
                        .into(imgAds);
            }
        }
    }

    private void setMuteStatus() throws Exception {
        if (!isPushStatus) {
            muteImage.setVisibility(View.VISIBLE);
        } else {
            muteImage.setVisibility(View.GONE);
        }
    }

    private void setEnableCheckboxDelete() {
        if (!isDeleteRoomStatus) {
            checkBoxDelete.setVisibility(GONE);
        } else {
            checkBoxDelete.setVisibility(VISIBLE);
        }
    }

    public void setView() {
        setUpdateTime();
        setName();
        setSubject();
        setLastMessage();
        setAdsImage();
        try {
            setMuteStatus();
        } catch (Exception e) {
            Log.e(TAG, " setView error = " + e.toString());
        }
        setEnableCheckboxDelete();
        setUpdateCheckbox();
    }

    public RoomListView setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public RoomListView setName(String name) {
        this.name = name;
        return this;
    }

    public RoomListView setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public RoomListView setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
        return this;
    }

    public RoomListView setAdsImage(String pathImageAds) {
        this.pathImageAds = pathImageAds;
        return this;
    }

    public RoomListView setStatusMute(boolean pushStatus) {
        this.isPushStatus = pushStatus;
        return this;
    }

    public RoomListView setStatusRoomDelete(boolean deleteStatus) {
        this.isDeleteRoomStatus = deleteStatus;
        return this;
    }

    public RoomListView setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        return this;
    }

    public RoomListView setStatus(String status) {
        this.status = status;
        return this;
    }
}
