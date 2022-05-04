package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Arrays;
import java.util.Objects;

public class DifficultyActivity extends AppCompatActivity {

    String gameName;

    public void goToGameList(View view){
        Intent intent;
        if(gameName.contains(getString(R.string.Sudoku))) intent = new Intent(getApplicationContext(), SizeActivityForTwoSizedGames.class);
        else intent = new Intent(this, GameListActivity.class);
        intent.putExtra("type","single");
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("UseCompatLoadingForDrawables")
    public void goToGame(View view){
        TextView tvdiff = (TextView) view;
        tvdiff.setBackground(getResources().getDrawable(R.drawable.clicked_diff_bg));
        tvdiff.setTextColor(getResources().getColor(R.color.f7f5fa));
        Intent intent = null;
        if (gameName.matches(getString(R.string.SayıBulmaca))) {
            intent = new Intent(getApplicationContext(), GameActivitySayiBulmaca.class);
        }
        else if (gameName.matches(getString(R.string.Piramit))){
            intent = new Intent(getApplicationContext(), GameActivityPiramit.class);
        }
        else if (gameName.contains(getString(R.string.Sudoku))){
            intent = new Intent(getApplicationContext(), GameActivitySudoku.class);
        }
        else if (gameName.matches(getString(R.string.HazineAvı))){
            intent = new Intent(getApplicationContext(), GameActivityHazineAvi.class);
        }
        else if (gameName.matches(getString(R.string.Patika))){
            intent = new Intent(getApplicationContext(), GameActivityPatika.class);
        }
        else if (gameName.matches(getString(R.string.SözcükTuru))){
            intent = new Intent(getApplicationContext(), GameActivitySozcukTuru.class);
        }
        else if (gameName.matches("Pentomino")){
            intent = new Intent(getApplicationContext(), GameActivityPentomino.class);
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

    @SuppressLint({"UseCompatLoadingForDrawables", "UseCompatLoadingForColorStateLists"})
    public void initDiffs(){
        LinearLayout diffList = findViewById(R.id.diffList_d);
        for (int i = 0; i < 4; i++){
            TextView currentTV = (TextView) ((ConstraintLayout)diffList.getChildAt(i+1)).getChildAt(1);
            currentTV.setBackground(getResources().getDrawable(R.drawable.diff_selector_bg));
            currentTV.setTextColor(getResources().getColorStateList(R.color.diff_selector_tvcolor));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void arrangeDifficulties(){
        LinearLayout diffList = findViewById(R.id.diffList_d);
        SharedPreferences sP = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        if (Objects.equals(getString(R.string.Piramit), gameName)){
            int[] diffIds = {R.string.Easy,R.string.Medium,R.string.Hard,R.string.VeryHard};
            String[] diffs = {"Easy","Medium","Hard","VeryHard"};
            for (int i = 0; i < 4; i++){
                ConstraintLayout currentRL = ((ConstraintLayout)diffList.getChildAt(i+1));
                TextView currentTV = (TextView) currentRL.getChildAt(1);
                currentTV.setText(diffIds[i]);
                currentRL.setVisibility(View.VISIBLE);
                currentTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 33);
                ((TextView) currentRL.getChildAt(0)).setText(formatTime(Integer.parseInt(Objects.requireNonNull(sP.getString("BestPiramit."+diffs[i], "0")))));
            }
        }
        else if (Arrays.asList(getString(R.string.Sudoku)+"6", getString(R.string.Sudoku)+"9", getString(R.string.Patika), getString(R.string.HazineAvı), getString(R.string.SayıBulmaca)).contains(gameName)){
            int[] diffIds = {R.string.Easy,R.string.Medium,R.string.Hard};
            String[] diffs = {"Easy","Medium","Hard"};
            for (int i = 0; i < 3; i++){
                ConstraintLayout currentRL = ((ConstraintLayout)diffList.getChildAt(i+1));
                TextView currentTV = (TextView) currentRL.getChildAt(1);
                currentTV.setText(diffIds[i]);
                if(gameName.contains(getString(R.string.Sudoku)))
                    ((TextView) currentRL.getChildAt(0)).setText(formatTime(Integer.parseInt(Objects.requireNonNull(sP.getString("BestSudoku."+gameName.charAt(gameName.length()-1)+"."+diffs[i], "0")))));
                else if(gameName.equals(getString(R.string.Patika)))
                    ((TextView) currentRL.getChildAt(0)).setText(formatTime(Integer.parseInt(Objects.requireNonNull(sP.getString("BestPatika."+diffs[i], "0")))));
                else if(gameName.equals(getString(R.string.HazineAvı)))
                    ((TextView) currentRL.getChildAt(0)).setText(formatTime(Integer.parseInt(Objects.requireNonNull(sP.getString("BestHazineAvi."+diffs[i], "0")))));
                else if(gameName.equals(getString(R.string.SayıBulmaca)))
                    ((TextView) currentRL.getChildAt(0)).setText(formatTime(Integer.parseInt(Objects.requireNonNull(sP.getString("BestSayiBulmaca."+diffs[i], "0")))));

            }
        }

        else{
            int[] diffIds = {R.string.Easy,R.string.Medium,R.string.Hard};
            for (int i = 0; i < 3; i++){
                ConstraintLayout currentRL = ((ConstraintLayout)diffList.getChildAt(i+1));
                TextView currentTV = (TextView) currentRL.getChildAt(1);
                currentTV.setText(diffIds[i]);
            }
        }
    }

    @SuppressLint("DefaultLocale")
    public static String formatTime(int secs) {
        return String.format("%02d:%02d", (secs % 3600) / 60, secs % 60);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
        if(gameName.contains(getString(R.string.Sudoku))) intent = new Intent(getApplicationContext(), SizeActivityForTwoSizedGames.class);
        else intent = new Intent(this, GameListActivity.class);
        intent.putExtra("type","single");
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }
}