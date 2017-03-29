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
        float x = event.getX();
        float y = event.getY();
        int fromLow = 0, fromHigh = this.getWidth();

        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                path.moveTo(centerX, centerY);

                if (x > fromHigh)
                    x = fromHigh;
                if (x < fromLow)
                    x = fromLow;
                if (y > fromHigh)
                    y = fromHigh;
                if (y < fromLow)
                    y = fromLow;
                path.lineTo(x, y);
                if (mService != null) {
                    mService.setValues(x - centerX, y - centerY, -(this.getWidth() / 2), this.getWidth() / 2);
                }
                return true;
            case MotionEvent.ACTION_MOVE:

                path.reset();
                path.moveTo(centerX, centerY);

                if (x > fromHigh)
                    x = fromHigh;
                if (x < fromLow)
                    x = fromLow;
                if (y > fromHigh)
                    y = fromHigh;
                if (y < fromLow)
                    y = fromLow;
                path.lineTo(x, y);
                //Right track = f(x, y)
                //Left track = f(-x, y)
                if (mService != null) {
                    mService.setValues(x - centerX, y - centerY, -(this.getWidth() / 2), this.getWidth() / 2);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mService != null) {
                    mService.setValues(0, 0, fromLow, fromHigh);
                }
                path.reset();
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
}