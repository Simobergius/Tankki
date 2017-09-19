package com.example.simo.bluetoothtesti;

/**
 * Created by Simo on 24.3.2017.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class TouchView extends View {

    private float centerY;
    private float centerX;
    MyBluetoothService mService;

    Paint drawPaint;
    private Path path = new Path();

    TextView text;

    public TouchView(Context context) {
        super(context);
    }

    public TouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        drawPaint = new Paint(Paint.DITHER_FLAG);
        drawPaint.setAntiAlias(true);
        drawPaint.setColor(Color.parseColor("#003300"));
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeWidth(5);
        setWillNotDraw(false);
        centerY = this.getHeight() / 2;
        centerX = this.getWidth() / 2;
    }

    @Override
    protected void onSizeChanged(int w, int h, int width, int height) {
        super.onSizeChanged(w, h, width, height);
        centerY = this.getHeight() / 2;
        centerX = this.getWidth() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        double x = event.getX();
        double y = event.getY();

        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:

                if(x < 0)
                    x = 0;
                if (x > this.getWidth())
                    x = this.getWidth();
                if(y < 0)
                    y = 0;
                if (y > this.getWidth())
                    y = this.getWidth();

                path.moveTo(centerX, centerY);
                path.lineTo((float) x, (float) y);
                text.setText("X: " + x + "\nY: " + y +"\nDiff X: " + (x - centerX) + "\nDiff Y: " + (y - centerY));

                if (mService != null) {
                    // Parameters for setValues: x, y, low, high
                    mService.setValues(x - centerX, y - centerY, -(this.getWidth() / 2), this.getWidth() / 2);
                }
                return true;
            case MotionEvent.ACTION_MOVE:

                if(x < 0)
                    x = 0;
                if (x > this.getWidth())
                    x = this.getWidth();
                if(y < 0)
                    y = 0;
                if (y > this.getWidth())
                    y = this.getWidth();

                path.reset();
                path.moveTo(centerX, centerY);

                path.lineTo((float) x, (float) y);
                text.setText("X: " + x + "\nY: " + y +"\nDiff X: " + (x - centerX) + "\nDiff Y: " + (y - centerY));

                if (mService != null) {
                    mService.setValues(x - centerX, y - centerY, -(this.getWidth() / 2), this.getWidth() / 2);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mService != null) {
                    mService.setValues(0, 0, -(this.getWidth() / 2), this.getWidth() / 2);
                }
                path.reset();
                text.setText("X: " + 0 + "\nY: " + 0 +"\nDiff X: " + 0 + "\nDiff Y: " + 0);
                break;
            default:
                path.reset();
        }
        invalidate();
        return true;
    }

    public void setService (MyBluetoothService service) {
        mService = service;
    }

    public void setTextView (TextView tw) {
        text = tw;
        text.setText("Im Alive!");}
}