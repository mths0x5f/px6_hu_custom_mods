package android.microntek.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.microntek.service.R;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MyButton extends FrameLayout {
    ImageView imageView;
    ImageView imageView2;

    public MyButton(Context context) {
        super(context);
    }

    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyButton);
        int resId2 = a.getResourceId(R.styleable.MyButton_imgSrc2, 0);
        if (resId2 != 0) {
            int width2 = a.getDimensionPixelSize(R.styleable.MyButton_imgWidth2, 10);
            int height2 = a.getDimensionPixelSize(R.styleable.MyButton_imgHeight2, 10);
            FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(width2, height2, 17);
            ImageView imageView = new ImageView(context);
            this.imageView2 = imageView;
            imageView.setImageResource(resId2);
            this.imageView2.setScaleType(ImageView.ScaleType.FIT_XY);
            this.imageView2.setDuplicateParentStateEnabled(true);
            this.imageView2.setLayoutParams(lp2);
            addView(this.imageView2);
        }
        int resId = a.getResourceId(R.styleable.MyButton_imgSrc, 0);
        int width = a.getDimensionPixelSize(R.styleable.MyButton_imgWidth, 10);
        int height = a.getDimensionPixelSize(R.styleable.MyButton_imgHeight, 10);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height, 17);
        ImageView imageView2 = new ImageView(context);
        this.imageView = imageView2;
        imageView2.setImageResource(resId);
        this.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        this.imageView.setDuplicateParentStateEnabled(true);
        this.imageView.setLayoutParams(lp);
        addView(this.imageView);
    }

    public void setColorResource(int color) {
        this.imageView.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
    }

    public void setImageResource(int resId) {
        this.imageView.setImageResource(resId);
    }
}
