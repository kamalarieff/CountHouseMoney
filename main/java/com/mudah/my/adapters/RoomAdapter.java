package com.mudah.my.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.chatcafe.sdk.core.CCMessage;
import com.chatcafe.sdk.core.CCRoom;
import com.chatcafe.sdk.tool.RoomListBaseAdapter;
import com.lib701.utils.ACUtils;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.models.ChatCafe;
import com.mudah.my.utils.TimeUtil;
import com.mudah.my.widgets.RoomListView;

import java.util.ArrayList;
import java.util.List;

public class RoomAdapter extends RoomListBaseAdapter {
    private static final String DEFAULT_CREATED_TIME = "0";
    static ArrayList<Integer> checked;
    RoomListView view;
    boolean isDeleteRoom = false;
    OnDeleteChatRoomCallback onDeleteChatRoomCallback;
    OnUpdateRoomCallback onUpdateRoomCallback;

    public RoomAdapter(Context context) {
        super(context);
        checked = new ArrayList<>();
    }

    @Override
    public void onUpdateRoomComplete(List<CCRoom> result, @Nullable String error, boolean noMore) {
        super.onUpdateRoomComplete(result, error, noMore);
        int resultSize = 0;
        if (result != null) {
            resultSize = result.size();
        }
        getOnUpdateRoomCallback().onUpdateRoomComplete(resultSize, error);
    }

    public void setUpdateViewDeleteRoom(boolean isDeleteRoom) {
        this.isDeleteRoom = isDeleteRoom;
        checked = new ArrayList<Integer>();
        notifyDataSetChanged();
    }

    public void setCheckBox(int position) {
        checked.add(position);
        getOnDeleteChatRoomCallback().onCount(checked.size());
    }

    public void removeCheckBox(int position) {
        checked.remove(new Integer(position));
        getOnDeleteChatRoomCallback().onCount(checked.size());
    }

    public ArrayList<Integer> getListCCRoomPosition() {
        return checked;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        if (convertView != null && convertView instanceof RoomListView) {
            view = (RoomListView) convertView;
        } else {
            view = new RoomListView(context);
        }

        CCRoom ccRoom = getItem(position);
        boolean isChecked = false;
        for (Integer amount : checked) {
            if (amount == position) {
                isChecked = true;
            }
        }
        String createdTime = DEFAULT_CREATED_TIME;
        if (ccRoom.getLastMessage() != null) {
            if (!ACUtils.isEmpty(ccRoom.getLastMessage().getClientCreatedTime()))
                createdTime = ccRoom.getLastMessage().getClientCreatedTime();
            else if (!ACUtils.isEmpty(ccRoom.getLastMessage().getCreatedTime()))
                createdTime = ccRoom.getLastMessage().getCreatedTime();
        }
        view.setName(getName(ccRoom))
                .setUnreadNumber(String.valueOf(ccRoom.getUnreadCount()))
                .setSubject(ccRoom.getRoomDetail().getProduct().getName())
                .setLastMessage(ChatCafe.getLastMessage(context, ccRoom.getLastMessage()))
                .setUpdateTime(ccRoom.getLastMessage() != null ? TimeUtil.getTimeAgo(context, Long.parseLong(createdTime)) : Constants.EMPTY_STRING)
                .setAdsImage(getAdsImagePath(ccRoom))
                .setStatusMute(ccRoom.getPushStatus())
                .setStatusRoomDelete(isDeleteRoom)
                .setChecked(isChecked)
                .setStatus(ccRoom.getStatus() != null ? ccRoom.getStatus() : Constants.EMPTY_STRING)
                .setView();

        return view;
    }

    private String buildLastMessage(CCRoom ccRoom) {
        CCMessage ccMessage = ccRoom.getLastMessage();
        if (ccMessage == null) {
            return Constants.EMPTY_STRING;
        }
        String lastMsg = ChatCafe.getLastMessage(context, ccRoom.getLastMessage());
        return !getName(ccRoom).equals(Constants.EMPTY_STRING) ? getName(ccRoom) + ": " + lastMsg : lastMsg;
    }

    private String getSenderNameFromCCMessage(CCRoom ccRoom, CCMessage ccMessage) {
        String name = Constants.EMPTY_STRING;
        if (ChatCafe.isSender(ccMessage) && ChatCafe.isImageMessage(ccMessage)) {
            if (ccRoom.getRoomDetail() != null) {
                if (ccMessage.getSenderId().equals(ccRoom.getSellerId())) {
                    name = getSellerName(ccRoom);
                } else {
                    name = getBuyerName(ccRoom);
                }
            }
        }
        return name;
    }

