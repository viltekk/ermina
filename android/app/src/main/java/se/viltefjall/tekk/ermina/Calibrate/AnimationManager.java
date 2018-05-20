package se.viltefjall.tekk.ermina.CustomPlant;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import se.viltefjall.tekk.ermina.R;

public class AnimationManager {
    private static final String ID = "AnimationManager";

    private static final int SHOW = 0;
    private static final int HIDE = 1;

    private View                mProgress;
    private View                mButton;
    private CustomPlantActivity mActivity;

    AnimationManager(CustomPlantActivity activity) {
        mProgress = activity.findViewById(R.id.progress);
        mButton   = activity.findViewById(R.id.button);
        mActivity = activity;
    }

    void setCustom() {
        AnimatorSet animSet = new AnimatorSet();

        Animator buttonX = ObjectAnimator.ofFloat(mButton  , "scaleX", 0f);
        Animator buttonY = ObjectAnimator.ofFloat(mButton  , "scaleY", 0f);
        Animator buttonA = ObjectAnimator.ofFloat(mButton  , "alpha" , 0f);
        Animator progX   = ObjectAnimator.ofFloat(mProgress, "scaleX", 1f);
        Animator progY   = ObjectAnimator.ofFloat(mProgress, "scaleY", 1f);

        animSet.play(buttonX).with(buttonY).with(buttonA);
        animSet.play(progX).after(buttonY).with(progY);
        animSet.setDuration(200);

        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mActivity.runTasks();
            }
        });

        animSet.start();
    }

    void showButton() {
        AnimatorSet animSet = new AnimatorSet();

        Animator buttonX = ObjectAnimator.ofFloat(mButton  , "scaleX", 1f);
        Animator buttonY = ObjectAnimator.ofFloat(mButton  , "scaleY", 1f);
        Animator buttonA = ObjectAnimator.ofFloat(mButton  , "alpha" , 1f);
        Animator progX   = ObjectAnimator.ofFloat(mProgress, "scaleX", 0f);
        Animator progY   = ObjectAnimator.ofFloat(mProgress, "scaleY", 0f);

        animSet.play(buttonX).with(buttonY).with(buttonA);
        animSet.play(progX).before(buttonY).with(progY);
        animSet.setDuration(200);

        animSet.start();
    }
}
