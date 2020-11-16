package com.yaquila.akiloyunlariapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class DifficultyActivity extends AppCompatActivity {

    String gameName;

    public void goToGameList(View view){
        Intent intent;
        if(gameName.contains("Sudoku")) intent = new Intent(getApplicationContext(), SizeActivityForTwoSizedGames.class);
        else intent = new Intent(this, GameListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    public void goToGame(View view){
        TextView tvdiff = (TextView) view;
        tvdiff.setBackground(getResources().getDrawable(R.drawable.clicked_diff_bg));
        tvdiff.setTextColor(getResources().getColor(R.color.f7f5fa));
        Intent intent = null;
        if (gameName.matches("Sayı Bulmaca")) {
            intent = new Intent(getApplicationContext(), GameActivitySayiBulmaca.class);
        }
        else if (gameName.matches("Piramit")){
            intent = new Intent(getApplicationContext(), GameActivityPiramit.class);
        }
        else if (gameName.contains("Sudoku")){
            intent = new Intent(getApplicationContext(), GameActivitySudoku.class);
        }
        else if (gameName.matches("Hazine Avı")){
            intent = new Intent(getApplicationContext(), GameActivityHazineAvi.class);
        }
        else if (gameName.matches("Patika")){
            intent = new Intent(getApplicationContext(), GameActivityPatika.class);
        }
        else if (gameName.matches("Sözcük Türü")){
            intent = new Intent(getApplicationContext(), GameActivitySozcukTuru.class);
        }
        else{
//            intent = new Intent(getApplicationContext(), GameActivitySayiBulmaca.class);
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
//            throw new IllegalArgumentException("Not Sayı Bulmaca");
        }
        assert intent != null;
        intent.putExtra("gameName", gameName);
        intent.putExtra("difficulty",tvdiff.getText());
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    public void initDiffs(){
        LinearLayout diffList = findViewById(R.id.diffList_d);
        for (int i = 0; i < 5; i++){
            TextView currentTV = (TextView) ((RelativeLayout)diffList.getChildAt(i)).getChildAt(0);
            currentTV.setBackground(getResources().getDrawable(R.drawable.diff_selector_bg));
            currentTV.setTextColor(getResources().getColorStateList(R.color.diff_selector_tvcolor));
        }
    }

    public void arrangeDifficulties(){
        LinearLayout diffList = findViewById(R.id.diffList_d);
        if (Arrays.asList(new String[]{"Sözcük Türü", "Piramit"}).contains(gameName)){
            int[] diffIds = {R.string.Easy,R.string.Medium,R.string.Hard,R.string.VeryHard};
            for (int i = 0; i < 4; i++){
                RelativeLayout currentRL = ((RelativeLayout)diffList.getChildAt(i));
                TextView currentTV = (TextView) currentRL.getChildAt(0);
                currentTV.setText(diffIds[i]);
                currentRL.setVisibility(View.VISIBLE);
                currentTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 33);
            }
        }
        else if (Arrays.asList(new String[]{"Sudoku6", "Sudoku9", "Patika", "Hazine Avı", "Sayı Bulmaca"}).contains(gameName)){
            int[] diffIds = {R.string.Easy,R.string.Medium,R.string.Hard};
            for (int i = 0; i < 3; i++){
                RelativeLayout currentRL = ((RelativeLayout)diffList.getChildAt(i));
                TextView currentTV = (TextView) currentRL.getChildAt(0);
                currentTV.setText(diffIds[i]);}
            }
        else{
            int[] diffIds = {R.string.Easy,R.string.Medium,R.string.Hard};
            for (int i = 0; i < 3; i++){
                RelativeLayout currentRL = ((RelativeLayout)diffList.getChildAt(i));
                TextView currentTV = (TextView) currentRL.getChildAt(0);
                currentTV.setText(diffIds[i]);
            }
//            throw new IllegalArgumentException("Unknown game name: "+gameName);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);
        initDiffs();
        Intent intent = getIntent();
        gameName = intent.getStringExtra("gameName");
//        if (gameName!=null){
////            Log.i("gameName", intent.getStringExtra("gameName"));
//            Toast.makeText(this, gameName, Toast.LENGTH_SHORT).show();
//        }

        arrangeDifficulties();

    }

    @Override
    public void onBackPressed() {
        Intent intent;
        if(gameName.contains("Sudoku")) intent = new Intent(getApplicationContext(), SizeActivityForTwoSizedGames.class);
        else intent = new Intent(this, GameListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }
}