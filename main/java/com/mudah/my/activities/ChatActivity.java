package com.mudah.my.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.ImageView;

import com.chatcafe.sdk.core.CCConstant;
import com.chatcafe.sdk.core.CCRoom;
import com.chatcafe.sdk.model.CCRoomDetail;
import com.lib701.datasets.ACAd;
import com.lib701.helper.FullScreenGallery;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.fragments.BlockChatRoomDialogFragment;
import com.mudah.my.fragments.ChatCafeFragment;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.models.ChatCafeData;
import com.mudah.my.models.ChatImageItem;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends MudahBaseActivity {

    public static final String INTENT_PRODUCT = "product";
    public static final String INTENT_ROOM_ID = "roomId";
    public static final String INTENT_CCROOM = "ccRoom";
    public static final String FROM_PUSH = "FROM_PUSH";
    public static final String EXTRA_PARAMS = "extra";
    public static final String ADDITIONAL = "additional";
    public static final int MAX_RETRY = 2;
    public static int RETRY_LOOP = 1;
    private CCRoom ccRoom;
    private boolean fromPush;
    private String roomId;
    private CreateRoomFromProduct createRoomFromProduct;
    private ACAd dfAdsDO;
    private ProgressDialog progressDialog;
    private ChatCafeFragment chatCafeFragment;
    private View vConnectionLost;
    private boolean isCreateRoomFromProduct;
    private ActionBarHelper actionBar;
    private View.OnClickListener productBarClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            startProductDetail();
        }
    };

    public static void buildStackChatRoomListActivity(Activity activity, String roomId) {
        try {
            Intent targetIntent = new Intent(activity, ChatActivity.class);
            targetIntent.putExtra(ChatActivity.INTENT_ROOM_ID, roomId);

            TaskStackBuilder tsb = TaskStackBuilder.create(activity);
            tsb.addParentStack(activity);
            tsb.addNextIntent(new Intent(activity, InboxActivity.class));
            tsb.addNextIntent(targetIntent);
            tsb.startActivities();
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
    }

    //use by room list page
    public static void start(Context context, CCRoom ccRoom) {
        Intent intent = new Intent(context, ChatActivity.class)
                .putExtra(INTENT_CCROOM, ccRoom);

        if (ccRoom.getRoomDetail() != null && ccRoom.getRoomDetail().getExtraParams() != null) {
            Map<String, Object> extraParamsFromAPI = ccRoom.getRoomDetail().getExtraParams();
            if (extraParamsFromAPI.containsKey(EXTRA_PARAMS)) {
                intent.putExtra(EXTRA_PARAMS, (String) extraParamsFromAPI.get(EXTRA_PARAMS));
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    //use by Ad Detail page
    public static void start(Context context, ACAd dfAdsDO) {
        Intent intent = new Intent(context, ChatActivity.class)
                .putExtra(INTENT_PRODUCT, dfAdsDO);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    //use by push notification
    public static void start(Context context, String roomId) {
        Intent intent = new Intent(context, ChatActivity.class)
                .putExtra(INTENT_ROOM_ID, roomId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void start(Context context, ArrayList<ChatImageItem> mImages) {
        ArrayList<String> resources = new ArrayList<>();
        resources.add(mImages.get(0).getLargeUrl());
        Intent intent = FullScreenGallery.newIntent(
                context, resources, 0);
        context.startActivity(intent
                .putExtra(Constants.LIST_IMAGE_URL, mImages)
                .putExtra(Constants.CURRENT_ITEM, 0)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void setActionbarTitles() {
        String talkToName;
        if (Config.userAccount.getUserId().equals(ccRoom.getSellerId())) {
            talkToName = ccRoom.getRoomDetail().getBuyer().getName();
        } else {
            talkToName = ccRoom.getRoomDetail().getSeller().getName();
        }
        Log.d("setActionbarTitles= " + talkToName + ", product url: " + ccRoom.getRoomDetail().getProduct().getUrl());
        if (ACUtils.isEmpty(talkToName)) {
            talkToName = Constants.CHAT;
        }

        actionBar.setProductTitles(this,
                ccRoom.getRoomDetail().getProduct().getUrl(),
                talkToName,
                ccRoom.getRoomDetail().getProduct().getName(),
                ccRoom.getRoomDetail().getProduct().getPrice());
    }

    private void doCommitFragmentFromCallback(@Nullable CCRoom result, final boolean isFromProduct) {
        if (result != null && !isFinishing()) {
            commitFragment(result);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //retry
                    if (RETRY_LOOP == MAX_RETRY) {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                            vConnectionLost.setVisibility(View.VISIBLE);
                        }
                        RETRY_LOOP = 1;
                    } else if (!isFinishing()) {
                        updateLoading(isFromProduct);
                    }
                    RETRY_LOOP++;
                }
            }, 5000);
        }
    }

    @Override
    protected void onDestroy() {
        createRoomFromProduct = null;
        super.onDestroy();
        if (vConnectionLost != null) {
            ACUtils.unbindDrawables(vConnectionLost);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat);

        setActionbar();

        createRoomFromProduct = new CreateRoomFromProduct();

        dfAdsDO = (ACAd) getIntent().getSerializableExtra(INTENT_PRODUCT);
        fromPush = getIntent().getBooleanExtra(FROM_PUSH, false);
        roomId = getIntent().getStringExtra(INTENT_ROOM_ID);
        ccRoom = getIntent().getParcelableExtra(INTENT_CCROOM);
        vConnectionLost = findViewById(R.id.view_chat_connection_lost);
        ImageView connectionLostImg = (ImageView) findViewById(R.id.imgv_connection_lost);
        Picasso.with(getApplicationContext()).load(R.drawable.loader_connection_lost).fit().centerInside().into(connectionLostImg);
        vConnectionLost.setVisibility(View.GONE);
        vConnectionLost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLoading(isCreateRoomFromProduct);
            }
        });

        displayProgressDialog();
        isCreateRoomFromProduct = false;
        if (ccRoom != null) {
            addRoomExtraParams(getIntent());
            commitFragment(ccRoom);
            Log.d("commitFragment() from Inbox");
        } else if (roomId != null) {
            createChatRoom(roomId);
            Log.d("createChatRoom() roomId " + roomId);
        } else if (dfAdsDO != null) {
            createChatRoom(dfAdsDO);
            Log.d("createChatRoom(dfAdsDO) from Adview");
            isCreateRoomFromProduct = true;
        }

        //Dismiss chat msg notification from the action bar
        MudahUtil.clearNotificationsByID(this, Config.NOTIFICATION_CHAT_MSG);
    }

    private void addRoomExtraParams(Intent intent) {
        if (intent == null)
            return;
        String strExtraParam = intent.getStringExtra(EXTRA_PARAMS);
        if (ccRoom.getRoomDetail() != null && !ACUtils.isEmpty(strExtraParam)) {
            HashMap<String, Object> extra = new HashMap<>();
            extra.put(EXTRA_PARAMS, strExtraParam);
            ccRoom.getRoomDetail().setExtraParams(extra);
        }
    }

    private void updateLoading(boolean isFromProduct) {
        vConnectionLost.setVisibility(View.GONE);
        displayProgressDialog();
        if (isFromProduct) {
            createChatRoom(dfAdsDO);
        } else {
            createChatRoom(roomId);
        }
    }

    private void setActionbar() {
        actionBar = new ActionBarHelper(this);
        actionBar.createChatActionBar(R.id.actionbar_chat, productBarClickListener);
    }

    private void startProductDetail() {
        ArrayList<String> listId = new ArrayList<>();
        listId.add(ccRoom.getProductId());
        Log.d("list id: " + ccRoom.getProductId());
        Intent intent = new Intent(this, AdViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(AdViewActivity.EXTRA_LIST_ITEM_POSITION, 0);
        intent.putExtra(AdViewActivity.EXTRA_GRAND_TOTAL, 1);
        intent.putStringArrayListExtra(AdViewActivity.EXTRA_ALL_LIST_ID, listId);
        startActivity(intent);
    }

    private void displayProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        Log.d(" displayProgressDialog ");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.show();
    }

    private void createChatRoom(final String roomId) {
        CCRoom.getRoomById(roomId, createRoomFromProduct);
    }

    private void createChatRoom(final ACAd dfAdsDO) {
        ChatCafeData data = new ChatCafeData(dfAdsDO);
        CCRoomDetail roomDetail = data.getRoomDetail();
        roomDetail.setExtraParams(TealiumHelper.prepareExtraParamForAPI(dfAdsDO));
        Log.d("request data: " + CCRoomDetail.serializeObject(roomDetail));
        CCRoom.createRoom(roomDetail, createRoomFromProduct);
    }

    private void commitFragment(final CCRoom ccRoom) {
        customizeRoomDetailExtraParam(ccRoom);
        TealiumHelper.tagTealiumViewChatRoom(ChatActivity.this, dfAdsDO, ccRoom);
        EventTrackingUtils.sendTagWithMode(XitiUtils.MODE_OTHERS, getApplicationContext(), TealiumHelper.CHAT_ROOM, XitiUtils.LEVEL2_CHAT_ID, null);
        this.ccRoom = ccRoom;
        setActionbarTitles();
        currentCCRoom = ccRoom;
        if (progressDialog != null && progressDialog.isShowing() && !ChatActivity.this.isFinishing())
            progressDialog.dismiss();

        chatCafeFragment = ChatCafeFragment.newInstance(ccRoom, fromPush);
        chatCafeFragment.setCreateRoomFromProduct(isCreateRoomFromProduct);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (!isFinishing() && !isDestroyed()) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.content, chatCafeFragment);
                ft.commit();
            }
        } else {
            if (!isFinishing()) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.content, chatCafeFragment);
                ft.commit();
            }
        }
    }

    public void onCompleteBlockChat(BlockChatRoomDialogFragment.StatusBlock statusBlock) {
        if (chatCafeFragment != null) {
            chatCafeFragment.onRetrieveBlock(statusBlock);
        }
    }

    /* Change response from API from this JSONobject
                extra: {
                    universal: {}
                    additional: {}
                }

       To the extra Hashmap <String, Object>
                universal: Hashmap <String, String>
                additional: Hashmap <String, String>
            }
            */
    private void customizeRoomDetailExtraParam(CCRoom result) {
        if (result == null || result.getRoomDetail() == null)
            return;

        HashMap<String, Object> customizedExtra = TealiumHelper.getChatExtraParamForAPI(result);
        result.getRoomDetail().setExtraParams(customizedExtra);
        Log.d("updated extra params: " + customizedExtra);
    }

    private class CreateRoomFromProduct implements CCConstant.CCResultCallback<CCRoom> {
        @Override
        public void onComplete(@Nullable CCRoom result, @Nullable String error) {
            doCommitFragmentFromCallback(result, isCreateRoomFromProduct);
        }
    }
}
