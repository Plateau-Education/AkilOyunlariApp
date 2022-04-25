package com.yaquila.akiloyunlariapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.yaquila.akiloyunlariapp.gameutils.SozcukTuruUtils;

public class WordTourView extends View {


    /**
     * This is demo code to accompany the Mobiletuts+ tutorial series:
     * - Android SDK: Create a Drawing App
     * - extended for follow-up tutorials on using patterns and opacity
     *
     * Sue Smith
     * August 2013 / September 2013
     *
     */

    //drawing path
    public static Path drawPath;
    //drawing and canvas paint
    public static Paint drawPaint, canvasPaint;
    //initial color
    public static int paintColor = 0xFF660000, paintAlpha = 255;
    //canvas
    public static Canvas drawCanvas;
    //canvas bitmap
    public static Bitmap canvasBitmap;
    //brush sizes
    public static float brushSize, lastBrushSize;
    //erase flag
    public static boolean erase=false;

    public static boolean is_moving = false;

    public static AppCompatActivity ctx;

    public WordTourView(Context context, AttributeSet attrs){
        super(context,attrs);
        ctx = GameActivitySozcukTuru.appCompatActivity;
        setupDrawing();
    }

    //setup drawing
    private void setupDrawing(){

        //prepare for drawing and setup paint stroke properties
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth((float)SozcukTuruUtils.pxHeightY/75);//TODO density
        drawPaint.setStyle(Paint.Style.STROKE);
        SozcukTuruUtils.paint=drawPaint;
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    //size assigned to view
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        SozcukTuruUtils.bitmap = canvasBitmap;
        SozcukTuruUtils.canvas = drawCanvas;
    }

    //draw the view - will be called after touch event
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    //register user touches as drawing action
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        //respond to down, move and up events
        if(touchX >= 0 && touchX <= SozcukTuruUtils.pxHeightX && touchY >= 0 && touchY <= SozcukTuruUtils.pxHeightY){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    SozcukTuruUtils.rowColumn = SozcukTuruUtils.xyToRowColumn(touchX,touchY);
                    SozcukTuruUtils.previousCoor = SozcukTuruUtils.rowColumn[0] + SozcukTuruUtils.rowColumn[1];
                    is_moving=false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    SozcukTuruUtils.rowColumn = SozcukTuruUtils.xyToRowColumn(touchX,touchY);
                    String currentCoor = SozcukTuruUtils.rowColumn[0] + SozcukTuruUtils.rowColumn[1];
//                            Log.i("coor",currentCoor);
                    if(SozcukTuruUtils.lineCanBeDrawn(currentCoor,SozcukTuruUtils.previousCoor) || (SozcukTuruUtils.operations.contains(SozcukTuruUtils.previousCoor+currentCoor) || SozcukTuruUtils.operations.contains(currentCoor+SozcukTuruUtils.previousCoor))){
                        is_moving=true;
                        int[] firstMP = SozcukTuruUtils.middlePoint(SozcukTuruUtils.previousCoor);
                        int[] secondMP = SozcukTuruUtils.middlePoint(currentCoor);
                        if((SozcukTuruUtils.operations.contains(SozcukTuruUtils.previousCoor+currentCoor) || SozcukTuruUtils.operations.contains(currentCoor+SozcukTuruUtils.previousCoor))){
//                                    Log.i("eraseMode","ON");
                            SozcukTuruUtils.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//                                    SozcukTuruUtils.paint.setColor(getResources().getColor(R.color.f7f5fa));
                            SozcukTuruUtils.paint.setStrokeWidth((float)SozcukTuruUtils.pxHeightY/60);
                            SozcukTuruUtils.drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1],true);
                            if(SozcukTuruUtils.operations.contains(SozcukTuruUtils.previousCoor+currentCoor)){
                                SozcukTuruUtils.removeLine(SozcukTuruUtils.previousCoor,currentCoor);
                                SozcukTuruUtils.operations.remove(SozcukTuruUtils.previousCoor+currentCoor);
                            }
                            else{
                                SozcukTuruUtils.removeLine(currentCoor,SozcukTuruUtils.previousCoor);
                                SozcukTuruUtils.operations.remove(currentCoor+SozcukTuruUtils.previousCoor);
                            }
                            SozcukTuruUtils.opsForUndo.add(SozcukTuruUtils.previousCoor+currentCoor+"-");
                            SozcukTuruUtils.paint.setXfermode(null);
//                                    SozcukTuruUtils.paint.setColor(getResources().getColor(R.color.near_black_blue));
                            SozcukTuruUtils.paint.setStrokeWidth((float)SozcukTuruUtils.pxHeightY/75);
                        }
                        else{
                            Log.i("eraseMode","OFF  "+currentCoor+ "  "+ SozcukTuruUtils.previousCoor);
                            SozcukTuruUtils.drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1],false);
                            SozcukTuruUtils.addLine(SozcukTuruUtils.previousCoor, currentCoor);
                            SozcukTuruUtils.operations.add(SozcukTuruUtils.previousCoor+currentCoor);
                            SozcukTuruUtils.opsForUndo.add(SozcukTuruUtils.previousCoor+currentCoor+"+");
                        }
                        SozcukTuruUtils.previousCoor = currentCoor;
                        Log.i("operations",SozcukTuruUtils.operations+"      /      "+SozcukTuruUtils.answer);
                        if(SozcukTuruUtils.isGridFull()){
                            Log.i("isGridFull","grid is full");
                            GameActivitySozcukTuru.checkAnswer(ctx,null);
                        }
//                                Log.i("LineGrid", Arrays.deepToString(lineGrid));
                    }
                    break;
                case MotionEvent.ACTION_UP:
//                            if(!is_moving){
//                                Log.i("Pressed","Pressed");
//                            }
//                            if(isGridFull()){
//                                checkAnswer(null);
//                            }
                    break;
            }
        }
        //redraw
        invalidate();
        return true;

    }

    //update color
    public void setColor(String newColor){
        invalidate();
        //check whether color value or pattern name
        if(newColor.startsWith("#")){
            paintColor = Color.parseColor(newColor);
            drawPaint.setColor(paintColor);
            drawPaint.setShader(null);
        }
        else{
            //pattern
            int patternID = getResources().getIdentifier(
                    newColor, "drawable", "com.yaquila.akiloyunlariapp");
            //decode
            Bitmap patternBMP = BitmapFactory.decodeResource(getResources(), patternID);
            //create shader
            BitmapShader patternBMPshader = new BitmapShader(patternBMP,
                    Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            //color and shader
            drawPaint.setColor(0xFFFFFFFF);
            drawPaint.setShader(patternBMPshader);
        }
    }
    public Bitmap getCanvasBitmap(){
        return canvasBitmap;
    }
    public Paint getDrawPaint(){
        return drawPaint;
    }
    public Canvas getDrawCanvas(){
        return drawCanvas;
    }
    public void setCanvasBitmap(Bitmap bitmap){
        canvasBitmap = bitmap;
    }
    public void setDrawPaint(Paint paint){
        drawPaint = paint;
    }
    public void setDrawCanvas(Canvas canvas){
        drawCanvas = canvas;
    }
}
