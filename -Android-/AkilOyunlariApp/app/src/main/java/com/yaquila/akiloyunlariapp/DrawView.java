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
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.yaquila.akiloyunlariapp.gameutils.PatikaUtils;

import java.util.Arrays;

public class DrawView extends View {


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

    public DrawView(Context context, AttributeSet attrs){
        super(context,attrs);
        ctx = GameActivityPatika.appCompatActivity;
        setupDrawing();
    }

    //setup drawing
    private void setupDrawing(){

        //prepare for drawing and setup paint stroke properties
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth((float)PatikaUtils.pxHeight/75);//TODO density
        drawPaint.setStyle(Paint.Style.STROKE);
        PatikaUtils.paint=drawPaint;
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    //size assigned to view
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        PatikaUtils.bitmap = canvasBitmap;
        PatikaUtils.canvas = drawCanvas;
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
        if(touchX >= 0 && touchX <= PatikaUtils.pxHeight && touchY >= 0 && touchY <= PatikaUtils.pxHeight){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    PatikaUtils.rowColumn = PatikaUtils.xyToRowColumn(touchX,touchY);
                    PatikaUtils.previousCoor = PatikaUtils.rowColumn[0] + PatikaUtils.rowColumn[1];
                    is_moving=false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    PatikaUtils.rowColumn = PatikaUtils.xyToRowColumn(touchX,touchY);
                    String currentCoor = PatikaUtils.rowColumn[0] + PatikaUtils.rowColumn[1];
//                            Log.i("coor",currentCoor);
                    if(PatikaUtils.lineCanBeDrawn(currentCoor,PatikaUtils.previousCoor) || (PatikaUtils.operations.contains(PatikaUtils.previousCoor+currentCoor) || PatikaUtils.operations.contains(currentCoor+PatikaUtils.previousCoor))){
                        is_moving=true;
                        int[] firstMP = PatikaUtils.middlePoint(PatikaUtils.previousCoor);
                        int[] secondMP = PatikaUtils.middlePoint(currentCoor);
                        if((PatikaUtils.operations.contains(PatikaUtils.previousCoor+currentCoor) || PatikaUtils.operations.contains(currentCoor+PatikaUtils.previousCoor))){
//                                    Log.i("eraseMode","ON");
                            PatikaUtils.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//                                    paint.setColor(Color.TRANSPARENT);
                            PatikaUtils.paint.setStrokeWidth((float)PatikaUtils.pxHeight/60);
                            PatikaUtils.drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1],true,(ImageView) findViewById(R.id.canvasIV));
                            if(PatikaUtils.operations.contains(PatikaUtils.previousCoor+currentCoor)){
                                PatikaUtils.removeLine(PatikaUtils.previousCoor,currentCoor);
                                PatikaUtils.operations.remove(PatikaUtils.previousCoor+currentCoor);
                            }
                            else{
                                PatikaUtils.removeLine(currentCoor,PatikaUtils.previousCoor);
                                PatikaUtils.operations.remove(currentCoor+PatikaUtils.previousCoor);
                            }
                            PatikaUtils.opsForUndo.add(PatikaUtils.previousCoor+currentCoor+"-");
                            PatikaUtils.paint.setXfermode(null);
//                                    paint.setColor(getResources().getColor(R.color.near_black_blue));
                            PatikaUtils.paint.setStrokeWidth((float)PatikaUtils.pxHeight/75);
                        }
                        else{
                            Log.i("eraseMode","OFF  "+currentCoor+ "  "+ PatikaUtils.previousCoor);
                            PatikaUtils.drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1],false,(ImageView) findViewById(R.id.canvasIV));
                            PatikaUtils.addLine(PatikaUtils.previousCoor, currentCoor);
                            PatikaUtils.operations.add(PatikaUtils.previousCoor+currentCoor);
                            PatikaUtils.opsForUndo.add(PatikaUtils.previousCoor+currentCoor+"+");
                        }
                        PatikaUtils.previousCoor = currentCoor;
                        if(PatikaUtils.isGridFull()){
                            GameActivityPatika.checkAnswer(ctx, null);
                        }
                        Log.i("PatikaUtils.lineGrid", Arrays.deepToString(PatikaUtils.lineGrid));
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    return false;
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
