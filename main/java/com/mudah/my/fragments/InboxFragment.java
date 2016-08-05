package com.mudah.my.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.chatcafe.sdk.core.CCRoom;
import com.chatcafe.sdk.tool.RoomListBaseAdapter;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.activities.ChatActivity;
import com.mudah.my.activities.InboxActivity;
import com.mudah.my.adapters.RoomAdapter;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.utils.EventTrackingUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by pin on 29/1/16.
 */
public class InboxFragment extends Fragment implements View.OnClickListener {

    public boolean isDeleteChatRoom = false;
    private int roomType = RoomListBaseAdapter.FILTER_ALL;
    private RoomAdapter adapter;
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (isDeleteChatRoom) {
                ToggleButton item = (ToggleButton) view.findViewById(R.id.checkBoxDelete);
                setUpdateCheckbox(item, position);
            } else {
                CCRoom room = adapter.getItem(position);
                Config.badgeUnreadChatNumber -= room.getUnreadCount();
                room.setUnreadCount(0);
                ChatActivity.start(getActivity(), room);
                adapter.notifyDataSetChanged();
            }
        }
    };
    private Button btnConfirmDelete;
    private LinearLayout layoutDeleteRoom;
    private ListView list;
    private TextView emptyText;
    private View vConnectionLost;
    private ProgressBar pbLoading;

    public InboxFragment() {
    }

    public static InboxFragment newInstance(int newRomType) {
        InboxFragment inboxFragment = new InboxFragment();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("roomType", newRomType);

        inboxFragment.setArguments(args);
        return inboxFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            roomType = getArguments().getInt("roomType");
        }
        Log.d("roomType: " + roomType);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        vConnectionLost = view.findViewById(R.id.inbox_connection_lost);
        ImageView connectionLostImg = (ImageView) view.findViewById(R.id.imgv_connection_lost);
        Picasso.with(getContext()).load(R.drawable.loader_connection_lost).fit().centerInside().into(connectionLostImg);
        vConnectionLost.setVisibility(View.GONE);
        vConnectionLost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshRoom();
            }
        });

        pbLoading = (ProgressBar) view.findViewById(R.id.inbox_pb_loading);
        layoutDeleteRoom = (LinearLayout) view.findViewById(R.id.layoutActionDelete);
        btnConfirmDelete = (Button) view.findViewById(R.id.btnConfirmDelete);
        list = (ListView) view.findViewById(R.id.inbox_listView);
        emptyText = (TextView) view.findViewById(R.id.empty_chat);
        Button btnCancel = (Button) view.findViewById(R.id.btnCancel);

        btnConfirmDelete.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        adapter = new RoomAdapter(getActivity());
        adapter.filter(roomType);
        adapter.setOnDeleteChatRoomCallback(new RoomAdapter.OnDeleteChatRoomCallback() {
            @Override
            public void onCount(int size) {
                Button btnConfirmDelete = (Button) view.findViewById(R.id.btnConfirmDelete);
                btnConfirmDelete.setText(getString(R.string.delete) + getSize(size) + Constants.EMPTY_STRING);
            }
        });

        adapter.setListView(list);
        adapter.setOnItemClickListener(itemClickListener);

        adapter.setOnUpdateRoomCallback(new RoomAdapter.OnUpdateRoomCallback() {

            @Override
            public void onUpdateRoomComplete(int size, String error) {
                Log.d();
                if (!ACUtils.isEmpty(error)) {
                    vConnectionLost.setVisibility(View.VISIBLE);
                    pbLoading.setVisibility(View.GONE);
                    list.setVisibility(View.GONE);
                    emptyText.setVisibility(View.GONE);
                } else {
                    Log.d("error: " + error);
                    vConnectionLost.setVisibility(View.GONE);
                    pbLoading.setVisibility(View.GONE);
                    list.setVisibility(View.VISIBLE);
                    if (size == 0) {
                        setEmptyPageView();
                    }
                }
            }
        });

        return view;
    }

    public String getSize(int size) {
        if (size != 0) {
            return Constants.OPEN_BLACKET + size + Constants.CLOSE_BLACKET;
        } else {
            return Constants.EMPTY_STRING;
        }
    }

    private void setEmptyPageView() {
        Log.d("roomType: " + roomType);
        switch (roomType) {
            case RoomListBaseAdapter.FILTER_ALL:
                emptyText.setText(R.string.empty_chat_list);
                break;
            case RoomListBaseAdapter.FILTER_BUY:
                emptyText.setText(R.string.empty_chat_list_buy);
                break;
            case RoomListBaseAdapter.FILTER_SELL:
                emptyText.setText(R.string.empty_chat_list_sell);
                break;
        }
        list.setEmptyView(emptyText);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConfirmDelete:
                if (adapter.getListCCRoomPosition().size() == 0) {
                    Toast.makeText(getContext(), R.string.select_room_to_delete, Toast.LENGTH_SHORT).show();
                } else if (getActivity() instanceof InboxActivity) {
                    ((InboxActivity) getActivity()).setDisplayDialogDelete();
                }
                break;
            case R.id.btnCancel:
                setCancelDeleteChatRoom();
                break;
        }
    }

    public boolean isDeleteChatRoom() {
        return isDeleteChatRoom;
    }

    public int getNumberOfRooms() {
        return adapter.getCount();
    }

    public void setShowDeleteChatRoom() {
        toggleIsDeleteChatRoom();
        setToggleDeleteIcon();
        setShowCheckboxOnDeleteRoom();
    }

    public void onDelete(boolean isDeleteRoom) {
        Log.d("deleting in room type: " + roomType);
        try {
            if (adapter.getListCCRoomPosition().size() > 0) {
                List<CCRoom> listCCRoom = new ArrayList<>();
                String deletedUserIds = Constants.EMPTY_STRING;
                String roomIds = Constants.EMPTY_STRING;
                String deletedAdId = Constants.EMPTY_STRING;
                String deletedListId = Constants.EMPTY_STRING;
                String tmpAdId;

                for (int position : adapter.getListCCRoomPosition()) {
                    CCRoom ccRoom = adapter.getItem(position);
                    listCCRoom.add(ccRoom);
                    roomIds += ccRoom.getObjectId() + Constants.COMMA;
                    deletedListId += ccRoom.getProductId() + Constants.COMMA;

                    tmpAdId = TealiumHelper.getAdIdFromAPIExtraParam(ccRoom.getRoomDetail().getExtraParams());
                    if (!ACUtils.isEmpty(tmpAdId)) {
                        deletedAdId += tmpAdId + Constants.COMMA;
                    }

                    if (Config.userAccount.getUserId().equalsIgnoreCase(ccRoom.getBuyerId())) {
                        deletedUserIds += ccRoom.getSellerId() + Constants.COMMA;
                    } else if (Config.userAccount.getUserId().equalsIgnoreCase(ccRoom.getSellerId())) {
                        deletedUserIds += ccRoom.getBuyerId() + Constants.COMMA;
                    }
                }

                deletedUserIds = removeLastSeparator(deletedUserIds);
                roomIds = removeLastSeparator(roomIds);
                deletedListId = removeLastSeparator(deletedListId);
                deletedAdId = removeLastSeparator(deletedAdId);

                if (listCCRoom.size() > 0) {
                    updateExtraParam(listCCRoom.get(0), deletedListId, deletedAdId);
                }

                TealiumHelper.tagTealiumDeleteRoom(getActivity(), adapter.getListCCRoomPosition().size() + Constants.EMPTY_STRING, deletedUserIds, roomIds, deletedListId, deletedAdId);
                EventTrackingUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.DELETE_ROOM, XitiUtils.NAVIGATION);
                adapter.deleteRoom(listCCRoom);
            }

        } catch (Exception e) {
            Log.e(e.getMessage());
        }
        setCancelDeleteChatRoom();
    }

    /* Example
        extra: {
            universal: {}
            additional: {}
        }
    */
    private void updateExtraParam(CCRoom ccRoom, String deletedListId, String deletedAdId) {
        try {
            Map<String, Object> extraParamsFromAPI = ccRoom.getRoomDetail().getExtraParams();
            if (extraParamsFromAPI != null && extraParamsFromAPI.containsKey(ChatActivity.EXTRA_PARAMS)) {
                JSONObject jsonExtra = new JSONObject((String) extraParamsFromAPI.get(ChatActivity.EXTRA_PARAMS));
                //removing the current additional object and replace with a new one
                if (jsonExtra.has(ChatActivity.ADDITIONAL)) {
                    jsonExtra.remove(ChatActivity.ADDITIONAL);
                }
                JSONObject additional = new JSONObject();
                additional.put(TealiumHelper.AD_ID, deletedAdId);
                additional.put(TealiumHelper.LIST_ID, deletedListId);
                jsonExtra.put(TealiumHelper.ADDITIONAL, additional);

                extraParamsFromAPI.put(ChatActivity.EXTRA_PARAMS, jsonExtra.toString());
                ccRoom.getRoomDetail().setExtraParams(extraParamsFromAPI);
            }
        } catch (JSONException ignore) {
        }
    }

    private String removeLastSeparator(String original) {
        if (!ACUtils.isEmpty(original))
            return original.substring(0, original.length() - 1);
        else
            return Constants.EMPTY_STRING;
    }

    private void setCancelDeleteChatRoom() {
        btnConfirmDelete.setText(getResources().getString(R.string.delete));
        toggleIsDeleteChatRoom();
        setToggleDeleteIcon();
        setShowCheckboxOnDeleteRoom();
    }

    private void toggleIsDeleteChatRoom() {
        isDeleteChatRoom = !isDeleteChatRoom;
        Log.d("current isDeleteChatRoom: " + isDeleteChatRoom);
    }

    public void setToggleDeleteIcon() {
        Log.d("isDeleteChatRoom: " + isDeleteChatRoom);
        if (!isDeleteChatRoom) {
            layoutDeleteRoom.setVisibility(View.GONE);
        } else {
            layoutDeleteRoom.setVisibility(View.VISIBLE);
        }
    }

    private void setShowCheckboxOnDeleteRoom() {
        onUpdate(isDeleteChatRoom);
    }

    public void setUpdateCheckbox(ToggleButton checkBoxDelete, int position) {
        if (checkBoxDelete != null && checkBoxDelete.isChecked()) {
            checkBoxDelete.setChecked(false);
            adapter.removeCheckBox(position);
        } else {
            if (checkBoxDelete != null) {
                checkBoxDelete.setChecked(true);
            }
            adapter.setCheckBox(position);

        }
    }

    public void forceHiddenDeleteRoom() {
        try {
            if (layoutDeleteRoom != null && layoutDeleteRoom.isShown()) {
                isDeleteChatRoom = false;
                setToggleDeleteIcon();
                onUpdate(isDeleteChatRoom);
            }
        } catch (Exception e) {

        }
    }

    public void refreshRoom() {
        adapter.refreshRoomList();
    }

    public void onUpdate(final boolean isDeleteRoom) {
        adapter.setUpdateViewDeleteRoom(isDeleteRoom);
        adapter.setOnItemClickListener(itemClickListener);
    }


}
