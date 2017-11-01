package se.viltefjall.tekk.ermina.ViewStatus;

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

    private View mMoisture;
    private View mWater;
    private View mProgress;
    private View mSettings;
    private View mReload;

    private ViewStatusActivity mActivity;


    AnimationManager(ViewStatusActivity activity) {
        mActivity = activity;
        mMoisture = activity.findViewById(R.id.moistureView);
        mWater    = activity.findViewById(R.id.waterView);
        mProgress = activity.findViewById(R.id.progress);
        mSettings = activity.findViewById(R.id.settings);
        mReload   = activity.findViewById(R.id.reload);
    }

    void hideConnecting() {
        animConnecting(HIDE);
    }

    void showConnecting() {
        animConnecting(SHOW);
    }

    void hideStatus() {
        Log.d(ID, "hideStatus");
        mSettings.setClickable(false);
        mReload.setClickable(false);
        animStatus(HIDE);
    }

    void showStatus() {
        animStatus(SHOW);
        mSettings.setClickable(true);
        mReload.setClickable(true);
    }

    private void animStatus(final int what) {
        float       prop;
        AnimatorSet animSet;

        if(what == SHOW) {
            prop = 1f;
        } else {
            prop = 0f;
        }

        animSet = new AnimatorSet();
        animSet.playTogether(
                ObjectAnimator.ofFloat(mMoisture, "alpha", prop),
                ObjectAnimator.ofFloat(mWater   , "alpha", prop),
                ObjectAnimator.ofFloat(mSettings, "alpha", prop),
                ObjectAnimator.ofFloat(mReload  , "alpha", prop)
        );

        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if(what == SHOW) {
                    mActivity.setValues();
                } else {
                    mActivity.connect();
                }
            }
        });

        animSet.start();
    }

    private void animConnecting(int what) {
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
        animSet.start();
    }
}
