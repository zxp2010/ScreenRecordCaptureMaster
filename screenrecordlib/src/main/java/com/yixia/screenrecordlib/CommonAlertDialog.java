package com.yixia.screenrecordlib;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 作者： duanyikang on 2017/1/11.
 * 邮箱： duanyikang@yixia.com
 * 描述：
 */

public class CommonAlertDialog implements View.OnClickListener {

    private TextView mTitle;
    private TextView mContent, mSecondContent;
    private LinearLayout ll_space;
    private Button mLeftBtn;
    private Button mRightBtn;
    private Button mSingleBtn;

    private Dialog mDialog;
    private View mDialogView;
    private Builder mBuilder;

    public CommonAlertDialog(Context context, Builder builder) {

        this.mBuilder = builder;
        mDialog = new Dialog(context, R.style.CommonAlertDialogStyle);
        mDialogView = View.inflate(context, R.layout.common_widget_dialog_prepare, null);
        mTitle = (TextView) mDialogView.findViewById(R.id.dialog_normal_title);
        mContent = (TextView) mDialogView.findViewById(R.id.dialog_normal_content);
        mLeftBtn = (Button) mDialogView.findViewById(R.id.dialog_normal_leftbtn);
        mRightBtn = (Button) mDialogView.findViewById(R.id.dialog_normal_rightbtn);
        mSingleBtn = (Button) mDialogView.findViewById(R.id.dialog_normal_midbtn);
        ll_space = (LinearLayout) mDialogView.findViewById(R.id.ll_space);
        mSecondContent = (TextView) mDialogView.findViewById(R.id.dialog_normal_secondcontent);
        mDialog.setContentView(mDialogView);

        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return true;
            }
        });

        Window dialogWindow = mDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.width = dip2px(context.getApplicationContext(), 280f);
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);

        initDialog(builder);
    }

    private void initDialog(Builder builder) {

        mDialog.setCanceledOnTouchOutside(builder.isTouchOutside());

        if (builder.getTitleVisible()) {

            mTitle.setVisibility(View.VISIBLE);
        } else {

            mTitle.setVisibility(View.GONE);
        }

        if (builder.isSingleMode()) {
            mSingleBtn.setVisibility(View.VISIBLE);
            ll_space.setVisibility(View.GONE);
        }

        mTitle.setText(builder.getTitleText());
        mTitle.setTextColor(builder.getTitleTextColor());
        mTitle.setTextSize(builder.getTitleTextSize());
        if (builder.getSecondContentText() != null && !builder.getSecondContentText().equals(""))
            mSecondContent.setVisibility(View.VISIBLE);
        mContent.setText(builder.getContentText());
        mContent.setTextColor(builder.getContentTextColor());
        mContent.setTextSize(builder.getContentTextSize());
        mSecondContent.setText(builder.getSecondContentText());
        mSecondContent.setTextColor(builder.getSecondContentTextColor());
        mSecondContent.setTextSize(builder.getSecondContentTextSize());
        mLeftBtn.setText(builder.getLeftButtonText());
        mLeftBtn.setTextColor(builder.getLeftButtonTextColor());
        mLeftBtn.setTextSize(builder.getButtonTextSize());
        mRightBtn.setText(builder.getRightButtonText());
        mRightBtn.setTextColor(builder.getRightButtonTextColor());
        mRightBtn.setTextSize(builder.getButtonTextSize());
        mSingleBtn.setText(builder.getSingleButtonText());
        mSingleBtn.setTextColor(builder.getSingleButtonTextColor());
        mSingleBtn.setTextSize(builder.getButtonTextSize());

        mLeftBtn.setOnClickListener(this);
        mRightBtn.setOnClickListener(this);
        mSingleBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        int i = view.getId();
        if (i == R.id.dialog_normal_leftbtn && mBuilder.getOnclickListener() != null) {
            mBuilder.getOnclickListener().clickLeftButton(mLeftBtn);
            return;
        }
        if (i == R.id.dialog_normal_rightbtn && mBuilder.getOnclickListener() != null) {
            mBuilder.getOnclickListener().clickRightButton(mRightBtn);
            return;
        }

        if (i == R.id.dialog_normal_midbtn && mBuilder.getSingleListener() != null) {

            mBuilder.getSingleListener().onClick(mSingleBtn);
            return;
        }

    }

    public void show() {

        mDialog.show();
    }

    public void dismiss() {

        mDialog.dismiss();
    }

    private int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5F);
    }

    /**
     * 按钮回掉
     */
    public interface DialogOnItemClickListener {
        void clickLeftButton(View view);

        void clickRightButton(View view);

        void onItemClick(Button button, int position);
    }

    public static class Builder {
        private Context mContext;
        private String titleText;
        private int titleTextColor;
        private int titleTextSize;
        private String contentText;
        private int contentTextColor;
        private int contentTextSize;
        private boolean isSingleMode;

        private String secondContentText;
        private int secondContentTextColor;
        private int secondContentTextSize;

        private String singleButtonText;
        private int singleButtonTextColor;
        private String leftButtonText;
        private int leftButtonTextColor;
        private String rightButtonText;
        private int rightButtonTextColor;
        private int buttonTextSize;
        private DialogOnItemClickListener onclickListener;
        private View.OnClickListener singleListener;
        private boolean isTitleVisible;
        private boolean isTouchOutside;

        public Builder(Context context) {
            mContext = context.getApplicationContext();
            titleText = "温馨提示";
            Resources resources = context.getResources();
            titleTextColor = resources.getColor(R.color.screen_record_black);

            contentText = "";
            contentTextColor = resources.getColor(R.color.screen_record_black);
            isSingleMode = false;
            singleButtonText = "确定";
            singleButtonTextColor = resources.getColor(R.color.screen_record_black);
            leftButtonText = "取消";
            leftButtonTextColor = resources.getColor(R.color.screen_record_black);
            rightButtonText = "确定";
            rightButtonTextColor = resources.getColor(R.color.screen_record_black);
            onclickListener = null;
            singleListener = null;
            isTitleVisible = false;
            isTouchOutside = true;
            titleTextSize = 16;
            contentTextSize = 14;
            buttonTextSize = 14;

        }

        public String getTitleText() {
            return titleText;
        }

        public Builder setTitleText(String titleText) {
            this.titleText = titleText;
            return this;
        }

        public int getTitleTextColor() {
            return titleTextColor;
        }

        public Builder setTitleTextColor( int titleTextColor) {
            this.titleTextColor = mContext.getResources().getColor(titleTextColor);
            return this;
        }

        public String getSecondContentText() {
            return secondContentText;
        }

        public Builder setSecondContentText(String SecondContentText) {
            this.secondContentText = SecondContentText;
            return this;
        }

        public int getSecondContentTextColor() {
            return secondContentTextColor;
        }

        public Builder setSecondContentTextColor( int contentTextColor) {
            this.secondContentTextColor = mContext.getResources().getColor(contentTextColor);
            return this;
        }

        public int getSecondContentTextSize() {
            return secondContentTextSize;
        }

        public Builder setSecondContentTextSize(int contentTextSize) {
            this.secondContentTextSize = contentTextSize;
            return this;
        }


        public String getContentText() {
            return contentText;
        }

        public Builder setContentText(String contentText) {
            this.contentText = contentText;
            return this;
        }

        public int getContentTextColor() {
            return contentTextColor;
        }

        public Builder setContentTextColor( int contentTextColor) {
            this.contentTextColor = mContext.getResources().getColor(contentTextColor);
            return this;
        }


        public boolean isSingleMode() {
            return isSingleMode;
        }

        public Builder setSingleMode(boolean singleMode) {
            isSingleMode = singleMode;
            return this;
        }

        public String getSingleButtonText() {
            return singleButtonText;
        }

        public Builder setSingleButtonText(String singleButtonText) {
            this.singleButtonText = singleButtonText;
            return this;
        }

        public int getSingleButtonTextColor() {
            return singleButtonTextColor;
        }

        public Builder setSingleButtonTextColor( int singleButtonTextColor) {
            this.singleButtonTextColor = mContext.getResources().getColor(singleButtonTextColor);
            return this;
        }

        public String getLeftButtonText() {
            return leftButtonText;
        }

        public Builder setLeftButtonText(String leftButtonText) {
            this.leftButtonText = leftButtonText;
            return this;
        }

        public int getLeftButtonTextColor() {
            return leftButtonTextColor;
        }

        public Builder setLeftButtonTextColor( int leftButtonTextColor) {
            this.leftButtonTextColor = mContext.getResources().getColor(leftButtonTextColor);
            return this;
        }

        public String getRightButtonText() {
            return rightButtonText;
        }

        public Builder setRightButtonText(String rightButtonText) {
            this.rightButtonText = rightButtonText;
            return this;
        }

        public int getRightButtonTextColor() {
            return rightButtonTextColor;
        }

        public Builder setRightButtonTextColor( int rightButtonTextColor) {
            this.rightButtonTextColor = mContext.getResources().getColor(rightButtonTextColor);
            return this;
        }

        public DialogOnItemClickListener getOnclickListener() {
            return onclickListener;
        }

        public Builder setOnclickListener(DialogOnItemClickListener onclickListener) {
            this.onclickListener = onclickListener;
            return this;
        }

        public View.OnClickListener getSingleListener() {
            return singleListener;
        }

        public Builder setSingleListener(View.OnClickListener singleListener) {
            this.singleListener = singleListener;
            return this;
        }

        public boolean getTitleVisible() {
            return isTitleVisible;
        }

        public Builder setTitleVisible(boolean isVisible) {
            isTitleVisible = isVisible;
            return this;
        }

        public boolean isTouchOutside() {
            return isTouchOutside;
        }

        public Builder setCanceledOnTouchOutside(boolean isTouchOutside) {

            this.isTouchOutside = isTouchOutside;
            return this;
        }

        public int getContentTextSize() {
            return contentTextSize;
        }

        public Builder setContentTextSize(int contentTextSize) {
            this.contentTextSize = contentTextSize;
            return this;
        }

        public int getTitleTextSize() {
            return titleTextSize;
        }

        public Builder setTitleTextSize(int titleTextSize) {
            this.titleTextSize = titleTextSize;
            return this;
        }

        public int getButtonTextSize() {
            return buttonTextSize;
        }

        public Builder setButtonTextSize(int buttonTextSize) {
            this.buttonTextSize = buttonTextSize;
            return this;
        }


        public CommonAlertDialog build(Context context) {

            return new CommonAlertDialog(context, this);
        }
    }


}
