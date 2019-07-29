package com.example.toeic_game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;

public class ReadyToStartView extends View {

    private boolean ini = false;
    private int width, height;
    private float textScale = 1, textSize;
    private String text = " ";
    private Paint p;
    private Rect bound = new Rect();
    private CountDownTimer clock;

    public ReadyToStartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!ini) {
            ini = true;
            width = getMeasuredWidth();
            height = getMeasuredHeight();
            if(width > height)
                textSize = height * 0.9f;
            else
                textSize = width * 0.9f;
        }

        p.setTextSize(textScale * textSize);
        p.getTextBounds(text, 0, 1, bound);
        canvas.drawText(text, width / 2f - bound.width() / 2f , height * 0.4f + bound.height() / 2f, p);
    }

    public void start() {
        if(clock != null) {
            clock.cancel();
        }
        text = "3";
        textScale = 1;
        clock = new CountDownTimer(3040,40){

            @Override
            public void onFinish() {
                changeTextSize();
                ReadyToStartView.this.invalidate();
            }

            @Override
            public void onTick(long millisUntilFinished) {
                changeTextSize();
                ReadyToStartView.this.invalidate();
            }

        };
        clock.start();
    }

    public void changeTextSize() {
        if(textScale > 0)
            textScale -= 0.04;
        else {
            textScale = 1;
            if(Integer.valueOf(text) > 1)
                text = String.format("%d", Integer.valueOf(text) - 1);
            else
                text = " ";
        }
    }
}
