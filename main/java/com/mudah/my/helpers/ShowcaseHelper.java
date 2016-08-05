package com.mudah.my.helpers;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.mudah.my.R;
import com.mudah.my.activities.MudahBaseActivity;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by w_ongkl on 4/23/15.
 */
public class ShowcaseHelper {
    public static final int LISTING_TUTORIAL_STEPS = 3;
    public static final int ADVIEW_TUTORIAL_STEPS = 1;
    public static final int VERIFIED_TUTORIAL_STEPS = 1;
    public static final int HOMEPAGE_TUTORIAL_STEPS = 6;
    public static final String SKIP_TUTORIAL = "skip_tutorial";
    public static final int NAVIGATION_TUTORIAL_STEPS = 1;
    private Activity hostActivity;
    private ArrayList<TutorialStepAndResource> tutorialStepsAndResources = new ArrayList<TutorialStepAndResource>();
    private TutorialStepAndResource currentTutorial;
    private ShowcaseView svTutorial;
    private OnShowcaseEventListener showcaseEventListener = new OnShowcaseEventListener() {
        @Override
        public void onShowcaseViewHide(ShowcaseView showcaseView) {
            Log.d();
            if (hostActivity != null) {
                increaseTutorialStep();
                saveTutorialState(hostActivity);
                closeAllTutorials(true);
                showTutorial();
            }
        }

        @Override
        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
        }

        @Override
        public void onShowcaseViewShow(ShowcaseView showcaseView) {
        }

