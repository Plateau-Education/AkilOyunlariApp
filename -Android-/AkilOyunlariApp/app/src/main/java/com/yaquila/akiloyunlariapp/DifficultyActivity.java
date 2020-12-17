package com.yaquila.akiloyunlariapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Objects;

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

    public void initDiffs(){
        LinearLayout diffList = findViewById(R.id.diffList_d);
        for (int i = 0; i < 4; i++){
            TextView currentTV = (TextView) ((ConstraintLayout)diffList.getChildAt(i)).getChildAt(1);
            currentTV.setBackground(getResources().getDrawable(R.drawable.diff_selector_bg));
            currentTV.setTextColor(getResources().getColorStateList(R.color.diff_selector_tvcolor));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void arrangeDifficulties(){
        LinearLayout diffList = findViewById(R.id.diffList_d);
        SharedPreferences sP = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        if (Arrays.asList(new String[]{"Sözcük Türü", "Piramit"}).contains(gameName)){
            int[] diffIds = {R.string.Easy,R.string.Medium,R.string.Hard,R.string.VeryHard};
            String[] diffs = {"Easy","Medium","Hard","VeryHard"};
            for (int i = 0; i < 4; i++){
                ConstraintLayout currentRL = ((ConstraintLayout)diffList.getChildAt(i));
                TextView currentTV = (TextView) currentRL.getChildAt(1);
                currentTV.setText(diffIds[i]);
                currentRL.setVisibility(View.VISIBLE);
                currentTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 33);
                if(gameName.equals("Sözcük Türü"))
                    ((TextView) currentRL.getChildAt(0)).setText(formatTime(Integer.parseInt(Objects.requireNonNull(sP.getString("BestSozcukTuru."+diffIds[i], "0")))));
                else
                    ((TextView) currentRL.getChildAt(0)).setText(formatTime(Integer.parseInt(Objects.requireNonNull(sP.getString("BestPiramit."+diffs[i], "0")))));

                Log.i("piramit"+diffs[i],Objects.requireNonNull(sP.getString("BestPiramit."+diffs[i], "0")));
            }
        }
        else if (Arrays.asList(new String[]{"Sudoku6", "Sudoku9", "Patika", "Hazine Avı", "Sayı Bulmaca"}).contains(gameName)){
            int[] diffIds = {R.string.Easy,R.string.Medium,R.string.Hard};
            for (int i = 0; i < 3; i++){
                ConstraintLayout currentRL = ((ConstraintLayout)diffList.getChildAt(i));
                TextView currentTV = (TextView) currentRL.getChildAt(1);
                currentTV.setText(diffIds[i]);}
            }
        else{
            int[] diffIds = {R.string.Easy,R.string.Medium,R.string.Hard};
            for (int i = 0; i < 3; i++){
                ConstraintLayout currentRL = ((ConstraintLayout)diffList.getChildAt(i));
                TextView currentTV = (TextView) currentRL.getChildAt(1);
                currentTV.setText(diffIds[i]);
            }
        }

        TextView easyTime = (TextView) findViewById(R.id.easyTimeTV_d);
        TextView mediumTime = (TextView) findViewById(R.id.mediumTimeTV_d);
        TextView hardTime = (TextView) findViewById(R.id.hardTimeTV_d);
        TextView hardestTime = (TextView) findViewById(R.id.hardestTimeTV_d);

        if(gameName.equals("Sudoku6")){
            easyTime.setText(formatTime(Integer.parseInt(Objects.requireNonNull(sP.getString("Sudoku.6.Easy", "0")))));
            mediumTime.setText(formatTime(Integer.parseInt(Objects.requireNonNull(sP.getString("Sudoku.6.Easy", "0")))));
            easyTime.setText(formatTime(Integer.parseInt(Objects.requireNonNull(sP.getString("Sudoku.6.Easy", "0")))));

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
        if(gameName.contains("Sudoku")) intent = new Intent(getApplicationContext(), SizeActivityForTwoSizedGames.class);
        else intent = new Intent(this, GameListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }
}