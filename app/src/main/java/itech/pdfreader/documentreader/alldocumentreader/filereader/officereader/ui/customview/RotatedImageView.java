package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.ui.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class RotatedImageView extends androidx.appcompat.widget.AppCompatImageView {

    private double mRotatedWidth;
    private double mRotatedHeight;

    public RotatedImageView(Context context) {
        super(context);
    }

    public RotatedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean update() {
        Drawable d = getDrawable();

        if (d == null) {
            return false;
        }

        int drawableWidth = d.getIntrinsicWidth();
        int drawableHeight = d.getIntrinsicHeight();

        if (drawableWidth <= 0 || drawableHeight <= 0) {
            return false;
        }

        double rotationRad = getRotation() / 180 * Math.PI;

        // calculate intrinsic rotated size
        // see diagram

        mRotatedWidth = (Math.abs(Math.sin(rotationRad)) * drawableHeight
                + Math.abs(Math.cos(rotationRad)) * drawableWidth);
        mRotatedHeight = (Math.abs(Math.cos(rotationRad)) * drawableHeight
                + Math.abs(Math.sin(rotationRad)) * drawableWidth);

        return true;
    }


    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (update()) {
            double ratio = mRotatedWidth / mRotatedHeight;

            int wMax = Math.min(getDefaultSize(Integer.MAX_VALUE, widthMeasureSpec), getMaxWidth());
            int hMax = Math.min(getDefaultSize(Integer.MAX_VALUE, heightMeasureSpec), getMaxHeight());

            int w = (int) Math.min(wMax, hMax * ratio);
            int h = (int) Math.min(hMax, wMax / ratio);

            setMeasuredDimension(w, h);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }

    private final float[] values = new float[9];

    protected void onDraw(Canvas canvas) {

        if (update()) {
            int availableWidth = getMeasuredWidth();
            int availableHeight = getMeasuredHeight();

            float scale = (float) Math.min(availableWidth / mRotatedWidth, availableHeight / mRotatedHeight);

            getImageMatrix().getValues(values);

            setScaleX(scale / values[Matrix.MSCALE_X]);
            setScaleY(scale / values[Matrix.MSCALE_Y]);
        }

        super.onDraw(canvas);
    }

    @Override
    public void setRotation(float rotation) {
        super.setRotation(rotation);
        requestLayout();
    }
}