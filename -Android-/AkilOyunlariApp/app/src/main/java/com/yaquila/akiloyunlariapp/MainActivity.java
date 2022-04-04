package com.yaquila.akiloyunlariapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    public void goToGameList(View view){
        Intent intent = new Intent(getApplicationContext(),
                GameTypesActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    public void goToProfile(View view){
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    public void goToLeaderboard(View view){
        Intent intent = new Intent(getApplicationContext(), LeaderboardActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    public void goToClassroom(View view){
        Intent intent = new Intent(getApplicationContext(), MyClassActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void changeLanguage(View view){
        Locale currentLocale = getResources().getConfiguration().locale;
        if (currentLocale.getLanguage().contains("tr")){
            setLocale(this, "en");
            ((TextView)view).setText("EN \uD83C\uDDEC\uD83C\uDDE7");
            sharedPreferences.edit().putString("locale","en").apply();
        } else {
            setLocale(this, "tr");
            ((TextView)view).setText("TR \uD83C\uDDF9\uD83C\uDDF7");
            sharedPreferences.edit().putString("locale","tr").apply();
        }
        ((TextView)findViewById(R.id.uygulama_ismi1)).setText(R.string.app_name1);
        ((TextView)findViewById(R.id.uygulama_ismi2)).setText(R.string.app_name2);
        ((TextView)findViewById(R.id.playTV)).setText(R.string.play_button);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void setLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public void checkSavedQuestions() throws IOException {
        SharedPreferences sP = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        if("none".equals(sP.getString("Sudoku.6.Easy","none"))){
            Log.d("NONE","QWEQWEQWEQWEQWEWQ");
            sP.edit().putString("IDSudoku.6.Easy",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSudoku.6.Medium",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSudoku.6.Hard",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSudoku.9.Easy",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSudoku.9.Medium",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSudoku.9.Hard",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDHazineAvi.5",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDHazineAvi.8",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDHazineAvi.10",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDPatika.5",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDPatika.7",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDPatika.9",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSayiBulmaca.3",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSayiBulmaca.4",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSayiBulmaca.5",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSozcukTuru.Easy",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSozcukTuru.Medium",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSozcukTuru.Hard",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSozcukTuru.Hardest",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDPiramit.3",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDPiramit.4",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDPiramit.5",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDPiramit.6",ObjectSerializer.serialize(new ArrayList<String>())).apply();

            sP.edit().putString("Sudoku.6.Easy",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Sudoku.6.Medium",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Sudoku.6.Hard",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Sudoku.9.Easy",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Sudoku.9.Medium",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Sudoku.9.Hard",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("HazineAvi.5",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("HazineAvi.8",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("HazineAvi.10",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Patika.5",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Patika.7",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Patika.9",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("SayiBulmaca.3",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("SayiBulmaca.4",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("SayiBulmaca.5",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("SozcukTuru.Easy",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("SozcukTuru.Medium",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("SozcukTuru.Hard",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("SozcukTuru.Hardest",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Piramit.3",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Piramit.4",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Piramit.5",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Piramit.6",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();


            sP.edit().putString("ScoreSudoku.6.Easy",Integer.toString(0)).apply();
            sP.edit().putString("ScoreSudoku.6.Medium",Integer.toString(0)).apply();
            sP.edit().putString("ScoreSudoku.6.Hard",Integer.toString(0)).apply();
            sP.edit().putString("ScoreSudoku.9.Easy",Integer.toString(0)).apply();
            sP.edit().putString("ScoreSudoku.9.Medium",Integer.toString(0)).apply();
            sP.edit().putString("ScoreSudoku.9.Hard",Integer.toString(0)).apply();

            sP.edit().putString("ScoreHazineAvi.Easy",Integer.toString(0)).apply();
            sP.edit().putString("ScoreHazineAvi.Medium",Integer.toString(0)).apply();
            sP.edit().putString("ScoreHazineAvi.Hard",Integer.toString(0)).apply();

            sP.edit().putString("ScorePatika.Easy",Integer.toString(0)).apply();
            sP.edit().putString("ScorePatika.Medium",Integer.toString(0)).apply();
            sP.edit().putString("ScorePatika.Hard",Integer.toString(0)).apply();

            sP.edit().putString("ScoreSayiBulmaca.Easy",Integer.toString(0)).apply();
            sP.edit().putString("ScoreSayiBulmaca.Medium",Integer.toString(0)).apply();
            sP.edit().putString("ScoreSayiBulmaca.Hard",Integer.toString(0)).apply();

            sP.edit().putString("ScoreSozcukTuru.Easy",Integer.toString(0)).apply();
            sP.edit().putString("ScoreSozcukTuru.Medium",Integer.toString(0)).apply();
            sP.edit().putString("ScoreSozcukTuru.Hard",Integer.toString(0)).apply();
            sP.edit().putString("ScoreSozcukTuru.VeryHard",Integer.toString(0)).apply();

            sP.edit().putString("ScorePiramit.Easy",Integer.toString(0)).apply();
            sP.edit().putString("ScorePiramit.Medium",Integer.toString(0)).apply();
            sP.edit().putString("ScorePİramit.Hard",Integer.toString(0)).apply();
            sP.edit().putString("ScorePiramit.VeryHard",Integer.toString(0)).apply();

            sP.edit().putString("BestSudoku.6.Easy",Integer.toString(0)).apply();
            sP.edit().putString("BestSudoku.6.Medium",Integer.toString(0)).apply();
            sP.edit().putString("BestSudoku.6.Hard",Integer.toString(0)).apply();
            sP.edit().putString("BestSudoku.9.Easy",Integer.toString(0)).apply();
            sP.edit().putString("BestSudoku.9.Medium",Integer.toString(0)).apply();
            sP.edit().putString("BestSudoku.9.Hard",Integer.toString(0)).apply();

            sP.edit().putString("BestHazineAvi.Easy",Integer.toString(0)).apply();
            sP.edit().putString("BestHazineAvi.Medium",Integer.toString(0)).apply();
            sP.edit().putString("BestHazineAvi.Hard",Integer.toString(0)).apply();

            sP.edit().putString("BestPatika.Easy",Integer.toString(0)).apply();
            sP.edit().putString("BestPatika.Medium",Integer.toString(0)).apply();
            sP.edit().putString("BestPatika.Hard",Integer.toString(0)).apply();

            sP.edit().putString("BestSayiBulmaca.Easy",Integer.toString(0)).apply();
            sP.edit().putString("BestSayiBulmaca.Medium",Integer.toString(0)).apply();
            sP.edit().putString("BestSayiBulmaca.Hard",Integer.toString(0)).apply();

            sP.edit().putString("BestSozcukTuru.Easy",Integer.toString(0)).apply();
            sP.edit().putString("BestSozcukTuru.Medium",Integer.toString(0)).apply();
            sP.edit().putString("BestSozcukTuru.Hard",Integer.toString(0)).apply();
            sP.edit().putString("BestSozcukTuru.VeryHard",Integer.toString(0)).apply();

            sP.edit().putString("BestPiramit.Easy",Integer.toString(0)).apply();
            sP.edit().putString("BestPiramit.Medium",Integer.toString(0)).apply();
            sP.edit().putString("BestPiramit.Hard",Integer.toString(0)).apply();
            sP.edit().putString("BestPiramit.VeryHard",Integer.toString(0)).apply();



        }
        Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sP.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));
        assert solvedQuestions != null;
        Log.i("solveque",solvedQuestions+"\n  "+solvedQuestions.size());
        if(solvedQuestions.size()<5){
            solvedQuestions = new HashMap<>();
            solvedQuestions.put("Sudoku.6.Easy",new ArrayList<String>());
            solvedQuestions.put("Sudoku.6.Medium",new ArrayList<String>());
            solvedQuestions.put("Sudoku.6.Hard",new ArrayList<String>());
            solvedQuestions.put("Sudoku.9.Easy",new ArrayList<String>());
            solvedQuestions.put("Sudoku.9.Medium",new ArrayList<String>());
            solvedQuestions.put("Sudoku.9.Hard",new ArrayList<String>());
            solvedQuestions.put("HazineAvi.5",new ArrayList<String>());
            solvedQuestions.put("HazineAvi.8",new ArrayList<String>());
            solvedQuestions.put("HazineAvi.10",new ArrayList<String>());
            solvedQuestions.put("Patika.5",new ArrayList<String>());
            solvedQuestions.put("Patika.7",new ArrayList<String>());
            solvedQuestions.put("Patika.9",new ArrayList<String>());
            solvedQuestions.put("SayiBulmaca.3",new ArrayList<String>());
            solvedQuestions.put("SayiBulmaca.4",new ArrayList<String>());
            solvedQuestions.put("SayiBulmaca.5",new ArrayList<String>());
            solvedQuestions.put("SozcukTuru.Easy",new ArrayList<String>());
            solvedQuestions.put("SozcukTuru.Medium",new ArrayList<String>());
            solvedQuestions.put("SozcukTuru.Hard",new ArrayList<String>());
            solvedQuestions.put("SozcukTuru.Hardest",new ArrayList<String>());
            solvedQuestions.put("Piramit.3",new ArrayList<String>());
            solvedQuestions.put("Piramit.4",new ArrayList<String>());
            solvedQuestions.put("Piramit.5",new ArrayList<String>());
            solvedQuestions.put("Piramit.6",new ArrayList<String>());

            sP.edit().putString("SolvedQuestions",ObjectSerializer.serialize((Serializable) solvedQuestions)).apply();
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        String localeString = sharedPreferences.getString("locale","en");
        if (localeString.equals("tr")){
            setLocale(this, "tr");
            ((TextView)findViewById(R.id.changeLanguageTV)).setText("TR \uD83C\uDDF9\uD83C\uDDF7");
        } else {
            setLocale(this, "en");
            ((TextView)findViewById(R.id.changeLanguageTV)).setText("EN \uD83C\uDDEC\uD83C\uDDE7");
        }
        ((TextView)findViewById(R.id.uygulama_ismi1)).setText(R.string.app_name1);
        ((TextView)findViewById(R.id.uygulama_ismi2)).setText(R.string.app_name2);
        ((TextView)findViewById(R.id.playTV)).setText(R.string.play_button);

//        Log.i("cacheDir",getCacheDir().getAbsolutePath());
        try {
            checkSavedQuestions();
        } catch (IOException e) {
            e.printStackTrace();
        }//checkSavedQuestions


//        try{
//            Intent intent = getIntent();
//            if(Objects.equals(intent.getStringExtra("Logged in"), "Logged in")){
//                Log.i("loggedin","loggedin");
//            } else {
//                Intent intent1 = new Intent(getApplicationContext(),LoginActivity.class);
//                startActivity(intent1);
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//            Intent intent1 = new Intent(getApplicationContext(),LoginActivity.class);
//            startActivity(intent1);
//        }

    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }


}