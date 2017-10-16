package se.viltefjall.tekk.ermina.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
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

    private int   mLimitColor;
    private int   mOutsideLimitColor;
    private int   mRingColor;
    private int   mFromDegree;
    private int   mToDegree;
    private int   mContentWidth;
    private int   mContentHeight;
    private int   mGaugePenSize;
    private int   mRingPenSize;
    private int   mRadius;
    private RectF mRect;

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

        mLimitColor = a.getColor(
                    R.styleable.MoistureView_limitColor,
                    ContextCompat.getColor(getContext(), R.color.colorPrimary)
        );

        mOutsideLimitColor = a.getColor(
                R.styleable.MoistureView_outsideLimitColor,
                Color.GRAY
        );

        mRingColor = a.getColor(
                R.styleable.MoistureView_ringColor,
                ResourcesCompat.getColor(getResources(), R.color.colorAccent, null)
        );

        mGaugePenSize = a.getInt(R.styleable.MoistureView_gaugePenSize,  50);
        mRingPenSize  = a.getInt(R.styleable.MoistureView_ringPenSize ,  50);
        mFromDegree   = a.getInt(R.styleable.MoistureView_fromDegree  ,   0);
        mToDegree     = a.getInt(R.styleable.MoistureView_toDegree    , 360);

        mRect = new RectF();

        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mContentWidth  = getWidth() - getPaddingLeft() - getPaddingRight();
        mContentHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        //paint.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL));


        if(mContentWidth > mContentHeight) {
            mRadius = mContentHeight / 2 - mRingPenSize;
        } else {
            mRadius = mContentWidth / 2 - mRingPenSize;
        }

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);

        paint.setColor(mRingColor);
        paint.setStrokeWidth(mRingPenSize);
        canvas.drawCircle(
                mContentWidth/2,
                mContentHeight/2,
                mRadius,
                paint
        );

        int d = mRadius - mRingPenSize*2 - mGaugePenSize;
        int t = mContentHeight/2 - d;
        int l = mContentWidth /2 - d;
        int r = mContentWidth /2 + d;
        int b = mContentHeight/2 + d;
        mRect.set(l, t, r, b);

        paint.setColor(mOutsideLimitColor);
        paint.setStrokeWidth(mGaugePenSize);
        canvas.drawArc(
                mRect,
                mFromDegree,
                mToDegree-mFromDegree,
                false,
                paint
        );

        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setColor(mLimitColor);
        canvas.drawArc(
                mRect,
                210,
                330 - 210,
                false,
                paint
        );


        int p0x, p0y, p1x, p1y, p2x, p2y;
        int cx, cy;
        int sz, ang, startang;
        cx  = mContentWidth/2;
        cy  = mContentHeight/2;
        sz  = mGaugePenSize;
        d   = (int) (d - mGaugePenSize/4);
        ang = 5;
        startang = 210;
        p0x = (int) (Math.cos(Math.toRadians(startang)) * d + cx);
        p0y = (int) (Math.sin(Math.toRadians(startang)) * d + cy);
        p1x = (int) (Math.cos(Math.toRadians(startang+ang)) * (d+sz) + cx);
        p1y = (int) (Math.sin(Math.toRadians(startang+ang)) * (d+sz) + cy);
        p2x = (int) (Math.cos(Math.toRadians(startang-ang)) * (d+sz) + cx);
        p2y = (int) (Math.sin(Math.toRadians(startang-ang)) * (d+sz) + cy);

        Path path = new Path();
        path.moveTo(p0x, p0y);
        path.lineTo(p1x, p1y);
        path.lineTo(p2x, p2y);
        path.lineTo(p0x, p0y);
        path.close();

        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mGaugePenSize/6);
        paint.setColor(Color.RED);
        canvas.drawPath(path, paint);

        /*paint.setStrokeWidth(mGaugePenSize/4);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.RED);
        canvas.drawPath(path, paint);
        canvas.drawLine(p0x, p0y, p1x, p1y, paint);
        canvas.drawLine(p0x, p0y, p2x, p2y, paint);
        canvas.drawLine(p1x, p1y, p2x, p2y, paint);*/
    }
}
