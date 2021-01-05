package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GameGuideActivity extends AppCompatActivity {

    String clickedBox = "-1";
    String switchPosition = "diamond";
    int inNum = 0;
    boolean gameFinished=false;


    List<String> allowedBoxes = new ArrayList<>();
    List<String> inStrings = new ArrayList<>();
    TextView inTV;
    GridLayout gl;

    public void goBackToHTP(View view){

        Intent intent = new Intent(getApplicationContext(), HowToPlayActivity.class);
        intent.putExtra("gameName", "Hazine Avı");
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);

//        LayoutInflater factory = LayoutInflater.from(this);
//        final View leaveDialogView = factory.inflate(R.layout.leave_dialog, null);
//        final AlertDialog leaveDialog = new AlertDialog.Builder(this).create();
//        leaveDialog.setView(leaveDialogView);
//
//        leaveDialogView.findViewById(R.id.leaveDialogYes).setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
//                startActivity(intent);
//                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
//                leaveDialog.dismiss();
//            }
//        });
//        leaveDialogView.findViewById(R.id.leaveDialogNo).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                leaveDialog.dismiss();
//            }
//        });
//        leaveDialog.show();
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

    public void changeSwitch(View view){
        ImageView switchTV = (ImageView) view;
        if(switchPosition.equals("diamond")){
            switchTV.setImageResource(R.drawable.ic_cross);
            switchPosition = "cross";
        }
        else if(switchPosition.equals("cross")){
            switchTV.setImageResource(R.drawable.ic_diamond);
            switchPosition = "diamond";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void changeClicked(View view){
        TextView box = (TextView) view;
        String answerIndex = box.getTag().toString();
        if(allowedBoxes.contains(answerIndex)) {
            if (switchPosition.equals("diamond")) {
                if (Objects.equals(box.getBackground().getConstantState(), getResources().getDrawable(R.drawable.ic_diamond).getConstantState())) {
                    box.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                } else {
                    box.setBackground(getResources().getDrawable(R.drawable.ic_diamond));
                }
            }
            else if (switchPosition.equals("cross")) {
                if (Objects.equals(box.getBackground().getConstantState(), getResources().getDrawable(R.drawable.ic_cross).getConstantState())) {
                    box.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                } else {
                    box.setBackground(getResources().getDrawable(R.drawable.ic_cross));
                }
            }
            clickedBox = answerIndex;
            allowedBoxes.remove(answerIndex);
            gl.findViewWithTag(answerIndex).clearAnimation();
        }
    }

    @SuppressLint("SetTextI18n")
    public void instructionChange(View view){
        List<String> tapBoxes = new ArrayList<>();
        List<String> relatedClues = new ArrayList<>();
        List<ArrayList<Integer>> grid = new ArrayList<>();


        if (view.getTag().equals("+")) {
            if(inNum<inStrings.size()-1) inNum++;
        }
        else{
            if(inNum>0) inNum--;
        }
        inTV.setText(inStrings.get(inNum));

        if(inNum<=2){
            Log.i("inNum","<=2");
            grid.add(new ArrayList<>(Arrays.asList(0, 0, 2, 4, 0)));
            grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 0, 0)));
            grid.add(new ArrayList<>(Arrays.asList(0, 3, 0, 0, 0)));
            grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
            grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, 0)));
            createGridAndPlace(grid);
        }

        else if(inNum==3){
            tapBoxes = new ArrayList<>(Arrays.asList("40","21","31","41"));
            relatedClues = new ArrayList<>(Arrays.asList("30"));
            grid.add(new ArrayList<>(Arrays.asList(0, 0, 2, 4, 0)));
            grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 0, 0)));
            grid.add(new ArrayList<>(Arrays.asList(0, 3, 0, 0, 0)));
            grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
            grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, 0)));
            createGridAndPlace(grid);
        }

        for(String index : tapBoxes) {
            allowedBoxes.add(index);
            gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_red));
            Animation anim = new AlphaAnimation(0.5f, 1.0f);
            anim.setDuration(500); //You can manage the blinking time with this parameter
            anim.setStartOffset(20);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            gl.findViewWithTag(index).startAnimation(anim);
        }

        for(String index : relatedClues){
            gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_bluegreen));
        }
    }

    public void setInStrings(){
        inStrings.add("Hazine Avı uygulamalı rehberine hoşgeldiniz. Başlamak için tıklayınız.");
        inStrings.add("Hazine Avı oyununda, verilen sayılar komşularında kaç elmas bulunduğunu gösterir.");
        inStrings.add("Çözerken ilk bakılması gereken şey, içinde yazan sayı kadar komşusu olan ipuculardır.");
        inStrings.add("Örnekte görüldüğü üzere, içinde 4 yazılı ipucunun sadece 4 komşusu vardır. Bu komşular kesin olarak elmasla doldurulabilir.");

    }


    public void createGridAndPlace(List<ArrayList<Integer>> grid){
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++) {
                String n = grid.get(i).get(j).toString();
                if (Integer.parseInt(n) > 0) {
                    ((TextView) gl.findViewWithTag(Integer.toString(j) + i)).setText(n);
                }
                gl.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                gl.findViewWithTag(Integer.toString(j) + i).clearAnimation();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_guide);
        inTV = findViewById(R.id.instructionTV_guide);
        gl = findViewById(R.id.gridGL_guide);
        setInStrings();
        createGridAndPlace(new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList(0, 0, 2, 4, 0)),
                new ArrayList<>(Arrays.asList(1, 0, 0, 0, 0)),
                new ArrayList<>(Arrays.asList(0, 3, 0, 0, 0)),
                new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)),
                new ArrayList<>(Arrays.asList(1, 0, 0, 2, 0)))));
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        goBackToHTP(null);
    }
}