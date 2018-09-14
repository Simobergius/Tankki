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
    double x;
    double y;


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
        x = event.getX();
        y = event.getY();

        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:

                if(x < 0)
                    x = 0;
                if (x > this.getWidth())
                    x = this.getWidth();
                if(y < 0)
                    y = 0;
                if (y > this.getHeight())
                    y = this.getHeight();

                path.moveTo(centerX, centerY);
                path.lineTo((float) x, (float) y);

                if (mService != null) {
                    setValues();
                }
                return true;
            case MotionEvent.ACTION_MOVE:

                if(x < 0)
                    x = 0;
                if (x > this.getWidth())
                    x = this.getWidth();
                if(y < 0)
                    y = 0;
                if (y > this.getHeight())
                    y = this.getHeight();

                path.reset();
                path.moveTo(centerX, centerY);

                path.lineTo((float) x, (float) y);

                if (mService != null) {
                    setValues();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mService != null) {
                    x = 0;
                    y = 0;
                    setNullValues();
                }
                path.reset();
                break;
            default:
                x = 0;
                y = 0;
                setNullValues();
                path.reset();
        }
        invalidate();
        return true;
    }

    public void setService (MyBluetoothService service) {
        mService = service;
    }

    private void setValues() {
        if (getId() == R.id.TouchViewMovement) {
            mService.setMovementValues(x - centerX, y - centerY,this.getWidth() / 2);
        } else if(getId() == R.id.TouchViewTurret) {
            mService.setTurretValues(x - centerX, y - centerY, this.getHeight() / 2, this.getWidth() / 2);
        }
    }

    private void setNullValues() {
        if (getId() == R.id.TouchViewMovement) {
            mService.setMovementValues(0, 0,this.getWidth() / 2);
        } else if(getId() == R.id.TouchViewTurret) {
            mService.setTurretValues(0, 0, this.getHeight() / 2, this.getWidth() / 2);
        }
    }

}