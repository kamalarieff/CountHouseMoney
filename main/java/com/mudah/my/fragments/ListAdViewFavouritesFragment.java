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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.activities.AdViewActivity;
import com.mudah.my.activities.ListAdViewFavouritesActivity;
import com.mudah.my.adapters.ListAdModelAdapter;
import com.mudah.my.configs.Config;
import com.mudah.my.dao.AdViewFavouritesDAO;
import com.mudah.my.models.ListAdModel;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahUtil;

import java.util.ArrayList;

public class ListAdViewFavouritesFragment extends ListFragment {

    private ListMode viewMode = ListMode.VIEW;
    private ListAdModelAdapter adapter = new ListAdModelAdapter();
    private AdViewFavouritesDAO adViewFavouritesDAO;
    private boolean itemsCanClick = true;
    private TextView tvTitle;
    private TextView tvInstruction;
    private SparseArray<Integer> selectedPositions = new SparseArray<Integer>();//SparseArray Better performance than Hashmap
    private String level2Fav;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        level2Fav = XitiUtils.LEVEL2_FAVOURITE_ID;
        if (savedInstanceState != null) {
            viewMode = ListMode.valueOf(savedInstanceState.getString("savedViewMode"));
        } else
            viewMode = ListMode.VIEW;
    }

    public ListMode getViewMode() {
        return viewMode;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout contentLayout = (LinearLayout) inflater.inflate(R.layout.common_listview, null);
        tvTitle = (TextView) contentLayout.findViewById(R.id.result_title);
        tvTitle.setText(getActivity().getString(R.string.list_my_favourites));

        tvInstruction = (TextView) contentLayout.findViewById(R.id.tv_instruction);
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
            inflater.inflate(R.menu.ad_view_favourites, menu);
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
                    EventTrackingUtils.sendClick(level2Fav, "Multiple delete Favourites", XitiUtils.NAVIGATION);
                }
                return true;
            case R.id.menu_delete_all:
                selectedPositions.clear();
                deleteAllItem();
                EventTrackingUtils.sendClick(level2Fav, "Delete all Favourites", XitiUtils.NAVIGATION);
                return true;
        }
        return false;
    }

    private void updateActionButtonText() {
        if (getActivity() instanceof ListAdViewFavouritesActivity)
            ((ListAdViewFavouritesActivity) getActivity()).updateActionButtonText(viewMode);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adViewFavouritesDAO = new AdViewFavouritesDAO(getActivity());
            ArrayList<ListAdModel> arrayList = adViewFavouritesDAO.getAllFavourites();
            updateSelectedStatus(arrayList);
            adapter.setItems(arrayList);

            ListView lv = getListView();
            lv.setDivider(null);
            //lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            setListAdapter(adapter);

            updateTitle();
            redrawOptionMenu();
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
                    deleteSingleItem(position);
                    return true;
                }
            });
        }
    }

    private void updateSelectedStatus(ArrayList<ListAdModel> arrayList) {
        if (selectedPositions.size() > 0 && arrayList != null) {
            int loopSize = selectedPositions.size();
            for (int loop = 0; loop < loopSize; loop++) {
                int position = selectedPositions.keyAt(loop);
                if (position < arrayList.size()) {
                    ListAdModel fav = arrayList.get(position);
                    fav.setSelected(true);
                }
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getString(R.string.empty_favourites));
        if (!itemsCanClick) {
            getListView().setEnabled(false);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (itemsCanClick == false)
            return;
        ListAdModel item = adapter.getItem(position);

        if (ListMode.VIEW == viewMode) { // View Mode
            Intent intent = new Intent(getActivity(), AdViewActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            Log.i("Action", "View Ad Detail Bookmark");

            intent.putExtra(AdViewActivity.EXTRA_LIST_ITEM_POSITION, position);
            intent.putExtra(AdViewActivity.EXTRA_CATEGORY_ID, item.getAdCategoryId());
            intent.putExtra(AdViewActivity.EXTRA_GRAND_TOTAL, adapter.getCount());
            intent.putStringArrayListExtra(AdViewActivity.EXTRA_ALL_LIST_ID, adapter.getItemIds());
            startActivity(intent);
        } else {
            if (item.isSelected()) {
                selectedPositions.remove(position);
                item.setSelected(false);
                removeViewBackground(v);
            } else {
                selectedPositions.put(position, position);
                item.setSelected(true);
                setSelectedViewBackground(v);
            }
            updateViewMode(ListMode.EDIT);
        }
    }

    private void removeViewBackground(View v) {
        if (Build.VERSION.SDK_INT < 19) {
            v.setBackgroundColor(getResources().getColor(R.color.white));
        } else
            v.setBackgroundResource(0);
    }

    private void setSelectedViewBackground(View v) {
        if (Build.VERSION.SDK_INT < 19) {
            v.setBackgroundColor(getResources().getColor(R.color.selected_box_background));
        } else
            v.setBackground(getResources().getDrawable(R.drawable.bg_selected_row));
    }

    private void updateTitle() {
        tvTitle.setText(String.format("%s (%d/%d)", getString(R.string.list_my_favourites),
                getListAdapter().getCount(), Config.maxAdviewFavTotal));
    }

    public void updateViewMode(ListMode mode) {
        viewMode = mode;
        adapter.setListMode(mode);
        if (ListMode.VIEW == mode) {
            updateTitle();
            tvInstruction.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();//reset selected row
        } else {
            updateSelectedTitle();
            tvInstruction.setVisibility(View.VISIBLE);
        }
        updateActionButtonText();

    }

    private void updateSelectedTitle() {
        tvTitle.setText(String.format("%d %s", selectedPositions.size(), getString(R.string.selected_favourites)));
    }

    public void deleteSingleItem(final int position) {
        ListAdModel adViewFavourite = adapter.getItem(position);
        selectedPositions.clear();
        selectedPositions.put(position, position);
        if (adViewFavourite != null) {
            ArrayList<String> selectedIds = new ArrayList<String>();
            selectedIds.add(adViewFavourite.getAdId());
            showDeleteDialog(DeleteMode.SINGLE, selectedIds);
            EventTrackingUtils.sendClick(level2Fav, "Single delete Favourites", XitiUtils.NAVIGATION);
        }
    }

    public void deleteSelectedItem() {
        if (selectedPositions.size() > 0) {
            int loopSize = selectedPositions.size();
            ArrayList<String> selectedIds = new ArrayList<String>();
            for (int loop = 0; loop < loopSize; loop++) {
                int position = selectedPositions.keyAt(loop);
                ListAdModel adViewFavourite = adapter.getItem(position);
                selectedIds.add(adViewFavourite.getAdId());
            }
            showDeleteDialog(DeleteMode.MULTIPLE, selectedIds);
            EventTrackingUtils.sendClick(level2Fav, "Delete Favourites", XitiUtils.NAVIGATION);//tag click on the button
        } else {
            updateViewMode(ListMode.VIEW);
        }
    }

    public void deleteAllItem() {
        showDeleteDialog(DeleteMode.ALL, null);
    }

    private void showDeleteDialog(final DeleteMode mode, final ArrayList<String> selectedIds) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MudahDialogStyle)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.cancel_favourite)
                .setMessage((mode.ALL == mode) ? R.string.confirm_dialog_delete_all_favourite : R.string.confirm_dialog_delete_favourite)
                .setNegativeButton(R.string.favourite_dialog_button_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                updateViewMode(ListMode.VIEW);
                                adapter.notifyDataSetChanged();//remove all selected items background
                            }
                        }
                )
                .setPositiveButton(
                        R.string.favourite_confirm_dialog_delete_button_yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int which) {
                                dialog.dismiss();
                                int result = 0;
                                EventTrackingUtils.sendClick(level2Fav, "Yes delete Favourites", XitiUtils.NAVIGATION);
                                switch (mode) {
                                    case SINGLE:
                                        result = adViewFavouritesDAO.deleteFavourite(Long.parseLong(selectedIds.get(0)));
                                        if (result > 0) { //Delete successfully
                                            adapter.removeItem(selectedPositions.valueAt(0));
                                            Config.allFavouritAdIds.delete(Integer.parseInt(selectedIds.get(0)));
                                        }
                                        break;
                                    case MULTIPLE:
                                        result = adViewFavouritesDAO.deleteMultipleFavourites(selectedIds);
                                        if (result > 0) {//Delete successfully
                                            adapter.removeMultipleItems(selectedPositions);
                                            for (String selectedId : selectedIds) {
                                                Config.allFavouritAdIds.delete(Integer.parseInt(selectedId));
                                            }
                                        }
                                        break;
                                    case ALL:
                                        result = adViewFavouritesDAO.deleteAllFavourites();
                                        if (result > 0) {//Delete successfully
                                            adapter.removeAllItems();
                                            Config.allFavouritAdIds.clear();
                                        }
                                        break;
                                }

                                if (result > 0) { //Delete successfully
                                    updateViewMode(ListMode.VIEW);
                                    if (adapter.getCount() == 0)
                                        redrawOptionMenu();//to hide the option menu
                                    Toast.makeText(getActivity(), R.string.favourite_delete_success,
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
        savedInstanceState.putString("savedViewMode", viewMode.toString());
    }

    public enum ListMode {VIEW, EDIT}

    private enum DeleteMode {SINGLE, MULTIPLE, ALL}

}