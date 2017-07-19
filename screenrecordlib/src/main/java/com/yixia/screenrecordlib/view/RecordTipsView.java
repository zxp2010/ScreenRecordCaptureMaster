package com.yixia.screenrecordlib.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yixia.screenrecordlib.R;

public class RecordTipsView extends RelativeLayout {

    //方向控制 0：下  1：左  2：上  3：右
    private int direction;
    private float targetX, targetY, targetWidth, targetHeight;
    private String text;

    private TextView record_tv_center_text;
    private ImageView record_iv_triangle;
    private LayoutParams tv_params;
    private LayoutParams iv_params;
    private LayoutParams parent_params;
    private int animetime = 0;
    private AnimatorSet animSet;
    private OnBubleDialogListner listner;


    public RecordTipsView(Context context, int direction, float targetX, float targetY, float targetWidth, float targetHeight, String text, OnBubleDialogListner listner) {
        super(context);
        this.direction = direction;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.text = text;
        this.listner = listner;
        initView(context);
    }

    public RecordTipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.record_tips_view, this);
        record_tv_center_text = (TextView) findViewById(R.id.text);
    }

    public void setLayoutText(String str) {
        record_tv_center_text.setText(str);
    }

    public void startLayoutAnim(int targetX, int targetWidth, Context context, OnBubleDialogListner listner) {
        this.listner = listner;
        this.setX(targetX - dip2px(context, 150) / 2 + targetWidth / 2);
        if (this.getVisibility() == INVISIBLE)
            startAnim();
        else
            animetime = 0;
    }

    /**
     * 初始化控件
     *
     * @param context
     */
    private void initView(Context context) {
        //文字设置
        record_tv_center_text = new TextView(context);
        record_tv_center_text.setId(R.id.record_tv_center_text);
        record_tv_center_text.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            record_tv_center_text.setBackground(getResources().getDrawable(R.drawable.record_dialog_tv_bg));
        } else {
            record_tv_center_text.setBackgroundDrawable(getResources().getDrawable(R.drawable.record_dialog_tv_bg));
        }
        record_tv_center_text.setTextColor(Color.parseColor("#FFFFFF"));
        //record_tv_center_text.setPadding(dip2px(context,10),dip2px(context,10),dip2px(context,10),dip2px(context,10));
        //图片设置
        record_iv_triangle = new ImageView(context);
        record_iv_triangle.setId(R.id.record_iv_triangle);
        record_tv_center_text.setText(text);
        record_iv_triangle.setImageDrawable(getResources().getDrawable(R.mipmap.pic_hite_triangle));
        setDirection(context);
    }


    /**
     * 设置布局结构
     *
     * @param context
     */
    private void setDirection(Context context) {
        if (direction == 0) {
            tv_params = new LayoutParams(dip2px(context, 100), dip2px(context, 30));
            iv_params = new LayoutParams(dip2px(context, 10), dip2px(context, 5));
            iv_params.addRule(RelativeLayout.BELOW, R.id.record_tv_center_text);
            iv_params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            tv_params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        } else if (direction == 1) {
            record_iv_triangle.setRotation(90);
            tv_params = new LayoutParams(dip2px(context, 150), dip2px(context, 30));
            iv_params = new LayoutParams(dip2px(context, 10), dip2px(context, 10));
            tv_params.addRule(RelativeLayout.RIGHT_OF, R.id.record_iv_triangle);
            iv_params.addRule(CENTER_VERTICAL);
            tv_params.addRule(CENTER_VERTICAL);
        } else if (direction == 2) {
            record_iv_triangle.setRotation(180);
            tv_params = new LayoutParams(dip2px(context, 150), dip2px(context, 30));
            iv_params = new LayoutParams(dip2px(context, 10), dip2px(context, 5));
            tv_params.addRule(RelativeLayout.BELOW, R.id.record_iv_triangle);
            iv_params.addRule(CENTER_HORIZONTAL);
            tv_params.addRule(CENTER_HORIZONTAL);
        } else if (direction == 3) {
            record_iv_triangle.setRotation(-90);
            tv_params = new LayoutParams(dip2px(context, 150), dip2px(context, 30));
            iv_params = new LayoutParams(dip2px(context, 10), dip2px(context, 10));
            iv_params.addRule(RelativeLayout.RIGHT_OF, R.id.record_tv_center_text);
            iv_params.addRule(CENTER_VERTICAL);
            tv_params.addRule(CENTER_VERTICAL);
        }

        record_tv_center_text.setLayoutParams(tv_params);
        record_iv_triangle.setLayoutParams(iv_params);


        addView(record_tv_center_text);
        addView(record_iv_triangle);
        parent_params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(parent_params);

        setPosition(context);
    }


    /**
     * 设置布局移动的位置
     */
    private void setPosition(Context context) {
        if (direction == 0) {
            this.setX(targetX - dip2px(context, 100) / 2 + targetWidth / 2);
            this.setY(targetY - dip2px(context, 30));
        } else if (direction == 1) {
            this.setX(targetX + targetWidth);
            this.setY(targetY + dip2px(context, 30) / 2);
        } else if (direction == 2) {
            this.setX(targetX - dip2px(context, 150) / 2 + targetWidth / 2);
            this.setY(targetY + targetHeight);
        } else if (direction == 3) {
            this.setX(targetX - dip2px(context, 150));
            this.setY(targetY + dip2px(context, 30) / 2);
        }
        startAnim();
    }

    /**
     * 设置动画开始
     */
    private void startAnim() {
        this.setVisibility(View.VISIBLE);
        int height = -dip2px(getContext(), 15);
        animSet = new AnimatorSet();
        if (direction == 0) {
            ObjectAnimator moveUP = ObjectAnimator.ofFloat(this, "translationY", this.getY(), this.getY() + height);
            moveUP.setDuration(600);
            ObjectAnimator moveDown = ObjectAnimator.ofFloat(this, "translationY", this.getY() + height, this.getY());
            moveDown.setDuration(600);
            animSet.play(moveDown).after(moveUP);
        } else if (direction == 1) {
            ObjectAnimator moveLeft = ObjectAnimator.ofFloat(this, "translationX", this.getX(), this.getX() - height);
            moveLeft.setDuration(600);
            ObjectAnimator moveRight = ObjectAnimator.ofFloat(this, "translationX", this.getX() - height, this.getX());
            moveRight.setDuration(600);
            animSet.play(moveLeft).after(moveRight);
        } else if (direction == 2) {
            ObjectAnimator moveUP = ObjectAnimator.ofFloat(this, "translationY", this.getY(), this.getY() - height);
            moveUP.setDuration(600);
            ObjectAnimator moveDown = ObjectAnimator.ofFloat(this, "translationY", this.getY() - height, this.getY());
            moveDown.setDuration(600);
            animSet.play(moveUP).after(moveDown);
        } else if (direction == 3) {
            ObjectAnimator moveLeft = ObjectAnimator.ofFloat(this, "translationX", this.getX(), this.getX() + height);
            moveLeft.setDuration(600);
            ObjectAnimator moveRight = ObjectAnimator.ofFloat(this, "translationX", this.getX() + height, this.getX());
            moveRight.setDuration(600);
            animSet.play(moveLeft).after(moveRight);
        }
        animSet.start();
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (animetime > 5) {
                    setVisibility(GONE);
                    if (listner != null)
                        listner.animOver();
                } else {
                    animetime++;
                    animation.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * 对外设置的接口
     */
    public interface OnBubleDialogListner {
        void animOver();
    }

    public void setText(String str) {
        record_tv_center_text.setText(str);
        animetime = 0;
    }

    private int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5F);
    }
}
