package com.example.toeic_game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class borderView extends View {

    private boolean ini = false;
    private int width, height;
    private Paint p;
    private RectF rect;

    public borderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStrokeWidth(8);
        p.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!ini) {
            ini = true;
            width = getMeasuredWidth();
            height = getMeasuredHeight();
            rect = new RectF(0, 0, width, height);
            rect.inset(2.5f, 2.5f);
        }
        canvas.drawRoundRect(rect, 10, 10, p);
    }
}
