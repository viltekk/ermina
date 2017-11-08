package se.viltefjall.tekk.ermina.common;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import se.viltefjall.tekk.ermina.R;

public class MoistureView extends View {

    @SuppressWarnings("unused")
    private static final String ID = "MoistureView";

    // for drawing the view
    private Paint mPaintOuterRing;
    private Paint mPaintBackground;
    private Paint mPaintForeground;
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

    // moisture values
    private float     mCur;
    private float     mRangeAngleMin;
    private float     mRangeAngleSweep;
    private TextPaint mPaintText;

    // icon
    private Paint mPaintIcon;
    private Path  mIcon;

    // animation
    long          mAnimDuration;
    ValueAnimator mRangeAnimator;
    ValueAnimator mArrowAnimator;

    // custom mode
    private float   mMin;
    private float   mMax;
    private float   mLoAng;
    private float   mHiAng;
    private boolean mCustomMode;
    private float   mHandleRadius;
    private Point   mHandleLo;
    private Point   mHandleHi;
    private Paint   mPaintHandle;
    private boolean mLoHit;
    private boolean mHiHit;

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

        mIcon = new Path();
        mRect = new RectF();
        mPtr0 = new Point();
        mPtr1 = new Point();
        mPtr2 = new Point();
        mPtr3 = new Point();

        mAnimDuration = 1000;
        mCur = 0;

        mCustomMode = a.getBoolean(R.styleable.MoistureView_cfgMode, false);

        mGaugePenSize = a.getInt(R.styleable.MoistureView_gaugePenSize,  50);
        mRingPenSize  = a.getInt(R.styleable.MoistureView_ringPenSize ,  50);
        mFromDegree   = a.getInt(R.styleable.MoistureView_fromDegree  ,   0);
        mToDegree     = a.getInt(R.styleable.MoistureView_toDegree    , 360);
        mHandleRadius = mGaugePenSize * 1.2f;

        mPaintForeground = new Paint();
        mPaintForeground.setStrokeWidth(mGaugePenSize);
        mPaintForeground.setStyle(Paint.Style.STROKE);
        mPaintForeground.setStrokeCap(Paint.Cap.ROUND);
        mPaintForeground.setColor(
                a.getColor(
                        R.styleable.MoistureView_foregroundColor,
                        ContextCompat.getColor(getContext(), R.color.colorPrimary)
                )
        );

        mPaintBackground = new Paint();
        mPaintBackground.setStrokeWidth(mGaugePenSize);
        mPaintBackground.setStyle(Paint.Style.STROKE);
        mPaintBackground.setStrokeCap(Paint.Cap.ROUND);
        mPaintBackground.setColor(
                a.getColor(
                        R.styleable.MoistureView_backgroundColor,
                        Color.GRAY
                )
        );

        int textSize    = a.getInt(R.styleable.MoistureView_textSize, 80);
        int textPenSize = a.getInt(R.styleable.MoistureView_textPenSize, 10);

        if(mCustomMode) {
            textSize /= 2;
            textPenSize /= 2;
        }

