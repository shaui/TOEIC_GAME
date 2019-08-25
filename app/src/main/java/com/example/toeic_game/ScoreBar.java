package com.example.toeic_game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

public class ScoreBar extends View {

    private boolean ini = false;
    private int p1_score = 0, p2_score = 0, colorA = Color.RED, colorB = Color.BLUE, width, height;
    private Paint p;

    public ScoreBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStrokeWidth(5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!ini) {
            ini = true;
            width = getMeasuredWidth();
            height = getMeasuredHeight();
            Path path = new Path();
            path.addRoundRect(width * 0.05f, height * 0.05f, width * 0.95f, height * 0.95f, height * 0.9f, height * 0.9f, Path.Direction.CW);
            canvas.clipPath(path, Region.Op.INTERSECT);
        }
        float spilt = 0;
        if(p1_score + p2_score == 0)
            spilt = 0.5f * width;
        else
            spilt = p1_score / (float) (p1_score + p2_score) * width;
        p.setColor(colorA);
        canvas.drawRect(width * 0.05f, height * 0.05f, spilt, height * 0.95f, p);
        p.setColor(colorB);
        canvas.drawRect(spilt, height * 0.05f, width * 0.95f, height * 0.95f, p);
    }

    public void setScore(boolean isP1, int num) {
        if(isP1)
            p1_score = num;
        else
            p2_score = num;
        invalidate();
    }

    public int[] getScore() {
        return new int[] {p1_score, p2_score};
    }

    public void setColorA(int color) {
        colorA = color;
    }

    public void setColorB(int color) {
        colorB = color;
    }
}
