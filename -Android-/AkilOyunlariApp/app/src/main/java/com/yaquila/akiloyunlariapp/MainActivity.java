package com.yaquila.akiloyunlariapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    public void goToGameList(View view){
        Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    public void nicknameDialog(){
        LayoutInflater factory = LayoutInflater.from(this);
        final View nicknameDialogView = factory.inflate(R.layout.nickname_dialog, null);
        final AlertDialog nicknameDialog = new AlertDialog.Builder(this).create();
        nicknameDialog.setView(nicknameDialogView);

        nicknameDialogView.findViewById(R.id.nicknameButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nicknameET = nicknameDialogView.findViewById(R.id.nicknameET);
                if(nicknameET.getText().length() == 0){
                    Log.i(".",".");
                } else if (nicknameET.getText().length() < 4 || nicknameET.getText().length() < 12){
                    Toast.makeText(getApplicationContext(), "Nickname should be 4-12 words.", Toast.LENGTH_SHORT).show();
                } else {
                    nicknameDialog.dismiss();
                }
            }
        });
        nicknameDialog.show();
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        if(Objects.equals(intent.getStringExtra("hasNickname"), "Has")){
            Log.i("hasNickname","Has");
        } else {
//            Toast.makeText(this, "You dont have a nickname", Toast.LENGTH_SHORT).show();
            nicknameDialog();
        }
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