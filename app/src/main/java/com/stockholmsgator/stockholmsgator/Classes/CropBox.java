package com.stockholmsgator.stockholmsgator.Classes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

public class CropBox extends View {


    private Rect rect;
    private Rect topRect;
    private Rect bottomRect;
    private Paint paint;
    private Paint blackTransparentPaint;

    public CropBox(Context context, Rect rect){
        super(context);
        this.rect = rect;

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(10);

        blackTransparentPaint = new Paint();
        blackTransparentPaint.setStyle(Paint.Style.FILL);
        blackTransparentPaint.setColor(Color.BLACK);
        blackTransparentPaint.setAlpha(130);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                topRect = new Rect(0,0,getWidth(),CropBox.this.rect.top);
                bottomRect = new Rect(0,CropBox.this.rect.bottom,getWidth(),getHeight());
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(rect,paint);
        canvas.drawRect(topRect,blackTransparentPaint);
        canvas.drawRect(bottomRect,blackTransparentPaint);
    }
}
