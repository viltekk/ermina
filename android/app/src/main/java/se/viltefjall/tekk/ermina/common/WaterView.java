package se.viltefjall.tekk.ermina.common;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import se.viltefjall.tekk.ermina.R;

public class WaterView extends View {

    @SuppressWarnings("unused")
    private static final String ID = "MoistureView";

    // for drawing the view
    private Paint mPaintOuterRing;
    private Paint mPaintBackground;
    private Paint mPaintForeground;

    private int   mFromDegree;
    private int   mToDegree;
    private int   mContentWidth;
    private int   mContentHeight;
    private int   mGaugePenSize;
    private int   mRingPenSize;
    private int   mOuterRadius;
    private int   mInnerRadius;
    private RectF mRect;

    // water values
    private float mRangeAngleSweep;
    private float mWaterLvl;

    // animation
    long          mAnimDuration;
    ValueAnimator mAnimator;

    public WaterView(Context context) {
        super(context);
        init(null, 0);
    }

    public WaterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public WaterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.WaterView, defStyle, 0);

        mRect         = new RectF();
        mWaterLvl     = 0;
        mAnimDuration = 1000;

        mGaugePenSize = a.getInt(R.styleable.WaterView_gaugePenSize,  50);
        mRingPenSize  = a.getInt(R.styleable.WaterView_ringPenSize ,  50);
        mFromDegree   = a.getInt(R.styleable.WaterView_fromDegree  ,   0);
        mToDegree     = a.getInt(R.styleable.WaterView_toDegree    , 360);

        mPaintForeground = new Paint();
        mPaintForeground.setStrokeWidth(mGaugePenSize);
        mPaintForeground.setStyle(Paint.Style.STROKE);
        mPaintForeground.setStrokeCap(Paint.Cap.ROUND);
        mPaintForeground.setColor(
                a.getColor(
                        R.styleable.WaterView_foregroundColor,
                        ContextCompat.getColor(getContext(), R.color.colorPrimary)
                )
        );

        mPaintBackground = new Paint();
        mPaintBackground.setStrokeWidth(mGaugePenSize);
        mPaintBackground.setStyle(Paint.Style.STROKE);
        mPaintBackground.setStrokeCap(Paint.Cap.ROUND);
        mPaintBackground.setColor(
                a.getColor(
                        R.styleable.WaterView_backgroundColor,
                        Color.GRAY
                )
        );

        mPaintOuterRing = new Paint();
        mPaintOuterRing.setStrokeWidth(mRingPenSize);
        mPaintOuterRing.setStyle(Paint.Style.STROKE);
        mPaintOuterRing.setColor(
            a.getColor(
                    R.styleable.WaterView_ringColor,
                    ContextCompat.getColor(getContext(), R.color.colorAccent)
            )
        );

        a.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        // set canvas size
        mContentWidth  = w - getPaddingLeft() - getPaddingRight();
        mContentHeight = h - getPaddingTop() - getPaddingBottom();

        if(mContentWidth > mContentHeight) {
            mOuterRadius = mContentHeight / 2 - mRingPenSize;
        } else {
            mOuterRadius = mContentWidth / 2 - mRingPenSize;
        }

        int d = mOuterRadius - mRingPenSize*2 - mGaugePenSize;
        int t = mContentHeight/2 - d;
        int l = mContentWidth /2 - d;
        int r = mContentWidth /2 + d;
        int b = mContentHeight/2 + d;
        mRect.set(l, t, r, b);

        if(mRect.width() > mRect.height()) {
            mInnerRadius = (int) (mRect.height() + mGaugePenSize) / 2;
        } else {
            mInnerRadius = (int) (mRect.width()  + mGaugePenSize) / 2;
        }
    }

    public void setWater(int lvl) {
        if(mAnimator != null) {
            mAnimator.cancel();
        }

        long duration = (long)(mAnimDuration * ((float)Math.abs(mWaterLvl-lvl))/100f);
        mAnimator = ValueAnimator.ofFloat(mWaterLvl, lvl);
        mAnimator.setDuration(duration);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float v = (float) valueAnimator.getAnimatedValue();
                createWater(v);
                invalidate();
            }
        });
        mAnimator.start();
        mWaterLvl = lvl;
    }

    private void createWater(float v) {
        mRangeAngleSweep = v * ((float)(mToDegree-mFromDegree)/100f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(
                mContentWidth/2,
                mContentHeight/2,
                mOuterRadius,
                mPaintOuterRing
        );

        canvas.drawArc(
                mRect,
                mFromDegree,
                mToDegree-mFromDegree,
                false,
                mPaintBackground
        );


        canvas.drawArc(
                mRect,
                mFromDegree,
                mRangeAngleSweep,
                false,
                mPaintForeground
        );
    }
}
