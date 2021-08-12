package com.yaquila.akiloyunlariapp.gameutils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.yaquila.akiloyunlariapp.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PiramitUtils {

    public static AppCompatActivity context;

    public static String clickedBox = "-1";
    public static int gridSize = 3;
    public static int answerCount = 3;
    public static boolean undoing = false;
    public static boolean[] draftModeActive = new boolean[14];

    public static List<List<String>> operations = new ArrayList<>();
    public static JSONArray answer;

    public static void initVars(AppCompatActivity ctx){
        context = ctx;
        clickedBox = "-1";
        gridSize = 3;
        answerCount = 3;
        undoing = false;
        draftModeActive = new boolean[14];
        operations = new ArrayList<>();
        answer = null;
    }

    public static void changeClicked(final View view){
        TextView box = (TextView) view;
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        GridLayout numsLayout = context.findViewById(R.id.numsGL_ga);
        String answerIndex = (box.getTag().toString()).replace("answer","");
        if (!clickedBox.equals(answerIndex)){
            if (!clickedBox.equals("-1")){
                gridLayout.findViewWithTag("answer"+ clickedBox).setBackground(context.getResources().getDrawable(R.drawable.stroke_bg2));
                if (draftModeActive[Integer.parseInt(clickedBox)])
                    ((TextView)gridLayout.findViewWithTag("answer"+clickedBox)).setTextColor(context.getResources().getColor(R.color.draft_grey));
                else ((TextView)gridLayout.findViewWithTag("answer"+clickedBox)).setTextColor(context.getResources().getColor(R.color.light_red));

            }
            box.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg2_shallow));
            box.setTextColor(context.getResources().getColor(R.color.f7f5fa));
            clickedBox = answerIndex;
        }
        else{
            if(!undoing){
                box.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg2));
                if (draftModeActive[Integer.parseInt(clickedBox)])
                    box.setTextColor(context.getResources().getColor(R.color.draft_grey));
                else box.setTextColor(context.getResources().getColor(R.color.light_red));
                clickedBox = "-1";
            }
        }
        if (!clickedBox.equals("-1")){
            ImageView draftBtn = context.findViewById(R.id.draftbutton_ga);
            if (draftModeActive[Integer.parseInt(clickedBox)]) {
                for (int i = 1; i < 10; i++) {
                    ((Button) numsLayout.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                }
                draftBtn.setBackground(context.getResources().getDrawable(R.drawable.rounded_light_bluegreen_bg));
            }
            else{
                for (int i = 1; i < 10; i++) {
                    ((Button) numsLayout.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                }
                draftBtn.setBackground(context.getResources().getDrawable(R.drawable.nums_gl_bg));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    public static boolean numClicked(final View view){
        Button btn = (Button) view;
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        if(!clickedBox.equals("-1")){
            TextView currentBox = gridLayout.findViewWithTag("answer"+ clickedBox);
            if(currentBox.getText().toString().equals("")){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox,"-1")));
            }
            if(draftModeActive[Integer.parseInt(clickedBox)]){
                if(currentBox.getText().toString().length() == 0){
                    currentBox.setText(btn.getTag().toString());
                    if(gridSize<=4) currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                    else if(gridSize==5) currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    else currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    List<String> newOp = new ArrayList<>(Arrays.asList(clickedBox, btn.getTag().toString()));
                    if(!newOp.equals(operations.get(operations.size() - 1))){
                        operations.add(new ArrayList<>(Arrays.asList(clickedBox, btn.getTag().toString())));
                    }
                }
                else{
                    if(currentBox.getText().toString().contains(btn.getTag().toString())){
                        currentBox.setText(currentBox.getText().toString().replace(" "+btn.getTag().toString(),"").replace(btn.getTag().toString()+" ","").replace(btn.getTag().toString(),""));
                        List<String> newOp = new ArrayList<>(Arrays.asList(clickedBox, currentBox.getText().toString()));
                        if(!newOp.equals(operations.get(operations.size() - 1))){
                            operations.add(new ArrayList<>(Arrays.asList(clickedBox, currentBox.getText().toString())));
                        }
                        if(currentBox.getText().toString().length() == 1){
                            currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                            draftModeActive[Integer.parseInt(clickedBox)] = false;
                            context.findViewById(R.id.draftbutton_ga).setBackground(context.getResources().getDrawable(R.drawable.nums_gl_bg));
                        }
                    }
                    else{
                        if(currentBox.getText().toString().length() <= 8) {
                            currentBox.setText(currentBox.getText().toString()+" "+btn.getTag().toString());
                            List<String> newOp = new ArrayList<>(Arrays.asList(clickedBox, currentBox.getText().toString()));
                            if(!newOp.equals(operations.get(operations.size() - 1))){
                                operations.add(new ArrayList<>(Arrays.asList(clickedBox, currentBox.getText().toString())));
                            }
                        }
                    }
                }
            }
            else{
                currentBox.setText(btn.getTag().toString());
                List<String> newOp = new ArrayList<>(Arrays.asList(clickedBox, btn.getTag().toString()));
                if(!newOp.equals(operations.get(operations.size() - 1))){
                    operations.add(new ArrayList<>(Arrays.asList(clickedBox, btn.getTag().toString())));
                }
            }
            boolean isFull = true;
            for (int i = 0; i<answerCount; i++){
                if (((TextView)gridLayout.findViewWithTag("answer"+i)).getText().toString().equals("")){
                    isFull = false;
                    break;
                }
            }
            Log.i("operations",operations+"");
            return isFull;
        }
        return false;
    }

    public static void deleteNum(){
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        if(!clickedBox.equals("-1")){
            TextView currentBox = gridLayout.findViewWithTag("answer"+ clickedBox);
            if(!currentBox.getText().toString().equals("")){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox, "-1")));
                Log.i("operations",operations+"");
                currentBox.setText("");
            }
        }
    }

    public static void undoOperation(){
        if(operations.size() > 1){
            operations = operations.subList(0,operations.size()-1);
            List<String> tuple = operations.get(operations.size()-1);
            String co = tuple.get(0);
            String num = tuple.get(1);
            Log.i("co/num",co+" / "+num);
            GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
            TextView currentBox = gridLayout.findViewWithTag("answer"+ co);
            if(num.equals("-1")){
                currentBox.setText("");
            }
            else{
                currentBox.setText(num);
            }
            undoing=true;
            changeClicked(currentBox);
            undoing=false;
        }
    }

    public static void resetGrid(final View view){
        try {
            final TextView resetTV = (TextView) view;
            resetTV.setTextColor(context.getResources().getColor(R.color.light_red));
            resetTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
            resetTV.setText(R.string.ResetNormal);
            resetTV.postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetTV.setTextColor(context.getResources().getColorStateList(R.color.reset_selector_tvcolor));
                    resetTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    resetTV.setText(R.string.ResetUnderlined);
                }
            }, 100);

            operations = new ArrayList<>();
            GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
            for (int i = 0; i < answerCount; i++) {
                TextView currentBox = gridLayout.findViewWithTag("answer" + i);
                currentBox.setText("");
            }
            if(!clickedBox.equals("-1")) {
                gridLayout.findViewWithTag("answer" + clickedBox).setBackground(context.getResources().getDrawable(R.drawable.stroke_bg2));
            }
            clickedBox = "-1";
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    if(tv!=null)tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg2));
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean checkAnswer(final GridLayout gridLayout){
        boolean checking=true;
        for(int i = 0; i < answerCount; i++){
            try {
                if(!((TextView)gridLayout.findViewWithTag("answer"+ i)).getText().equals(answer.get(i).toString())){
                    checking=false;
                }
                Log.i("checking",((TextView)gridLayout.findViewWithTag("answer"+ i)).getText().toString()+" / "+(answer.get(i).toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("check",checking+"  "+answer);
        return checking;
    }

    public static void draftClicked(){
        ImageView draftBtn = context.findViewById(R.id.draftbutton_ga);
        GridLayout numGrid = context.findViewById(R.id.numsGL_ga);
        GridLayout questionGrid = context.findViewById(R.id.gridGL_ga);
        TextView currentClickedBox = questionGrid.findViewWithTag("answer"+clickedBox);
        if(!clickedBox.equals("-1")) {
            if (!draftModeActive[Integer.parseInt(clickedBox)]) {
                if (currentClickedBox.getText().toString().length() == 1) {
                    if(gridSize<=4) currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                    else if(gridSize==5) currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    else currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                }
                for (int i = 1; i < 10; i++) {
                    ((Button) numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                }
                draftModeActive[Integer.parseInt(clickedBox)] = true;
                draftBtn.setBackground(context.getResources().getDrawable(R.drawable.rounded_light_bluegreen_bg));
            } else {
                if (currentClickedBox.getText().toString().length() <= 1) {
                    currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                    for (int i = 1; i < 10; i++) {
                        ((Button) numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                    }
                    draftModeActive[Integer.parseInt(clickedBox)] = false;
                    draftBtn.setBackground(context.getResources().getDrawable(R.drawable.nums_gl_bg));
                }
            }
        }
    }

    public static void seperateGridAnswer(final JSONArray grid){
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        try {
            if (gridSize == 3) {
                ((TextView) gridLayout.findViewWithTag("00")).setText(((JSONArray) grid.get(0)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("02")).setText(((JSONArray) grid.get(2)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("22")).setText(((JSONArray) grid.get(2)).get(2).toString());
                answer = new JSONArray(new ArrayList<>(Arrays.asList(((JSONArray) grid.get(1)).get(0).toString(), ((JSONArray) grid.get(1)).get(1).toString(), ((JSONArray) grid.get(2)).get(1).toString())));
            }
            else if (gridSize == 4){
                ((TextView) gridLayout.findViewWithTag("00")).setText(((JSONArray) grid.get(0)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("12")).setText(((JSONArray) grid.get(2)).get(1).toString());
                ((TextView) gridLayout.findViewWithTag("03")).setText(((JSONArray) grid.get(3)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("33")).setText(((JSONArray) grid.get(3)).get(3).toString());
                answer = new JSONArray(new ArrayList<>(Arrays.asList(((JSONArray) grid.get(1)).get(0).toString(), ((JSONArray) grid.get(1)).get(1).toString(), ((JSONArray) grid.get(2)).get(0).toString(), ((JSONArray) grid.get(2)).get(2).toString(), ((JSONArray) grid.get(3)).get(1).toString(), ((JSONArray) grid.get(3)).get(2).toString())));
            }
            else if (gridSize == 5){
                ((TextView) gridLayout.findViewWithTag("00")).setText(((JSONArray) grid.get(0)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("02")).setText(((JSONArray) grid.get(2)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("22")).setText(((JSONArray) grid.get(2)).get(2).toString());
                ((TextView) gridLayout.findViewWithTag("04")).setText(((JSONArray) grid.get(4)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("24")).setText(((JSONArray) grid.get(4)).get(2).toString());
                ((TextView) gridLayout.findViewWithTag("44")).setText(((JSONArray) grid.get(4)).get(4).toString());
                answer = new JSONArray(new ArrayList<>(Arrays.asList(((JSONArray) grid.get(1)).get(0).toString(), ((JSONArray) grid.get(1)).get(1).toString(), ((JSONArray) grid.get(2)).get(1).toString(), ((JSONArray) grid.get(3)).get(0).toString(), ((JSONArray) grid.get(3)).get(1).toString(), ((JSONArray) grid.get(3)).get(2).toString(), ((JSONArray) grid.get(3)).get(3).toString(), ((JSONArray) grid.get(4)).get(1).toString(), ((JSONArray) grid.get(4)).get(3).toString())));
            }
            else{
                ((TextView) gridLayout.findViewWithTag("00")).setText(((JSONArray) grid.get(0)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("02")).setText(((JSONArray) grid.get(2)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("22")).setText(((JSONArray) grid.get(2)).get(2).toString());
                ((TextView) gridLayout.findViewWithTag("14")).setText(((JSONArray) grid.get(4)).get(1).toString());
                ((TextView) gridLayout.findViewWithTag("34")).setText(((JSONArray) grid.get(4)).get(3).toString());
                ((TextView) gridLayout.findViewWithTag("05")).setText(((JSONArray) grid.get(5)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("55")).setText(((JSONArray) grid.get(5)).get(5).toString());
                answer = new JSONArray(new ArrayList<>(Arrays.asList(((JSONArray) grid.get(1)).get(0).toString(), ((JSONArray) grid.get(1)).get(1).toString(), ((JSONArray) grid.get(2)).get(1).toString(), ((JSONArray) grid.get(3)).get(0).toString(), ((JSONArray) grid.get(3)).get(1).toString(), ((JSONArray) grid.get(3)).get(2).toString(), ((JSONArray) grid.get(3)).get(3).toString(), ((JSONArray) grid.get(4)).get(0).toString(), ((JSONArray) grid.get(4)).get(2).toString(), ((JSONArray) grid.get(4)).get(4).toString(), ((JSONArray) grid.get(5)).get(1).toString(), ((JSONArray) grid.get(5)).get(2).toString(), ((JSONArray) grid.get(5)).get(3).toString(), ((JSONArray) grid.get(5)).get(4).toString())));
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void initDraftModeActiveVar(){
        for(int i = 0; i<answerCount; i++){
            draftModeActive[i] = false;
        }
    }

    public static void clearGrid(){
        operations = new ArrayList<>();
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        for (int i = 0; i < answerCount; i++) {
            TextView currentBox = gridLayout.findViewWithTag("answer" + i);
            currentBox.setText("");
            currentBox.setEnabled(true);
        }
        if(!clickedBox.equals("-1")){
            gridLayout.findViewWithTag("answer" + clickedBox).setBackground(context.getResources().getDrawable(R.drawable.stroke_bg2));
        }
        clickedBox = "-1";
    }
}