    private String getName(CCRoom ccRoom) {
        String name = Constants.EMPTY_STRING;

        if (ccRoom.getRoomDetail() != null && !ACUtils.isEmpty(ccRoom.getSellerId())) {
            if (Config.userAccount.getUserId().equals(ccRoom.getSellerId())) {
                name = getBuyerName(ccRoom);
            } else {
                name = getSellerName(ccRoom);
            }
        }
        return name;
    }

    private String getSellerName(CCRoom ccRoom) {
        String buyerName = Constants.EMPTY_STRING;
        if (ccRoom.getRoomDetail().getSeller() != null) {

            if (!TextUtils.isEmpty(ccRoom.getRoomDetail().getSeller().getName())) {
                buyerName = ccRoom.getRoomDetail().getSeller().getName();
            } else {
                buyerName = Constants.EMPTY_STRING;
            }
        }
        return buyerName;
    }

    private String getBuyerName(CCRoom ccRoom) {
        String sellerName = Constants.EMPTY_STRING;
        if (ccRoom.getRoomDetail().getBuyer() != null) {

            if (!TextUtils.isEmpty(ccRoom.getRoomDetail().getBuyer().getName())) {
                sellerName = ccRoom.getRoomDetail().getBuyer().getName();
            } else {
                sellerName = Constants.EMPTY_STRING;
            }
        }
        return sellerName;
    }

    private String getAdsImagePath(CCRoom ccRoom) {
        String adsImagePath = Constants.EMPTY_STRING;

        if (ccRoom.getRoomDetail() != null
                && ccRoom.getRoomDetail().getProduct() != null) {

            if (!TextUtils.isEmpty(ccRoom.getRoomDetail().getProduct().getUrl())) {
                adsImagePath = ccRoom.getRoomDetail().getProduct().getUrl();
            } else {
                adsImagePath = Constants.EMPTY_STRING;
            }
        }

        return adsImagePath;
    }

    private String getProfileImagePath(CCRoom ccRoom) {
        String profileImagePath = Constants.EMPTY_STRING;

        if (ccRoom.getRoomDetail() != null) {
            if (Config.userAccount.getUserId().equals(ccRoom.getSellerId())) {
                profileImagePath = getBuyerImagePath(ccRoom, profileImagePath);
            } else {
                profileImagePath = getSellerImagePath(ccRoom, profileImagePath);
            }
        }
        return profileImagePath;
    }

    private String getSellerImagePath(CCRoom ccRoom, String profileImagePath) {
        if (ccRoom.getRoomDetail().getSeller() != null) {

            if (!TextUtils.isEmpty(ccRoom.getRoomDetail().getSeller().getUrl())) {
                profileImagePath = ccRoom.getRoomDetail().getSeller().getUrl();
            } else {
                profileImagePath = Constants.EMPTY_STRING;
            }
        }
        return profileImagePath;
    }

    private String getBuyerImagePath(CCRoom ccRoom, String profileImagePath) {
        if (ccRoom.getRoomDetail().getBuyer() != null) {

            if (!TextUtils.isEmpty(ccRoom.getRoomDetail().getBuyer().getUrl())) {
                profileImagePath = ccRoom.getRoomDetail().getBuyer().getUrl();
            } else {
                profileImagePath = Constants.EMPTY_STRING;
            }
        }
        return profileImagePath;
    }

    public OnDeleteChatRoomCallback getOnDeleteChatRoomCallback() {
        return onDeleteChatRoomCallback;
    }

    public void setOnDeleteChatRoomCallback(OnDeleteChatRoomCallback onDeleteChatRoomCallback) {
        this.onDeleteChatRoomCallback = onDeleteChatRoomCallback;
    }

    public OnUpdateRoomCallback getOnUpdateRoomCallback() {
        return onUpdateRoomCallback;
    }

    public void setOnUpdateRoomCallback(OnUpdateRoomCallback onUpdateRoomCallback) {
        this.onUpdateRoomCallback = onUpdateRoomCallback;
    }

    public interface OnDeleteChatRoomCallback {
        void onCount(int size);
    }

    public interface OnUpdateRoomCallback {
        void onUpdateRoomComplete(int size, String error);
    }
}

