package com.yixia.screenrecordlib.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by zhaoxiaopo on 2017/7/17.
 */

public class ProgressButtonView extends View {
    private final int DEGREE_PROGRESS_DISTANCE = dipToPx(8);
    /**
     * 圆心坐标
     */
    private float centerX;
    private float centerY;
    private Paint allArcPaint;
    private Paint centerPaint;
    private Paint progressPaint;
    private Paint mCenterCirclePaint;

    private RectF bgRect;
    private RectF mStopRect;
    private ValueAnimator progressAnimator;
    private float mStartAngle = 270;
    private float mTotalAngle = 360;
    private float currentAngle = 0;
    private float lastAngle;
    private int bgArcColor;
    private float progressWidth = dipToPx(2);
    private float maxValues = 60;
    private float curValues = 0;//进度值
    private int aniSpeed = 1000;
    /**
     * mTotalAngle / maxValues 的值
     */
    private float k;
    private boolean isRecordState = false;

    public ProgressButtonView(Context context) {
        super(context);
        initView(context);
    }

    public ProgressButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ProgressButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        bgArcColor = Color.parseColor("#33f4f4f4");

        //背景
        allArcPaint = new Paint();
        allArcPaint.setAntiAlias(true);
        allArcPaint.setStyle(Paint.Style.FILL);
        allArcPaint.setColor(bgArcColor);
        allArcPaint.setStrokeCap(Paint.Cap.ROUND);

        mCenterCirclePaint = new Paint();
        mCenterCirclePaint.setAntiAlias(true);
        mCenterCirclePaint.setStyle(Paint.Style.FILL);
        mCenterCirclePaint.setColor(Color.WHITE);
        mCenterCirclePaint.setStrokeCap(Paint.Cap.ROUND);

        centerPaint = new Paint();
        centerPaint.setAntiAlias(true);
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setColor(Color.argb(255, 255, 45, 53));
        centerPaint.setStrokeCap(Paint.Cap.ROUND);

        //当前进度
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(progressWidth);
        progressPaint.setColor(Color.argb(255, 255, 45, 53));
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(widthMeasureSpec));
        } else {
            setMeasuredDimension(500, 500);
        }

        centerX = getMeasuredHeight() / 2;
        centerY = getMeasuredHeight() / 2;
        bgRect = new RectF(5, 5, getMeasuredWidth() - 5, getMeasuredHeight() - 5);
        mStopRect = new RectF(centerX - 30, centerY - 30, centerX + 30, centerY + 30);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isRecordState) {
            //画背景
            canvas.drawCircle(centerX, centerY, getMeasuredWidth() / 2, allArcPaint);
            canvas.drawRoundRect(mStopRect, 5, 5, centerPaint);
            //当前进度
            canvas.drawArc(bgRect, mStartAngle, currentAngle, false, progressPaint);
        } else {
            //画背景
            canvas.drawCircle(centerX, centerY, getMeasuredWidth() / 2, allArcPaint);
            canvas.drawCircle(centerX, centerY, (getMeasuredWidth() / 2) - 20, mCenterCirclePaint);
        }
        invalidate();
    }

    public void setMaxValues(float maxValues) {
        this.maxValues = maxValues;
        k = mTotalAngle / maxValues;
    }

    public void setCurrentValues(float currentValues) {
        if (currentValues > maxValues) {
            currentValues = maxValues;
        }
        if (currentValues < 0) {
            currentValues = 0;
        }
        this.curValues = currentValues;
        lastAngle = currentAngle;
        setAnimation(lastAngle, currentValues * k, aniSpeed);
    }

    public void setIsRecordState(boolean recordState) {
        isRecordState = recordState;
    }

    /**
     * 为进度设置动画
     *
     * @param last
     * @param current
     */
    private void setAnimation(float last, float current, int length) {
        progressAnimator = ValueAnimator.ofFloat(last, current);
        progressAnimator.setDuration(length);
        progressAnimator.setTarget(currentAngle);
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentAngle = (float) animation.getAnimatedValue();
                curValues = currentAngle / k;
            }
        });
        progressAnimator.start();
    }

    /**
     * dip 转换成px
     *
     * @param dip
     * @return
     */
    private int dipToPx(float dip) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    /**
     * 得到屏幕宽度
     *
     * @return
     */
    private int getScreenWidth() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
}
