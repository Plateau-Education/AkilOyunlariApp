package com.yaquila.akiloyunlariapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    public void goToGameList(View view){
        Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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