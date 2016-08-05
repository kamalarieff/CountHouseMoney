package com.mudah.my.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chatcafe.sdk.Validate;
import com.chatcafe.sdk.core.CCConstant;
import com.chatcafe.sdk.core.CCImage;
import com.chatcafe.sdk.core.CCMessage;
import com.chatcafe.sdk.core.CCRoom;
import com.chatcafe.sdk.core.CCUser;
import com.chatcafe.sdk.tool.MessageBaseAdapter;
import com.chatcafe.sdk.tool.OnBottomReachedListener;
import com.lib701.datasets.ACAd;
import com.lib701.utils.ACUtils;
import com.lib701.utils.FileUtils;
import com.lib701.utils.ImageUtils;
import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.activities.ChatActivity;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.KahunaHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.utils.AmplitudeUtils;
import com.mudah.my.utils.AppsFlyerUtils;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahUtil;
import com.mudah.my.widgets.ChatMessageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatCafeFragment extends Fragment implements View.OnClickListener, OnBottomReachedListener {
    public static final String STRING = "";
    public static final String BLOCK = "block";
    public static final String REPORT = "report";
    public static final String OTHERS = "others";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final String MENU_RETRY = "Try again";
    private static final String MENU_CANCEL = "Cancel";
    private static final String INTENT = "ccroom";
    private static final String KEY_DATA = "data";
    private static final String EXTERNAL = "external";
    private static final int REQUEST_PICTURE_PERMISSION = 10;
    private static final int REQUEST_GALLERY_PERMISSION = 9;
    private static final int PICK_FROM_CAMERA = 11;
    private static final int PICK_IMAGE_GALLERY = 12;
    private static final int IMAGE_EDITOR = 14;
    protected OnBottomReachedListener callback;
    private MessageAdapter messageAdapter;
    private EditText messageBodyField;
    private CCRoom ccRoom;
    private ACAd dfAdsDO;
    private String memberId;
    private Button sendButton;
    private Button addButton;
    private ListView messagesList;
    private LinearLayout relSendMessage;
    private TextView ban;
    private File fileCamera;
    private ProgressDialog pd;
    private boolean mIsPush;
    private boolean mIsBlock;
    private MenuItem mMenuItem;
    private int currentReason;
    private LinearLayout layoutItem;//layoutSeller
    private boolean isCreateRoomFromProduct;
    private MenuItem menuMute;
    private boolean fromPush;
    private TextView noChatHistory;
    private int maxWidth = 700;
    private int maxHeight = 700;
    private ArrayList appsFlyerParams = new ArrayList();
    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            sendMessageWithImage(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }

    };

    public static ChatCafeFragment newInstance(CCRoom ccRoom, boolean fromPush) {
        Bundle args = new Bundle();
        args.putParcelable(INTENT, ccRoom);
        args.putBoolean(ChatActivity.FROM_PUSH, fromPush);
        ChatCafeFragment fragment = new ChatCafeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setCreateRoomFromProduct(boolean fromProduct) {
        isCreateRoomFromProduct = fromProduct;
    }

    public void setCallback(OnBottomReachedListener callback) {
        this.callback = callback;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        if (b != null) {
            ccRoom = b.getParcelable(INTENT);
            fromPush = b.getBoolean(ChatActivity.FROM_PUSH, false);
            boolean hasOptionMenu = true;
            if (ccRoom.getStatus().equals(CCRoom.STATUS_SYSTEM)) hasOptionMenu = false;
            if (ccRoom.getStatus().equals(CCRoom.STATUS_BAN)) hasOptionMenu = false;
            setHasOptionsMenu(hasOptionMenu);
        }
        dfAdsDO = (ACAd) getActivity().getIntent().getSerializableExtra(ChatActivity.INTENT_PRODUCT);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        setUpdateMessageMenuItem(menu);
        super.onPrepareOptionsMenu(menu);
    }

    private void setUpdateMessageMenuItem(Menu menu) {
        MenuItem menuNoti = menu.findItem(R.id.mute_chat_notification);
        MenuItem menuBlock = menu.findItem(R.id.menu_chat_block);
        updateMenuNotification(menuNoti);
        updateMenuBlock(menuBlock);
    }

    private void setUpdateMessageMenuItem(MenuItem menu) {
        if (menu.getItemId() == R.id.mute_chat_notification) {
            updateMenuNotification(menu);
        } else if (menu.getItemId() == R.id.menu_chat_block) {
            updateMenuBlock(menu);
        }
    }

    private void updateMenuNotification(MenuItem menu) {
        if (mIsPush) {
            menu.setTitle(getResources().getString(R.string.chat_notification_close));
        } else {
            menu.setTitle(getResources().getString(R.string.chat_notification_open));
        }
    }

    private void updateMenuBlock(MenuItem menu) {
        if (mIsBlock) {
            menu.setTitle(getResources().getString(R.string.chat_state_unblock));
        } else {
            menu.setTitle(getResources().getString(R.string.chat_state_block));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat, menu);
        menuMute = menu.findItem(R.id.mute_chat_notification);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mMenuItem = item;

        switch (item.getItemId()) {
            case R.id.mute_chat_notification:
                setMute(true);
                return true;
            case R.id.menu_chat_block:
                setShowBlockDialog();
                return true;
            case R.id.menu_chat_report:
                reportDialog();
                return true;
            case R.id.menu_attach_image:
                if (mIsBlock) {
                    Toast.makeText(getContext(), R.string.chat_status_blocked, Toast.LENGTH_SHORT).show();
                } else if (!isRequiredPermission(REQUEST_GALLERY_PERMISSION)) {
                    getGallery();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayReportConfirm(boolean retry) {
        displayDialog(Constants.EMPTY_STRING
                , retry ? getResources().getString(R.string.chat_report_status_fail)
                        : getResources().getString(R.string.chat_report_confirm)
                , retry ? getResources().getString(R.string.dialog_error_retry_button)
                        : getResources().getString(R.string.send)
                , getResources().getString(R.string.chat_report_text_eng));
    }

    private void reportDialog() {

        final CharSequence[] reportOptions = {getResources().getString(R.string.chat_report_reason_fraud)
                , getResources().getString(R.string.chat_report_reason_spam)
                , getResources().getString(R.string.chat_report_reason_others)};

        final AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(getActivity(), R.style.MudahDialogStyle)
                .setTitle(R.string.chat_report_title)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        myAlertDialog.setItems(reportOptions, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                reportConfirm(which + 1, reportOptions[which]);//ID starts from 1, but which start from 0. Need to plus 1 before sending
            }
        });

        AlertDialog dialog = myAlertDialog.show();
        dialog.getListView().setDividerHeight(0);
        MudahUtil.hideDialogDivider(getContext(), dialog);
    }

    private void reportConfirm(int reason, final CharSequence reportReason) {
        Log.d(" reasonId: " + reason);
        currentReason = reason;
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.common_text_wait));
        progressDialog.show();
        ccRoom.reportUser(currentReason, new CCConstant.CCResultCallback<JSONObject>() {
            @Override
            public void onComplete(@Nullable JSONObject result, @Nullable String error) {
                progressDialog.dismiss();
                if (error != null) {
                    displayReportConfirm(true);
                } else {
                    displayDialog(
                            getResources().getString(R.string.chat_report_title_dialog),
                            getResources().getString(R.string.chat_report_msg_dialog),
                            getResources().getString(R.string.chat_close), OTHERS);
                    TealiumHelper.tagTealiumChatOverFlowAction(getActivity(), ccRoom, TealiumHelper.REPORT, reportReason);
                    XitiUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.CHAT_REPORT_USER, XitiUtils.NAVIGATION);
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_FROM_CAMERA:
                if (resultCode == Activity.RESULT_OK && fileCamera != null) {
                    Uri uri = Uri.fromFile(fileCamera);
                    if (uri != null) {
                        Log.d("PICK_FROM_CAMERA " + uri.getPath());
                        prepareAndSendMessageWithImage(uri.getPath());
                        break;
                    }
                }
            case PICK_IMAGE_GALLERY:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String uri = Constants.EMPTY_STRING;
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        uri = FileUtils.getPath(getActivity(), selectedImage);
                    }

                    if (!ACUtils.isEmpty(uri)) {
                        Log.d("PICK_FROM_GALLERY 1 " + uri);
                        prepareAndSendMessageWithImage(uri);
                    } else if (selectedImage != null) {
                        try {
                            if (selectedImage != null && "content".equals(selectedImage.getScheme())) {
                                uri = FileUtils.getPath(getActivity(), selectedImage);
                            } else if (selectedImage != null && "file".equals(selectedImage.getScheme())) {
                                // convert file:// URI to filePath
                                uri = selectedImage.getPath();
                            }

                            if (!ACUtils.isEmpty(uri)) {
                                Log.d("PICK_FROM_GALLERY 2 " + uri);
                                prepareAndSendMessageWithImage(uri);
                            } else {
                                Log.d("PICK_FROM_GALLERY 3 -> getting bitmap from uri");
                                prepareAndSendMessageWithImage(selectedImage);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
        }
    }

    private void prepareAndSendMessageWithImage(String url) {
        Log.d("url: " + url);
        new DisplayImageTask(this, maxWidth, maxHeight, null).execute(url);
    }

    private void prepareAndSendMessageWithImage(Uri uri) {
        new DisplayImageTask(this, maxWidth, maxHeight, uri).execute(Constants.EMPTY_STRING);
    }

    private void setMute(final boolean isCallFromOptionMenu) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.common_text_wait));
        progressDialog.show();
        ccRoom.togglePushStatus(new CCConstant.CCResultCallback<CCRoom>() {
            @Override
            public void onComplete(@Nullable CCRoom result, @Nullable String error) {
                progressDialog.dismiss();
                if (result != null)
                    setToggleMuteIcon(ccRoom.getPushStatus());
                mIsPush = ccRoom.getPushStatus();
                if (isCallFromOptionMenu) {
                    setUpdateMessageMenuItem(mMenuItem);
                } else if (menuMute != null) {
                    updateMenuNotification(menuMute);
                }
                if (!mIsPush) {
                    TealiumHelper.tagTealiumChatOverFlowAction(getActivity(), ccRoom, TealiumHelper.MUTE, Constants.EMPTY_STRING);
                    XitiUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.CHAT_MUTE_NOTIFICATION, XitiUtils.NAVIGATION);
                } else {
                    TealiumHelper.tagTealiumChatOverFlowAction(getActivity(), ccRoom, TealiumHelper.UNMUTE, Constants.EMPTY_STRING);
                    XitiUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.CHAT_UNMUTE_NOTIFICATION, XitiUtils.NAVIGATION);
                }
            }
        });
    }

    public void setToggleMuteIcon(final boolean isPushStatus) {
        if (!isPushStatus) {
            Toast.makeText(getContext(), R.string.room_mute, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), R.string.room_unmute, Toast.LENGTH_SHORT).show();
        }
    }

    private void setDisplayStatusMute(CCRoom room) {
        if (room != null) {
            mIsPush = room.getPushStatus();
        }
    }

    private void setShowBlockDialog() {
        String header, title, buttonAction;
        String buyername = getName(ccRoom);
        if (mIsBlock) {
            header = getResources().getString(R.string.dialog_chat_unblock_confirm);
            title = getResources().getString(R.string.unblock_confirm_question, buyername);
            buttonAction = getResources().getString(R.string.chat_btn_unblock);
        } else {
            header = getResources().getString(R.string.dialog_chat_block_confirm);
            title = getResources().getString(R.string.block_confirm_question, buyername);
            buttonAction = getResources().getString(R.string.chat_btn_block);
            MudahUtil.hideSoftKeyboard(getActivity());
        }
        displayDialog(header, title, buttonAction, BLOCK);
    }

    private void setShowBlockRetryDialog(String failText) {
        displayDialog(getResources().getString(R.string.dialog_chat_block_fail_title)
                , failText
                , getResources().getString(R.string.dialog_chat_block_retry), BLOCK);
    }

    private void displayDialog(String header, String title, String titleActionButton, String type) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        BlockChatRoomDialogFragment dialog;
        dialog = BlockChatRoomDialogFragment.dialogInstance(header, title, titleActionButton, type);
        dialog.setRetainInstance(true);
        dialog.show(fm, BLOCK);
    }

    public void onRetrieveBlock(BlockChatRoomDialogFragment.StatusBlock statusBlock) {
        if (statusBlock.getType().equals(BLOCK)) {

            try {
                if (statusBlock.isStatusBlock()) {
                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage(getResources().getString(R.string.common_text_wait));
                    progressDialog.show();

                    Log.d("D/Cafe " + getMemberIdForBlock());
                    if (ccRoom.getStatus().equals(CCRoom.STATUS_ME_BLOCK)) {
                        ccRoom.unBlock(new CCConstant.CCResultCallback<JSONObject>() {
                            @Override
                            public void onComplete(@Nullable JSONObject result, @Nullable String error) {
                                progressDialog.dismiss();

                                if (result != null && result.optBoolean(KEY_DATA, false)) {
                                    block(false);
                                    mIsBlock = false;
                                    setUpdateMessageMenuItem(mMenuItem);
                                    TealiumHelper.tagTealiumChatOverFlowAction(getActivity(), ccRoom, TealiumHelper.UNBLOCK, Constants.EMPTY_STRING);
                                    XitiUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.CHAT_UNBLOCK_USER, XitiUtils.NAVIGATION);
                                } else {
                                    setShowBlockRetryDialog(getResources().getString(R.string.dialog_chat_unblock_fail_desc));
                                }
                            }
                        });
                    } else {
                        ccRoom.block(new CCConstant.CCResultCallback<JSONObject>() {
                            @Override
                            public void onComplete(@Nullable JSONObject result, @Nullable String error) {
                                progressDialog.dismiss();

                                if (result != null && result.optBoolean(KEY_DATA, false)) {
                                    block(true);
                                    mIsBlock = true;
                                    setUpdateMessageMenuItem(mMenuItem);
                                    TealiumHelper.tagTealiumChatOverFlowAction(getActivity(), ccRoom, TealiumHelper.BLOCK, Constants.EMPTY_STRING);
                                    XitiUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.CHAT_BLOCK_USER, XitiUtils.NAVIGATION);
                                } else {
                                    setShowBlockRetryDialog(getResources().getString(R.string.dialog_chat_block_fail_desc));
                                }
                            }
                        });
                    }

                }
            } catch (Exception e) {

            }
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getResources().getString(R.string.common_text_wait));
            progressDialog.show();
            ccRoom.reportUser(currentReason, new CCConstant.CCResultCallback<JSONObject>() {
                @Override
                public void onComplete(@Nullable JSONObject result, @Nullable String error) {
                    progressDialog.dismiss();
                    if (error != null) {
                        displayReportConfirm(true);
                    } else {
                        displayDialog(
                                getResources().getString(R.string.chat_report_title_dialog),
                                getResources().getString(R.string.chat_report_msg_dialog),
                                getResources().getString(R.string.chat_close), OTHERS);

                    }
                }
            });
        }
    }

    private String getMemberIdForBlock() {
        String id = Constants.EMPTY_STRING;
        memberId = ccRoom.getSellerId();
        if (memberId != null) {
            if (Config.userAccount.getUserId().equals(memberId)) {
                id = ccRoom.getBuyerId();
            } else {
                id = memberId;
            }
        }

        return id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {

        //bus.post(new ChatRoomObject(ccRoom));
        View view = inflater.inflate(R.layout.chat_room_view_main, container, false);
        messagesList = (ListView) view.findViewById(R.id.listMessages);
        messageBodyField = (EditText) view.findViewById(R.id.messageBodyField);
        ban = (TextView) view.findViewById(R.id.ban);
        relSendMessage = (LinearLayout) view.findViewById(R.id.relSendMessage);

        addButton = (Button) view.findViewById(R.id.addButton);
        addButton.setOnClickListener(this);
        sendButton = (Button) view.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);

        callback = this;
        messageAdapter = new MessageAdapter(getActivity(), ccRoom);
        messageAdapter.setListView(messagesList);
        messageAdapter.setItemCountListener(new CCConstant.CCResultCallback<Integer>() {
            @Override
            public void onComplete(@Nullable Integer result, @Nullable String error) {
                //Tag tealium for create room
                if (result == 0) {
                    setHideViewNoItem();
                    TealiumHelper.tagTealiumCreateRoom(getActivity(), dfAdsDO, ccRoom);
                }
            }
        });

        messageAdapter.setOnIncomingMessage(new CCConstant.CCResultCallback<CCMessage>() {
            @Override
            public void onComplete(@Nullable CCMessage result, @Nullable String error) {
                if (result == null) return;
            }
        });

        messageAdapter.setOnScrollListener(new AbsListView.OnScrollListener() {
            int mOffset = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int position = firstVisibleItem + visibleItemCount;
                int limit = totalItemCount - mOffset;

                // Check if bottom has been reached
                if (position >= limit && totalItemCount > 0) {
                    callback.onBottomReached();
                } else {
                    callback.onOutOfBottomReached();
                }
            }
        });
        messageAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        noChatHistory = (TextView) view.findViewById(R.id.no_chat);

        verifyStatus();
        setDisplayStatusMute(ccRoom);

        return view;
    }

    private void verifyStatus() {
        if (ccRoom.getStatus().equals(CCRoom.STATUS_BAN)) {
            ban(true);
        } else if (ccRoom.getStatus().equals(CCRoom.STATUS_SYSTEM)) {
            system(true);
        } else if (ccRoom.getStatus().equals(CCRoom.STATUS_ME_BLOCK)) {
            mIsBlock = true;
            block(true);
        }
    }

    private void system(boolean isSystem) {
        if (isSystem) {
            layoutItem.setOnClickListener(null);
        }
        block(isSystem);
    }

    private void ban(boolean isBan) {
        if (isBan) {
            messagesList.setVisibility(View.GONE);
            relSendMessage.setVisibility(View.GONE);
            this.ban.setVisibility(View.VISIBLE);
        } else {
            messagesList.setVisibility(View.VISIBLE);
            relSendMessage.setVisibility(View.VISIBLE);
            this.ban.setVisibility(View.GONE);
        }
        if (ccRoom != null && ccRoom.getStatus().equals(CCRoom.STATUS_BAN)) {
            this.ban.setText(ccRoom.getStatusMessage());
        }
    }

    private void block(boolean block) {
        if (block) {
            messageBodyField.setText(Constants.EMPTY_STRING);
            messageBodyField.setHint(R.string.chat_status_blocked);
            messageBodyField.setFocusable(false);
            sendButton.setVisibility(View.INVISIBLE);
            addButton.setVisibility(View.INVISIBLE);
        } else {
            messageBodyField.setHint(getString(R.string.send_chat_hint));
            messageBodyField.setFocusable(true);
            messageBodyField.setFocusableInTouchMode(true);
            sendButton.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.VISIBLE);
            sendButton.setOnClickListener(this);
            sendButton.setBackgroundResource(R.drawable.pn_chat_btnsend);
            addButton.setOnClickListener(this);
        }
    }

    private void setHideViewNoItem() {
        if (!ccRoom.getStatus().equals(CCRoom.STATUS_BAN)) {
            messagesList.setEmptyView(noChatHistory);
        }
    }

    private String getName(CCRoom ccRoom) {
        String name = STRING;

        if (ccRoom.getRoomDetail() != null) {
            if (Config.userAccount.getUserId().equals(ccRoom.getSellerId())) {
                name = getBuyerName(ccRoom);
            } else {
                name = getSellerName(ccRoom);
            }
        }
        return name;
    }

    private String getSellerName(CCRoom ccRoom) {
        String buyerName = STRING;
        if (ccRoom.getRoomDetail().getSeller() != null) {

            if (!TextUtils.isEmpty(ccRoom.getRoomDetail().getSeller().getName())) {
                buyerName = ccRoom.getRoomDetail().getSeller().getName();
            } else {
                buyerName = STRING;
            }
        }
        return buyerName;
    }

    private String getBuyerName(CCRoom ccRoom) {
        String sellerName = STRING;
        if (ccRoom.getRoomDetail().getBuyer() != null) {

            if (!TextUtils.isEmpty(ccRoom.getRoomDetail().getBuyer().getName())) {
                sellerName = ccRoom.getRoomDetail().getBuyer().getName();
            } else {
                sellerName = STRING;
            }
        }
        return sellerName;
    }

    private String getBuyerImagePath(CCRoom ccRoom) {
        String profileImagePath = STRING;
        if (ccRoom.getRoomDetail().getBuyer() != null) {

            if (!TextUtils.isEmpty(ccRoom.getRoomDetail().getBuyer().getUrl())) {
                profileImagePath = ccRoom.getRoomDetail().getBuyer().getUrl();
            } else {
                profileImagePath = STRING;
            }
        }
        return profileImagePath;
    }

    private void setAdsImage(CCRoom ccRoom, ImageView itemImage) {
        String adsImagePath;

        if (ccRoom.getRoomDetail() != null
                && ccRoom.getRoomDetail().getProduct() != null) {

            if (!TextUtils.isEmpty(ccRoom.getRoomDetail().getProduct().getUrl())) {
                adsImagePath = ccRoom.getRoomDetail().getProduct().getUrl();
            } else {
                adsImagePath = STRING;
            }

            if (!TextUtils.isEmpty(adsImagePath)) {

                Picasso.with(getActivity())
                        .load(adsImagePath)
                        .centerCrop()
                        .resize(200, 200)
                        .placeholder(R.drawable.cat_others)
                        .into(itemImage);
            } else {

                Picasso.with(getActivity())
                        .load(R.drawable.cat_others)
                        .centerCrop()
                        .resize(200, 200)
                        .into(itemImage);
            }
        }
    }

    public void sendMessage() {
        String messageBody = messageBodyField.getText().toString().trim();
        if (Validate.isNullOrEmpty(messageBody)) {
            return;
        }

        CCMessage.Builder builder = new CCMessage.Builder()
                .addPart(messageBody)
                .setCustomUniqueId(MudahUtil.getChatUniqueId());
        CCMessage ccMessage = builder.build();
        if (messageAdapter.getCount() == 0) {
            //Tag tealium when the first message sent
            TealiumHelper.tagTealiumSendFirstMessage(getActivity(), dfAdsDO, ccMessage, ccRoom, Constants.EMPTY_STRING);
            if (dfAdsDO != null) {
                tagAppsFlyerChatEvent(Integer.parseInt(dfAdsDO.getCategoryId()));
                AmplitudeUtils.tagReply(dfAdsDO, Constants.CHAT);
                AmplitudeUtils.incrementProperty(AmplitudeUtils.CHAT_EVENT);
                String fullTagName = TealiumHelper.CHAT_SEND_FIRST_MESSAGE + XitiUtils.CHAPTER_SIGN + ACUtils.encodeSign(dfAdsDO.getParentCategoryName()) + XitiUtils.CHAPTER_SIGN + ACUtils.encodeSign(dfAdsDO.getCategoryName());
                EventTrackingUtils.sendAdReply(fullTagName, dfAdsDO);
            } else {
                EventTrackingUtils.sendAdReply(TealiumHelper.CHAT_SEND_FIRST_MESSAGE, dfAdsDO);
            }
        } else {
            //Tag tealium when the message sent
            TealiumHelper.tagTealiumSendMessage(getActivity(), dfAdsDO, ccMessage, ccRoom, Constants.EMPTY_STRING);
            XitiUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.CHAT_SEND_MESSAGE, XitiUtils.NAVIGATION);
        }
        tagKahuna();
        messageAdapter.sendMessage(ccMessage);

        messageBodyField.setText(STRING);

    }

    private void tagAppsFlyerChatEvent(int categoryId) {
        if (Constants.CARS == categoryId) {
            AppsFlyerUtils.sendConversionTag(getActivity(), AppsFlyerUtils.AppsFlyerTags.CHAT_CARS_APP, dfAdsDO);
        } else {
            AppsFlyerUtils.sendConversionTag(getActivity(), AppsFlyerUtils.AppsFlyerTags.CHAT_MARKETPLACE_APP, dfAdsDO);
        }
    }

    private void tagKahuna() {
        KahunaHelper.tagEvent(KahunaHelper.LAST_CHATTED);
        KahunaHelper.tagAttributes(KahunaHelper.LAST_CHAT, KahunaHelper.USER_NAME, Config.userAccount.getUsername());
    }

    public void sendMessageWithImage(Bitmap imageFileBitmap) {
        CCImage image = CCImage.setBitmap(imageFileBitmap);
        CCMessage.Builder builder = new CCMessage.Builder()
                .addPart(image)
                .setCustomUniqueId(MudahUtil.getChatUniqueId());
        CCMessage ccMessage = builder.build();
        String imageUrl = ccRoom.getRoomDetail().getProduct().getUrl();
        if (messageAdapter.getCount() == 0) {
            //Tag tealium when the first image sent
            TealiumHelper.tagTealiumSendFirstMessage(getActivity(), dfAdsDO, ccMessage, ccRoom, imageUrl);
            EventTrackingUtils.sendAdReply(TealiumHelper.CHAT_SEND_FIRST_IMAGE, dfAdsDO);
        } else {
            //Tag tealium when the image sent
            TealiumHelper.tagTealiumSendMessage(getActivity(), dfAdsDO, ccMessage, ccRoom, imageUrl);
            XitiUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.CHAT_SEND_IMAGE, XitiUtils.NAVIGATION);
        }
        messageAdapter.sendMessage(ccMessage);

    }

    @Override
    public void onBottomReached() {
    }

    @Override
    public void onOutOfBottomReached() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendButton:
                sendMessage();
                break;
            case R.id.addButton:
                if (!isRequiredPermission(REQUEST_PICTURE_PERMISSION)) {
                    getCamera();
                }
                break;
        }
    }

    private void getCamera() {
        Intent pictureActionIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileCamera = FileUtils.getPublicTempImageFile(Config.UPLOAD_IMAGES_DIR, System.currentTimeMillis() + Config.CHAT_IMG_NAME);
        pictureActionIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileCamera));
        startActivityForResult(pictureActionIntent, PICK_FROM_CAMERA);
    }

    private void getGallery() {
        Intent pictureActionIntent;
        //Kitkat sdk 4.4 has changed a return URI format
        //Ref: http://stackoverflow.com/questions/19834842/android-gallery-on-kitkat-returns-different-uri-for-intent-action-get-content

        if (Build.VERSION.SDK_INT < 19) {
            pictureActionIntent = new Intent();
            pictureActionIntent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            pictureActionIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pictureActionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            pictureActionIntent.setAction(Intent.ACTION_GET_CONTENT);
        }
        pictureActionIntent.setType(FileUtils.MIME_TYPE_IMAGE);
        startActivityForResult(pictureActionIntent, PICK_IMAGE_GALLERY);
    }

    private boolean isRequiredPermission(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission Granted");
            switch (requestCode) {
                case REQUEST_PICTURE_PERMISSION:
                    getCamera();
                    break;
                case REQUEST_GALLERY_PERMISSION:
                    getGallery();
                    break;
            }
        } else {
            Log.d("Permission Denial");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        messageAdapter.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Picasso.with(getActivity().getApplicationContext()).cancelRequest(target);
        messageAdapter.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        messageAdapter.onPause();
    }

    private static class DisplayImageTask extends AsyncTask<String, Void, Bitmap> {
        WeakReference<ChatCafeFragment> fragmentWeakReference;
        int imageViewWidth, imageViewHeight;
        Uri selectedImageUri;

        public DisplayImageTask(ChatCafeFragment chatFragment, int viewWidth, int viewHeight, Uri selectedImage) {
            fragmentWeakReference = new WeakReference<>(chatFragment);
            imageViewWidth = viewWidth;
            imageViewHeight = viewHeight;
            selectedImageUri = selectedImage;
        }

        protected Bitmap doInBackground(String... selectedImgPath) {
            if (fragmentWeakReference == null || fragmentWeakReference.get() == null)
                return null;

            ChatCafeFragment chatFragment = fragmentWeakReference.get();
            Bitmap selectedBitmap = null;

            if (ACUtils.isEmpty(selectedImgPath[0]) && selectedImageUri != null) {
                //get Bitmap from Uri
                try {
                    selectedBitmap = Picasso.with(chatFragment.getContext())
                            .load(selectedImageUri)
                            .resize(imageViewWidth, imageViewHeight).centerInside().onlyScaleDown()
                            .get();
                } catch (IOException ignore) {
                    Log.e(ignore);
                } catch (SecurityException ignore2) {
                    Log.e(ignore2);
                }
            } else {
                //get Bitmap from path
                try {
                    selectedBitmap = ImageUtils.getBitmapWithinOption(chatFragment.getContext(), selectedImgPath[0], imageViewWidth, imageViewHeight, imageViewWidth, imageViewHeight);
                } catch (Exception ignoreException) {

                }
            }
            return selectedBitmap;
        }

        protected void onPostExecute(Bitmap selectedBitmap) {
            if (fragmentWeakReference == null || fragmentWeakReference.get() == null)
                return;

            ChatCafeFragment chatFragment = fragmentWeakReference.get();

            if (selectedBitmap == null) {
                Toast.makeText(chatFragment.getContext(), R.string.chat_upload_img_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            if (chatFragment != null) {
                chatFragment.sendMessageWithImage(selectedBitmap);
            }

        }
    }

    private class MessageAdapter extends MessageBaseAdapter {

        public MessageAdapter(@NonNull Activity activity, @NonNull CCRoom ccRoom) {
            super(activity, ccRoom);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (getDirection(position) == CELL_TYPE_DATE) {

                CellDateViewHolder view;

                if (convertView == null) {
                    view = new CellDateViewHolder(activity, null);
                    convertView = view.itemView;
                    convertView.setTag(view);
                } else {
                    view = (CellDateViewHolder) convertView.getTag();
                }

                CCMessage ccMessage = getItem(position);

                if (Validate.isNullOrEmpty(ccMessage.getCreatedTime())) {
                    view.dateText.setText(sdf.format(new Date(Long.parseLong(ccMessage.getClientCreatedTime()))));
                } else {
                    String createdDate = sdf.format(new Date(Long.parseLong(ccMessage.getCreatedTime())));
                    String today = sdf.format(new Date());
                    if (today.equals(createdDate)) {
                        view.dateText.setText(Constants.TODAY);
                    } else {
                        view.dateText.setText(createdDate);
                    }
                }

                return view.itemView;
            } else {
                ChatMessageView view;
                if (convertView != null && convertView instanceof ChatMessageView) {
                    view = (ChatMessageView) convertView;
                } else {
                    view = new ChatMessageView(activity);
                }

                view.setDirection(getDirection(position))
                        .setTime(getTimeStamp(position))
                        .setStatusMessage(readCount(position))
                        .setAlphaOnProcessSending(sendComplete(position), isFail(position))
                        .setMessage(getText(position))
                        .setImage(getItem(position).getData())
                        .setProgress(getProgress(position));

                if (isFail(position)) {
                    if (getItem(position).getSenderId().equals(CCUser.getCurrentUser().getObjectId())) {
                        view.btnRetry.setVisibility(View.VISIBLE);
                        view.btnRetry.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                retryDialog(position);
                            }
                        });
                    }
                } else {
                    if (getItem(position).getSenderId().equals(CCUser.getCurrentUser().getObjectId())) {
                        view.btnRetry.setVisibility(View.GONE);
                        view.btnRetry.setOnClickListener(null);
                    }
                }
                return view;
            }

        }

        private void retryDialog(final int position) {

            CharSequence[] menuImage = {MENU_RETRY, MENU_CANCEL};

            AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(activity);
            myAlertDialog.setItems(menuImage, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            sendMessage(getItem(position));
                            break;

                    }
                }
            });
            myAlertDialog.show();
        }

        public class CellDateViewHolder {

            public View itemView;
            public TextView dateText;

            public CellDateViewHolder(Context context, ViewGroup parent) {
                itemView = LayoutInflater.from(context).inflate(R.layout.chat_room_view_messages_cell_date, parent);
                dateText = (TextView) itemView.findViewById(R.id.dateText);
            }
        }
    }
}
