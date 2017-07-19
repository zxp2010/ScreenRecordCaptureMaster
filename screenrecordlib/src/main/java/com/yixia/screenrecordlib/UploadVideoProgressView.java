package com.yixia.screenrecordlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhaoxiaopo on 2017/7/11.
 */

public class UploadVideoProgressView extends View {
    private Paint Bg_paint, Ft_paint, Tv_paint;
    private int Bg_color, Ft_color, Tv_color;
    private float LineWidth = 5f;
    private RectF ArcRectF;
    private float Centre;
    private float Radius;
    private int currentNumber;
    private Boolean isShowCenterText = true; //中间是否显示进度
    private int rotationDirection = 1;  //1默认顺时针 2逆时针
    private Timer timer;
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (currentNumber > 0) {
                currentNumber = currentNumber - 1;
                setProgress(currentNumber);
            } else {
                timer.cancel();
            }
        }
    };

    public UploadVideoProgressView(Context context) {
        super(context);
        init();
    }

    public UploadVideoProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.UploadVideoProgressView);
        isShowCenterText = typedArray.getBoolean(R.styleable.UploadVideoProgressView_showCenterText, true);
        rotationDirection = typedArray.getInt(R.styleable.UploadVideoProgressView_directionOrient, 1);
        typedArray.recycle();
        init();
    }

    private void init() {
        //背景的圆圈
        Bg_paint = new Paint();
        Bg_color = Color.parseColor("#33f4f4f4");
        Bg_paint.setColor(Bg_color);
        Bg_paint.setAntiAlias(true);

        //前面的圆弧
        Ft_paint = new Paint();
        Ft_color = Color.parseColor("#F9743A");
        Ft_paint.setColor(Ft_color);
        Ft_paint.setStrokeWidth(LineWidth);
        Ft_paint.setStyle(Paint.Style.STROKE);
        Ft_paint.setAntiAlias(true);

        //文字画笔
        Tv_paint = new Paint();
        Tv_color = Color.parseColor("#F9743A");
        Tv_paint.setColor(Tv_color);
        Tv_paint.setStrokeWidth(LineWidth);
        Bg_paint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(widthMeasureSpec));
        } else {
            setMeasuredDimension(500, 500);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //算出这里需要的区域
        Centre = getWidth() / 2;
        Radius = getWidth() / 2;
        ArcRectF = new RectF(Centre - Radius + 4, Centre - Radius + 4, Centre + Radius - 4, Centre + Radius - 4);

        //画出圆和圆弧
        canvas.drawCircle(Centre, Centre, Radius, Bg_paint);
        canvas.drawRoundRect(Centre - 50, Centre - 50, Centre - 50, Centre - 50, 5, 5, Ft_paint);

        if (rotationDirection == 1) {
            canvas.drawArc(ArcRectF, 270f, 360 * currentNumber / 100, false, Ft_paint);
        } else {
            canvas.drawArc(ArcRectF, 270f, -360 * currentNumber / 100, false, Ft_paint);
        }
        if (isShowCenterText) {
            //文字画在区域中央
            Tv_paint.setTextSize(Radius * 2 / 3);
            Paint.FontMetricsInt fontMetrics = Tv_paint.getFontMetricsInt();
            int baseline = (int) (ArcRectF.bottom + ArcRectF.top - fontMetrics.bottom - fontMetrics.top) / 2;
            Tv_paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(String.valueOf(currentNumber) + "%", ArcRectF.centerX(), baseline, Tv_paint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (timer != null) {
            timer.cancel();
        }
    }

    public void setProgress(int number) {
        if (number <= 100) {
            currentNumber = number;
            postInvalidate();
        }
    }

    public void setSumTime(int time) {
        if (time > 0 && timer == null) {
            currentNumber = time;
            timer = new Timer();
            timer.schedule(timerTask, 0, 1000);
        }
    }
}
