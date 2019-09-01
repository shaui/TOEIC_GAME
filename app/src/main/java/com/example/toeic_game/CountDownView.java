package com.example.toeic_game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;

import com.example.toeic_game.widget.MyCountDownTimer;

public class CountDownView extends androidx.appcompat.widget.AppCompatImageView {

    private boolean ini = false;
    private int color = Color.GREEN;
    private long time = 4880, passTime = 0;
    private Paint p;
    private Path path;
    private RectF oval;

    private MyCountDownTimer clock = new MyCountDownTimer(time,40) {

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

    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE); //空心
        p.setStrokeWidth(15);
        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!ini) {
            ini = true;
            float cx, cy, r;
            cx = getMeasuredWidth() / 2f;
            cy = getMeasuredHeight() / 2f;
            if (getMeasuredHeight() > getMeasuredWidth())
                r = getMeasuredWidth() * 0.4f;
            else
                r = getMeasuredHeight() * 0.4f;
            float ix = cx - r, iy = cy - r, fx = cx + r, fy = cy + r;
            oval = new RectF(ix, iy, fx, fy);
            path.addOval(new RectF(ix - 5, iy - 5, fx + 5, fy + 5), Path.Direction.CW);
        }
        canvas.clipPath(path, Region.Op.INTERSECT);
        super.onDraw(canvas);
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

    public long getRemainingTime() {
        return time - passTime;
    }

    public void start(boolean isContinue) {
        if(!isContinue)
            passTime = 0;
        clock.setMillisInFuture(time - passTime);
        clock.start();
    }

    public void pause() {
        clock.cancel();
    }

}
