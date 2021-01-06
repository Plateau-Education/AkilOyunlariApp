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
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    ImageView switchIV;
    ImageView arrow;

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
//        ImageView switchTV = (ImageView) view;
//        if(switchPosition.equals("diamond")){
//            switchTV.setImageResource(R.drawable.ic_cross);
//            switchPosition = "cross";
//        }
//        else if(switchPosition.equals("cross")){
//            switchTV.setImageResource(R.drawable.ic_diamond);
//            switchPosition = "diamond";
//        }
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
        List<String> relatedBoxes = new ArrayList<>();
        List<ArrayList<Integer>> grid = new ArrayList<>();
        allowedBoxes = new ArrayList<>();

        if (view.getTag().equals("+")) {
            if(inNum<inStrings.size()-1) inNum++;
        }
        else{
            if(inNum>0) inNum--;
        }
        inTV.setText(inStrings.get(inNum));

        if(inNum<=3){
            Log.i("inNum","<=2");
            switchIV.setImageResource(R.drawable.ic_diamond);
            switchPosition="diamond";
            grid.add(new ArrayList<>(Arrays.asList(0, 0, 2, 4, 0)));
            grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 0, 0)));
            grid.add(new ArrayList<>(Arrays.asList(0, 3, 0, 0, 0)));
            grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
            grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, 0)));
            createGridAndPlace(grid);
        }

        if(inNum==3){
            tapBoxes = new ArrayList<>(Arrays.asList("40","21","31","41"));
            relatedClues = new ArrayList<>(Collections.singletonList("30"));
        }

        if(inNum==4 || inNum==5){
            relatedClues = new ArrayList<>(Collections.singletonList("20"));
            switchIV.setImageResource(R.drawable.ic_diamond);
            switchPosition="diamond";
            switchIV.clearAnimation();
            grid.add(new ArrayList<>(Arrays.asList(0, 0, 2, 4, -1)));
            grid.add(new ArrayList<>(Arrays.asList(1, 0, -1, -1, -1)));
            grid.add(new ArrayList<>(Arrays.asList(0, 3, 0, 0, 0)));
            grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
            grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, 0)));
            createGridAndPlace(grid);
        }

        if(inNum>=6 && inNum <= 10){
            switchIV.setImageResource(R.drawable.ic_cross);
            switchPosition="cross";
            switchIV.clearAnimation();
            grid.add(new ArrayList<>(Arrays.asList(0, -2, 2, 4, -1)));
            grid.add(new ArrayList<>(Arrays.asList(1, -2, -1, -1, -1)));
            grid.add(new ArrayList<>(Arrays.asList(0, 3, 0, 0, 0)));
            grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
            grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, 0)));
            createGridAndPlace(grid);
        }
        if(inNum==11){
            switchIV.setImageResource(R.drawable.ic_cross);
            switchPosition="cross";
            switchIV.clearAnimation();
            grid.add(new ArrayList<>(Arrays.asList(0, -2, 2, 4, -1)));
            grid.add(new ArrayList<>(Arrays.asList(1, -2, -1, -1, -1)));
            grid.add(new ArrayList<>(Arrays.asList(0, 3, -2, -2, -2)));
            grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
            grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, 0)));
            createGridAndPlace(grid);
        }
        if(inNum>=12 && inNum <= 15){
            switchIV.setImageResource(R.drawable.ic_diamond);
            switchPosition="diamond";
            switchIV.clearAnimation();
            grid.add(new ArrayList<>(Arrays.asList(0, -2, 2, 4, -1)));
            grid.add(new ArrayList<>(Arrays.asList(1, -2, -1, -1, -1)));
            grid.add(new ArrayList<>(Arrays.asList(0, 3, -2, -2, -2)));
            grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
            grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, -1)));
            createGridAndPlace(grid);
        }
        if(inNum==16){
            switchIV.setImageResource(R.drawable.ic_cross);
            switchPosition="cross";
            switchIV.clearAnimation();
            grid.add(new ArrayList<>(Arrays.asList(0, -2, 2, 4, -1)));
            grid.add(new ArrayList<>(Arrays.asList(1, -2, -1, -1, -1)));
            grid.add(new ArrayList<>(Arrays.asList(-2, 3, -2, -2, -2)));
            grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
            grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, -1)));
            createGridAndPlace(grid);
        }
        if(inNum==17){
            switchIV.setImageResource(R.drawable.ic_diamond);
            switchPosition="diamond";
            switchIV.clearAnimation();
            grid.add(new ArrayList<>(Arrays.asList(-1, -2, 2, 4, -1)));
            grid.add(new ArrayList<>(Arrays.asList(1, -2, -1, -1, -1)));
            grid.add(new ArrayList<>(Arrays.asList(-2, 3, -2, -2, -2)));
            grid.add(new ArrayList<>(Arrays.asList(0, 2, 0, 2, 1)));
            grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, -1)));
            createGridAndPlace(grid);
        }
        if(inNum>=18){
            switchIV.setImageResource(R.drawable.ic_diamond);
            switchPosition="diamond";
            switchIV.clearAnimation();
            grid.add(new ArrayList<>(Arrays.asList(-1, -2, 2, 4, -1)));
            grid.add(new ArrayList<>(Arrays.asList(1, -2, -1, -1, -1)));
            grid.add(new ArrayList<>(Arrays.asList(-2, 3, -2, -2, -2)));
            grid.add(new ArrayList<>(Arrays.asList(-1, 2, -1, 2, 1)));
            grid.add(new ArrayList<>(Arrays.asList(1, 0, 0, 2, -1)));
            createGridAndPlace(grid);
        }



        if(inNum==5) {
            tapBoxes = new ArrayList<>(Arrays.asList("10", "11"));
            relatedClues = new ArrayList<>(Collections.singletonList("20"));
        }
        if(inNum==8){
            relatedBoxes = new ArrayList<>(Arrays.asList("23","24","44"));
            relatedClues = new ArrayList<>(Collections.singletonList("34"));
        }
        if(inNum==9){
            relatedClues = new ArrayList<>(Collections.singletonList("33"));
            relatedBoxes = new ArrayList<>(Arrays.asList("23","24","44"));
        }
        if(inNum==10){
            relatedBoxes = new ArrayList<>(Arrays.asList("23","24","44"));
            relatedClues = new ArrayList<>(Collections.singletonList("33"));
            tapBoxes = new ArrayList<>(Arrays.asList("42","32","22"));
        }
        if(inNum==11){
            tapBoxes = new ArrayList<>(Collections.singletonList("44"));
            relatedClues = new ArrayList<>(Collections.singletonList("43"));
        }
        if(inNum==12){
            relatedBoxes = new ArrayList<>(Arrays.asList("23","24"));
            relatedClues = new ArrayList<>(Collections.singletonList("34"));
        }
        if(inNum==13){
            relatedBoxes = new ArrayList<>(Arrays.asList("03","14"));
            relatedClues = new ArrayList<>(Collections.singletonList("04"));
        }
        if(inNum==14){
            relatedBoxes = new ArrayList<>(Arrays.asList("03","14","23","24"));
        }
        if(inNum==15){
            relatedBoxes = new ArrayList<>(Arrays.asList("03","14","23","24"));
            relatedClues = new ArrayList<>(Collections.singletonList("13"));
            tapBoxes = new ArrayList<>(Collections.singletonList("02"));
        }
        if(inNum==16){
            relatedClues = new ArrayList<>(Collections.singletonList("01"));
            tapBoxes = new ArrayList<>(Collections.singletonList("00"));
        }
        if(inNum==17){
            relatedClues = new ArrayList<>(Collections.singletonList("12"));
            tapBoxes = new ArrayList<>(Arrays.asList("03","23"));
        }

        arrow.setVisibility(View.INVISIBLE);
        arrow.clearAnimation();

        if(inNum==5 || inNum==11 || inNum==15 || inNum==16){
            for (String index : relatedClues) gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_bluegreen));
            for (String index : relatedBoxes) gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_shallow_light));
            animateView(switchIV,0.3f,1.0f);

            arrow.setVisibility(View.VISIBLE);
            Animation anim = new TranslateAnimation(-(100f/3)*getResources().getDisplayMetrics().density, (100f/3)*getResources().getDisplayMetrics().density, 0f, 0f);
            anim.setDuration(500);
            anim.setStartOffset(20);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            arrow.startAnimation(anim);

            final List<String> finalTapBoxes = tapBoxes;
            switchIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(inNum==5 || inNum==11 || inNum==15 || inNum==16) {
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
                animateView(gl.findViewWithTag(index),0.5f,1.0f);
            }
            for (String index : relatedClues) gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_bluegreen));
            for (String index : relatedBoxes) gl.findViewWithTag(index).setBackground(getResources().getDrawable(R.drawable.stroke_bg_shallow_light));
        }
    }

    public void setInStrings(){
        inStrings.add("Hazine Avı öğretici uygulamasına hoşgeldiniz. Öğretici boyunca yanıp sönen kutulara tıklayarak kendiniz de çözüme dahil olabilirsiniz.");
        inStrings.add("Hazine Avı oyununda, verilen sayılar komşularında kaç elmas bulunduğunu gösterir.");
        inStrings.add("Çözerken ilk bakılması gereken şey, içinde yazan sayı kadar komşusu olan ipuçlarıdır.");
        inStrings.add("Örnekte görüldüğü üzere, içinde 4 yazılı ipucunun sadece 4 komşusu vardır. Bu komşular kesin olarak elmasla doldurulabilir.");
        inStrings.add("Farkedilebileceği üzere koyduğumuz 4 elmastan 2'si, yandaki içinde 2 yazan ipucunun da komşusudur.");
        inStrings.add("Yani içinde 2 yazan ipucu, alması gereken tüm elmasları almıştır ve diğer komşularında elmas olamaz. Bu komşulara sol aşağıdaki değişim kutusundan çarpıya geçilerek çarpı koyulur.");
        inStrings.add("İçinde yazan sayı kadar komşusu olan ipuçları, her soruda bulunmayabilir veya çözüme ulaşmada yetersiz kalabilir.");
        inStrings.add("Bu durumlarda bakılması gereken şey, ipuçlarının komşularına koyulacak elmasların diğer ipuçlarındaki ortak etkileridir.");
        inStrings.add("Örneğin bu 2 ipucusunun 3 komşusu vardır ve bu 3 kutudan 2'si elmas olmak zorundadır.");
        inStrings.add("Görüldüğü üzere bu 3 kutu aynı zamanda bir üstteki 2 ipucusunun da komşusudur. Yani bu 3 kutudaki 2 elmas onu da etkiler.");
        inStrings.add("Bu gri renkli 3 komşuda kesin olarak 2 elmas bulunacağı için diğer komşularda elmas olamaz. Bu komşulara çarpı koyulur.");
        inStrings.add("Sağdaki 1 ipucusunun tek boş komşusu kalmıştır. Bu komşuya da değişim kutusundan elmasa geçilerek elmas koyulur.");
        inStrings.add("Yeşille işaretlenmiş 2 ipucusunun komşularından biri elmastır. Yani kalan 2 komşudan birisi elmas olmak zorundadır.");
        inStrings.add("Yeşille işaretlenen 1 ipucusunun da 2 komşusundan birinde elmas olmak zorundadır.");
        inStrings.add("Yani bu 4 kutuda toplam 2 elmas bulunmaktadır.");
        inStrings.add("Bu 4 kutuda bulunan 2 elmas, yeşille işaretli 2 ipucusunu da etkiler. Bu nedenle, 2 ipucusunun kalan komşusuna çarpı koyulur.");
        inStrings.add("Yeşille işaretlenen 1 ipucusunun tek komşusu kalmıştır. Bu komşuya da elmas gelmelidir.");
        inStrings.add("Yeşille işaretlenen 3 ipucusunun bir komşusunda elmas vardır. Geriye kalan 2 komşusuna da elmas gelmelidir.");
        inStrings.add("Bu öğreticinin sonuna geldiniz.\uD83C\uDFC1 Sol üstteki geri butonundan çıkabilir veya ok tuşlarıyla önceki adımlara dönebilirsiniz.");
    }

    public void animateView(View view,float s1, float s2){
        Animation anim = new AlphaAnimation(s1, s2);
        anim.setDuration(500); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        view.startAnimation(anim);
    }

    public void createGridAndPlace(List<ArrayList<Integer>> grid){
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++) {
                String n = grid.get(i).get(j).toString();
                if (Integer.parseInt(n) > 0) {
                    ((TextView) gl.findViewWithTag(Integer.toString(j) + i)).setText(n);
                    gl.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                } else if (Integer.parseInt(n) == 0){
                    gl.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                } else if (Integer.parseInt(n) == -1)
                    gl.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.ic_diamond));
                else gl.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.ic_cross));

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
        switchIV = findViewById(R.id.switchIV);
        arrow = findViewById(R.id.arrowIV_guide);
        setInStrings();
        inTV.setText(inStrings.get(0));
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