        private void increaseTutorialStep() {
            if (currentTutorial != null) {
                currentTutorial.setStep(currentTutorial.getStep() + 1);
                Config.tutorialPagesAndSteps.put(currentTutorial.getTutorialPage().toString(), currentTutorial.getStep());
            }
        }
    };

    public ShowcaseHelper(Activity activity) {
        hostActivity = activity;
    }

    public void saveTutorialState(Activity activity) {
        if (Config.tutorialPagesAndSteps != null && activity != null) {
            String tutorialStepsString = getUriWithQuery(Config.tutorialPagesAndSteps).getEncodedQuery();
            Log.d("######### SAVE tutorialPagesAndSteps:" + tutorialStepsString + ", " + activity);
            PreferencesUtils.getSharedPreferences(activity).edit()
                    .putString(PreferencesUtils.TUTORIAL_PAGES_AND_STEPS, tutorialStepsString)
                    .apply();
        }
    }

    private Uri getUriWithQuery(Map<String, Integer> params) {
        Uri.Builder builder = Uri.parse(Config.shareHost).buildUpon().appendPath("list");
        if (params != null) {
            for (String key : params.keySet()) {
                builder.appendQueryParameter(key, params.get(key) + Constants.EMPTY_STRING);
            }
        }

        return builder.build();
    }

    public void declareSupportedTutorial(TutorialStepAndResource tutorialStepAndResource) {
        tutorialStepsAndResources.add(tutorialStepAndResource);
    }

    public void skipAndClearAllTutorial() {
        tutorialStepsAndResources.clear();
        Config.tutorialPagesAndSteps.put(SKIP_TUTORIAL, 1);
        closeAllTutorials(false);
    }

    public int[] showSkipTutorialOption(TextView skipTutorial) {
        skipTutorial.setVisibility(View.VISIBLE);

        int location[] = new int[2];
        skipTutorial.getLocationOnScreen(location);
        return location;
    }

    public int getRemainingTutorialStep() {
        if (hostActivity != null && tutorialStepsAndResources != null)
            return tutorialStepsAndResources.size();
        else
            return -1;
    }

    public void showTutorial() {
        try {
            Log.d("tutorialStepsAndResources size: " + tutorialStepsAndResources.size());

            if (tutorialStepsAndResources.size() == 0) {
                hideSkipTutorialOption();
            }

            if (tutorialStepsAndResources.size() > 0 && hostActivity != null) {
                TutorialStepAndResource tutorialStepAndResource = tutorialStepsAndResources.get(0);
                currentTutorial = tutorialStepAndResource;
                tutorialSteps(tutorialStepAndResource);
                tutorialStepsAndResources.remove(0);
            } else {
                releaseHostActivity();
            }

        } catch (Exception e) {
            ACUtils.debug(e);
        }
    }

    private void hideSkipTutorialOption() {
        if (hostActivity != null) {
            ((MudahBaseActivity) hostActivity).hideSkipTutorialOption();
        }

    }

    public void releaseHostActivity() {
        //release activity
        hostActivity = null;
    }

    private ShowcaseView createShowCaseBuilder(int resourceID, TutorialStepAndResource tutorialStepAndResource, boolean isHideOnTouchOutside) {
        ShowcaseView.Builder builder = new ShowcaseView.Builder(hostActivity, true, tutorialStepAndResource.isActionBarIcon())//, R.layout.tutorial_search)
                .setTarget(new ViewTarget(resourceID, hostActivity))
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle(tutorialStepAndResource.getTitleResource())
                .setContentText(tutorialStepAndResource.getDescriptionResource());

        if (isHideOnTouchOutside) {
            builder.hideOnTouchOutside();
        }

        return builder.build();
    }

    private void tutorialSteps(TutorialStepAndResource tutorialStepAndResource) {
        int resourceID = tutorialStepAndResource.getResourceID();
        svTutorial = createShowCaseBuilder(resourceID, tutorialStepAndResource, tutorialStepAndResource.isHideOnTouchOutside());
        svTutorial.hideButton();
        svTutorial.setOnShowcaseEventListener(showcaseEventListener);
    }

    public void closeAllTutorials(boolean reset) {
        if (reset) {
            svTutorial = null;
        }
        if (svTutorial != null && svTutorial.isShown()) {
            svTutorial.hide();
            svTutorial = null;
        }
    }

    public class TutorialStepAndResource {
        private Config.TutorialPages tutorialPage = null;
        private View resource = null;
        private Integer resourceID = null;
        private int titleResource;
        private int descriptionResource;
        private int step;
        private boolean actionBarIcon = false;
        private boolean hideOnTouchOutside = false;

        public TutorialStepAndResource(Config.TutorialPages tutorialPage) {
            this.tutorialPage = tutorialPage;
        }

        public TutorialStepAndResource(Config.TutorialPages tutorialPage, View resource) {
            this.tutorialPage = tutorialPage;
            this.resource = resource;
        }

        public TutorialStepAndResource(Config.TutorialPages tutorialPage, int resourceID) {
            this.tutorialPage = tutorialPage;
            this.resourceID = resourceID;
        }

        public TutorialStepAndResource(Config.TutorialPages tutorialPage, int resourceID, boolean isActionBarIcon) {
            this.tutorialPage = tutorialPage;
            this.resourceID = resourceID;
            this.actionBarIcon = isActionBarIcon;
        }

        public boolean isHideOnTouchOutside() {
            return hideOnTouchOutside;
        }

        public void setHideOnTouchOutside(boolean hide) {
            hideOnTouchOutside = hide;
        }

        public boolean isActionBarIcon() {
            return actionBarIcon;
        }

        public int getStep() {
            return step;
        }

        public void setStep(int step) {
            this.step = step;
        }

        public int getDescriptionResource() {
            return descriptionResource;
        }

        public void setDescriptionResource(int descriptionResource) {
            this.descriptionResource = descriptionResource;
        }

        public int getTitleResource() {
            return titleResource;
        }

        public void setTitleResource(int titleResource) {
            this.titleResource = titleResource;
        }

        public Config.TutorialPages getTutorialPage() {
            return tutorialPage;
        }

        public View getResource() {
            return resource;
        }

        public Integer getResourceID() {
            return resourceID;
        }


    }


}
