package com.mudah.my.helpers;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.utils.MudahUtil;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by w_ongkl on 12/5/14.
 * For creating animation using codes
 */
public class AnimationHelper {

    public static final int EXPAND = 1;
    public static final int COLLAPSE = 2;
    public static final int ACTIVITY_TRANSITION_OPEN = 3;
    public static final int ACTIVITY_TRANSITION_CLOSE = 4;
    public static final int ENTER_FLOATING_ACTION_BTN = 5;
    public static final int SHOW_FLOATING_ACTION_BTN = 6;
    public static final int HIDE_FLOATING_ACTION_BTN = 7;
    public static final int ENTER_BOUNCE = 8;
    public static final int EXIT_BOUNCE = 9;
    public static final int SHOW_FLOATING_ACTIONBAR = 10;
    public static final int HIDE_FLOATING_ACTIONBAR = 11;

    private final int delayShowHideFloatingActionBtnAnimation = 500;
    private final int delayEnterFloatingActionBtnAnimation = 2000;
    private final int delayBounceAnimation = 200;

    private int mode = 0;
    private Activity activityContext;
    private ViewGroup viewGroup;
    private View view;

    public AnimationHelper(final ViewGroup viewGroup, int mode) {
        this.mode = mode;
        this.viewGroup = viewGroup;
    }

    public AnimationHelper(final View view, int mode) {
        this.mode = mode;
        this.view = view;
    }

    public AnimationHelper(final Activity activityContext, int mode) {
        this.mode = mode;
        this.activityContext = activityContext;
    }

    public void run() {
        switch (mode) {
            case EXPAND:
                expand(viewGroup);
                break;
            case COLLAPSE:
                collapse(viewGroup);
                break;
            case ACTIVITY_TRANSITION_OPEN:
                activityTransitionOpen();
                break;
            case ACTIVITY_TRANSITION_CLOSE:
                activityTransitionClose();
                break;
            case ENTER_FLOATING_ACTION_BTN:
                enterFloatingActionButton(view);
                break;
            case SHOW_FLOATING_ACTION_BTN:
                showFloatingActionButton(view);
                break;
            case HIDE_FLOATING_ACTION_BTN:
                hideFloatingActionButton(view);
                break;
            case ENTER_BOUNCE:
                enterBounce(view);
                break;
            case EXIT_BOUNCE:
                exitBounce(view);
                break;
            case SHOW_FLOATING_ACTIONBAR:
                showFloatingActionBar(view);
                break;
            case HIDE_FLOATING_ACTIONBAR:
                hideFloatingActionBar(view);
                break;
            default:
                break;
        }
    }

    private void expand(final ViewGroup viewGroup) {
        //set Visible
        viewGroup.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= 11) {
            final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            viewGroup.measure(widthSpec, heightSpec);
            ValueAnimator mAnimator = slideAnimator(viewGroup, 0, viewGroup.getMeasuredHeight());
            mAnimator.start();
        }
    }

    private void collapse(final ViewGroup viewGroup) {

        if (Build.VERSION.SDK_INT >= 11) {
            int finalHeight = viewGroup.getHeight();
            ValueAnimator mAnimator = slideAnimator(viewGroup, finalHeight, 0);
            mAnimator.start();
        } else {
            viewGroup.setVisibility(View.GONE);
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private ValueAnimator slideAnimator(final ViewGroup viewGroup, int start, int end) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = viewGroup.getLayoutParams();
                layoutParams.height = value;
                viewGroup.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    private void activityTransitionOpen() {

        if (Build.VERSION.SDK_INT >= 11) {
            activityContext.overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
        }
    }

    private void activityTransitionClose() {

        if (Build.VERSION.SDK_INT >= 11) {
            activityContext.overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
        }
    }

    private void showFloatingActionButton(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "translationY", view.getHeight() + MudahUtil.dpToPx(10, view.getContext()), 0)
        );
        set.setDuration(delayShowHideFloatingActionBtnAnimation).start();
    }

    private void hideFloatingActionButton(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "translationY", 0, view.getHeight() + MudahUtil.dpToPx(10, view.getContext()))
        );
        set.setDuration(delayShowHideFloatingActionBtnAnimation).start();
    }

    private void enterFloatingActionButton(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 0, 1.3f, 1),
                ObjectAnimator.ofFloat(view, "scaleY", 0, 1.3f, 1)
        );
        set.setDuration(delayEnterFloatingActionBtnAnimation).start();
    }

    private void enterBounce(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1, 1.3f),
                ObjectAnimator.ofFloat(view, "scaleY", 1, 1.3f)
        );
        set.setDuration(delayBounceAnimation).start();
    }

    private void exitBounce(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1.3f, 1),
                ObjectAnimator.ofFloat(view, "scaleY", 1.3f, 1)
        );
        set.setDuration(delayBounceAnimation).start();
    }

    private void showFloatingActionBar(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "translationY", -view.getHeight(), 0)
        );
        set.setDuration(delayShowHideFloatingActionBtnAnimation).start();
    }

    private void hideFloatingActionBar(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "translationY", 0, -view.getHeight())
        );
        set.setDuration(delayShowHideFloatingActionBtnAnimation).start();
    }

}
