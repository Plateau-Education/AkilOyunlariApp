package com.yaquila.akiloyunlariapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        String gameName = intent.getStringExtra("gameName");
        if (gameName!=null){
            Log.i("gameName", intent.getStringExtra("gameName"));
            Toast.makeText(this, gameName, Toast.LENGTH_SHORT).show();
        }


    }
}