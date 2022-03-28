package com.yaquila.akiloyunlariapp;

import static com.yaquila.akiloyunlariapp.gameutils.HazineAviUtils.gridDCs;
import static com.yaquila.akiloyunlariapp.gameutils.HazineAviUtils.switchPosition;
import static com.yaquila.akiloyunlariapp.gameutils.PatikaUtils.bitmap;
import static com.yaquila.akiloyunlariapp.gameutils.PatikaUtils.blackList;
import static com.yaquila.akiloyunlariapp.gameutils.PatikaUtils.canvas;
import static com.yaquila.akiloyunlariapp.gameutils.PatikaUtils.paint;
import static com.yaquila.akiloyunlariapp.gameutils.PatikaUtils.previousCoor;
import static com.yaquila.akiloyunlariapp.gameutils.PatikaUtils.pxHeight;
import static com.yaquila.akiloyunlariapp.gameutils.PatikaUtils.rowColumn;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.yaquila.akiloyunlariapp.gameutils.HazineAviUtils;
import com.yaquila.akiloyunlariapp.gameutils.PatikaUtils;
import com.yaquila.akiloyunlariapp.gameutils.PiramitUtils;
import com.yaquila.akiloyunlariapp.gameutils.SayiBulmacaUtils;
import com.yaquila.akiloyunlariapp.gameutils.SozcukTuruUtils;
import com.yaquila.akiloyunlariapp.gameutils.SudokuUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings({"SuspiciousNameCombination", "MismatchedQueryAndUpdateOfCollection"})
public class GameGuideActivity extends AppCompatActivity {

    String gameName;
    String type = "single";
    int inNum = 0;
    int counterIn5 = 0;
    boolean gameFinished=false;
    boolean is_moving = false;

    List<String> inStrings = new ArrayList<>();
    List<String> unrelatedBoxes = new ArrayList<>(Arrays.asList("00","01","02","03","10","11","12","13","20","21","22","23","30","31","32","33"));
    Map<String, Class<?>> utilsMap = new HashMap<>();
    TextView inTV;
    GridLayout gl;
    ImageView switchIV;
    ImageView arrow;
    Handler animator;
    Runnable[] runnables = new Runnable[100];

   List<String> allowedBoxes = new ArrayList<>();
   
    public void goBackToHTP(View view){
        Intent intent = new Intent(getApplicationContext(), HowToPlayActivity.class);
        intent.putExtra("gameName", gameName);
        intent.putExtra("type",type);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    public void nextQuestion(View view){
        if(gameFinished){
            LayoutInflater factory = LayoutInflater.from(this);
            final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
            final AlertDialog correctDialog = new AlertDialog.Builder(this).create();
            TextView timerTV = leaveDialogView.findViewById(R.id.timeTV_correctDialog);
            timerTV.setVisibility(View.GONE);
            correctDialog.setView(leaveDialogView);

            leaveDialogView.findViewById(R.id.correctDialogNext).setVisibility(View.GONE);
            leaveDialogView.findViewById(R.id.correctDialogGameMenu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                    correctDialog.dismiss();
                }
            });
            correctDialog.show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void changeClicked(View view){
        TextView box = (TextView) view;
        String answerIndex = box.getTag().toString();
        if(allowedBoxes.contains(answerIndex)) {
            try {
                utilsMap.get(gameName).getDeclaredMethod("changeClicked", View.class).invoke(null,view);
            } catch (Exception e) {
                e.printStackTrace();
            }
            allowedBoxes.remove(answerIndex);
            gl.findViewWithTag(answerIndex).clearAnimation();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    public void numClicked(View view) {
        String num = view.getTag().toString();
        Log.i("num",num + " - allowedBoxes - "+allowedBoxes.toString());
        if(allowedBoxes.contains(num)){
            TextView currentBox = gl.findViewWithTag("answer"+ PiramitUtils.clickedBox);
            if(PiramitUtils.draftModeActive[Integer.parseInt(PiramitUtils.clickedBox)]) {
                if(currentBox.getText().toString().length() == 0){
                    currentBox.setText(view.getTag().toString());
                    currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                }
                else{
                    currentBox.setText(currentBox.getText().toString()+" "+view.getTag().toString());
                }
            } else currentBox.setText(view.getTag().toString());
            view.clearAnimation();
            allowedBoxes.remove((String)view.getTag());
        }
    }

    public void draftClicked(View view){
        if(inNum==5)PiramitUtils.draftClicked(view);
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    public void instructionChange(View view) {
        if(gameName.contains(getString(R.string.HazineAvı))) {
            List<String> tapBoxes = new ArrayList<>();
            List<String> relatedClues = new ArrayList<>();
            List<String> relatedBoxes = new ArrayList<>();
            List<ArrayList<Integer>> grid = new ArrayList<>();
            allowedBoxes = new ArrayList<>();

            if (view.getTag().equals("+")) {
                if (inNum < inStrings.size() - 1){
                    inNum++;
                    view.setAlpha(1f);
                    ((LinearLayout)inTV.getParent()).findViewWithTag("-").setAlpha(1f);
                }
                if(inNum == inStrings.size()-1){
                    view.setAlpha(0.3f);
                }
            } else {
                if (inNum > 0){
                    inNum--;
                    view.setAlpha(1f);
                    ((LinearLayout)inTV.getParent()).findViewWithTag("+").setAlpha(1f);
                }
                if(inNum == 0){
                    view.setAlpha(0.3f);
                }
            }
            inTV.setText(inStrings.get(inNum));

            if (inNum <= 3) {
                Log.i("inNum", "<=2");
                switchIV.setImageResource(R.drawable.ic_diamond);
                switchPosition = "diamond";
                grid.add(new ArrayList<>(Arrays.asList(0, 0, 2, 4, 0)));
                grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 0, 0)));
                grid.add(new ArrayList<>(Arrays.asList(0, 3, 0, 0, 0)));
                grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
                grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, 0)));
                createGridAndPlace(grid);
            }

            if (inNum == 3) {
                tapBoxes = new ArrayList<>(Arrays.asList("40", "21", "31", "41"));
                relatedClues = new ArrayList<>(Collections.singletonList("30"));
            }

