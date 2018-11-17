package com.liweijian.camerademo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by Administrator on 2018/11/14.
 */

public class PhotoWithText extends FrameLayout {
    public PhotoWithText(@NonNull Context context) {
        super(context);
    }

    public PhotoWithText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoWithText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PhotoWithText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setBackground(Bitmap bitmap){
        setBackground(new BitmapDrawable(bitmap));
    }

    public void addText(Context context,String string){
        FrameLayout layout = this;
        TextView text = new TextView(context);
        text.setTextColor(Color.BLACK);
        text.setTextSize(20.0f);
        text.setText(string);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM|Gravity.CENTER;
        layout.addView(text,layoutParams);
    }



    public  Bitmap createViewBitmap(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        float scaleWidth = (float) width / getWidth();
        float scaleHeight = (float) height / getHeight();
        canvas.scale(scaleWidth, scaleHeight);
        draw(canvas);

        return bitmap;
    }
}
