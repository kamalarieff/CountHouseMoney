package com.mudah.my.utils;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.activities.AdsListActivity;
import com.mudah.my.activities.FilterMenuActivity;
import com.mudah.my.activities.ListBookmarksActivity;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.dao.BookmarksDAO;
import com.mudah.my.helpers.NotificationBuilderHelper;
import com.mudah.my.models.BookmarkNotificationModel;
import com.mudah.my.widgets.ClearableEditText;

import java.util.HashMap;

/**
 * Created by moehninhtwee on 26/1/15.
 */
public class BookmarkUtil {
    public static final String CATEGORY = "category";
    public static final String REGION = "region";
    public static final String SUBREGION = "subregion";
    public static final String SEARCH_QUERY = "search_query";
    public static final String QUERY = "query";
    public static final String FILTER = "filter";
    public static final String LIST_IDS = "list_ids";
    public static final String REDIRECT_TO_BOOKMARK = "redirect";
    private Activity activity;
    private HashMap<String, String> values;
    private BookmarksDAO bookmarksDAO;
    private String[] arrayListId;

    public BookmarkUtil(Activity activity, String[] listId) {
        this.activity = activity;
        this.arrayListId = listId;
    }

    public BookmarkUtil(Activity activity, HashMap<String, String> values, String[] listId) {
        this(activity, values);
        this.arrayListId = listId;
    }

    public BookmarkUtil(Activity activity, HashMap<String, String> values) {
        this.activity = activity;
        this.values = values;
        bookmarksDAO = new BookmarksDAO(activity);
    }

    public void showSaveBookmarksDialog(final boolean createNotification) {
        Log.d();
        try {
            LayoutInflater inflater = activity.getLayoutInflater();
            final AlertDialog dialogAnnounce = new AlertDialog.Builder(activity, R.style.MudahDialogStyle)
                    .setTitle(R.string.bookmarks_dialog_title)
                    .setView(inflater.inflate(R.layout.dialog_save_ad_watch, null))
                    .setPositiveButton(R.string.bookmarks_dialog_button_save, null)
                    .setNegativeButton(R.string.bookmarks_dialog_button_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                    .create();

            dialogAnnounce.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    final EditText edt = ((ClearableEditText) dialogAnnounce
                            .findViewById(R.id.edtDialogComment)).getEditTextView();
                    final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Service.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(edt, 0);
                    edt.requestFocus();
                    StringBuilder strName = new StringBuilder();
                    String keyword = values.get(SEARCH_QUERY);
                    if (keyword != null && !keyword.equals("")) {
                        strName.append(keyword).append(" - ");
                    }
                    if (!ACUtils.isEmpty(values.get(SUBREGION))) {
                        strName.append(values.get(SUBREGION));
                    } else {
                        strName.append(values.get(REGION));
                    }
                    strName.append(Constants.DASH)
                            .append(values.get(CATEGORY));
                    edt.setText(strName.toString());
                    edt.setSelection(strName.length());

                    Button positiveButton = dialogAnnounce.getButton(AlertDialog.BUTTON_POSITIVE);

                    positiveButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            final EditText edt = ((ClearableEditText) dialogAnnounce
                                    .findViewById(R.id.edtDialogComment)).getEditTextView();
                            String bookmarkName = edt.getText().toString();
                            if (ACUtils.isEmpty(bookmarkName)) {
                                Toast.makeText(activity, R.string.bookmarks_validate_empty_name,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                long result;
                                if (values.containsKey(BookmarkUtil.LIST_IDS)) {
                                    result = bookmarksDAO.insertBookmarks(bookmarkName, values.get(QUERY), values.get(FILTER), values.get(LIST_IDS));
                                } else {
                                    result = bookmarksDAO.insertBookmarks(bookmarkName, values.get(QUERY), values.get(FILTER));
                                }
                                Config.bookmarkTotal++; // update this for displaying in Menu
                                dialogAnnounce.dismiss();
                                if (result > 0) {
                                    Toast.makeText(activity, R.string.bookmarks_save_success, Toast.LENGTH_SHORT).show();
                                    if (createNotification && arrayListId != null) {
                                        createBookmarkNotification(result);
                                    }

                                    if (values.containsKey(REDIRECT_TO_BOOKMARK) && values.get(REDIRECT_TO_BOOKMARK).equalsIgnoreCase("true")) {
                                        Intent intent = new Intent(activity, ListBookmarksActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        activity.startActivity(intent);
                                    } else if (activity instanceof FilterMenuActivity) {
                                        FilterMenuActivity filterMenuActivity = (FilterMenuActivity) activity;
                                        filterMenuActivity.directToListingPage(AdsListActivity.REQUEST_SAVE_BOOKMARKS, BookmarksDAO.BOOKMARK_ID, String.valueOf(result));
                                    } else if (!(activity instanceof AdsListActivity)) {
                                        Intent intent = new Intent(activity, AdsListActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        activity.startActivity(intent);
                                    } else if (activity instanceof AdsListActivity) {
                                        ((AdsListActivity) activity).sendTagSavedSearch();
                                    }

                                }
                            }
                        }
                    });
                }
            });

            dialogAnnounce.show();

            MudahUtil.hideDialogDivider(activity, dialogAnnounce);
        } catch (Exception e) {
            ACUtils.debug(e);
        }
    }

    public void createBookmarkNotification(long bookmarkId) {
        BookmarksDAO bookmarksDAO = new BookmarksDAO(activity);
        long result = bookmarksDAO.updateAdIdOfBookmark(bookmarkId, arrayListId);
        Log.d("bookmarkId: " + bookmarkId + ", result " + result);
        if (result >= 0) {
            BookmarkNotificationModel bookmarkNotificationModel = BookmarkNotificationModel.newInstance(activity.getBaseContext());
            NotificationBuilderHelper notificationBuilderHelper = new NotificationBuilderHelper();
            notificationBuilderHelper.createBookmarkNotificationReminder(activity.getBaseContext(), bookmarkNotificationModel);
        }
    }
}