        mPaintText = new TextPaint();
        mPaintText.setStrokeWidth(textPenSize);
        mPaintText.setTextSize(textSize * getResources().getDisplayMetrics().density);
        mPaintText.setStyle(Paint.Style.FILL);
        mPaintText.setStrokeCap(Paint.Cap.ROUND);
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.setColor(
                a.getColor(
                        R.styleable.MoistureView_textColor,
                        Color.WHITE
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

        mPaintHandle = new Paint();
        mPaintHandle.setColor(
                a.getColor(
                        R.styleable.MoistureView_handleColor,
                        ContextCompat.getColor(getContext(), R.color.colorAccent)
                )
        );

        mPaintIcon = new Paint();
        mPaintIcon.setStrokeWidth(textPenSize);
        mPaintIcon.setStyle(Paint.Style.FILL);
        mPaintIcon.setStrokeCap(Paint.Cap.ROUND);
        mPaintIcon.setStrokeJoin(Paint.Join.ROUND);
        mPaintIcon.setColor(
                a.getColor(
                        R.styleable.MoistureView_iconColor,
                        Color.WHITE
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

        if(mCustomMode) {
            mHandleLo = new Point();
            mHandleHi = new Point();
            mMin      = 25;
            mMax      = 75;
        }
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

        createIcon(mContentWidth, mContentHeight);

        if(mCustomMode) {
            setRange(mMin, mMax);
        }
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

        mCur = v;
    }

    void createRange(float v) {
        float a = ((float)mFromDegree) + (v * ((float)(mToDegree-mFromDegree)/100f));
        mRangeAngleSweep = a - mRangeAngleMin;
    }

    public void setRange(float min, float max) {
        mRangeAngleMin = ((float)mFromDegree) + (min * ((float)(mToDegree-mFromDegree)/100f));

        if(mCustomMode) {
            float a;
            createRange(max);

            a = mRangeAngleMin;
            setHandle(mHandleLo, a);

            a = mRangeAngleMin + mRangeAngleSweep;
            setHandle(mHandleHi, a);

            mLoAng = mRangeAngleMin;
            mHiAng = mRangeAngleMin + mRangeAngleSweep;
        } else {
            if (mRangeAnimator != null) {
                mRangeAnimator.cancel();
            }

            long duration = (long) (mAnimDuration * (Math.abs(min - max)) / 100f);
            mRangeAnimator = ValueAnimator.ofFloat(min, max);
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
        }
    }

    public void setCur(int cur) {
        if(mCustomMode) { return; }

        if(mArrowAnimator != null) {
            mArrowAnimator.cancel();
        }

        long duration = (long)(mAnimDuration * (Math.abs(mCur-cur))/100f);
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x, y, cx, cy;
        super.onTouchEvent(event);

        cx = mContentWidth/2f;
        cy = mContentHeight/2f;
        x  = event.getX();
        y  = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float minx;
                float maxx;
                float miny;
                float maxy;

                minx = mHandleLo.x - mHandleRadius;
                maxx = mHandleLo.x + mHandleRadius;
                miny = mHandleLo.y - mHandleRadius;
                maxy = mHandleLo.y + mHandleRadius;
                mLoHit = x >= minx && x <= maxx && y >= miny && y <= maxy;

                minx = mHandleHi.x - mHandleRadius;
                maxx = mHandleHi.x + mHandleRadius;
                miny = mHandleHi.y - mHandleRadius;
                maxy = mHandleHi.y + mHandleRadius;
                mHiHit = x >= minx && x <= maxx && y >= miny && y <= maxy;

                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                /*
                 *   2 |  3
                 * ----+----
                 *   1 |  0
                 */
                float a = 0;
                float t = (float) Math.toDegrees( Math.atan((y-cy)/(x-cx)) );

                // quadrant 0
                if(x > cx && y > cy) {
                    a = 360 + t;
                }
                // quadrant 1
                if(x <= cx && y > cy) {
                    a = 180 + t;
                }
                // quadrant 2
                if(x <= cx && y <= cy) {
                    a = 180 + t;
                }
                // quadrant 3
                if(x > cx && y <= cy) {
                    a = 360 + t;
                }

                if(a < mFromDegree) {
                    a = mFromDegree;
                }
                if(a > mToDegree) {
                    a = mToDegree;
                }

                if(mLoHit) {
                    if(a >= mHiAng-20) {
                        a = mHiAng-20;
                    }
                    mMin = 100f*(a-mFromDegree)/(mToDegree-mFromDegree);
                    mRangeAngleSweep += mRangeAngleMin - a;
                    mRangeAngleMin = mLoAng = a;

                    setHandle(mHandleLo, a);
                    invalidate();
                }

                if(mHiHit) {
                    if(a <= mLoAng+20) {
                        a = mLoAng+20;
                    }
                    mMax = 100f*(a-mFromDegree)/(mToDegree-mFromDegree);
                    mRangeAngleSweep = a - mRangeAngleMin;
                    mHiAng = a;
                    setHandle(mHandleHi, a);
                    invalidate();
                }
                break;
        }
        return true;
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
                mRangeAngleMin,
                mRangeAngleSweep,
                false,
                mPaintForeground
        );

        if(mCustomMode) {
            canvas.drawCircle(
                    mHandleLo.x,
                    mHandleLo.y,
                    mHandleRadius,
                    mPaintHandle
            );

            canvas.drawCircle(
                    mHandleHi.x,
                    mHandleHi.y,
                    mHandleRadius,
                    mPaintHandle
            );
        }

        canvas.drawPath(mPathArrow, mPaintArrow);
        canvas.drawPath(mIcon, mPaintIcon);

        if(mCustomMode) {
            canvas.drawText(
                    String.valueOf((int) mMin) + "|" + String.valueOf((int)mMax),
                    mContentWidth / 2,
                    3 * (mContentHeight / 4),
                    mPaintText
            );
        } else {
            canvas.drawText(
                    String.valueOf((int) mCur),
                    mContentWidth / 2,
                    3 * (mContentHeight / 4),
                    mPaintText
            );
        }
    }

    private void setHandle(Point p, float a) {
        float cx, cy, r;
        r  = mInnerRadius - mGaugePenSize/2f;
        cx = mContentWidth/2f;
        cy = mContentHeight/2f;

        p.x = (int) (Math.cos(Math.toRadians(a)) * r + cx);
        p.y = (int) (Math.sin(Math.toRadians(a)) * r + cy);
    }

    private void createIcon(int w, int h) {
        mIcon.reset();
        mIcon.moveTo(w/2, 4.5f*h/16);
        mIcon.lineTo(14.5f*w/32, 6*h/16);
        mIcon.moveTo(w/2, 4.5f*h/16);
        mIcon.lineTo(17.5f*w/32, 6*h/16);
        mIcon.arcTo(
                new RectF(
                        14.5f*w/32,
                        5.5f*h/16,
                        17.5f*w/32,
                        7*h/16
                ),
                0,
                180
        );
        mIcon.lineTo(14.5f*w/32, 6*h/16);
        mIcon.close();
    }
}
