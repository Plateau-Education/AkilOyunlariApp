package com.yaquila.akiloyunlariapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

public class CustomDragShadowBuilder extends View.DragShadowBuilder {
    private static Drawable shadow;
    float boxSizeCoef;

    public CustomDragShadowBuilder(View v, float boxSize){
        super(v);
        boxSizeCoef = boxSize;
        shadow = ((ImageView)getView()).getDrawable();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
        float height,width;

        width = getView().getWidth()*(boxSizeCoef/15);
        height = getView().getHeight()*(boxSizeCoef/15);


        shadow.setBounds(0,0,(int)width,(int)height);
//        shadow.setAlpha(255);
//        Log.i("alpha",shadow.getAlpha()+"");
        outShadowSize.set((int)width,(int)height);
        outShadowTouchPoint.set((int)width/2,(int)height/2);
//        shadow.setAlpha(1);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
//        super.onDrawShadow(canvas);
        shadow.draw(canvas);
    }
}
