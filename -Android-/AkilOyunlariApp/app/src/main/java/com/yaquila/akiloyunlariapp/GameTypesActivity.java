package com.yaquila.akiloyunlariapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class GameTypesActivity extends AppCompatActivity {

    public void goToMainMenu(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    public void goToGameList(View view) {
        TextView tvdiff = (TextView) view;
        if("multi3P multi5P".contains(view.getTag().toString())){
            Toast.makeText(this, getString(R.string.feature_not_active), Toast.LENGTH_SHORT).show();
        } else {
            tvdiff.setBackground(getResources().getDrawable(R.drawable.clicked_diff_bg));
            tvdiff.setTextColor(getResources().getColor(R.color.f7f5fa));
            Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
            intent.putExtra("type", view.getTag().toString());
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        }
    }

    public void goToTournament(View view){
//        TextView tvdiff = (TextView) view;
//        tvdiff.setBackground(getResources().getDrawable(R.drawable.clicked_diff_bg));
//        tvdiff.setTextColor(getResources().getColor(R.color.f7f5fa));
        Toast.makeText(this, getString(R.string.Comingsoon), Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(getApplicationContext(), TournamentActivity.class);
//        startActivity(intent);
//        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_types);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }
}