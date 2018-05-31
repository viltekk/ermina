package se.viltefjall.tekk.ermina.Calibrate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.TextView;

import se.viltefjall.tekk.ermina.R;

public class AnimationManager {
    private static final String ID = "AnimationManager";

    private static final int SHOW = 0;
    private static final int HIDE = 1;

    private View              mButton;
    private View              mInfo;
    private View              mStep1;
    private View              mStep2;
    private View              mStep3;
    private CalibrateActivity mActivity;

    AnimationManager(CalibrateActivity activity) {
        mButton   = activity.findViewById(R.id.next);
        mInfo     = activity.findViewById(R.id.info);
        mStep1    = activity.findViewById(R.id.step1);
        mStep2    = activity.findViewById(R.id.step2);
        mStep3    = activity.findViewById(R.id.step3);
        mActivity = activity;
    }

    void step0() {
        AnimatorSet animSet = new AnimatorSet();

        Animator s1ScaleX = ObjectAnimator.ofFloat(mStep1, "scaleX", 0.5f);
        Animator s1ScaleY = ObjectAnimator.ofFloat(mStep1, "scaleY", 0.5f);

        Animator s2ScaleX = ObjectAnimator.ofFloat(mStep2, "scaleX", 1f);
        Animator s2ScaleY = ObjectAnimator.ofFloat(mStep2, "scaleY", 1f);

        Animator infoA0   = ObjectAnimator.ofFloat(mInfo, "alpha", 0f);
        Animator infoA1   = ObjectAnimator.ofFloat(mInfo, "alpha", 1f);

        animSet.play(s1ScaleX).with(s1ScaleY);
        animSet.play(s2ScaleX).with(s2ScaleY);
        animSet.play(s1ScaleX).with(s2ScaleX);
        animSet.play(infoA1).after(infoA0);
        animSet.play(infoA0).with(s1ScaleX);
        animSet.setDuration(200);

        infoA0.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                TextView tv = (TextView)mInfo;
                tv.setText(R.string.calibDry);
            }
        });

        animSet.start();
    }

    void step1() {
        mButton.setClickable(false);

        AnimatorSet animSet = new AnimatorSet();

        Animator btnScaleX = ObjectAnimator.ofFloat(mButton, "scaleX", 0f);
        Animator btnScaleY = ObjectAnimator.ofFloat(mButton, "scaleY", 0f);

        Animator infoA0    = ObjectAnimator.ofFloat(mInfo, "alpha", 0f);
        Animator infoA1    = ObjectAnimator.ofFloat(mInfo, "alpha", 1f);

        animSet.play(btnScaleX).with(btnScaleY);
        animSet.play(infoA1).after(infoA0);
        animSet.play(infoA0).with(btnScaleX);
        animSet.setDuration(200);

        infoA0.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                TextView tv = (TextView)mInfo;
                tv.setText(R.string.calibWait);
            }
        });

        animSet.start();
    }

    void step1Done() {
        AnimatorSet animSet = new AnimatorSet();

        Animator s2ScaleX = ObjectAnimator.ofFloat(mStep2, "scaleX", 0.5f);
        Animator s2ScaleY = ObjectAnimator.ofFloat(mStep2, "scaleY", 0.5f);

        Animator s3ScaleX = ObjectAnimator.ofFloat(mStep3, "scaleX", 1f);
        Animator s3ScaleY = ObjectAnimator.ofFloat(mStep3, "scaleY", 1f);

        Animator btnScaleX = ObjectAnimator.ofFloat(mButton, "scaleX", 1f);
        Animator btnScaleY = ObjectAnimator.ofFloat(mButton, "scaleY", 1f);

        Animator infoA0    = ObjectAnimator.ofFloat(mInfo, "alpha", 0f);
        Animator infoA1    = ObjectAnimator.ofFloat(mInfo, "alpha", 1f);

        animSet.play(s2ScaleX).with(s2ScaleY);
        animSet.play(s3ScaleX).with(s3ScaleY);
        animSet.play(btnScaleX).with(btnScaleY);
        animSet.play(infoA1).after(infoA0);

        animSet.play(infoA0).with(btnScaleX);
        animSet.play(infoA0).with(s2ScaleX);
        animSet.setDuration(200);

        infoA0.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                TextView tv = (TextView)mInfo;
                tv.setText(R.string.calibWet);
            }
        });

        btnScaleX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mButton.setClickable(true);
            }
        });

        animSet.start();
    }

    void step2() {
        mButton.setClickable(false);

        AnimatorSet animSet = new AnimatorSet();

        Animator btnScaleX = ObjectAnimator.ofFloat(mButton, "scaleX", 0f);
        Animator btnScaleY = ObjectAnimator.ofFloat(mButton, "scaleY", 0f);

        Animator infoA0    = ObjectAnimator.ofFloat(mInfo, "alpha", 0f);
        Animator infoA1    = ObjectAnimator.ofFloat(mInfo, "alpha", 1f);

        animSet.play(btnScaleX).with(btnScaleY);
        animSet.play(infoA1).after(infoA0);
        animSet.play(infoA0).with(btnScaleX);
        animSet.setDuration(200);

        infoA0.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                TextView tv = (TextView)mInfo;
                tv.setText(R.string.calibWait);
            }
        });

        animSet.start();
    }
}
