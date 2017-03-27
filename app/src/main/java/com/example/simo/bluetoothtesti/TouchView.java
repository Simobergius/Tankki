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
    TextView textView, textView2;

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
        int right, left;
        int fromLow = 0, fromHigh = this.getWidth();
        String str;
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
                right = mapTouchToTracks(Math.round(x - centerX), Math.round(centerY - y)) + 7;
                left = mapTouchToTracks(-Math.round(x - centerX), Math.round(centerY - y)) + 7;
                str = "Vasen: ";
                str += Integer.toString(left);
                str += "\tOikea: ";
                str += Integer.toString(right);
                textView.setText(str);
                if (mService != null) {
                    mService.setValues(right, left);
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
                right = mapTouchToTracks(Math.round(x - centerX), Math.round(centerY - y)) + 7;
                left = mapTouchToTracks(-Math.round(x - centerX), Math.round(centerY - y)) + 7;
                str = "Vasen: ";
                str += Integer.toString(left);
                str += "\tOikea: ";
                str += Integer.toString(right);
                textView.setText(str);
                if (mService != null) {
                    mService.setValues(right, left);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mService != null) {
                    mService.setValues(mapTouchToTracks(0, 0) + 7, mapTouchToTracks(0, 0) + 7);
                }
                path.reset();
                break;
            default:
                path.reset();
        }
        invalidate();
        return true;
    }

    public void setTextView(TextView tw, TextView tw2) {
        textView = tw;
        textView2 = tw2;
    }

    public void setService (MyBluetoothService service) {
        mService = service;
    }

    private int mapTouchToTracks(int x, int y) {
        int val = 0;
        int toLow = 0, toHigh = 14;
        int fromLow = -this.getWidth() / 2, fromHigh = this.getWidth() / 2;

        if(x == 0)
            val = y;
        if(y == 0)
            val = -x;

        if (y > 0) {
            if (x > 0)
                val = y - x;
            else if (x < 0) {
                if (Math.abs(x) >= Math.abs(y))
                    val = Math.abs(x);
                else if (Math.abs(x) < Math.abs(y))
                    val = Math.abs(y);
            }
        }
        else if (y < 0) {
            if (x > 0) {
                if (x + y <= 0) {
                    val = y + x;
                }
                else if (x + y > 0) {
                    val = -x - y;
                }
            }
            else if (x < 0) {
                if (x - y >= 0) {
                    val = y;
                }
                else if (x - y < 0) {
                    val = -x + (2 * y);
                }
            }
        }

        fromHigh -= fromLow - toLow;
        fromLow -= fromLow - toLow;

        float coeff = (((float) (toHigh-toLow)) / ((float) (fromLow-fromHigh)));
        float temp = val;

        val = Math.round(temp * coeff);

        return -val;
    }
}