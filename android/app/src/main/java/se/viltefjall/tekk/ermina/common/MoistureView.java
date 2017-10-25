package se.viltefjall.tekk.ermina.common;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import se.viltefjall.tekk.ermina.R;

/**
 * TODO: document your custom view class.
 */
public class MoistureView extends View {

    @SuppressWarnings("unused")
    private static final String ID = "MoistureView";

    // for drawing the view
    private Paint mPaintOuterRing;
    private Paint mPaintOutsideLimit;
    private Paint mPaintInsideLimit;
    private Paint mPaintArrow;

    private int   mFromDegree;
    private int   mToDegree;
    private int   mContentWidth;
    private int   mContentHeight;
    private int   mGaugePenSize;
    private int   mRingPenSize;
    private int   mOuterRadius;
    private int   mInnerRadius;
    private RectF mRect;

    private Point mPtr0;
    private Point mPtr1;
    private Point mPtr2;
    private Point mPtr3;
    private Path  mPathArrow;
    private int   mAngle;

    // moisture values
    private float mMin;
    private float mMax;
    private float mCur;
    private float mRangeAngleMin;
    private float mRangeAngleSweep;

    // animation
    long          mAnimDuration;
    ValueAnimator mRangeAnimator;
    ValueAnimator mArrowAnimator;

    public MoistureView(Context context) {
        super(context);
        init(null, 0);
    }

    public MoistureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MoistureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.MoistureView, defStyle, 0);

        mRect = new RectF();
        mPtr0 = new Point();
        mPtr1 = new Point();
        mPtr2 = new Point();
        mPtr3 = new Point();

        mAnimDuration = 1000;
        mCur = 0;

        mGaugePenSize = a.getInt(R.styleable.MoistureView_gaugePenSize,  50);
        mRingPenSize  = a.getInt(R.styleable.MoistureView_ringPenSize ,  50);
        mFromDegree   = a.getInt(R.styleable.MoistureView_fromDegree  ,   0);
        mToDegree     = a.getInt(R.styleable.MoistureView_toDegree    , 360);

        mPaintInsideLimit = new Paint();
        mPaintInsideLimit.setStrokeWidth(mGaugePenSize);
        mPaintInsideLimit.setStyle(Paint.Style.STROKE);
        mPaintInsideLimit.setStrokeCap(Paint.Cap.ROUND);
        mPaintInsideLimit.setColor(
                a.getColor(
                        R.styleable.MoistureView_limitColor,
                        ContextCompat.getColor(getContext(), R.color.colorPrimary)
                )
        );

        mPaintOutsideLimit = new Paint();
        mPaintOutsideLimit.setStrokeWidth(mGaugePenSize);
        mPaintOutsideLimit.setStyle(Paint.Style.STROKE);
        mPaintOutsideLimit.setStrokeCap(Paint.Cap.ROUND);
        mPaintOutsideLimit.setColor(
                a.getColor(
                        R.styleable.MoistureView_outsideLimitColor,
                        Color.GRAY
                )
        );

        mPaintOuterRing = new Paint();
        mPaintOuterRing.setStrokeWidth(mRingPenSize);
        mPaintOuterRing.setStyle(Paint.Style.STROKE);
        mPaintOuterRing.setColor(
            a.getColor(
                    R.styleable.MoistureView_ringColor,
                    ContextCompat.getColor(getContext(), R.color.colorAccent)
            )
        );

        mPathArrow  = new Path();
        mPaintArrow = new Paint();
        mPaintArrow.setStrokeWidth(mGaugePenSize/4);
        mPaintArrow.setStyle(Paint.Style.STROKE);
        mPaintArrow.setStrokeCap(Paint.Cap.ROUND);
        mPaintArrow.setStrokeJoin(Paint.Join.ROUND);
        mPaintArrow.setColor(
                a.getColor(
                        R.styleable.MoistureView_arrowColor,
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

        //createArrow();
    }

    void createArrow(float v) {
        // 3         2
        // +---------+
        //  \       /
        //   +-----+
        //  0       1

        // moisture is from 0..100
        float ang, diff0, diff1, cx, cy;
        ang   = ((float)mFromDegree) + (v * ((float)(mToDegree-mFromDegree)/100f));
        diff0 = 3f;
        diff1 = 4f;
        cx    = mContentWidth/2f;
        cy    = mContentHeight/2f;

        mPtr0.x = (int) (Math.cos(Math.toRadians(ang-diff0)) * (mInnerRadius-mGaugePenSize*1.5) + cx);
        mPtr0.y = (int) (Math.sin(Math.toRadians(ang-diff0)) * (mInnerRadius-mGaugePenSize*1.5) + cy);

        mPtr1.x = (int) (Math.cos(Math.toRadians(ang+diff0)) * (mInnerRadius-mGaugePenSize*1.5) + cx);
        mPtr1.y = (int) (Math.sin(Math.toRadians(ang+diff0)) * (mInnerRadius-mGaugePenSize*1.5) + cy);

        mPtr2.x = (int) (Math.cos(Math.toRadians(ang+diff1)) * (mInnerRadius+mRingPenSize) + cx);
        mPtr2.y = (int) (Math.sin(Math.toRadians(ang+diff1)) * (mInnerRadius+mRingPenSize) + cy);

        mPtr3.x = (int) (Math.cos(Math.toRadians(ang-diff1)) * (mInnerRadius+mRingPenSize) + cx);
        mPtr3.y = (int) (Math.sin(Math.toRadians(ang-diff1)) * (mInnerRadius+mRingPenSize) + cy);

        mPathArrow.reset();
        mPathArrow.moveTo(mPtr1.x, mPtr1.y);
        mPathArrow.lineTo(mPtr0.x, mPtr0.y);
        mPathArrow.lineTo(mPtr3.x, mPtr3.y);
        mPathArrow.lineTo(mPtr2.x, mPtr2.y);
        mPathArrow.lineTo(mPtr1.x, mPtr1.y);
        mPathArrow.close();
    }

    void createRange(float v) {
        float a = ((float)mFromDegree) + (v * ((float)(mToDegree-mFromDegree)/100f));
        mRangeAngleSweep = a - mRangeAngleMin;
    }

    public void setRange(float min, float max) {
        mMin = min;
        mRangeAngleMin = ((float)mFromDegree) + (min * ((float)(mToDegree-mFromDegree)/100f));
        if(mRangeAnimator != null) {
            mRangeAnimator.cancel();
        }

        long duration = (long)(mAnimDuration * (Math.abs(mMin-max))/100f);
        mRangeAnimator = ValueAnimator.ofFloat(mMin, max);
        mRangeAnimator.setDuration(duration);
        mRangeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float v = (float) valueAnimator.getAnimatedValue();
                createRange(v);
                invalidate();
            }
        });
        mRangeAnimator.start();
        mMax = max;
    }

    public void setCur(int cur) {
        if(mArrowAnimator != null) {
            mArrowAnimator.cancel();
        }

        long duration = (long)(mAnimDuration * ((float)Math.abs(mCur-cur))/100f);
        mArrowAnimator = ValueAnimator.ofFloat(mCur, cur);
        mArrowAnimator.setDuration(duration);
        mArrowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float v = (float) valueAnimator.getAnimatedValue();
                createArrow(v);
                invalidate();
            }
        });
        mArrowAnimator.start();
        mCur = cur;
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
                mPaintOutsideLimit
        );

        canvas.drawArc(
                mRect,
                mRangeAngleMin,
                mRangeAngleSweep,
                false,
                mPaintInsideLimit
        );

        canvas.drawPath(mPathArrow, mPaintArrow);
    }
}
