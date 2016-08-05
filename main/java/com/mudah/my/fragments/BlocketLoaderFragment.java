package com.mudah.my.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.mudah.my.R;
import com.mudah.my.loaders.BlocketLoader;
import com.mudah.my.loaders.BlocketLoader.OnLoadCompleteListener;
import com.mudah.my.loaders.Method;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Extends ListFragment so that ListFragments that calls the API can extend from this.
 */
public class BlocketLoaderFragment extends Fragment {
    private static final boolean ANIMATE = true;
    protected final int LIST_TYPE_LISTVIEW = 0;
    protected String resource;
    protected Map<String, Object> params = new HashMap<>();
    protected Method method;
    protected boolean isLoading = false;
    protected int listType = LIST_TYPE_LISTVIEW;
    protected View vLoading;
    protected View vEmptyResult;
    private OnLoadCompleteListener onLoadCompleteListener;
    private View vConnectionLost;
    private RecyclerView vList;
    private boolean connectionLostShown = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        try {
            listType = PreferencesUtils.getSharedPreferences(getActivity().getApplicationContext()).getInt(PreferencesUtils.IMAGE_MODE, 0);
        } catch (ClassCastException e) {
            listType = 0;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (vConnectionLost != null){
            ACUtils.unbindDrawables(vConnectionLost);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FrameLayout root = (FrameLayout) inflater.inflate(R.layout.blocket_loader, container, false);
        vConnectionLost = root.findViewById(R.id.v_connection_lost);
        ImageView connectionLostImg = (ImageView) root.findViewById(R.id.imgv_connection_lost);
        Picasso.with(root.getContext()).load(R.drawable.loader_connection_lost).fit().centerInside().into(connectionLostImg);

        ImageView emptyResultImg = (ImageView) root.findViewById(R.id.img_empty_result);
        Picasso.with(root.getContext()).load(R.drawable.img_empty_result).fit().centerInside().into(emptyResultImg);

        vConnectionLost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setConnectionLostShown(false);
                onConnectionRetry();
            }
        });

        vEmptyResult = root.findViewById(R.id.v_empty_result);
        vEmptyResult.setVisibility(View.GONE);

        setConnectionLostShown(connectionLostShown, false);
        vLoading = root.findViewById(android.R.id.progress);
        vLoading.setVisibility(View.GONE);

        return root;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean isSetLoading) {
        isLoading = isSetLoading;
    }

    public void restartLoader() {
        restartLoader(true);
    }

    public void setOnLoadCompleteListener(OnLoadCompleteListener listener) {
        onLoadCompleteListener = listener;
    }

    public void setApi(Method method, String resource, Map<String, Object> params) {
        this.method = method;
        this.resource = resource;
        this.params = params;
    }

    protected RecyclerView getListView() {
        return vList;
    }

    protected void setListView(RecyclerView newList) {
        vList = newList;
    }

    public void setConnectionLostShown(boolean show) {
        if (connectionLostShown == show) return;
        setConnectionLostShown(show, true);
    }

    public void restartLoader(boolean hideList) {
        //check first to prevent "content view not yet created" error
        if (isResumed())
            setListShown(!hideList);
        setConnectionLostShown(false);
        setViewEmptyResultShown(false);
        isLoading = true;
        //Check if fragment is added to Activity before restarting
        if (isAdded() && getActivity() != null) {
            getActivity().getSupportLoaderManager().restartLoader(getLoaderId(this), null, newBlocketLoaderCallbacks());
        }
    }

    protected void onConnectionRetry() {
        restartLoader();
    }

    protected void onLoadComplete(BlocketLoader loader, JSONObject data) throws LoadException {
        //isLoading to be assigned in AdsFragment after finishing notifyDataSetChange
        //isLoading = false;
        //check first to prevent "content view not yet created" error
        if (isResumed())
            setListShown(true);
        setConnectionLostShown(false);
        setViewEmptyResultShown(false);
        if (onLoadCompleteListener != null) {
            onLoadCompleteListener.onLoadComplete(loader, data);

        }
    }

    public void setViewEmptyResultShown(boolean show) {
        if ((vEmptyResult.getVisibility() == View.VISIBLE) == show) return;
        if (show) {
            switchView(vEmptyResult, vList);
        } else {
            switchView(vList, vEmptyResult);
        }
    }

    public void switchView(View viewWillBeShowed, View viewWillBeHided) {
        if (ANIMATE && isVisible()) {
            viewWillBeHided.startAnimation(AnimationUtils.loadAnimation(
                    getActivity().getApplicationContext(), android.R.anim.fade_out));
            viewWillBeShowed.startAnimation(AnimationUtils.loadAnimation(
                    getActivity().getApplicationContext(), android.R.anim.fade_in));
        } else {
            viewWillBeShowed.clearAnimation();
            viewWillBeHided.clearAnimation();
        }
        viewWillBeHided.setVisibility(View.GONE);
        viewWillBeShowed.setVisibility(View.VISIBLE);
    }

    protected void setListShown(boolean isShow) {
        vList.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    protected void onLoadError(BlocketLoader loader, JSONObject data) {
        isLoading = false;
        vLoading.setVisibility(View.GONE);
        if (loader.isAbandoned() == false) {
            //check first to prevent "content view not yet created" error
            if (isResumed())
                setListShown(false);
            setConnectionLostShown(true);
            setViewEmptyResultShown(false);
        }

        if (onLoadCompleteListener != null) {
            onLoadCompleteListener.onLoadError(loader, data);
        }

        // destroy after onLoadCompleteListener.onLoadError(), since destroying a loader resets its isAbandoned() flag
        getLoaderManager().destroyLoader(loader.getId());
    }

    private void setConnectionLostShown(boolean show, boolean animate) {
        Log.d("show=" + show);
        connectionLostShown = show;

        if (show == true) {
            if (ANIMATE && isVisible()) {
                vList.startAnimation(AnimationUtils.loadAnimation(
                        getActivity().getApplicationContext(), android.R.anim.fade_out));
                vConnectionLost.startAnimation(AnimationUtils.loadAnimation(
                        getActivity().getApplicationContext(), android.R.anim.fade_in));
            } else {
                vList.clearAnimation();
                vConnectionLost.clearAnimation();
            }
            vList.setVisibility(View.GONE);
            vConnectionLost.setVisibility(View.VISIBLE);
        } else {
            if (ANIMATE && isVisible()) {
                vList.startAnimation(AnimationUtils.loadAnimation(
                        getActivity().getApplicationContext(), android.R.anim.fade_in));
                vConnectionLost.startAnimation(AnimationUtils.loadAnimation(
                        getActivity().getApplicationContext(), android.R.anim.fade_out));
            } else {
                vList.clearAnimation();
                vConnectionLost.clearAnimation();
            }
            vList.setVisibility(View.VISIBLE);
            vConnectionLost.setVisibility(View.GONE);
        }
    }

    private LoaderManager.LoaderCallbacks<JSONObject> newBlocketLoaderCallbacks() {
        return new BlocketLoader.Callbacks(method, resource, params, getActivity()) {
            @Override
            public void onLoadComplete(BlocketLoader loader, JSONObject data) {
                try {
                    BlocketLoaderFragment.this.onLoadComplete(loader, data);
                } catch (LoadException e) {
                    onLoadError(loader, data);
                }
            }

            @Override
            public void onLoadError(BlocketLoader loader, JSONObject data) {
                BlocketLoaderFragment.this.onLoadError(loader, data);
            }
        };
    }

    private int getLoaderId(Fragment fragment) {
        if (fragment.getTag() != null) {
            return fragment.getTag().hashCode();
        } else {
            return fragment.getId();
        }
    }

    protected static class LoadException extends Exception {
        private static final long serialVersionUID = 1387885230674601166L;

        public LoadException(Throwable e) {
            super(e);
        }
    }

}
