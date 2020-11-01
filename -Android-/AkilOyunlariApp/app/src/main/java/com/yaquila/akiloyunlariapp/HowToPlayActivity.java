package com.yaquila.akiloyunlariapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Typeface;
import android.icu.util.MeasureUnit;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class HowToPlayActivity extends AppCompatActivity {

    String currentGameName;

    public void goToGameList(View view){
        Intent intent = new Intent(this, GameListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    public void howToPlayGameListOpen(View view){
        ConstraintLayout constraintLayout = (ConstraintLayout) ((Button) view).getParent();
        RelativeLayout howToPlayGameListRL = (RelativeLayout) constraintLayout.getChildAt(2);
        howToPlayGameListRL.setY(constraintLayout.getBottom());
        howToPlayGameListRL.setVisibility(View.VISIBLE);
        ((Button)view).animate().translationYBy(-constraintLayout.getBottom()).setDuration(300);
        howToPlayGameListRL.animate().translationYBy(-constraintLayout.getBottom()).setDuration(300);
    }

    public void howToPlayGameListClose(View view){
        RelativeLayout howToPlayGameListRL = (RelativeLayout) ((ScrollView)((LinearLayout)((ImageView) view).getParent()).getParent()).getParent();
        ConstraintLayout constraintLayout = (ConstraintLayout) howToPlayGameListRL.getParent();
        Button slideupButton = (Button)constraintLayout.getChildAt(1);
        slideupButton.animate().translationYBy(constraintLayout.getBottom()).setDuration(300);
        howToPlayGameListRL.animate().translationYBy(constraintLayout.getBottom()).setDuration(300);
    }

    public void howToPlayChangeGame(View view){
        Log.i("tag", (String) view.getTag());
        TextView tv = (TextView) view;
        TextView gameNameTV = (TextView) findViewById(R.id.gameNameTV_htp);
        gameNameTV.setText((CharSequence) tv.getTag());
        //TODO change content of the how to play text
        LinearLayout howToPlayGameListLL = (LinearLayout) tv.getParent();
        Typeface mohave_bold = Typeface.createFromAsset(getAssets(), "fonts/mohave_bold.ttf");
        tv.setTypeface(mohave_bold);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 45);
        Typeface mohave_regular = Typeface.createFromAsset(getAssets(), "fonts/mohave_regular.ttf");
        ((TextView) howToPlayGameListLL.findViewWithTag(currentGameName)).setTypeface(mohave_regular);
        ((TextView) howToPlayGameListLL.findViewWithTag(currentGameName)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 37);
        currentGameName = (String) tv.getTag();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);

        Intent intent = getIntent();
        currentGameName = intent.getStringExtra("gameName");
        if (currentGameName!=null){
            Log.i("gameName", intent.getStringExtra("gameName"));
            Toast.makeText(this, currentGameName, Toast.LENGTH_SHORT).show();
        }
        TextView gameNameTV = (TextView) findViewById(R.id.gameNameTV_htp);
        if(currentGameName.contains(" ")){
            gameNameTV.setText(currentGameName.substring(0, currentGameName.indexOf(" "))+"\n"+currentGameName.substring(currentGameName.indexOf(" ")));
        }
        else{
            gameNameTV.setText(currentGameName);
        }
        ConstraintLayout constraintLayout = (ConstraintLayout) ((ScrollView) findViewById(R.id.scrollView_htp)).getParent();
        RelativeLayout howToPlayGameListRL = (RelativeLayout) constraintLayout.getChildAt(2);
        Typeface mohave_bold = Typeface.createFromAsset(getAssets(), "fonts/mohave_bold.ttf");
        ((TextView) howToPlayGameListRL.findViewWithTag(currentGameName)).setTypeface(mohave_bold);
        ((TextView) howToPlayGameListRL.findViewWithTag(currentGameName)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 45);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }
}