package se.viltefjall.tekk.ermina.ViewStatus;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.view.View;

import se.viltefjall.tekk.ermina.R;

public class AnimationManager {

    private View mMoisture;
    private View mWater;
    private View mProgress;

    public AnimationManager(Activity activity) {
        mMoisture = activity.findViewById(R.id.moistureView);
        mWater    = activity.findViewById(R.id.waterView);
        mProgress = activity.findViewById(R.id.progress);
    }

    void hideConnecting() {
        animConnecting(0f);
    }

    void showConnecting() {
        animConnecting(1f);
    }

    void hideStatus() {
        animStatus(0f);
    }

    void showStatus() {
        animStatus(1f);
    }

    private void animStatus(float prop) {
        AnimatorSet    animSet;

        mMoisture.setVisibility(View.VISIBLE);
        mWater.setVisibility(View.VISIBLE);

        animSet = new AnimatorSet();
        animSet.playTogether(
                ObjectAnimator.ofFloat(mMoisture, "alpha", prop),
                ObjectAnimator.ofFloat(mWater, "alpha", prop)
        );
        animSet.start();
    }

    private void animConnecting(float prop) {
        AnimatorSet animSet;

        mProgress.setVisibility(View.VISIBLE);

        animSet = new AnimatorSet();
        animSet.playTogether(
                ObjectAnimator.ofFloat(mProgress, "alpha", prop)
        );
        animSet.start();
    }
}
