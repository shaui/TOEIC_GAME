package com.example.toeic_game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.util.AttributeSet;

public class CountDownView extends androidx.appcompat.widget.AppCompatImageView {

    private boolean ini = false;
    private int color = Color.GREEN;
    private float ix = 0, iy = 0, fx = 0, fy = 0;
    private long time = 5000, passTime = 0;
    private Paint p;
    private CountDownTimer clock;
    private RectF oval;

    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE); //空心
        p.setStrokeWidth(15);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!ini) {
            ini = true;
            float cx, cy, r;
            cx = getMeasuredWidth() / 2f;
            cy = getMeasuredHeight() / 2f;
            if (getMeasuredHeight() > getMeasuredWidth())
                r = getMeasuredWidth() * 0.4f;
            else
                r = getMeasuredHeight() * 0.4f;
            ix = cx - r;
            iy = cy - r;
            fx = cx + r;
            fy = cy + r;
            oval = new RectF(ix, iy, fx, fy);
        }
        p.setColor(Color.GRAY);
        canvas.drawArc(oval, 0, 360, false, p);
        p.setColor(color);
        float a = 270 + passTime / (float) (time) * 360, b = 360 - passTime / (float) time * 360;
        canvas.drawArc(oval, a, b, false, p);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setTime(long ms) {
        time = ms;
    }

    public void start(boolean isContinue) {
        if(clock != null) {
            clock.cancel();
        }
        if(!isContinue)
            passTime = 0;
        clock = new CountDownTimer(time - passTime,40) {

            @Override
            public void onFinish() {
                passTime = time;
                CountDownView.this.invalidate();
            }

            @Override
            public void onTick(long millisUntilFinished) {
                passTime = time - millisUntilFinished;
                CountDownView.this.invalidate();
            }

        };
        clock.start();
    }

    public void pause() {
        clock.cancel();
    }

}
