package com.mudah.my.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.activities.AdsListActivity;
import com.mudah.my.activities.ListBookmarksActivity;
import com.mudah.my.adapters.ListBookmarksAdapter;
import com.mudah.my.configs.Config;
import com.mudah.my.dao.BookmarksDAO;
import com.mudah.my.models.BookmarksModel;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahUtil;

import java.util.ArrayList;

public class ListBookmarksFragment extends ListFragment {

    public static final String REQUEST_BOOKMARKS_QUERY = "bookmarks_query";
    public static final String REQUEST_BOOKMARKS_FILTER = "bookmarks_filter";

    private ListMode viewMode = ListMode.VIEW;
    private ListBookmarksAdapter adapter = new ListBookmarksAdapter();
    private BookmarksDAO bookmarksDAO;
    private TextView tvTitle;
    private SparseArray<Integer> selectedPositions = new SparseArray<Integer>();//SparseArray Better performance than Hashmap
    private String level2Bookmark;

    public ListMode getViewMode() {
        return viewMode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        level2Bookmark = XitiUtils.getLevel2Map(XitiUtils.LEVEL2_BOOKMARK);
        if (savedInstanceState != null) {
            viewMode = ListMode.valueOf(savedInstanceState.getString("savedViewMode"));
        } else {
            viewMode = ListMode.VIEW;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout contentLayout = (LinearLayout) inflater.inflate(R.layout.common_listview, null);
        tvTitle = (TextView) contentLayout.findViewById(R.id.result_title);
        tvTitle.setText(getActivity().getString(R.string.list_bookmarks));
        View resultView = super.onCreateView(inflater, container, savedInstanceState);

        resultView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        ((FrameLayout) contentLayout.findViewById(R.id.view_content)).addView(resultView);
        return contentLayout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //hide the options if there is no data
        if (adapter.getCount() > 0)
            inflater.inflate(R.menu.bookmark_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = super.onOptionsItemSelected(item);
        if (result) return result;
        switch (item.getItemId()) {
            case R.id.menu_delete_multiple:
                //to prevent users clicking twice, proceed with changes only when users in View mode
                if (ListMode.EDIT != viewMode) {
                    selectedPositions.clear();
                    updateViewMode(ListMode.EDIT);
                    adapter.setListMode(viewMode);
                    adapter.notifyDataSetChanged();
                    EventTrackingUtils.sendClick(level2Bookmark, "Multiple delete Bookmarks", XitiUtils.NAVIGATION);
                }
                return true;
            case R.id.menu_delete_all:
                selectedPositions.clear();
                deleteAllItem();
                EventTrackingUtils.sendClick(level2Bookmark, "Delete all Bookmarks", XitiUtils.NAVIGATION);
                return true;
        }
        return false;
    }

    private void updateActionButtonText() {
        if (getActivity() instanceof ListBookmarksActivity)
            ((ListBookmarksActivity) getActivity()).updateActionButtonText(viewMode);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getString(R.string.bookmarks_none));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            bookmarksDAO = new BookmarksDAO(getActivity());
            ArrayList<BookmarksModel> arrayList = bookmarksDAO.getAllBookmarks();
            updateSelectedStatus(arrayList);
            adapter.setItems(arrayList);

            setListAdapter(adapter);

            updateViewMode(viewMode);
            redrawOptionMenu();
            getListView().setItemsCanFocus(false);
            getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
                    BookmarksModel bookmarks = adapter.getItem(position);
                    if (bookmarks.getPlaceholder() == BookmarksModel.ACTUAL_BOOKMARK) {
                        deleteSingleItem(position);
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
    }

    private void updateSelectedStatus(ArrayList<BookmarksModel> arrayList) {
        if (selectedPositions.size() > 0 && arrayList != null) {
            int loopSize = selectedPositions.size();
            for (int loop = 0; loop < loopSize; loop++) {
                int position = selectedPositions.keyAt(loop);
                if (position < arrayList.size()) {
                    BookmarksModel bookmarks = arrayList.get(position);
                    bookmarks.setSelected(true);
                }
            }
        }
    }

    private void updateTitle() {
        tvTitle.setText(String.format("%s (%d/%d)", getString(R.string.list_bookmarks),
                bookmarksDAO.getAllBookmarks().size(), Config.maxBookmarksTotal));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        BookmarksModel item = adapter.getItem(position);
        if (ListMode.VIEW == viewMode) { // View Mode
            Intent intent = new Intent(getActivity(), AdsListActivity.class);
            BookmarksModel bookmarks = adapter.getItem(position);
            intent.putExtra(BookmarksDAO.BOOKMARK_ID, String.valueOf(bookmarks.getId()));
            intent.putExtra(REQUEST_BOOKMARKS_QUERY, bookmarks.getQuery());
            intent.putExtra(REQUEST_BOOKMARKS_FILTER, bookmarks.getFilter());
            intent.putExtra(AdsListActivity.FUNCTION_REQUEST, AdsListActivity.REQUEST_BOOKMARKS);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else {
            if (item.isSelected()) {
                selectedPositions.remove(position);
                item.setSelected(false);
                removeSelectedView(v);
            } else {
                selectedPositions.put(position, position);
                item.setSelected(true);
                setSelectedView(v);
            }
            updateViewMode(ListMode.EDIT);
        }
    }

    private void removeSelectedView(View v) {
        TextView tvName = (TextView) v.findViewById(R.id.tv_name);
        tvName.setTextColor(getActivity().getResources().getColor(R.color.black));
        CheckBox checkBox = (CheckBox) v.findViewById(R.id.cb_bookmark);
        checkBox.setChecked(false);
    }

    private void setSelectedView(View v) {
        TextView tvName = (TextView) v.findViewById(R.id.tv_name);
        tvName.setTextColor(getActivity().getResources().getColor(R.color.selected_text));
        CheckBox checkBox = (CheckBox) v.findViewById(R.id.cb_bookmark);
        checkBox.setChecked(true);
    }

    public void updateViewMode(ListMode mode) {
        viewMode = mode;
        adapter.setListMode(viewMode);
        if (ListMode.VIEW == mode) {
            updateTitle();
            adapter.notifyDataSetChanged();
        } else {// Edit mode
            updateSelectedTitle();
        }
        updateActionButtonText();

    }

    private void updateSelectedTitle() {
        tvTitle.setText(String.format("%d %s", selectedPositions.size(), getString(R.string.selected_favourites)));
    }

    public void deleteSingleItem(final int position) {
        BookmarksModel bookmarks = adapter.getItem(position);
        selectedPositions.clear();
        selectedPositions.put(position, position);
        if (bookmarks != null) {
            ArrayList<Long> selectedIds = new ArrayList<Long>();
            selectedIds.add(bookmarks.getId());
            showDeleteDialog(DeleteMode.SINGLE, selectedIds);
            EventTrackingUtils.sendClick(level2Bookmark, "Single delete Bookmarks", XitiUtils.NAVIGATION);
        }
    }

    public void deleteSelectedItem() {
        if (selectedPositions.size() > 0) {
            int loopSize = selectedPositions.size();
            ArrayList<Long> selectedIds = new ArrayList<Long>();
            for (int loop = 0; loop < loopSize; loop++) {
                int position = selectedPositions.keyAt(loop);
                BookmarksModel bookmarks = adapter.getItem(position);
                selectedIds.add(bookmarks.getId());
            }
            showDeleteDialog(DeleteMode.MULTIPLE, selectedIds);
            EventTrackingUtils.sendClick(level2Bookmark, "Delete Bookmarks", XitiUtils.NAVIGATION);
        } else {
            updateViewMode(ListMode.VIEW);
        }
    }

    public void deleteAllItem() {
        showDeleteDialog(DeleteMode.ALL, null);
    }

    private void showDeleteDialog(final DeleteMode mode, final ArrayList<Long> selectedIds) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MudahDialogStyle)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.bookmarks_confirm_dialog_delete_title)
                .setMessage((mode.ALL == mode) ? R.string.confirm_dialog_delete_all_bookmark : R.string.bookmarks_confirm_dialog_delete_message)
                .setNegativeButton(R.string.bookmarks_dialog_button_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                updateViewMode(ListMode.VIEW);
                            }
                        }
                )
                .setPositiveButton(
                        R.string.bookmarks_confirm_dialog_delete_button_yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int which) {
                                dialog.dismiss();
                                int result = 0;
                                EventTrackingUtils.sendClick(level2Bookmark, "Yes delete Bookmarks", XitiUtils.NAVIGATION);
                                switch (mode) {
                                    case SINGLE:
                                        result = bookmarksDAO.deleteBookmarks(selectedIds.get(0));
                                        if (result > 0) {//Delete successfully
                                            adapter.removeItem(selectedPositions.valueAt(0));
                                            Config.bookmarkTotal--;// update this for displaying in Menu
                                        }
                                        break;
                                    case MULTIPLE:
                                        result = bookmarksDAO.deleteMultipleBookmarks(selectedIds);
                                        if (result > 0) {//Delete successfully
                                            adapter.removeMultipleItems(selectedPositions);
                                            Config.bookmarkTotal -= selectedIds.size();// update this for displaying in Menu
                                        }
                                        break;
                                    case ALL:
                                        result = bookmarksDAO.deleteAllBookmarks();
                                        if (result > 0) {//Delete successfully
                                            adapter.removeAllItems();
                                            Config.bookmarkTotal = 0;// update this for displaying in Menu
                                        }
                                        break;
                                }

                                if (result > 0) { //Delete successfully
                                    updateViewMode(ListMode.VIEW);
                                    if (adapter.getCount() == 1)
                                        redrawOptionMenu();//to hide the option menu
                                    Toast.makeText(getActivity(), R.string.bookmarks_delete_success,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );

        AlertDialog alert = builder.show();
        MudahUtil.hideDialogDivider(getActivity(), alert);

    }

    private void redrawOptionMenu() {
        if (Build.VERSION.SDK_INT >= 11) {
            getActivity().invalidateOptionsMenu();
        } else
            getActivity().supportInvalidateOptionsMenu();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d("saving viewMode: " + viewMode);
        savedInstanceState.putString("savedViewMode", viewMode.toString());
    }

    public enum ListMode {VIEW, EDIT}

    private enum DeleteMode {SINGLE, MULTIPLE, ALL}
}