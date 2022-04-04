package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

public class HowToPlayActivity extends AppCompatActivity {

    String currentGameName;
    String type;
    SharedPreferences sharedPreferences;


    public void goToGameList(View view){
        Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
        intent.putExtra("type",type);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    public void goToGuide(View view) {
        Intent intent = new Intent(this, GameGuideActivity.class);
        intent.putExtra("type",type);
        if (currentGameName.contains(getString(R.string.HazineAvı))) {
            sharedPreferences.edit().putString("HazineAviHTPCount",sharedPreferences.getString("HazineAviHTPCount","0 0").split(" ")[0]  + " " + (Integer.parseInt(sharedPreferences.getString("HazineAviHTPCount","0 0").split(" ")[1])+1)).apply();
            intent.putExtra("gameName", getString(R.string.HazineAvı));
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        } else if (currentGameName.contains(getString(R.string.Patika))){
            sharedPreferences.edit().putString("PatikaHTPCount",sharedPreferences.getString("PatikaHTPCount","0 0").split(" ")[0]  + " " + (Integer.parseInt(sharedPreferences.getString("PatikaHTPCount","0 0").split(" ")[1])+1)).apply();
            intent.putExtra("gameName", getString(R.string.Patika));
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        } else if (currentGameName.contains(getString(R.string.SayıBulmaca))){
            sharedPreferences.edit().putString("SayiBulmacaHTPCount",sharedPreferences.getString("SayiBulmacaHTPCount","0 0").split(" ")[0]  + " " + (Integer.parseInt(sharedPreferences.getString("SayiBulmacaHTPCount","0 0").split(" ")[1])+1)).apply();
            intent.putExtra("gameName", getString(R.string.SayıBulmaca));
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        } else if (currentGameName.contains(getString(R.string.Piramit))){
            sharedPreferences.edit().putString("PiramitHTPCount",sharedPreferences.getString("PiramitHTPCount","0 0").split(" ")[0]  + " " + (Integer.parseInt(sharedPreferences.getString("PiramitHTPCount","0 0").split(" ")[1])+1)).apply();
            intent.putExtra("gameName", getString(R.string.Piramit));
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        } else {
            Toast.makeText(this, getString(R.string.feature_not_active), Toast.LENGTH_SHORT).show();
            Log.i("goToGuide","guide not active");
        }
    }

    public void howToPlayGameListOpen(View view){
        ConstraintLayout constraintLayout = (ConstraintLayout) view.getParent();
        RelativeLayout howToPlayGameListRL = (RelativeLayout) constraintLayout.getChildAt(2);
        howToPlayGameListRL.setY(constraintLayout.getBottom());
        howToPlayGameListRL.setVisibility(View.VISIBLE);
        view.animate().translationYBy(-constraintLayout.getBottom()).setDuration(300);
        howToPlayGameListRL.animate().translationYBy(-constraintLayout.getBottom()).setDuration(300);
    }

    public void howToPlayGameListClose(View view){
        RelativeLayout howToPlayGameListRL = (RelativeLayout) ((ScrollView)((LinearLayout) view.getParent()).getParent()).getParent();
        ConstraintLayout constraintLayout = (ConstraintLayout) howToPlayGameListRL.getParent();
        Button slideupButton = (Button)constraintLayout.getChildAt(1);
        slideupButton.animate().translationYBy(constraintLayout.getBottom()).setDuration(300);
        howToPlayGameListRL.animate().translationYBy(constraintLayout.getBottom()).setDuration(300);
    }

    @SuppressLint("SetTextI18n")
    public void howToPlayChangeGame(View view){
        Log.i("tag", (String) view.getTag());
        TextView tv = (TextView) view;
        TextView gameNameTV = findViewById(R.id.gameNameTV_htp);
        gameNameTV.setText((CharSequence) tv.getTag());
        if ("Sudoku".equals(tv.getTag())) {
            ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_sudoku);
            sharedPreferences.edit().putString("SudokuHTPCount",(Integer.parseInt(sharedPreferences.getString("SudokuHTPCount","0 0").split(" ")[0])+1) + " " + sharedPreferences.getString("SudokuHTPCount","0 0").split(" ")[1]).apply();
        } else if (getString(R.string.HazineAvı).equals(tv.getTag())) {
            gameNameTV.setText(tv.getTag().toString().split(" ")[0]+"\n"+tv.getTag().toString().split(" ")[1]);
            ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_hazineavi);
            sharedPreferences.edit().putString("HazineAviHTPCount",(Integer.parseInt(sharedPreferences.getString("HazineAviHTPCount","0 0").split(" ")[0])+1) + " " + sharedPreferences.getString("HazineAviHTPCount","0 0").split(" ")[1]).apply();
        } else if (getString(R.string.Patika).equals(tv.getTag())) {
            ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_patika);
            sharedPreferences.edit().putString("PatikaHTPCount",(Integer.parseInt(sharedPreferences.getString("PatikaHTPCount","0 0").split(" ")[0])+1) + " " + sharedPreferences.getString("PatikaHTPCount","0 0").split(" ")[1]).apply();
        } else if (getString(R.string.SayıBulmaca).equals(tv.getTag())) {
            gameNameTV.setText(tv.getTag().toString().split(" ")[0]+"\n"+tv.getTag().toString().split(" ")[1]);
            ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_sayibulmaca);
            sharedPreferences.edit().putString("SayiBulmacaHTPCount",(Integer.parseInt(sharedPreferences.getString("SayiBulmacaHTPCount","0 0").split(" ")[0])+1) + " " + sharedPreferences.getString("SayiBulmacaHTPCount","0 0").split(" ")[1]).apply();
        } else if (getString(R.string.SözcükTuru).equals(tv.getTag())) {
            gameNameTV.setText(tv.getTag().toString().split(" ")[0]+"\n"+tv.getTag().toString().split(" ")[1]);
            ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_sozcukturu);
            sharedPreferences.edit().putString("SozcukTuruHTPCount",(Integer.parseInt(sharedPreferences.getString("SozcukTuruHTPCount","0 0").split(" ")[0])+1) + " " + sharedPreferences.getString("SozcukTuruHTPCount","0 0").split(" ")[1]).apply();
        } else if (getString(R.string.Piramit).equals(tv.getTag())) {
            ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_piramit);
            sharedPreferences.edit().putString("PiramitHTPCount", (Integer.parseInt(sharedPreferences.getString("PiramitHTPCount","0 0").split(" ")[0])+1) + " " + sharedPreferences.getString("PiramitHTPCount", "0 0").split(" ")[1]).apply();
        } else {
            ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.Comingsoon);
        }

        LinearLayout howToPlayGameListLL = (LinearLayout) tv.getParent();
        Typeface mohave_bold = Typeface.createFromAsset(getAssets(), "fonts/mohave_bold.ttf");
        tv.setTypeface(mohave_bold);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 45);
        Typeface mohave_regular = Typeface.createFromAsset(getAssets(), "fonts/mohave_regular.ttf");
        ((TextView) howToPlayGameListLL.findViewWithTag(currentGameName)).setTypeface(mohave_regular);
        ((TextView) howToPlayGameListLL.findViewWithTag(currentGameName)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 37);
        currentGameName = (String) tv.getTag();
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);

        sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        Intent intent = getIntent();
        currentGameName = intent.getStringExtra("gameName");
        type = intent.getStringExtra("type");
        String comefrom = intent.getStringExtra("comefrom");
        if(comefrom==null) comefrom="";
        if (currentGameName!=null){
            Log.i("gameName", Objects.requireNonNull(intent.getStringExtra("gameName")));
//            Toast.makeText(this, currentGameName, Toast.LENGTH_SHORT).show();
        }
        TextView gameNameTV = findViewById(R.id.gameNameTV_htp);
        if(currentGameName.contains(" ")){
            gameNameTV.setText(currentGameName.substring(0, currentGameName.indexOf(" "))+"\n"+currentGameName.substring(currentGameName.indexOf(" ")));
        }
        else{
            gameNameTV.setText(currentGameName);
        }
