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

public class SudokuUtils {

    public static AppCompatActivity context;

    public static String clickedBox = "-1";
    public static List<String> clueIndexes = new ArrayList<>();
    public static int gridSize = 6;
    public static boolean undoing=false;
    public static boolean[] draftModeActive= new boolean[81];

    public static List<List<String>> operations = new ArrayList<>();
    public static JSONArray question;
    public static JSONArray answer;

    public static void initVars(AppCompatActivity ctx){
        context = ctx;
        clickedBox = "-1";
        clueIndexes = new ArrayList<>();
        gridSize = 6;
        undoing=false;
        draftModeActive= new boolean[81];
        operations = new ArrayList<>();
        question = null;
        answer = null;
    }

    public static void changeClicked(final View view){
        TextView box = (TextView) view;
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        GridLayout numsLayout = context.findViewById(R.id.numsGL_ga);
        String answerIndex = box.getTag().toString();
        if(!clueIndexes.contains(answerIndex)) {
            if (!clickedBox.equals(answerIndex)) {
                if (!clickedBox.equals("-1")) {
                    gridLayout.findViewWithTag(clickedBox).setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                    if (draftModeActive[Integer.parseInt(clickedBox.substring(0, 1)) * gridSize + Integer.parseInt(clickedBox.substring(1))])
                        ((TextView)gridLayout.findViewWithTag(clickedBox)).setTextColor(context.getResources().getColor(R.color.draft_grey));
                    else ((TextView)gridLayout.findViewWithTag(clickedBox)).setTextColor(context.getResources().getColor(R.color.light_red));
                }
                box.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg_shallow));
                box.setTextColor(context.getResources().getColor(R.color.f7f5fa));
                clickedBox = answerIndex;
            } else {
                if (!undoing) {
                    box.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                    if (draftModeActive[Integer.parseInt(clickedBox.substring(0, 1)) * gridSize + Integer.parseInt(clickedBox.substring(1))])
                        box.setTextColor(context.getResources().getColor(R.color.draft_grey));
                    else box.setTextColor(context.getResources().getColor(R.color.light_red));
                    clickedBox = "-1";
                }
            }
            if (!clickedBox.equals("-1")){
                ImageView draftBtn = context.findViewById(R.id.draftbutton_ga);
                if (draftModeActive[Integer.parseInt(clickedBox.substring(0, 1)) * gridSize + Integer.parseInt(clickedBox.substring(1))]) {
                    for (int i = 1; i < gridSize + 1; i++) {
                        ((Button) numsLayout.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    }
                    draftBtn.setBackground(context.getResources().getDrawable(R.drawable.rounded_light_bluegreen_bg));
                }
                else{
                    for (int i = 1; i < gridSize + 1; i++) {
                        ((Button) numsLayout.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                    }
                    draftBtn.setBackground(context.getResources().getDrawable(R.drawable.nums_gl_bg));
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    public static boolean numClicked(final View view){
        Button btn = (Button) view;
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        GridLayout numGrid = context.findViewById(R.id.numsGL_ga);
        if(!clickedBox.equals("-1")){
            TextView currentBox = gridLayout.findViewWithTag(clickedBox);
            if(currentBox.getText().toString().equals("")){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox,"-1")));
            }
            final int i1 = Integer.parseInt(clickedBox.substring(0, 1)) * gridSize + Integer.parseInt(clickedBox.substring(1));
            if(draftModeActive[i1]){
                if(currentBox.getText().toString().length() == 0){
                    currentBox.setText(btn.getTag().toString());
                    if(gridSize == 9)currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    else currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
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
                            if(gridSize == 9)currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                            else currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                            draftModeActive[i1] = false;
                            for(int i = 1; i<gridSize+1; i++){
                                ((Button)numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,22);
                            }
                            context.findViewById(R.id.draftbutton_ga).setBackground(context.getResources().getDrawable(R.drawable.nums_gl_bg));
                        }
                    }
                    else{
                        if(currentBox.getText().toString().length() <= 10) {
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
            for (int i = 0; i<gridSize; i++){
                for (int j = 0; j<gridSize; j++){
                    if (((TextView)gridLayout.findViewWithTag(Integer.toString(j)+i)).getText().toString().equals("")){
                        isFull = false;
                        break;
                    }
                }
            }
            return isFull;
        }
        return false;
    }

    public static void deleteNum(){
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        if(!clickedBox.equals("-1")){
            TextView currentBox = gridLayout.findViewWithTag(clickedBox);
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
            TextView currentBox = gridLayout.findViewWithTag(co);
            if(num.equals("-1")){
                currentBox.setText("");
            }
            else{
                final int index = Integer.parseInt(co.substring(0, 1)) * gridSize + Integer.parseInt(co.substring(1));
                if(num.length()>1){
                    draftModeActive[index]=false;
                }
                else{
                    draftModeActive[index]=true;
                }
                draftClicked(currentBox);
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
            clickedBox = "-1";
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                    if(!clueIndexes.contains(Integer.toString(j)+i)){
                        tv.setText("");
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    } // Tüm işlemleri sıfırla

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean checkAnswer(){
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        boolean checking=true;
        for(int i = 0; i < gridSize; i++){
            for(int j = 0; j <  gridSize; j++){
                try {
                    if(!((TextView)gridLayout.findViewWithTag(Integer.toString(j)+ i)).getText().equals(((JSONArray)answer.get(i)).get(j).toString())){
                        checking=false;
                    }
                    Log.i("checking",((TextView)gridLayout.findViewWithTag(Integer.toString(j)+i)).getText().toString()+" / "+(((JSONArray)answer.get(i)).get(j).toString()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return checking;
    }

    public static void draftClicked(View view){
        ImageView draftBtn = context.findViewById(R.id.draftbutton_ga);
        GridLayout numGrid = context.findViewById(R.id.numsGL_ga);
        GridLayout questionGrid = context.findViewById(R.id.gridGL_ga);
        TextView currentClickedBox;
        String cb;
        if(view.getId() == R.id.draftbutton_ga){
            currentClickedBox = questionGrid.findViewWithTag(clickedBox);
            cb = clickedBox;
        } else {
            currentClickedBox = (TextView) view;
            cb = (String) currentClickedBox.getTag();
        }
        if(!clickedBox.equals("-1")){
            int boxIndex = Integer.parseInt(cb.substring(0,1))*gridSize+Integer.parseInt(cb.substring(1));
            if(!draftModeActive[boxIndex]){
                if(currentClickedBox.getText().toString().length() == 1) {
                    if(gridSize == 9)currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    else currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
                }
                for(int i = 1; i<gridSize+1; i++){
                    ((Button)numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
                }
                draftModeActive[boxIndex] = true;
                draftBtn.setBackground(context.getResources().getDrawable(R.drawable.rounded_light_bluegreen_bg));
            }
            else{
                if(currentClickedBox.getText().toString().length() <= 1) {
                    if(gridSize == 9)currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    else currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                    for(int i = 1; i<gridSize+1; i++){
                        ((Button)numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,22);
                    }
                    draftModeActive[boxIndex] = false;
                    draftBtn.setBackground(context.getResources().getDrawable(R.drawable.nums_gl_bg));
                }
            }
        }
    }

    public static void seperateGridAnswer(final JSONArray grid) throws JSONException {
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        answer = (JSONArray) grid.get(1);
        question = (JSONArray) grid.get(0);
        Log.i("question",""+question);
        Log.i("answer",""+answer);
        for (int i = 0; i < question.length(); i++){
            JSONArray row = (JSONArray) question.get(i);
            for (int j = 0; j< row.length(); j++){
                if(!row.get(j).toString().equals("0")) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    tv.setText(row.get(j).toString());
                    tv.setTextColor(context.getResources().getColor(R.color.near_black_blue));
                    clueIndexes.add(Integer.toString(j)+i);
                }
            }
        }
    }

    public static void initDraftModeActiveVar(){
        for(int i = 0; i<SudokuUtils.gridSize*SudokuUtils.gridSize; i++){
            draftModeActive[i] = false;
        }
    }

    public static void clearGrid(){
        operations = new ArrayList<>();
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                tv.setText("");
                tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                tv.setEnabled(true);
            }
        }
        clickedBox = "-1";
        clueIndexes = new ArrayList<>();
    }
}
