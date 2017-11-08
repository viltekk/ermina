package se.viltefjall.tekk.ermina.SelectPlant;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;

import se.viltefjall.tekk.ermina.R;

class AnimationManager {
    private static final String ID   = "AnimationManager";
    private static final int    HIDE = 0;
    private static final int    SHOW = 1;

    private View mPlants;
    private View mCustom;
    private View mReload;
    private View mProgress;

    private SelectPlantActivity mActivity;

    AnimationManager(SelectPlantActivity activity) {
        mActivity = activity;
        mPlants   = activity.findViewById(R.id.plants);
        mCustom   = activity.findViewById(R.id.custom);
        mProgress = activity.findViewById(R.id.progress);
        mReload   = activity.findViewById(R.id.reload);
    }

    void hideConnecting() {
        Log.d(ID, "hideConnecting");
        animConnecting(HIDE);
    }

    void showConnecting() {
        Log.d(ID, "showConnecting");
        animConnecting(SHOW);
    }

    void hideList() {
        Log.d(ID, "hideList");
        mReload.setClickable(false);
        animList(HIDE);
    }

    void showList() {
        Log.d(ID, "showList");
        animList(SHOW);
        mReload.setClickable(true);
    }

    private void animList(final int what) {
        float       prop;
        AnimatorSet animSet;

        if(what == SHOW) {
            prop = 1f;
        } else {
            prop = 0f;
        }

        animSet = new AnimatorSet();
        animSet.playTogether(
                ObjectAnimator.ofFloat(mPlants, "alpha", prop),
                ObjectAnimator.ofFloat(mReload, "alpha", prop)
        );
        animSet.start();
    }

    private void animConnecting(final int what) {
        float       prop;
        AnimatorSet animSet;

        if(what == SHOW) {
            prop = 1f;
        } else {
            prop = 0f;
        }
        mProgress.setVisibility(View.VISIBLE);

        animSet = new AnimatorSet();
        animSet.playTogether(
                ObjectAnimator.ofFloat(mProgress, "alpha", prop)
        );

        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if(what == SHOW) {
                    mActivity.loadPlants();
                }
            }
        });

        animSet.start();
    }
}
