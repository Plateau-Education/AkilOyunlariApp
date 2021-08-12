package com.yaquila.akiloyunlariapp.gameutils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.yaquila.akiloyunlariapp.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class PatikaUtils {

    public static AppCompatActivity context;

    public static String previousCoor;
    public static String[] rowColumn;
    public static String[][] lineGrid = new String[81][81];
    public static int gridSize = 5;
    public static int pxHeight = 900;

    public static List<String> operations = new ArrayList<>();
    public static List<String> opsForUndo = new ArrayList<>();

    public static List<String> blackList = new ArrayList<>();
    public static List<String> answerCornerRD = new ArrayList<>();
    public static List<String> answerCornerRU = new ArrayList<>();
    public static List<String> answerCornerLD = new ArrayList<>();
    public static List<String> answerCornerLU = new ArrayList<>();
    public static List<String> answerEdgeRL = new ArrayList<>();
    public static List<String> answerEdgeUD = new ArrayList<>();

    public static Bitmap bitmap;
    public static Canvas canvas;
    public static Paint paint;

    public static void initVars(AppCompatActivity ctx){
        context = ctx;
        previousCoor = null;
        rowColumn = null;
        lineGrid = new String[81][81];
        gridSize = 5;
        pxHeight = 900;
        operations = new ArrayList<>();
        opsForUndo = new ArrayList<>();
        blackList = new ArrayList<>();
        answerCornerLD = new ArrayList<>();
        answerCornerLU = new ArrayList<>();
        answerCornerRD = new ArrayList<>();
        answerCornerRU = new ArrayList<>();
        answerEdgeRL = new ArrayList<>();
        answerEdgeUD = new ArrayList<>();
        bitmap = null;
        canvas = null;
        paint = null;
    }

    public static String[] xyToRowColumn(final float x, final float y){
        String[] rowColumn = new String[2];
        float coef = (float)pxHeight/gridSize;
        rowColumn[0] = Integer.toString((int) Math.floor(x/coef));
        rowColumn[1] = Integer.toString((int) Math.floor(y/coef));
        return rowColumn;
    }

    public static int[] middlePoint(final String coor){
        int x = Integer.parseInt(String.valueOf(coor.charAt(0)));
        int y = Integer.parseInt(String.valueOf(coor.charAt(1)));
        int[] middle_point = new int[2];
        float coef = (float)pxHeight/gridSize;
        middle_point[0] = (int) (coef*x+coef/2);
        middle_point[1] = (int) (coef*y+coef/2);
        return middle_point;
    }

    public static void drawALine(final float startX, final float startY, final float stopX, final float stopY, final boolean erasing, final ImageView imageView){
//        Log.i("x1,y1,x2,y2",startX+"  "+startY+"  "+stopX+"  "+stopY);
        if(!erasing) {
            int offset = pxHeight / 150;
            if (startY - stopY == 0) {
                if (startX < stopX)
                    canvas.drawLine(startX - offset, startY, stopX + offset, stopY, paint);
                else canvas.drawLine(startX + offset, startY, stopX - offset, stopY, paint);
            } else {
                if (startY < stopY)
                    canvas.drawLine(startX, startY - offset, stopX, stopY + offset, paint);
                else canvas.drawLine(startX, startY + offset, stopX, stopY - offset, paint);
            }
        }
        else{
            String[] previousC = xyToRowColumn(startX,startY);
            int px = Integer.parseInt(previousC[0]);
            int py = Integer.parseInt(previousC[1]);
            String[] currentC = xyToRowColumn(stopX,stopY);
            int cx = Integer.parseInt(currentC[0]);
            int cy = Integer.parseInt(currentC[1]);
            int offset1 = pxHeight / 120;
            int offset2 = pxHeight / 120;
            if (startY - stopY == 0) {
                if(lineGrid[px][py].length() == 2){
                    offset1 = -pxHeight/140;
                }
                if(lineGrid[cx][cy].length() == 2){
                    offset2 = -pxHeight/140;
                }
                if (startX < stopX)
                    canvas.drawLine(startX - offset1, startY, stopX + offset2, stopY, paint);
                else canvas.drawLine(startX + offset1, startY, stopX - offset2, stopY, paint);
            } else {
                if(lineGrid[px][py].length() == 2){
                    offset1 = -pxHeight/140;
                }
                if(lineGrid[cx][cy].length() == 2){
                    offset2 = -pxHeight/140;
                }
                if (startY < stopY)
                    canvas.drawLine(startX, startY - offset1, stopX, stopY + offset2, paint);
                else canvas.drawLine(startX, startY + offset1, stopX, stopY - offset2, paint);
            }
        }
        imageView.setImageBitmap(bitmap);
    }

    public static void addLine(final String firstRC, final String secondRC){
        int r1 = Integer.parseInt(String.valueOf(firstRC.charAt(0)));
        int c1 = Integer.parseInt(String.valueOf(firstRC.charAt(1)));
        int r2 = Integer.parseInt(String.valueOf(secondRC.charAt(0)));
        int c2 = Integer.parseInt(String.valueOf(secondRC.charAt(1)));
        if(r1 == r2){ // vertical line
            if(c1 < c2){ // first one is above second
                lineGrid[r1][c1] += "d";
                lineGrid[r2][c2] += "u";
            }
            else if(c1 > c2){ // first one is below second
                lineGrid[r1][c1] += "u";
                lineGrid[r2][c2] += "d";
            }
        }
        else if(c1 == c2){ // horizontal line
            if(r1 < r2){ // first one is left of second
                lineGrid[r1][c1] += "r";
                lineGrid[r2][c2] += "l";
            }
            else { // first one is right of second
                lineGrid[r1][c1] += "l";
                lineGrid[r2][c2] += "r";
            }
        }
    }

    public static void removeLine(final String firstRC, final String secondRC){
        int r1 = Integer.parseInt(String.valueOf(firstRC.charAt(0)));
        int c1 = Integer.parseInt(String.valueOf(firstRC.charAt(1)));
        int r2 = Integer.parseInt(String.valueOf(secondRC.charAt(0)));
        int c2 = Integer.parseInt(String.valueOf(secondRC.charAt(1)));
        if(r1 == r2){ // vertical line
            if(c1 < c2){ // first one is above second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("d","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("u","");
            }
            else if(c1 > c2){ // first one is below second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("u","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("d","");
            }
        }
        else if(c1 == c2){ // horizontal line
            if(r1 < r2){ // first one is left of second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("r","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("l","");
            }
            else { // first one is right of second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("l","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("r","");

            }
        }
//        Log.i("eraseModeRemoveLine",r1+" "+c1+" "+lineGrid[r1][c1]+" / "+r2+" "+c2+" "+lineGrid[r2][c2]);
    }

    public static boolean isMoreLineCanBeAdded(final String coor){
        int r = Integer.parseInt(String.valueOf(coor.charAt(0)));
        int c = Integer.parseInt(String.valueOf(coor.charAt(1)));
        Log.i("rc",r+"  "+c);
        return lineGrid[r][c].length() < 2;
    }

    public static boolean lineCanBeDrawn(final String currentC, final String previousC){
        return (!currentC.equals(previousC)
                && (
                (currentC.charAt(0) == previousC.charAt(0)
                        && Math.abs(Integer.parseInt(String.valueOf(previousC.charAt(1))) - Integer.parseInt(String.valueOf(currentC.charAt(1)))) == 1)
                        ||      (currentC.charAt(1) == previousC.charAt(1)
                        && Math.abs(Integer.parseInt(String.valueOf(previousC.charAt(0))) - Integer.parseInt(String.valueOf(currentC.charAt(0)))) == 1)
        )
                && isMoreLineCanBeAdded(previousC) && isMoreLineCanBeAdded(currentC));
    }

    public static boolean isGridFull(){
        boolean isfull = true;
        for(int i = 0; i < gridSize; i++){
            for(int j = 0; j < gridSize; j++){
                if(lineGrid[i][j].length() < 2){
                    isfull=false;
                    break;
                }
            }
        }
        return isfull;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void undoOperation(final ImageView imageView){
        if(opsForUndo.size() > 2) {
            String op = opsForUndo.get(opsForUndo.size() - 1);
            String previousC = op.substring(0, 2);
            String currentC = op.substring(2, 4);
            int[] firstMP = middlePoint(previousC);
            int[] secondMP = middlePoint(currentC);
            if (op.charAt(4) == '+') {
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                paint.setStrokeWidth((float) pxHeight / 60);
                drawALine(firstMP[0], firstMP[1], secondMP[0], secondMP[1], true, imageView);
                removeLine(previousC, currentC);
                for (int i = operations.size() - 1; i >= 0; i--) {
                    if (operations.get(i).equals(previousC + currentC)) {
                        operations.remove(i);
                        break;
                    }
                }
                removeLine(currentC, previousC);
                for (int i = operations.size() - 1; i >= 0; i--) {
                    if (operations.get(i).equals(currentC + previousC)) {
                        operations.remove(i);
                        break;
                    }
                }
                paint.setStrokeWidth((float) pxHeight / 75);
                paint.setXfermode(null);

            } else {
                drawALine(firstMP[0], firstMP[1], secondMP[0], secondMP[1], false, imageView);
                addLine(previousC, currentC);
                operations.add(previousC + currentC);
            }
            for (int i = opsForUndo.size() - 1; i >= 0; i--) {
                if (opsForUndo.get(i).equals(op)) {
                    opsForUndo.remove(i);
                    break;
                }
            }
            Log.i("opsforUndo", opsForUndo.toString());
        }
    }

    public static void resetGrid(final View view){
        try {
            final TextView resetTV = (TextView) view;
            resetTV.setTextColor(context.getResources().getColor(R.color.light_red));
            resetTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
            resetTV.setText(R.string.ResetNormal);
            resetTV.postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetTV.setTextColor(context.getResources().getColorStateList(R.color.reset_selector_tvcolor));
                    resetTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    resetTV.setText(R.string.ResetUnderlined);
                }
            }, 100);

            operations = new ArrayList<>();
            operations.add("--");
            operations.add("--");
            opsForUndo = new ArrayList<>();
            opsForUndo.add("--");
            opsForUndo.add("--");

            bitmap.eraseColor(Color.TRANSPARENT);
            canvas = new Canvas(bitmap);

            for (int i = 0; i < gridSize; i++){
                for(int j = 0; j < gridSize; j++){
                    if(lineGrid[i][j].length() <=2){
                        lineGrid[i][j] = "";
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void seperateGridAnswer(final JSONArray grid) throws JSONException {
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        JSONArray bl = (JSONArray) grid.get(0);
        for(int bli = 0; bli < bl.length(); bli++){
            JSONArray co = (JSONArray) bl.get(bli);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            gridLayout.findViewWithTag(cos).setBackground(context.getResources().getDrawable(R.color.near_black_blue));
            blackList.add(cos);
            lineGrid[co.getInt(0)][co.getInt(1)] = "rldu";
        }

        JSONArray rd = (JSONArray) grid.get(1);
        for(int rdi = 0; rdi < rd.length(); rdi++){
            JSONArray co = (JSONArray) rd.get(rdi);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            answerCornerRD.add(cos);
        }

        JSONArray ru = (JSONArray) grid.get(2);
        for(int rui = 0; rui < ru.length(); rui++){
            JSONArray co = (JSONArray) ru.get(rui);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            answerCornerRU.add(cos);
        }

        JSONArray ld = (JSONArray) grid.get(3);
        for(int ldi = 0; ldi < ld.length(); ldi++){
            JSONArray co = (JSONArray) ld.get(ldi);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            answerCornerLD.add(cos);
        }

        JSONArray lu = (JSONArray) grid.get(4);
        for(int lui = 0; lui < lu.length(); lui++){
            JSONArray co = (JSONArray) lu.get(lui);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            answerCornerLU.add(cos);
        }

        JSONArray rl = (JSONArray) grid.get(5);
        for(int rli = 0; rli < rl.length(); rli++){
            JSONArray co = (JSONArray) rl.get(rli);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            answerEdgeRL.add(cos);
        }

        JSONArray ud = (JSONArray) grid.get(6);
        for(int udi = 0; udi < ud.length(); udi++){
            JSONArray co = (JSONArray) ud.get(udi);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            answerEdgeUD.add(cos);
        }
    }

    public static void clearGrid(){
        operations = new ArrayList<>();
        operations.add("--");
        operations.add("--");
        opsForUndo = new ArrayList<>();
        opsForUndo.add("--");
        opsForUndo.add("--");

        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        for (int i = 0; i < PatikaUtils.gridSize; i++) {
            for (int j = 0; j < PatikaUtils.gridSize; j++) {
                TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                tv.setText("");
                tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                tv.setEnabled(true);
            }
        }
        try{
            bitmap.eraseColor(Color.TRANSPARENT);
            canvas = new Canvas(bitmap);
        } catch (Exception e){
            e.printStackTrace();
        }

        for (int i = 0; i < PatikaUtils.gridSize; i++){
            for(int j = 0; j < PatikaUtils.gridSize; j++){
                PatikaUtils.lineGrid[i][j] = "";
            }
        }
        blackList = new ArrayList<>();
        answerCornerRD = new ArrayList<>();
        answerCornerRU = new ArrayList<>();
        answerCornerLD = new ArrayList<>();
        answerCornerLU = new ArrayList<>();
        answerEdgeRL = new ArrayList<>();
        answerEdgeUD = new ArrayList<>();

    }

    public static void initSomeVar(){
        //noinspection SuspiciousNameCombination
        bitmap = Bitmap.createBitmap(pxHeight, pxHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(context.getResources().getColor(R.color.transparent));
        paint = new Paint();
        paint.setColor(context.getResources().getColor(R.color.near_black_blue));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float)pxHeight/75);
        paint.setAntiAlias(true);

        for (int i = 0; i < PatikaUtils.gridSize; i++){
            for(int j = 0; j < PatikaUtils.gridSize; j++){
                PatikaUtils.lineGrid[j][i] = "";
            }
        }

    }
}
