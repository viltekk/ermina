package se.viltefjall.tekk.ermina.ViewStatus;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import se.viltefjall.tekk.ermina.R;

class AnimationManager {

    @SuppressWarnings("unused")
    private static final String ID = "AnimationManager";

    static final int HIDE = 0;
    static final int SHOW = 1;

    private View               mReload;
    private RotateAnimation    mRot;

    AnimationManager(ViewStatusActivity activity) {
        mReload = activity.findViewById(R.id.reload);
        mRot = new RotateAnimation(
                0,
                360,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        );
        mRot.setDuration(500);
    }

    void animReload(int what) {
        if(what == SHOW) {
            mReload.setClickable(false);
            mRot.setRepeatCount(Animation.INFINITE);
            mReload.startAnimation(mRot);
        } else {
            mRot.setRepeatCount(0);
            mReload.startAnimation(mRot);
            mReload.setClickable(true);
        }
    }
}