//        RelativeLayout howToPlayGameListRL = (RelativeLayout) constraintLayout.getChildAt(2);
//        Typeface mohave_bold = Typeface.createFromAsset(getAssets(), "fonts/mohave_bold.ttf");
//        ((TextView) howToPlayGameListRL.findViewWithTag(currentGameName)).setTypeface(mohave_bold);
//        ((TextView) howToPlayGameListRL.findViewWithTag(currentGameName)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 45);
        if ("Sudoku".equals(currentGameName)) {
            ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_sudoku);
            if(!comefrom.equals("GameGuide"))sharedPreferences.edit().putString("SudokuHTPCount",(Integer.parseInt(sharedPreferences.getString("SudokuHTPCount","0 0").split(" ")[0])+1) + " " + sharedPreferences.getString("SudokuHTPCount","0 0").split(" ")[1]).apply();
        } else if (getString(R.string.HazineAvı).equals(currentGameName)) {
            ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_hazineavi);
            if(!comefrom.equals("GameGuide"))sharedPreferences.edit().putString("HazineAviHTPCount",(Integer.parseInt(sharedPreferences.getString("HazineAviHTPCount","0 0").split(" ")[0])+1) + " " + sharedPreferences.getString("HazineAviHTPCount","0 0").split(" ")[1]).apply();
        } else if (getString(R.string.Patika).equals(currentGameName)) {
            ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_patika);
            if(!comefrom.equals("GameGuide"))sharedPreferences.edit().putString("PatikaHTPCount",(Integer.parseInt(sharedPreferences.getString("PatikaHTPCount","0 0").split(" ")[0])+1) + " " + sharedPreferences.getString("PatikaHTPCount","0 0").split(" ")[1]).apply();
        } else if (getString(R.string.SayıBulmaca).equals(currentGameName)) {
            ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_sayibulmaca);
            if(!comefrom.equals("GameGuide"))sharedPreferences.edit().putString("SayiBulmacaHTPCount",(Integer.parseInt(sharedPreferences.getString("SayiBulmacaHTPCount","0 0").split(" ")[0])+1) + " " + sharedPreferences.getString("SayiBulmacaHTPCount","0 0").split(" ")[1]).apply();
        } else if (getString(R.string.SözcükTuru).equals(currentGameName)) {
            ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_sozcukturu);
            if(!comefrom.equals("GameGuide"))sharedPreferences.edit().putString("SozcukTuruHTPCount",(Integer.parseInt(sharedPreferences.getString("SozcukTuruHTPCount","0 0").split(" ")[0])+1) + " " + sharedPreferences.getString("SozcukTuruHTPCount","0 0").split(" ")[1]).apply();
        } else if (getString(R.string.Piramit).equals(currentGameName)) {
            ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_piramit);
            if(!comefrom.equals("GameGuide"))sharedPreferences.edit().putString("PiramitHTPCount", (Integer.parseInt(sharedPreferences.getString("PiramitHTPCount","0 0").split(" ")[0])+1) + " " + sharedPreferences.getString("PiramitHTPCount", "0 0").split(" ")[1]).apply();
        } else {
            ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.Comingsoon);
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        goToGameList(null);
    }
}