            if (inNum == 4 || inNum == 5) {
                relatedClues = new ArrayList<>(Collections.singletonList("20"));
                switchIV.setImageResource(R.drawable.ic_diamond);
                switchPosition = "diamond";
                switchIV.clearAnimation();
                grid.add(new ArrayList<>(Arrays.asList(0, 0, 2, 4, -1)));
                grid.add(new ArrayList<>(Arrays.asList(1, 0, -1, -1, -1)));
                grid.add(new ArrayList<>(Arrays.asList(0, 3, 0, 0, 0)));
                grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
                grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, 0)));
                createGridAndPlace(grid);
            }

            if (inNum >= 6 && inNum <= 10) {
                switchIV.setImageResource(R.drawable.ic_cross);
                switchPosition = "cross";
                switchIV.clearAnimation();
                grid.add(new ArrayList<>(Arrays.asList(0, -2, 2, 4, -1)));
                grid.add(new ArrayList<>(Arrays.asList(1, -2, -1, -1, -1)));
                grid.add(new ArrayList<>(Arrays.asList(0, 3, 0, 0, 0)));
                grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
                grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, 0)));
                createGridAndPlace(grid);
            }
            if (inNum == 11) {
                switchIV.setImageResource(R.drawable.ic_cross);
                switchPosition = "cross";
                switchIV.clearAnimation();
                grid.add(new ArrayList<>(Arrays.asList(0, -2, 2, 4, -1)));
                grid.add(new ArrayList<>(Arrays.asList(1, -2, -1, -1, -1)));
                grid.add(new ArrayList<>(Arrays.asList(0, 3, -2, -2, -2)));
                grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
                grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, 0)));
                createGridAndPlace(grid);
            }
            if (inNum >= 12 && inNum <= 15) {
                switchIV.setImageResource(R.drawable.ic_diamond);
                switchPosition = "diamond";
                switchIV.clearAnimation();
                grid.add(new ArrayList<>(Arrays.asList(0, -2, 2, 4, -1)));
                grid.add(new ArrayList<>(Arrays.asList(1, -2, -1, -1, -1)));
                grid.add(new ArrayList<>(Arrays.asList(0, 3, -2, -2, -2)));
                grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
                grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, -1)));
                createGridAndPlace(grid);
            }
            if (inNum == 16) {
                switchIV.setImageResource(R.drawable.ic_cross);
                switchPosition = "cross";
                switchIV.clearAnimation();
                grid.add(new ArrayList<>(Arrays.asList(0, -2, 2, 4, -1)));
                grid.add(new ArrayList<>(Arrays.asList(1, -2, -1, -1, -1)));
                grid.add(new ArrayList<>(Arrays.asList(-2, 3, -2, -2, -2)));
                grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
                grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, -1)));
                createGridAndPlace(grid);
            }
            if (inNum == 17) {
                switchIV.setImageResource(R.drawable.ic_diamond);
                switchPosition = "diamond";
                switchIV.clearAnimation();
                grid.add(new ArrayList<>(Arrays.asList(-1, -2, 2, 4, -1)));
                grid.add(new ArrayList<>(Arrays.asList(1, -2, -1, -1, -1)));
                grid.add(new ArrayList<>(Arrays.asList(-2, 3, -2, -2, -2)));
                grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
                grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, -1)));
                createGridAndPlace(grid);
            }
            if (inNum >= 18) {
                switchIV.setImageResource(R.drawable.ic_diamond);
                switchPosition = "diamond";
                switchIV.clearAnimation();
                grid.add(new ArrayList<>(Arrays.asList(-1, -2, 2, 4, -1)));
                grid.add(new ArrayList<>(Arrays.asList(1, -2, -1, -1, -1)));
                grid.add(new ArrayList<>(Arrays.asList(-2, 3, -2, -2, -2)));
                grid.add(new ArrayList<>(Arrays.asList(-1, 2, -1, 2, 1)));
                grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, -1)));
                createGridAndPlace(grid);
            }


            if (inNum == 5) {
                tapBoxes = new ArrayList<>(Arrays.asList("10", "11"));
                relatedClues = new ArrayList<>(Collections.singletonList("20"));
            }
            if (inNum == 8) {
                relatedBoxes = new ArrayList<>(Arrays.asList("23", "24", "44"));
                relatedClues = new ArrayList<>(Collections.singletonList("34"));
            }
            if (inNum == 9) {
                relatedClues = new ArrayList<>(Collections.singletonList("33"));
                relatedBoxes = new ArrayList<>(Arrays.asList("23", "24", "44"));
            }
            if (inNum == 10) {
                relatedBoxes = new ArrayList<>(Arrays.asList("23", "24", "44"));
                relatedClues = new ArrayList<>(Collections.singletonList("33"));
                tapBoxes = new ArrayList<>(Arrays.asList("42", "32", "22"));
            }
            if (inNum == 11) {
                tapBoxes = new ArrayList<>(Collections.singletonList("44"));
                relatedClues = new ArrayList<>(Collections.singletonList("43"));
            }
            if (inNum == 12) {
                relatedBoxes = new ArrayList<>(Arrays.asList("23", "24"));
                relatedClues = new ArrayList<>(Collections.singletonList("34"));
            }
            if (inNum == 13) {
                relatedBoxes = new ArrayList<>(Arrays.asList("03", "14"));
                relatedClues = new ArrayList<>(Collections.singletonList("04"));
            }
            if (inNum == 14) {
                relatedBoxes = new ArrayList<>(Arrays.asList("03", "14", "23", "24"));
            }
            if (inNum == 15) {
                relatedBoxes = new ArrayList<>(Arrays.asList("03", "14", "23", "24"));
                relatedClues = new ArrayList<>(Collections.singletonList("13"));
                tapBoxes = new ArrayList<>(Collections.singletonList("02"));
            }
            if (inNum == 16) {
                relatedClues = new ArrayList<>(Collections.singletonList("01"));
                tapBoxes = new ArrayList<>(Collections.singletonList("00"));
            }
            if (inNum == 17) {
                relatedClues = new ArrayList<>(Collections.singletonList("12"));
                tapBoxes = new ArrayList<>(Arrays.asList("03", "23"));
            }

            arrow.setVisibility(View.INVISIBLE);
            arrow.clearAnimation();

            if (inNum == 5 || inNum == 11 || inNum == 15 || inNum == 16) {
                for (String index : relatedClues)
                    gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_bluegreen));
                for (String index : relatedBoxes)
                    gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_shallow_light));
                animateView(switchIV, 0.3f, 1.0f);

                arrow.setVisibility(View.VISIBLE);
                Animation anim = new TranslateAnimation(-(100f / 3) * getResources().getDisplayMetrics().density, (100f / 3) * getResources().getDisplayMetrics().density, 0f, 0f);
                anim.setDuration(500);
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                arrow.startAnimation(anim);

                final List<String> finalTapBoxes = tapBoxes;
                switchIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (inNum == 5 || inNum == 11 || inNum == 15 || inNum == 16) {
                            arrow.setVisibility(View.INVISIBLE);
                            arrow.clearAnimation();
                            switchIV.clearAnimation();
                            if (switchPosition.equals("diamond")) {
                                switchIV.setImageResource(R.drawable.ic_cross);
                                switchPosition = "cross";
                            } else {
                                switchIV.setImageResource(R.drawable.ic_diamond);
                                switchPosition = "diamond";
                            }
                            for (String index : finalTapBoxes) {
                                allowedBoxes.add(index);
                                gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_red));
                                animateView(gl.findViewWithTag(index), 0.5f, 1.0f);
                            }
                            switchIV.setOnClickListener(null);
                        }
                    }
                });

            }
            else {
                for (String index : tapBoxes) {
                    allowedBoxes.add(index);
                    gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_red));
                    animateView(gl.findViewWithTag(index), 0.5f, 1.0f);
                }
                for (String index : relatedClues)
                    gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_bluegreen));
                for (String index : relatedBoxes)
                    gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_shallow_light));
            }
        }
        else if (gameName.equals(getString(R.string.Patika))){
            List<String> relatedClues = new ArrayList<>();

            if (view.getTag().equals("+")) {
                if (inNum < inStrings.size() - 1){
                    inNum++;
                    view.setAlpha(1f);
                    ((LinearLayout)inTV.getParent()).findViewWithTag("-").setAlpha(1f);
                }
                if(inNum == inStrings.size()-1){
                    view.setAlpha(0.3f);
                }
            } else {
                if (inNum > 0){
                    inNum--;
                    view.setAlpha(1f);
                    ((LinearLayout)inTV.getParent()).findViewWithTag("+").setAlpha(1f);
                }
                if(inNum == 0){
                    view.setAlpha(0.3f);
                }
            }
            inTV.setText(inStrings.get(inNum));

            if (inNum <= 4) {
                Log.i("inNum", "<=4");
                bitmap.eraseColor(Color.TRANSPARENT);
                canvas = new Canvas(bitmap);
                allowedBoxes.clear();
                try {
                    for(Runnable run : runnables) animator.removeCallbacks(run);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }

            if(inNum==4){
                addAnim("01","00","10",0, true);
            }

            if(inNum==5){
                try {
                    for(Runnable run : runnables) animator.removeCallbacks(run);
                } catch(Exception e){
                    e.printStackTrace();
                }
                bitmap.eraseColor(Color.TRANSPARENT);
                canvas = new Canvas(bitmap);
                allowedBoxes.clear();
                drawALine(middlePoint("01")[0], middlePoint("01")[1], middlePoint("00")[0], middlePoint("00")[1], false);
                drawALine(middlePoint("00")[0], middlePoint("00")[1], middlePoint("10")[0], middlePoint("10")[1], false);
                counterIn5=0;

                addAnim("31","30","40",counterIn5+1, true);
            }
            if(inNum==6 || inNum==7 || inNum==8 || inNum==9){
                try {
                    for(Runnable run : runnables) animator.removeCallbacks(run);
                } catch(Exception e){
                    e.printStackTrace();
                }
                relatedClues = new ArrayList<>();
                for (int i = 0; i < 7; i++) {
                    for (int j = 0; j < 7; j++){
                        if(!blackList.contains(Integer.toString(j) + i))
                            gl.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                    }
                }

                bitmap.eraseColor(Color.TRANSPARENT);
                canvas = new Canvas(bitmap);
                allowedBoxes.clear();
                gl.findViewWithTag("16").setBackground(getResources().getDrawable(R.drawable.stroke_bg));

                drawALine(middlePoint("01")[0], middlePoint("01")[1], middlePoint("00")[0], middlePoint("00")[1], false);
                drawALine(middlePoint("00")[0], middlePoint("00")[1], middlePoint("10")[0], middlePoint("10")[1], false);
                drawALine(middlePoint("01")[0], middlePoint("01")[1], middlePoint("02")[0], middlePoint("02")[1], false);
                drawALine(middlePoint("02")[0], middlePoint("02")[1], middlePoint("03")[0], middlePoint("03")[1], false);
                drawALine(middlePoint("03")[0], middlePoint("03")[1], middlePoint("13")[0], middlePoint("13")[1], false);
                drawALine(middlePoint("10")[0], middlePoint("10")[1], middlePoint("11")[0], middlePoint("11")[1], false);
                drawALine(middlePoint("11")[0], middlePoint("11")[1], middlePoint("21")[0], middlePoint("21")[1], false);
                drawALine(middlePoint("21")[0], middlePoint("21")[1], middlePoint("22")[0], middlePoint("22")[1], false);
                drawALine(middlePoint("16")[0], middlePoint("16")[1], middlePoint("06")[0], middlePoint("06")[1], false);
                drawALine(middlePoint("06")[0], middlePoint("06")[1], middlePoint("05")[0], middlePoint("05")[1], false);
                drawALine(middlePoint("05")[0], middlePoint("05")[1], middlePoint("15")[0], middlePoint("15")[1], false);
                drawALine(middlePoint("26")[0], middlePoint("26")[1], middlePoint("36")[0], middlePoint("36")[1], false);
                drawALine(middlePoint("36")[0], middlePoint("36")[1], middlePoint("35")[0], middlePoint("35")[1], false);
                drawALine(middlePoint("35")[0], middlePoint("35")[1], middlePoint("25")[0], middlePoint("25")[1], false);
                drawALine(middlePoint("43")[0], middlePoint("43")[1], middlePoint("44")[0], middlePoint("44")[1], false);
                drawALine(middlePoint("44")[0], middlePoint("44")[1], middlePoint("45")[0], middlePoint("45")[1], false);
                drawALine(middlePoint("45")[0], middlePoint("45")[1], middlePoint("55")[0], middlePoint("55")[1], false);
                drawALine(middlePoint("55")[0], middlePoint("55")[1], middlePoint("56")[0], middlePoint("56")[1], false);
                drawALine(middlePoint("56")[0], middlePoint("56")[1], middlePoint("66")[0], middlePoint("66")[1], false);
                drawALine(middlePoint("66")[0], middlePoint("66")[1], middlePoint("65")[0], middlePoint("65")[1], false);
                drawALine(middlePoint("65")[0], middlePoint("65")[1], middlePoint("64")[0], middlePoint("64")[1], false);
                drawALine(middlePoint("64")[0], middlePoint("64")[1], middlePoint("63")[0], middlePoint("63")[1], false);
                drawALine(middlePoint("62")[0], middlePoint("62")[1], middlePoint("61")[0], middlePoint("61")[1], false);
                drawALine(middlePoint("61")[0], middlePoint("61")[1], middlePoint("51")[0], middlePoint("51")[1], false);
                drawALine(middlePoint("51")[0], middlePoint("51")[1], middlePoint("50")[0], middlePoint("50")[1], false);
                drawALine(middlePoint("50")[0], middlePoint("50")[1], middlePoint("40")[0], middlePoint("40")[1], false);
                drawALine(middlePoint("40")[0], middlePoint("40")[1], middlePoint("30")[0], middlePoint("30")[1], false);
                drawALine(middlePoint("30")[0], middlePoint("30")[1], middlePoint("31")[0], middlePoint("31")[1], false);
                drawALine(middlePoint("31")[0], middlePoint("31")[1], middlePoint("41")[0], middlePoint("41")[1], false);
                drawALine(middlePoint("41")[0], middlePoint("41")[1], middlePoint("42")[0], middlePoint("42")[1], false);
            }
            if(inNum==7){
                relatedClues = new ArrayList<>(Collections.singletonList("16"));
                addAnim("16","26","",17, false);
            }
            if(inNum==8){
                relatedClues = new ArrayList<>();
                gl.findViewWithTag("16").setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                drawALine(middlePoint("16")[0], middlePoint("16")[1], middlePoint("26")[0], middlePoint("26")[1], false);
                counterIn5=0;
                addAnim("15","14","",18,false);
            }
            if(inNum==9){
                drawALine(middlePoint("16")[0], middlePoint("16")[1], middlePoint("26")[0], middlePoint("26")[1], false);
                drawALine(middlePoint("15")[0], middlePoint("15")[1], middlePoint("14")[0], middlePoint("14")[1], false);
                drawALine(middlePoint("14")[0], middlePoint("14")[1], middlePoint("13")[0], middlePoint("13")[1], false);
                drawALine(middlePoint("25")[0], middlePoint("25")[1], middlePoint("24")[0], middlePoint("24")[1], false);
                drawALine(middlePoint("24")[0], middlePoint("24")[1], middlePoint("23")[0], middlePoint("23")[1], false);
                drawALine(middlePoint("23")[0], middlePoint("23")[1], middlePoint("33")[0], middlePoint("33")[1], false);
                drawALine(middlePoint("33")[0], middlePoint("33")[1], middlePoint("43")[0], middlePoint("43")[1], false);
                drawALine(middlePoint("22")[0], middlePoint("22")[1], middlePoint("32")[0], middlePoint("32")[1], false);
                drawALine(middlePoint("32")[0], middlePoint("32")[1], middlePoint("42")[0], middlePoint("42")[1], false);
                drawALine(middlePoint("62")[0], middlePoint("62")[1], middlePoint("52")[0], middlePoint("52")[1], false);
                drawALine(middlePoint("52")[0], middlePoint("52")[1], middlePoint("53")[0], middlePoint("53")[1], false);
                drawALine(middlePoint("53")[0], middlePoint("53")[1], middlePoint("63")[0], middlePoint("63")[1], false);
            }

            for (String index : relatedClues)
                gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_red));

        }
        else if (gameName.equals(getString(R.string.SayıBulmaca))){
            List<String> greenTapBoxes = new ArrayList<>();
            List<String> redTapBoxes = new ArrayList<>();
            List<String> grBoxes;
            List<String> relatedClues = new ArrayList<>();
            allowedBoxes = new ArrayList<>();
            grBoxes = new ArrayList<>();
            View.OnClickListener gocl = null;
            final View.OnClickListener[] rocl = {null};
            if (view.getTag().equals("+")) {
                if (inNum < inStrings.size() - 1){
                    inNum++;
                    view.setAlpha(1f);
                    ((LinearLayout)inTV.getParent()).findViewWithTag("-").setAlpha(1f);
                }
                if(inNum == inStrings.size()-1){
                    view.setAlpha(0.3f);
                }
            }
            else {
                if (inNum > 0){
                    inNum--;
                    view.setAlpha(1f);
                    ((LinearLayout)inTV.getParent()).findViewWithTag("+").setAlpha(1f);
                }
                if(inNum == 0){
                    view.setAlpha(0.3f);
                }
            }
            inTV.setText(inStrings.get(inNum));

            if (inNum <= 3) {
                Log.i("inNum", "<=3");
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4 - 1; j++) {
                        TextView tv = gl.findViewWithTag(Integer.toString(j) + i);
                        tv.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                    }
                    TextView rGuideTV = gl.findViewWithTag("g" + i);
                    rGuideTV.setBackground(getResources().getDrawable(R.drawable.strokebg_shallow));
                }
            }

            if (inNum == 3) {
                relatedClues = new ArrayList<>(Collections.singletonList("g1"));
            }

            if (inNum == 5) {
                relatedClues = new ArrayList<>(Arrays.asList("g0","g2"));
                redTapBoxes = new ArrayList<>(Arrays.asList("10", "32"));
                grBoxes.addAll(Arrays.asList("10","32"));
                final List<String> finalRedTapBoxes = redTapBoxes;
                rocl[0] = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for(String index : finalRedTapBoxes){
                            gl.findViewWithTag(index).clearAnimation();
                            allowedBoxes.clear();
                        }
                    }
                };
            }
            if (inNum == 6){
                relatedClues = new ArrayList<>(Arrays.asList("g0","g1","g2","g3"));
                grBoxes.addAll(Arrays.asList("10","32"));
                for(String index : grBoxes) gl.findViewWithTag(index).clearAnimation();
            }
            if (inNum == 7){
                grBoxes.addAll(Arrays.asList("10","32"));
                for(String index : grBoxes) gl.findViewWithTag(index).clearAnimation();
            }
            if (inNum == 8){
//                relatedClues = new ArrayList<>(Arrays.asList("g0","g3"));
                for (String index : Arrays.asList("00","20","33","21","13","03")) gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                grBoxes.addAll(Arrays.asList("10","32","30","31","23","00","20","33","21","13","03"));
                for(String index : grBoxes) gl.findViewWithTag(index).clearAnimation();
                greenTapBoxes = new ArrayList<>(Arrays.asList("30","31","23"));
                final List<String> finalGreenTapBoxes = greenTapBoxes;
                gocl = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for(String index : finalGreenTapBoxes){
                            gl.findViewWithTag(index).clearAnimation();
                            gl.findViewWithTag(index).setOnClickListener(null);
                        }
                        allowedBoxes = new ArrayList<>(Arrays.asList("00","20","21","03","13","33"));
                        rocl[0] = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if ("00 13".contains((String)view.getTag())){
                                    gl.findViewWithTag("00").clearAnimation();
                                    allowedBoxes.remove("00");
                                    gl.findViewWithTag("13").clearAnimation();
                                    allowedBoxes.remove("13");
                                } else if ("21 03".contains((String)view.getTag())){
                                    gl.findViewWithTag("21").clearAnimation();
                                    allowedBoxes.remove("21");
                                    gl.findViewWithTag("03").clearAnimation();
                                    allowedBoxes.remove("03");
                                }
                                else {
                                    view.clearAnimation();
                                    allowedBoxes.remove((String)view.getTag());
                                }
                            }
                        };
                        for (String index : Arrays.asList("00","20","21","03","13","33")) {
                            gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_red));
                            animateView(gl.findViewWithTag(index), 0.5f, 1.0f);
                            gl.findViewWithTag(index).setOnClickListener(rocl[0]);
                        }
                    }
                };
                ((TextView)gl.findViewWithTag("answer3")).setText("");
            }
            if (inNum==9){
                grBoxes.addAll(Arrays.asList("10","32","30","31","23","00","20","33","21","13","03"));
                for(String index : grBoxes) {
                    gl.findViewWithTag(index).clearAnimation();
                    gl.findViewWithTag(index).setOnClickListener(null);
                }
                for (String index : Arrays.asList("10","32","00","20","33","21","13","03"))
                    gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_red));
                for (String index : Arrays.asList("30","31","23"))
                    gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_bluegreen));
                ((TextView)gl.findViewWithTag("answer2")).setText("");
                ((TextView)gl.findViewWithTag("answer3")).setText("5");
                allowedBoxes.clear();
            }
            if (inNum==10){
                relatedClues = new ArrayList<>(Collections.singletonList("g1"));
                grBoxes.addAll(Arrays.asList("10","32","30","31","23","00","20","33","21","13","03","01","11","22"));
                greenTapBoxes = new ArrayList<>(Arrays.asList("01","11","22"));
                gocl = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if ("11 22".contains((String)view.getTag())){
                            gl.findViewWithTag("11").clearAnimation();
                            allowedBoxes.remove("11");
                            gl.findViewWithTag("22").clearAnimation();
                            allowedBoxes.remove("22");
                        } else {
                            view.clearAnimation();
                            allowedBoxes.remove((String)view.getTag());
                        }
                    }
                };
            }
            if (inNum==11){
                relatedClues = new ArrayList<>(Collections.singletonList("g2"));
                grBoxes.addAll(Arrays.asList("10","32","30","31","23","00","20","33","21","13","03","01","11","22"));
                ((TextView)gl.findViewWithTag("answer0")).setText("");
                ((TextView)gl.findViewWithTag("answer2")).setText("9");
                allowedBoxes.clear();
            }
            if (inNum==12){
                relatedClues = new ArrayList<>(Collections.singletonList("g1"));
                grBoxes.addAll(Arrays.asList("10","32","30","31","23","00","20","33","21","13","03","01","11","22"));
                ((TextView)gl.findViewWithTag("answer1")).setText("");
                ((TextView)gl.findViewWithTag("answer0")).setText("2");
                allowedBoxes.clear();
            }
            if (inNum==13){
                relatedClues = new ArrayList<>(Collections.singletonList("g2"));
                grBoxes.addAll(Arrays.asList("10","32","30","31","23","00","20","33","21","13","03","01","11","22","12"));
                greenTapBoxes = new ArrayList<>(Collections.singletonList("12"));
                gocl = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.clearAnimation();
                        allowedBoxes.remove((String)view.getTag());
                    }
                };
            }
            if (inNum==14){
                grBoxes.addAll(Arrays.asList("10","32","30","31","23","00","20","33","21","13","03","01","11","22","12"));
                ((TextView)gl.findViewWithTag("answer1")).setText("8");
                allowedBoxes.clear();
            }
            if (inNum>14){
                grBoxes.addAll(Arrays.asList("10","32","30","31","23","00","20","33","21","13","03","01","11","22","12"));
                allowedBoxes.clear();
            }


            List<String> unrelatedClues = new ArrayList<>(Arrays.asList("g0","g1","g2","g3"));
            unrelatedBoxes = new ArrayList<>(Arrays.asList("00","01","02","03","10","11","12","13","20","21","22","23","30","31","32","33"));
            for (String index : relatedClues) {
                gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_red));
                unrelatedClues.remove(index);
            }
            for (String index : unrelatedClues)
                gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.strokebg_shallow));
            for (String index : redTapBoxes) {
                allowedBoxes.add(index);
                gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_red));
                animateView(gl.findViewWithTag(index), 0.5f, 1.0f);
                gl.findViewWithTag(index).setOnClickListener(rocl[0]);
                unrelatedBoxes.remove(index);
            }
            for(String index : greenTapBoxes) {
                allowedBoxes.add(index);
                gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_bluegreen));
                animateView(gl.findViewWithTag(index), 0.5f, 1.0f);
                gl.findViewWithTag(index).setOnClickListener(gocl);
                unrelatedBoxes.remove(index);
            }
            for (String index : unrelatedBoxes) {
                if (!grBoxes.contains(index)) gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                gl.findViewWithTag(index).clearAnimation();
            }

        }
        else if (gameName.equals(getString(R.string.Piramit))){
            allowedBoxes = new ArrayList<>();

            if (view.getTag().equals("+")) {
                if (inNum < inStrings.size() - 1){
                    inNum++;
                    view.setAlpha(1f);
                    ((LinearLayout)inTV.getParent()).findViewWithTag("-").setAlpha(1f);
                }
                if(inNum == inStrings.size()-1){
                    view.setAlpha(0.3f);
                }
            } else {
                if (inNum > 0){
                    inNum--;
                    view.setAlpha(1f);
                    ((LinearLayout)inTV.getParent()).findViewWithTag("+").setAlpha(1f);
                }
                if(inNum == 0){
                    view.setAlpha(0.3f);
                }
            }
            inTV.setText(inStrings.get(inNum));

            final GridLayout numGL = findViewById(R.id.numsGL_ga);
            if(inNum <= 4){
                allowedBoxes.clear();
                List<String> coos = new ArrayList<>(Arrays.asList("00","answer0","answer1","02","answer2","22"));
                for(int i = 0; i < coos.size() ; i++){
                    gl.findViewWithTag(coos.get(i)).setBackground(getResources().getDrawable(R.drawable.stroke_bg2));
                }

                for(int i = 0; i<10 ; i++){
                    numGL.findViewWithTag(String.valueOf(i)).setBackground(getResources().getDrawable(R.drawable.nums_gl_bg));
                    numGL.findViewWithTag(String.valueOf(i)).clearAnimation();
                    numGL.findViewWithTag(String.valueOf(i)).setOnClickListener(null);
                }
            }
            PiramitUtils.context = this;
            if(inNum == 5){
                allowedBoxes.clear();
                ((TextView)gl.findViewWithTag("answer2")).setText("");
                ((TextView)gl.findViewWithTag("answer0")).setText("");
                ((TextView)gl.findViewWithTag("answer1")).setText("");
                gl.findViewWithTag("answer0").setBackground(getResources().getDrawable(R.drawable.stroke_bg2));
                gl.findViewWithTag("answer1").setBackground(getResources().getDrawable(R.drawable.stroke_bg2));

                PiramitUtils.clickedBox = "2";
                gl.findViewWithTag("answer2").setBackground(getResources().getDrawable(R.drawable.stroke_bg2_shallow));
                ((TextView)gl.findViewWithTag("answer2")).setTextColor(getResources().getColor(R.color.f7f5fa));

                List<String> coos = new ArrayList<>(Arrays.asList("00","answer0","answer1","02","answer2","22"));
                for(int i = 0; i < coos.size() ; i++){
                    gl.findViewWithTag(coos.get(i)).setBackground(getResources().getDrawable(R.drawable.stroke_bg2));
                    gl.findViewWithTag(coos.get(i)).clearAnimation();
                    gl.findViewWithTag(coos.get(i)).setOnClickListener(null);
                }
                for(int i = 0; i<10 ; i++){
                    numGL.findViewWithTag(String.valueOf(i)).setBackground(getResources().getDrawable(R.drawable.nums_gl_bg));
                    numGL.findViewWithTag(String.valueOf(i)).clearAnimation();
                    numGL.findViewWithTag(String.valueOf(i)).setOnClickListener(null);
                }

                View draftBox = numGL.findViewWithTag("0");
                animateView(draftBox, 0.5f, 1.0f);
                draftBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.setBackground(getResources().getDrawable(R.drawable.rounded_light_bluegreen_bg));
                        view.clearAnimation();
                        view.setOnClickListener(null);
                        PiramitUtils.draftModeActive[2] = true;
                        allowedBoxes = new ArrayList<>(Arrays.asList("1","2","9"));
                        animateView(numGL.findViewWithTag("1"),0.5f,1.0f);
                        animateView(numGL.findViewWithTag("2"),0.5f,1.0f);
                        animateView(numGL.findViewWithTag("9"),0.5f,1.0f);
                    }
                });
            }
            if (inNum == 6){
                allowedBoxes.clear();
                ((TextView)gl.findViewWithTag("answer2")).setText("1 2 9");
                ((TextView)gl.findViewWithTag("answer2")).setTextColor(getResources().getColor(R.color.light_blue_green));
                ((TextView)gl.findViewWithTag("answer2")).setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                ((TextView)gl.findViewWithTag("answer0")).setText("");
                ((TextView)gl.findViewWithTag("answer1")).setText("");
                List<String> coos = new ArrayList<>(Arrays.asList("00","answer0","answer1","02","answer2","22"));
                for(int i = 0; i < coos.size() ; i++){
                    gl.findViewWithTag(coos.get(i)).setBackground(getResources().getDrawable(R.drawable.stroke_bg2));
                    gl.findViewWithTag(coos.get(i)).clearAnimation();
                    gl.findViewWithTag(coos.get(i)).setOnClickListener(null);
                }
                for(int i = 0; i<10 ; i++){
                    numGL.findViewWithTag(String.valueOf(i)).setBackground(getResources().getDrawable(R.drawable.nums_gl_bg));
                    numGL.findViewWithTag(String.valueOf(i)).clearAnimation();
                    numGL.findViewWithTag(String.valueOf(i)).setOnClickListener(null);
                }

                PiramitUtils.clickedBox = "0";
                gl.findViewWithTag("answer2").setBackground(getResources().getDrawable(R.drawable.stroke_bg2));
                ((TextView)gl.findViewWithTag("answer2")).setTextColor(getResources().getColor(R.color.draft_grey));
                gl.findViewWithTag("answer0").setBackground(getResources().getDrawable(R.drawable.stroke_bg2_shallow));
                ((TextView)gl.findViewWithTag("answer0")).setTextColor(getResources().getColor(R.color.f7f5fa));

                numGL.findViewWithTag("0").setBackground(getResources().getDrawable(R.drawable.rounded_light_bluegreen_bg));
                PiramitUtils.draftModeActive[0] = true;
                final int[] counter = {0};
                View.OnClickListener ocl = new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(View view) {
                        counter[0]++;
                        allowedBoxes = new ArrayList<>(Arrays.asList("3","5","2","6"));
                        numClicked(view);
//                        allowedBoxes.remove((String)view.getTag());
                        view.clearAnimation();
                        view.setOnClickListener(null);
                        if(counter[0] == 4){
                            PiramitUtils.clickedBox = "1";
                            gl.findViewWithTag("answer0").setBackground(getResources().getDrawable(R.drawable.stroke_bg2));
                            ((TextView)gl.findViewWithTag("answer0")).setTextColor(getResources().getColor(R.color.draft_grey));
                            gl.findViewWithTag("answer1").setBackground(getResources().getDrawable(R.drawable.stroke_bg2_shallow));
                            ((TextView)gl.findViewWithTag("answer1")).setTextColor(getResources().getColor(R.color.f7f5fa));

                            PiramitUtils.draftModeActive[1] = true;
                            allowedBoxes = new ArrayList<>(Arrays.asList("6","8","5","9","2"));
                            for (View v : Arrays.asList(numGL.findViewWithTag("6"),numGL.findViewWithTag("8"),numGL.findViewWithTag("5"),numGL.findViewWithTag("9"),numGL.findViewWithTag("2"))){
                                v.setOnClickListener(new View.OnClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                    @Override
                                    public void onClick(View view) {
                                        numClicked(view);
                                    }
                                });
                                animateView(v,0.5f,1.0f);
                            }
                        }
                    }
                };
                for (View v : Arrays.asList(numGL.findViewWithTag("3"),numGL.findViewWithTag("5"),numGL.findViewWithTag("2"),numGL.findViewWithTag("6"))){
                    animateView(v,0.5f,1.0f);
                    v.setOnClickListener(ocl);
                }
            }
            if (inNum == 7 || inNum == 8){
                allowedBoxes.clear();
                ((TextView)gl.findViewWithTag("answer2")).setText("1 2 9");
                ((TextView)gl.findViewWithTag("answer0")).setText("3 5 2 6");
                ((TextView)gl.findViewWithTag("answer1")).setText("6 8 5 9 2");
                ((TextView)gl.findViewWithTag("answer2")).setTextColor(getResources().getColor(R.color.draft_grey));
                ((TextView)gl.findViewWithTag("answer0")).setTextColor(getResources().getColor(R.color.draft_grey));
                ((TextView)gl.findViewWithTag("answer1")).setTextColor(getResources().getColor(R.color.draft_grey));
                ((TextView)gl.findViewWithTag("answer0")).setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                ((TextView)gl.findViewWithTag("answer1")).setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                ((TextView)gl.findViewWithTag("answer2")).setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);

                List<String> coos = new ArrayList<>(Arrays.asList("00","answer0","answer1","02","answer2","22"));
                for(int i = 0; i < coos.size() ; i++){
                    gl.findViewWithTag(coos.get(i)).setBackground(getResources().getDrawable(R.drawable.stroke_bg2));
                    gl.findViewWithTag(coos.get(i)).clearAnimation();
                    gl.findViewWithTag(coos.get(i)).setOnClickListener(null);
                }
                for(int i = 0; i<10 ; i++){
                    numGL.findViewWithTag(String.valueOf(i)).setBackground(getResources().getDrawable(R.drawable.nums_gl_bg));
                    numGL.findViewWithTag(String.valueOf(i)).clearAnimation();
                    numGL.findViewWithTag(String.valueOf(i)).setOnClickListener(null);
                }
            }
            if(inNum == 8){
                final TextView answer0 = gl.findViewWithTag("answer0");
                final TextView answer1 = gl.findViewWithTag("answer1");
                final TextView answer2 = gl.findViewWithTag("answer2");
                final View delete = findViewById(R.id.deleteTV_guide);
                gl.findViewWithTag("answer2").setBackground(getResources().getDrawable(R.drawable.stroke_bg2_shallow));
                ((TextView)gl.findViewWithTag("answer2")).setTextColor(getResources().getColor(R.color.f7f5fa));
                animateView(answer2,0.5f,1.0f);
                answer2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.clearAnimation();
                        view.setOnClickListener(null);
                        animateView(delete, 0.5f, 1.0f);
                        delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                view.clearAnimation();
                                view.setOnClickListener(null);
                                answer2.setText("");
                                PiramitUtils.draftModeActive[2] = false;
                                answer2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                                answer2.setTextColor(getResources().getColor(R.color.light_red));
                                animateView(numGL.findViewWithTag("1"),0.5f,1.0f);
                                numGL.findViewWithTag("1").setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        view.clearAnimation();
                                        view.setOnClickListener(null);
                                        answer2.setText("1");
                                        gl.findViewWithTag("answer0").setBackground(getResources().getDrawable(R.drawable.stroke_bg2_shallow));
                                        ((TextView)gl.findViewWithTag("answer0")).setTextColor(getResources().getColor(R.color.f7f5fa));
                                        gl.findViewWithTag("answer2").setBackground(getResources().getDrawable(R.drawable.stroke_bg2));
                                        animateView(answer0,0.5f,1.0f);
                                        answer0.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                view.clearAnimation();
                                                view.setOnClickListener(null);
                                                animateView(delete, 0.5f, 1.0f);
                                                delete.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        view.clearAnimation();
                                                        view.setOnClickListener(null);
                                                        answer0.setText("");
                                                        PiramitUtils.draftModeActive[0] = false;
                                                        answer0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                                                        answer0.setTextColor(getResources().getColor(R.color.light_red));
                                                        animateView(numGL.findViewWithTag("3"),0.5f,1.0f);
                                                        numGL.findViewWithTag("3").setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                view.clearAnimation();
                                                                view.setOnClickListener(null);
                                                                answer0.setText("3");
                                                                gl.findViewWithTag("answer1").setBackground(getResources().getDrawable(R.drawable.stroke_bg2_shallow));
                                                                ((TextView)gl.findViewWithTag("answer1")).setTextColor(getResources().getColor(R.color.f7f5fa));
                                                                gl.findViewWithTag("answer0").setBackground(getResources().getDrawable(R.drawable.stroke_bg2));
                                                                animateView(answer1,0.5f,1.0f);
                                                                answer1.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        view.clearAnimation();
                                                                        view.setOnClickListener(null);
                                                                        animateView(delete, 0.5f, 1.0f);
                                                                        delete.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View view) {
                                                                                view.clearAnimation();
                                                                                view.setOnClickListener(null);
                                                                                answer1.setText("");
                                                                                PiramitUtils.draftModeActive[1] = false;
                                                                                answer1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                                                                                answer1.setTextColor(getResources().getColor(R.color.light_red));
                                                                                animateView(numGL.findViewWithTag("8"), 0.5f, 1.0f);
                                                                                numGL.findViewWithTag("8").setOnClickListener(new View.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(View view) {
                                                                                        view.clearAnimation();
                                                                                        view.setOnClickListener(null);
                                                                                        answer1.setText("8");
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
            if(inNum == 9){
                allowedBoxes.clear();
                List<String> coos = new ArrayList<>(Arrays.asList("00","answer0","answer1","02","answer2","22"));
                for(int i = 0; i < coos.size() ; i++){
                    gl.findViewWithTag(coos.get(i)).setBackground(getResources().getDrawable(R.drawable.stroke_bg2));
                    gl.findViewWithTag(coos.get(i)).clearAnimation();
                }
                for(int i = 0; i<10 ; i++){
                    numGL.findViewWithTag(String.valueOf(i)).setBackground(getResources().getDrawable(R.drawable.nums_gl_bg));
                    numGL.findViewWithTag(String.valueOf(i)).clearAnimation();
                }
                ((TextView)gl.findViewWithTag("answer0")).setText("3");
                ((TextView)gl.findViewWithTag("answer1")).setText("8");
                ((TextView)gl.findViewWithTag("answer2")).setText("1");
                ((TextView)gl.findViewWithTag("answer2")).setTextColor(getResources().getColor(R.color.light_red));
                ((TextView)gl.findViewWithTag("answer0")).setTextColor(getResources().getColor(R.color.light_red));
                ((TextView)gl.findViewWithTag("answer1")).setTextColor(getResources().getColor(R.color.light_red));
                ((TextView)gl.findViewWithTag("answer0")).setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                ((TextView)gl.findViewWithTag("answer1")).setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                ((TextView)gl.findViewWithTag("answer2")).setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            }
        }
    }

    public void setInStrings() {
        if (gameName.contains(getString(R.string.HazineAvı))) {
            inStrings.add(getString(R.string.hazineAvi_instrings1));
            inStrings.add(getString(R.string.hazineAvi_instrings2));
            inStrings.add(getString(R.string.hazineAvi_instrings3));
            inStrings.add(getString(R.string.hazineAvi_instrings4));
            inStrings.add(getString(R.string.hazineAvi_instrings5));
            inStrings.add(getString(R.string.hazineAvi_instrings6));
            inStrings.add(getString(R.string.hazineAvi_instrings7));
            inStrings.add(getString(R.string.hazineAvi_instrings8));
            inStrings.add(getString(R.string.hazineAvi_instrings9));
            inStrings.add(getString(R.string.hazineAvi_instrings10));
            inStrings.add(getString(R.string.hazineAvi_instrings11));
            inStrings.add(getString(R.string.hazineAvi_instrings12));
            inStrings.add(getString(R.string.hazineAvi_instrings13));
            inStrings.add(getString(R.string.hazineAvi_instrings14));
            inStrings.add(getString(R.string.hazineAvi_instrings15));
            inStrings.add(getString(R.string.hazineAvi_instrings16));
            inStrings.add(getString(R.string.hazineAvi_instrings17));
            inStrings.add(getString(R.string.hazineAvi_instrings18));
            inStrings.add(getString(R.string.hazineAvi_instrings19));
        }
        else if (gameName.equals(getString(R.string.Patika))){
            inStrings.add("Patika öğretici uygulamasına hoşgeldiniz. Öğretici boyunca animasyonlarla gösterilen çizgilerin üzerinden geçerek kendiniz de çözüme dahil olabilirsiniz.");
            inStrings.add("Patika oyununda amaç tüm boş karelerden geçen, kendini kesmeyen tek bir kapalı yol oluşturmaktır.");
            inStrings.add("Bir patika sorusu çözerken temelde 2 teknik vardır: Köşe bulma ve kapalı alan oluşmamasına dikkat etme.");
            inStrings.add("Köşe bulma tekniğinde; eğer bir kutunun iki kenar komşusu kapalı, yani yol çizilemeyecek, ise kapalı olmayan diğer iki kenar komşusuna doğru bir köşe/kenar çizilir.");
            inStrings.add("Örneğin sol üst kutunun üst ve sol kenar komşuları kapalıdır yani bu kutudan üste veya sola doğru bir yol çizilemez. Dolayısıyla aşağı ve sağa doğru bir köşe çizilmelidir.");
            inStrings.add("Benzer şekilde iki kenar komşusu kapalı diğer kutuların içine çizilmesi gereken köşeleri/kenarları çiziniz.");
            inStrings.add("2. çözüm tekniğinde; patikanın tek bir kapalı yol oluşması kuralına dayanarak, tüm kutuları kaplamayan küçük bir kapalı alan oluşması engellenir.");
            inStrings.add("Örneğin kırmızıyla işaretlenmiş kutuda yol ya sağ kutuya ya da yukarıya gidebilir. Eğer yukarıya giderse küçük bir kapalı yol oluşacağından sağa gitmek zorundadır.");
            inStrings.add("Benzer şekilde kapalı alan oluşmasını engelleme yöntemiyle, gösterilen çizgileri çiziniz.");
            inStrings.add("Bu öğreticinin sonuna geldiniz.\uD83C\uDFC1 Sol üstteki geri butonundan çıkabilir veya ok tuşlarıyla önceki adımlara dönebilirsiniz.");
        }
        else if (gameName.equals(getString(R.string.SayıBulmaca))){
            inStrings.add("Sayı Bulmaca öğretici uygulamasına hoşgeldiniz. Öğretici boyunca yanıp sönen butonlara tıklayarak kendiniz de çözüme dahil olabilirsiniz.");
            inStrings.add("Sayı Bulmaca oyununda sağ tarafta verilen rakamlar o satırdaki rakamların kaç tanesinin cevabın içinde geçtiğini gösterir.");
            inStrings.add("Yanında '+' işareti olan rakamlar o satırdaki rakam yada rakamların cevabın içinde aynı sütunda bulunduklarını, Yanında '-' işareti olanlar ise farklı sütunda olduğunu gösterir.");
            inStrings.add("Örneğin işaretli ipucunun olduğu satırda 2 adet sayı cevapta var ve yeri doğruyken 1 adet sayının yeri farklıdır.");
            inStrings.add("Sayı Bulmaca çözerken ilk dikkat edilmesi gereken ipuçları, + olan ipuçlarıdır. '+' bulunan iki farklı satırda bir sayı farklı yerlerde bulunuyorsa o sayı elenebilir.");
            inStrings.add("Örneğin işaretli ipuçlarının olduğu satırlarda 0 rakamı ortaktır ve iki ipucunda da yalnızca '+' bulunmaktadır. 0 rakamı farklı yerlerde bulunduğu için ve iki yerde aynı anda bulunamayacağı için elenir.");
            inStrings.add("Daha sonra, toplam ipucu sayısına bakılmalıdır. Örnekte bu sayı 1+3+2+1=7 dir. Yani, 7 ipucudan 4 sayı oluşacaktır. Bu durum, bazı sayıların satırlarda ortak olması gerektiğini gösterir.");
            inStrings.add("Toplam ipucu sayısı 7 gibi 4x4 lük bir Sayı bulmaca sorusu için orta-yüksek bir sayı ise çok satırda ortak olan sayılardan gidilmelidir. Bu sayı az olsaydı ortak olmayan sayılardan gidilebilirdi.");
            inStrings.add("Örnekte görüleceği üzere 5 sayısı üç satırda ortaktır. Bu sayıya basarak yeşil gölgeli duruma getirebilirsiniz. Daha sonra, ipucu hakkını dolduran satırlardaki kalan sayıları eleyebiliriz.");
            inStrings.add("İlk satırda ipucu +1 ve 5 sayısı yeşil renklidir. Dolayısıyla 5 sayısı cevapta vardır ve yeri doğrudur. Cevabın son basamağına 5 yazılmalıdır.");
            inStrings.add("Sayılar elendiğinde, 2.satırda olabilecek 2 sayı kalır ve ipucuda da 2 sayı kaldığı için bu sayılar kesin olarak cevapta yer alacaktır. Bu sayılar yeşille işaretlenir.");
            inStrings.add("3.satırda yeşil ile işaretlenen 9 sayısının yeri (ipucu +2 olduğu için) doğru olduğu söylenebilir. Cevabın 3.basamağına 9 yazılmalıdır.");
            inStrings.add("2.satırda yeşil ile işaretlenen 5 sayısının yeri doğru, 9 sayısının yeri yanlış olduğu görülmektedir. Yani ipucudan geriye yalnızca +1 kalmıştır. Dolayısıyla 2 sayısının yeri doğrudur ve ilk basamaktadır.");
            inStrings.add("Geriye yalnızca 2.basamaktaki sayı kalmıştır. 3.satırda 9 sayısının yeri doğrudur ve ipucudan geriye +1 kalmıştır. Boş olan basamak yalnızca 2.basamak olduğu için 3.satırdaki 8 sayısı cevapta olmalıdır.");
            inStrings.add("Çözüm sona erdiğinde cevap 2895 olarak bulunur. Sayı Bulmaca soruları genel olarak bu yöntemlerle çözülebilir. Ancak çözer bir yerde tıkanırsa, en çok satırda ortak olan sayılardan deneme-yanılma ile gidilebilir.");
            inStrings.add("Bu öğreticinin sonuna geldiniz.\uD83C\uDFC1 Sol üstteki geri butonundan çıkabilir veya ok tuşlarıyla önceki adımlara dönebilirsiniz.");
        }
        else if (gameName.equals(getString(R.string.Piramit))){
            inStrings.add("Piramit öğretici uygulamasına hoşgeldiniz. Öğretici boyunca işaretli butonlara tıklayarak kendiniz de çözüme dahil olabilirsiniz.");
            inStrings.add("Piramit oyununda çözerden istenen piramitteki boş kutuları kurallı bir şekilde doldurmasıdır. Oyunun kuralları şöyledir: 1. Her iki yan yana sayının toplamı ya da farkı üstündeki sayıya eşittir.");
            inStrings.add("2. Aynı satırda rakam tekrarı olamaz. 3. Yan yana olan sayılar ardışık olamaz. Bu 3 kurala dikkat edilerek piramit soruları çözülür. Piramit çözerken kullanılan en yaygın yöntem eleme yöntemidir.");
            inStrings.add("Örnekte 3 satırlık bir piramit sorusu verilmiştir. Örneğin, bu piramitte en alt satırdaki boş kutudan başlanabilir.");
            inStrings.add("En alt satırda 4 ve 7 ipuçları verilmiştir. Ardışık olmama ve tekrarlamama kurallarından yola çıkılarak boş kutunun 3,4,5,6,7 ve 8 sayılarını alamayacağı söylenebilir.");
            inStrings.add("Dolayısıyla bu kutuya yalnızca 1,2 veya 9 sayıları yazılabilir. Sağ alttaki not alma moduna geçilerek bu ihtimaller not alınabilir. Daha sonra bu ihtimallerin tek tek ipuçlarıyla farkı ve toplamı alınabilir.");
            inStrings.add("Örneğin 1,2 ve 9 ihtimalleri üstündeki kutuda sırasıyla (3,5), (2,6) ve 5 ihtimallerini ortaya çıkarır. Aynı şekilde yanındaki kutuda da sırasıyla (6,8), (5,9) ve 2 ihtimalleri oluşur.");
            inStrings.add("Bu ihtimaller akıldan bulunabilir veya not alınabilir. Son bir adım olarak bu ihtimallerden hangisinin farkı veya toplamının üstteki sayıyı yani 5'i verdiği bulunmalıdır.");
            inStrings.add("İhtimaller incelendiğinde 3 ve 8 sayılarının farkının 5 olduğu görülebilir. Dolayısıyla bu ihtimalleri veren 1 ihtimali de doğrudur. Sağlama yapıldığında piramidin kurallara uyduğu görülür.");
            inStrings.add("Bu öğreticinin sonuna geldiniz.\uD83C\uDFC1 Sol üstteki geri butonundan çıkabilir veya ok tuşlarıyla önceki adımlara dönebilirsiniz.");
        }
    }

    public void animateView(View view,float s1, float s2){
        Animation anim = new AlphaAnimation(s1, s2);
        anim.setDuration(500);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        view.startAnimation(anim);
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    public void createGridAndPlace(Object grid) {
        if (gameName.contains(getString(R.string.HazineAvı))) {
            List<ArrayList<Integer>> lGrid = (List<ArrayList<Integer>>) grid;
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    String n = lGrid.get(i).get(j).toString();
                    if (Integer.parseInt(n) > 0) {
                        ((TextView) gl.findViewWithTag(Integer.toString(j) + i)).setText(n);
                        gl.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                        gridDCs[j][i] = n;
                    } else if (Integer.parseInt(n) == 0) {
                        gl.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                        gridDCs[j][i] = "0";
                    } else if (Integer.parseInt(n) == -1) {
                        gl.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.ic_diamond));
                        gridDCs[j][i] = "-1";
                    } else {
                        gl.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.ic_cross));
                        gridDCs[j][i] = "-2";
                    }
                    gl.findViewWithTag(Integer.toString(j) + i).clearAnimation();
                }
            }
        }
        else if (gameName.equals(getString(R.string.Patika))){
            for(String cos: blackList){
                gl.findViewWithTag(cos).setBackground(getResources().getDrawable(R.color.near_black_blue));
            }
        }
        else if (gameName.equals(getString(R.string.SayıBulmaca))){
            JSONArray jGrid = (JSONArray) grid;
            Log.i("jsonGrid",""+grid);
            for (int i = 0; i < jGrid.length()-1; i++){
                try {
                    JSONArray row = (JSONArray) jGrid.get(i);
                    for (int j = 0; j< row.length()-1; j++){
                        TextView tv = gl.findViewWithTag(Integer.toString(j)+ i);
                        tv.setText(row.get(j).toString());
                    }
                    JSONArray rguide = (JSONArray)row.get(row.length()-1);
                    TextView rGuideTV = gl.findViewWithTag("g"+i);
                    if(rguide.get(0).toString().equals("0")){
                        rGuideTV.setText(rguide.get(1).toString());
                    }
                    else if(rguide.get(1).toString().equals("0")){
                        rGuideTV.setText("+"+rguide.get(0).toString());
                    }
                    else{
                        rGuideTV.setText("+"+rguide.get(0).toString()+"  "+rguide.get(1).toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            TextView guideanswer = gl.findViewWithTag("answerguide");
            guideanswer.setText("+4");
        }
        else if (gameName.equals(getString(R.string.Piramit))){
            List<String> coos = new ArrayList<>(Arrays.asList("00","answer0","answer1","02","answer2","22"));
            List<String> gridl = (ArrayList<String>)grid;
            for(int i = 0; i < coos.size() ; i++){
                ((TextView) gl.findViewWithTag(coos.get(i))).setText(gridl.get(i));
            }
        }
    }

    public void initSomeVar() {
        pxHeight = (int) (300 * getResources().getDisplayMetrics().density);
        bitmap = Bitmap.createBitmap(pxHeight, pxHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(getResources().getColor(R.color.transparent));
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.near_black_blue));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float) pxHeight / 75);
        paint.setAntiAlias(true);
    }

    public String[] xyToRowColumn(float x, float y){
        String[] rowColumn = new String[2];
        float coef = 0;
        try {
            coef = (float)pxHeight/7;
        } catch (Exception e) {
            e.printStackTrace();
        }
        rowColumn[0] = Integer.toString((int) Math.floor(x/coef));
        rowColumn[1] = Integer.toString((int) Math.floor(y/coef));
        return rowColumn;
    }

    public int[] middlePoint(String coor){
        int x = Integer.parseInt(String.valueOf(coor.charAt(0)));
        int y = Integer.parseInt(String.valueOf(coor.charAt(1)));
        int[] middle_point = new int[2];
        float coef = 0;
        try {
            coef = (float)pxHeight/7;
        } catch (Exception e) {
            e.printStackTrace();
        }
        middle_point[0] = (int) (coef*x+coef/2);
        middle_point[1] = (int) (coef*y+coef/2);
        return middle_point;
    }

    public void drawALine(float startX, float startY, float stopX, float stopY, boolean erasing){
        ImageView imageView = findViewById(R.id.canvasIV);
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
            int offset1 = pxHeight / 120;
            int offset2 = pxHeight / 120;
            if (startY - stopY == 0) {
                if (startX < stopX)
                    canvas.drawLine(startX - offset1, startY, stopX + offset2, stopY, paint);
                else canvas.drawLine(startX + offset1, startY, stopX - offset2, stopY, paint);
            } else {
                if (startY < stopY)
                    canvas.drawLine(startX, startY - offset1, stopX, stopY + offset2, paint);
                else canvas.drawLine(startX, startY + offset1, stopX, stopY - offset2, paint);
            }
        }
        imageView.setImageBitmap(bitmap);
    }

    public boolean lineCanBeDrawn(String currentC, String previousC){
        return (!currentC.equals(previousC)
                && (
                (currentC.charAt(0) == previousC.charAt(0)
                        && Math.abs(Integer.parseInt(String.valueOf(previousC.charAt(1))) - Integer.parseInt(String.valueOf(currentC.charAt(1)))) == 1)
                        ||      (currentC.charAt(1) == previousC.charAt(1)
                        && Math.abs(Integer.parseInt(String.valueOf(previousC.charAt(0))) - Integer.parseInt(String.valueOf(currentC.charAt(0)))) == 1)
                ));
    }

    public void eraseLine(String co1, String co2){
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paint.setStrokeWidth((float)pxHeight/60);
        drawALine(middlePoint(co1)[0], middlePoint(co1)[1],middlePoint(co2)[0], middlePoint(co2)[1], true);
        paint.setXfermode(null);
        paint.setStrokeWidth((float)pxHeight/75);
    }

    public void animateLine(final String co1, final String co2){
        final Handler handler = new Handler();
        Runnable run;
        final int step = (int) (2*getResources().getDisplayMetrics().density);
        if(co1.charAt(0) == co2.charAt(0)){
            final int[] i = {middlePoint(co1)[1]};
            if(Integer.parseInt(String.valueOf(co1.charAt(1))) > Integer.parseInt(String.valueOf(co2.charAt(1)))) {
                run = new Runnable() {
                    @Override
                    public void run() {
                        if(allowedBoxes.contains(co1)&&allowedBoxes.contains(co2)) {
                            i[0] -= step;
                            drawALine(middlePoint(co1)[0], i[0] + step, middlePoint(co2)[0], i[0], false);
                            if (i[0] <= middlePoint(co2)[1] + step) handler.removeCallbacks(this);
                            else handler.postDelayed(this, 10);
                        }
                    }
                };
            }
            else {
                run = new Runnable() {
                    @Override
                    public void run() {
//                        Log.i("Running"," Every seconds");
                        if(allowedBoxes.contains(co1)&&allowedBoxes.contains(co2)) {
                            i[0] += step;
                            drawALine(middlePoint(co1)[0], i[0] - step, middlePoint(co2)[0], i[0], false);
                            if (i[0] >= middlePoint(co2)[1] - step) handler.removeCallbacks(this);
                            else handler.postDelayed(this, 10);
                        }
                    }
                };
            }
        }
        else {
            final int[] i = {middlePoint(co1)[0]};
            if(Integer.parseInt(String.valueOf(co1.charAt(0))) > Integer.parseInt(String.valueOf(co2.charAt(0)))) {
                run = new Runnable() {
                    @Override
                    public void run() {
//                        Log.i("Running"," Every seconds");
                        if(allowedBoxes.contains(co1)&&allowedBoxes.contains(co2)) {
                            i[0] -= step;
                            drawALine(i[0] + step, middlePoint(co1)[1], i[0], middlePoint(co2)[1], false);
                            if (i[0] <= middlePoint(co2)[0] + step) handler.removeCallbacks(this);
                            else handler.postDelayed(this, 10);
                        }
                    }
                };
            } else {
                run = new Runnable() {
                    @Override
                    public void run() {
//                        Log.i("Running"," Every seconds");
                        if(allowedBoxes.contains(co1)&&allowedBoxes.contains(co2)) {
                            i[0] += step;
                            drawALine(i[0] - step, middlePoint(co1)[1], i[0], middlePoint(co2)[1], false);
                            if (i[0] >= middlePoint(co2)[0] - step) handler.removeCallbacks(this);
                            else handler.postDelayed(this, 10);
                        }
                    }
                };
            }
        }
        handler.post(run);
    }

    public void pathAnimation1(final String co1, final String co2, final int runIndex){
        animator = new Handler();
        runnables[runIndex] = new Runnable() {
            @Override
            public void run() {
                if(allowedBoxes.size()>0) animateLine(co1,co2);
                else animator.removeCallbacks(runnables[runIndex]);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(allowedBoxes.contains(co1)&&allowedBoxes.contains(co2)) eraseLine(co1, co2);
                        else animator.removeCallbacks(runnables[runIndex]);
                    }
                },42*10+50);
                animator.postDelayed(this,42*10+100);
            }
        };
        animator.post(runnables[runIndex]);
    }

    public void pathAnimation2(final String co11, final String co12, final String co21, final String co22, final int runIndex){
        animator = new Handler();
        runnables[runIndex] = new Runnable() {
            @Override
            public void run() {
                if(allowedBoxes.size()>0) animateLine(co11,co12);
                else animator.removeCallbacks(runnables[runIndex]);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(allowedBoxes.size()>0) animateLine(co21,co22);
                        else animator.removeCallbacks(runnables[runIndex]);

                    }
                },42*10);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(allowedBoxes.contains(co11)&&allowedBoxes.contains(co12)&&allowedBoxes.contains(co21)&&allowedBoxes.contains(co22)) {
                            eraseLine(co11, co12);
                            eraseLine(co21, co22);
                        } else{
                            if((allowedBoxes.contains(co11)&&allowedBoxes.contains(co12))){
                                eraseLine(co11, co12);
                            } else if (allowedBoxes.contains(co21)&&allowedBoxes.contains(co22)){
                                eraseLine(co21,co22);
                            } else {
                                animator.removeCallbacks(runnables[runIndex]);
                            }
                        }

                    }
                },42*20+100);
                animator.postDelayed(this,42*20+100);
            }
        };
        animator.post(runnables[runIndex]);
    }

    public void addAnim(String co1,String co2, String co3, int runIndex, boolean isTwo){
        allowedBoxes.clear();
        allowedBoxes.add(co1);
        allowedBoxes.add(co2);
        if(isTwo) {
            allowedBoxes.add(co2);
            allowedBoxes.add(co3);
            pathAnimation2(co1, co2, co2, co3, runIndex);
        } else{
            pathAnimation1(co1,co2,runIndex);
        }
    }

    public void in5next(){
        if(inNum==5) {
            if (counterIn5 == 1) addAnim("40", "50", "51", counterIn5 + 1, true);
            else if (counterIn5 == 2) addAnim("51", "61", "62", counterIn5 + 1, true);
            else if (counterIn5 == 3) addAnim("63", "64", "65", counterIn5 + 1, true);
            else if (counterIn5 == 4) addAnim("65", "66", "56", counterIn5 + 1, true);
            else if (counterIn5 == 5) addAnim("56", "55", "45", counterIn5 + 1, true);
            else if (counterIn5 == 6) addAnim("45", "44", "43", counterIn5 + 1, true);
            else if (counterIn5 == 7) addAnim("16", "06", "05", counterIn5 + 1, true);
            else if (counterIn5 == 8) addAnim("05", "15", "", counterIn5 + 1, false);
            else if (counterIn5 == 9) addAnim("26", "36", "35", counterIn5 + 1, true);
            else if (counterIn5 == 10) addAnim("35", "25", "", counterIn5 + 1, false);
            else if (counterIn5 == 11) addAnim("31", "41", "42", counterIn5 + 1, true);
            else if (counterIn5 == 12) addAnim("01", "02", "03", counterIn5 + 1, true);
            else if (counterIn5 == 13) addAnim("03", "13", "", counterIn5 + 1, false);
            else if (counterIn5 == 14) addAnim("10", "11", "21", counterIn5 + 1, true);
            else if (counterIn5 == 15) addAnim("21", "22", "", counterIn5 + 1, false);
        }
        else if (inNum==8){
            if (counterIn5 == 1) addAnim("25", "24", "", counterIn5 + 18, false);
            else if (counterIn5 == 2) addAnim("14", "13", "", counterIn5 + 18, false);
            else if (counterIn5 == 3) addAnim("24", "23", "", counterIn5 + 18, false);
            else if (counterIn5 == 4) addAnim("23", "33", "", counterIn5 + 18, false);
            else if (counterIn5 == 5) addAnim("22", "32", "", counterIn5 + 18, false);
            else if (counterIn5 == 6) addAnim("32", "42", "", counterIn5 + 18, false);
            else if (counterIn5 == 7) addAnim("33", "43", "", counterIn5 + 18, false);
            else if (counterIn5 == 8) addAnim("62", "52", "", counterIn5 + 18, false);
            else if (counterIn5 == 9) addAnim("52", "53", "", counterIn5 + 18, false);
            else if (counterIn5 == 10) addAnim("53", "63", "", counterIn5 + 18, false);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utilsMap = new HashMap<>();
        utilsMap.put("Sudoku", SudokuUtils.class);
        utilsMap.put(getString(R.string.HazineAvı), HazineAviUtils.class);
        utilsMap.put(getString(R.string.Patika), PatikaUtils.class);
        utilsMap.put(getString(R.string.SayıBulmaca), SayiBulmacaUtils.class);
        utilsMap.put(getString(R.string.SözcükTuru), SozcukTuruUtils.class);
        utilsMap.put(getString(R.string.Piramit), PiramitUtils.class);

        Intent intent = getIntent();
        gameName = intent.getStringExtra("gameName");
        type = intent.getStringExtra("type");

        try {
            utilsMap.get(gameName).getDeclaredMethod("initVars", AppCompatActivity.class).invoke(null,this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert gameName != null;
        if(gameName.contains(getString(R.string.HazineAvı))) {
            setContentView(R.layout.activity_game_guide_hazine_avi);
            inTV = findViewById(R.id.instructionTV_guide);
            gl = findViewById(R.id.gridGL_guide);
            setInStrings();
            inTV.setText(inStrings.get(0));
            ((LinearLayout)inTV.getParent()).findViewWithTag("-").setAlpha(0.3f);
            switchIV = findViewById(R.id.switchIV);
            arrow = findViewById(R.id.arrowIV_guide);
            createGridAndPlace(new ArrayList<>(Arrays.asList(
                    new ArrayList<>(Arrays.asList(0, 0, 2, 4, 0)),
                    new ArrayList<>(Arrays.asList(1, 0, 0, 0, 0)),
                    new ArrayList<>(Arrays.asList(0, 3, 0, 0, 0)),
                    new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)),
                    new ArrayList<>(Arrays.asList(1, 0, 0, 2, 0)))));
        }
        else if (gameName.equals(getString(R.string.Patika))) {
            setContentView(R.layout.activity_game_guide_patika);
            blackList = new ArrayList<>(Arrays.asList("20","60","12","04","34","46","54"));
            inTV = findViewById(R.id.instructionTV_guide);
            gl = findViewById(R.id.gridGL_guide);
            setInStrings();
            inTV.setText(inStrings.get(0));
            ((LinearLayout)inTV.getParent()).findViewWithTag("-").setAlpha(0.3f);
            createGridAndPlace(null);
            initSomeVar();
            gl.setOnTouchListener(new View.OnTouchListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    float mx = motionEvent.getX();
                    float my = motionEvent.getY();
                    Log.i("x / y",mx + " / " + my);
                    if(mx >= 0 && mx <= pxHeight && my >= 0 && my <= pxHeight){
                        switch (motionEvent.getAction()){
                            case MotionEvent.ACTION_DOWN:
                                rowColumn = xyToRowColumn(mx,my);
                                previousCoor = rowColumn[0] + rowColumn[1];
                                is_moving=false;
                                break;
                            case MotionEvent.ACTION_MOVE:
                                rowColumn = xyToRowColumn(mx,my);
                                String currentCoor = rowColumn[0] + rowColumn[1];
                                if(allowedBoxes.contains(currentCoor)&&allowedBoxes.contains(previousCoor) && lineCanBeDrawn(currentCoor, previousCoor)) {
                                    allowedBoxes.remove(currentCoor);
                                    allowedBoxes.remove(previousCoor);
                                    is_moving = true;
                                    int[] firstMP = middlePoint(previousCoor);
                                    int[] secondMP = middlePoint(currentCoor);
                                    drawALine(firstMP[0], firstMP[1], secondMP[0], secondMP[1], false);
                                    if((inNum==5 || inNum==8) && allowedBoxes.size()==0){
                                        counterIn5++;
                                        in5next();
                                    }
                                }
                                previousCoor = currentCoor;

                                break;
                            case MotionEvent.ACTION_UP:
                                if(!is_moving){
                                    Log.i("Pressed","Pressed");
                                }
                                break;
                        }
                    }

                    return true;
                }
            });
        }
        else if (gameName.equals(getString(R.string.SayıBulmaca))) {
            setContentView(R.layout.activity_game_guide_sayibulmaca);
            inTV = findViewById(R.id.instructionTV_guide);
            gl = findViewById(R.id.gridGL_guide);
            setInStrings();
            inTV.setText(inStrings.get(0));
            ((LinearLayout)inTV.getParent()).findViewWithTag("-").setAlpha(0.3f);
            try {
                String js = "[[4,0,6,5,[1,0]],[2,9,3,5,[2,-1]],[1,8,9,0,[2,0]],[3,4,5,7,[0,-1]],[2,8,9,5,[4,0]]]";
                createGridAndPlace(new JSONArray(js));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if (gameName.equals(getString(R.string.Piramit))){
            setContentView(R.layout.activity_game_guide_piramit);
            inTV = findViewById(R.id.instructionTV_guide);
            gl = findViewById(R.id.gridGL_guide);
            setInStrings();
            inTV.setText(inStrings.get(0));
            ((LinearLayout)inTV.getParent()).findViewWithTag("-").setAlpha(0.3f);
            createGridAndPlace(new ArrayList<>(Arrays.asList("5","","","4","","7")));
        }

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        goBackToHTP(null);
    }
}