package com.yaquila.akiloyunlariapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SizeActivityForTwoSizedGames extends AppCompatActivity {

    String type;

    public void goToGameList(View view){
        Intent intent = new Intent(this, GameListActivity.class);
        intent.putExtra("type",type);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    public void goToDiffs(View view){
        TextView tvdiff = (TextView) view;
        tvdiff.setBackground(getResources().getDrawable(R.drawable.clicked_diff_bg));
        tvdiff.setTextColor(getResources().getColor(R.color.f7f5fa));
        Intent intent = new Intent(this,DifficultyActivity.class);
        if(type.contains("multi")){
            intent = new Intent(getApplicationContext(), MultiplayerActivity.class);
            intent.putExtra("pType",String.valueOf(type.charAt(type.length()-1)));
        }
        intent.putExtra("gameName", view.getTag().toString());
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }


    public void initDiffs(){
        LinearLayout diffList = findViewById(R.id.diffList_d);
        for (int i = 0; i < 2; i++){
            TextView currentTV = (TextView) ((RelativeLayout)diffList.getChildAt(i)).getChildAt(0);
            currentTV.setBackground(getResources().getDrawable(R.drawable.diff_selector_bg));
            currentTV.setTextColor(getResources().getColorStateList(R.color.diff_selector_tvcolor));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_size_for_two_sized_games);
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        initDiffs();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, GameListActivity.class);
        intent.putExtra("type",type);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }
}