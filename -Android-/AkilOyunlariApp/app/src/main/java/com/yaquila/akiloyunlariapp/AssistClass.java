package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.content.Context;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AssistClass {

    /*            */
    /*   Common   */
    /*            */

    @SuppressLint("DefaultLocale")
    public static String formatTime(int secs) {
        return String.format("%02d:%02d", (secs % 3600) / 60, secs % 60);
    }

    public static Object[] resetGrid(final AppCompatActivity context, final View view, final List<String> clueIndexes, final int gridSize, List<List<String>> operations, String clickedBox){
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
        return new Object[]{operations, clickedBox};
    } // Tüm işlemleri sıfırla

    /*            */
    /* Hazine Avı */
    /*            */

    public static String changeSwitch(final View view, String switchPosition){
        ImageView switchTV = (ImageView) view;
        if(switchPosition.equals("diamond")){
            switchTV.setImageResource(R.drawable.ic_cross);
            switchPosition = "cross";
        }
        else if(switchPosition.equals("cross")){
            switchTV.setImageResource(R.drawable.ic_diamond);
            switchPosition = "diamond";
        }
        return switchPosition;
    } // Elmas - çarpı değiştir

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static Object[] changeClicked(final Context context, final View view, final String switchPosition, final List<String> clueIndexes, List<List<String>> operations, String clickedBox){
        TextView box = (TextView) view;
        String answerIndex = box.getTag().toString();
        if (!clueIndexes.contains(answerIndex)) {
            String op = null;
            if (switchPosition.equals("diamond")) {
                if (Objects.equals(box.getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.ic_diamond).getConstantState())) {
                    box.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                    op = "0";
                } else {
                    box.setBackground(context.getResources().getDrawable(R.drawable.ic_diamond));
                    op = "-1";
                }
            } else if (switchPosition.equals("cross")) {
                if (Objects.equals(box.getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.ic_cross).getConstantState())) {
                    box.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                    op = "0";
                } else {
                    box.setBackground(context.getResources().getDrawable(R.drawable.ic_cross));
                    op = "-2";
                }
            }
            clickedBox = answerIndex;
            List<String> newOp = new ArrayList<>(Arrays.asList(answerIndex, op));
            if (operations != null && !newOp.equals(operations.get(operations.size() - 1))) {
                operations.add(new ArrayList<>(Arrays.asList(answerIndex, op)));
            }
            Log.i("operations", operations + "");
        }
        return new Object[]{operations, clickedBox};

    }

    public static Object[] undoOperation(final AppCompatActivity context, List<List<String>> operations){
        String co1 = "0";
        String num2 = "0";
        if(operations.size() > 1){
            //            operations = operations.subList(0,operations.size()-1);
            List<String> tuple1 = operations.get(operations.size()-1);
            operations = operations.subList(0,operations.size()-1);
            co1 = tuple1.get(0);
            num2 = "0";
            String co2;
            for(int i = operations.size()-1; i>0; i--){
                List<String> tuple2 = operations.get(i);
                co2 = tuple2.get(0);
                if(co1.equals(co2)){
                    num2 = tuple2.get(1);
                    break;
                }
            }
            Log.i("co/num",co1+" / "+num2);
            GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
            TextView currentBox = gridLayout.findViewWithTag(co1);
            if(num2.equals("-1")){
                currentBox.setBackground(context.getResources().getDrawable(R.drawable.ic_diamond));
            }
            else if(num2.equals("-2")){
                currentBox.setBackground(context.getResources().getDrawable(R.drawable.ic_cross));
            }
            else{
                currentBox.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
            }
        }
        return new Object[]{operations, co1, num2};
    } // Son işlemi geri al

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean checkAnswer(final AppCompatActivity context, final int gridSize, final List<String> answer, final GridLayout gridLayout){
        boolean checking=true;
        for(int i = 0; i<gridSize; i++){
            for(int j = 0; j<gridSize; j++){
                String co = Integer.toString(j)+i;
                if(answer.contains(co) && !Objects.equals(gridLayout.findViewWithTag(co).getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.ic_diamond).getConstantState())){
                    checking=false;
                    break;
                }
                else if(!answer.contains(co) && Objects.equals(gridLayout.findViewWithTag(co).getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.ic_diamond).getConstantState())){
                    checking=false;
                    break;
                }
            }
        }
        Log.i("check",checking+"  "+answer);
        return checking;
    } // Çözümün doğruluğunu kontrol et

    public static Object[] seperateGridAnswer(final AppCompatActivity context, final JSONArray grid, final int gridSize, List<String> clueIndexes, List<String> answer) throws JSONException {
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        for(int i = 0; i < gridSize; i++){
            for(int j = 0; j <  gridSize; j++) {
                String n = ((JSONArray)grid.get(i)).get(j).toString();
                if(Integer.parseInt(n) > 0){
                    clueIndexes.add(Integer.toString(j)+i);
                    ((TextView) gridLayout.findViewWithTag(Integer.toString(j)+i)).setText(n);
                }
                else if(n.equals("-1")){
                    answer.add(Integer.toString(j)+i);
                }
            }
        }
        return new Object[]{clueIndexes, answer};
    } // Çekilen soruyu kullanıcıya göster


    /*              */
    /* Sayı Bulmaca */
    /*              */

    public static int changeClicked(final AppCompatActivity context, final View view, final boolean undoing, final boolean[] draftModeActive, int clickedBox){
        TextView box = (TextView) view;
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        int answerIndex = Integer.parseInt((box.getTag().toString()).substring(box.getTag().toString().length()-1));
        if (clickedBox != answerIndex){
            if (clickedBox != -1){
                if (clickedBox == 0){
                    gridLayout.findViewWithTag("answer"+ clickedBox).setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottomleft));
                }
                else{
                    gridLayout.findViewWithTag("answer"+ clickedBox).setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottom));
                }
                ((TextView) gridLayout.findViewWithTag("answer"+clickedBox)).setTextColor(context.getResources().getColor(R.color.light_red));
            }
            if(answerIndex == 0){
                box.setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottomleft_shallow2));
            }
            else{
                box.setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottom_shallow2));
            }
            box.setTextColor(context.getResources().getColor(R.color.f7f5fa));
            clickedBox = answerIndex;
        }
        else{
            if(!undoing){
                if (answerIndex == 0){
                    box.setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottomleft));
                }
                else{
                    box.setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottom));
                }
                box.setTextColor(context.getResources().getColor(R.color.light_red));
                clickedBox = -1;
            }
        }
        if(clickedBox != -1 && draftModeActive[clickedBox]){
            for(int i = 0; i<10; i++){
                ((Button)gridLayout.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
            }
        }
        return clickedBox;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    public static Object[] numClicked(final AppCompatActivity context, final View view, final boolean[] draftModeActive, final int gridSize, final int clickedBox, List<List<Integer>> operations){
        Button btn = (Button) view;
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        if(clickedBox != -1){
            TextView currentBox = gridLayout.findViewWithTag("answer"+ clickedBox);
            if(currentBox.getText().toString().equals("")){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox,-1)));
            }
            if(draftModeActive[clickedBox]){
                currentBox.setText(currentBox.getText().toString()+" "+btn.getTag().toString());
            }
            else{
                currentBox.setText(btn.getTag().toString());
            }
            List<Integer> newOp = new ArrayList<>(Arrays.asList(clickedBox, Integer.parseInt(btn.getTag().toString())));
            if(!newOp.equals(operations.get(operations.size() - 1))){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox, Integer.parseInt(btn.getTag().toString()))));
            }
            boolean isFull = true;
            for (int i = 0; i<gridSize; i++){
                if (((TextView)gridLayout.findViewWithTag("answer"+i)).getText().toString().equals("")){
                    isFull = false;
                    break;
                }
            }
            Log.i("operations",operations+"");
            return new Object[]{operations,isFull};
        }
        return null;
    }

    public static List<List<Integer>> deleteNum(final AppCompatActivity context, final int clickedBox, List<List<Integer>> operations){
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        if(clickedBox != -1){
            TextView currentBox = gridLayout.findViewWithTag("answer"+ clickedBox);
            if(!currentBox.getText().toString().equals("")){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox, -1)));
                Log.i("operations",operations+"");
                currentBox.setText("");
            }
        }
        return operations;
    }

    @SuppressLint("SetTextI18n")
    public static Object[] undoOperation(final AppCompatActivity context, final boolean[] draftModeActive, List<List<Integer>> operations, int clickedBox){
        if(operations.size() > 1){
            operations = operations.subList(0,operations.size()-1);
            List<Integer> tuple = operations.get(operations.size()-1);
            int co = tuple.get(0);
            int num = tuple.get(1);
            Log.i("co/num",co+" / "+num);
            GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
            TextView currentBox = gridLayout.findViewWithTag("answer"+ co);
            if(num == -1){
                currentBox.setText("");
            }
            else{
                currentBox.setText(Integer.toString(num));
            }
            boolean undoing = true;
            clickedBox = changeClicked(context,currentBox, undoing,draftModeActive,clickedBox);
            undoing =false;
            return new Object[]{operations, clickedBox, undoing};
        }
        return null;
    }

    public static Object[] resetGrid(final AppCompatActivity context, final View view, final int gridSize, int clickedBox){
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

            List<List<Integer>> operations = new ArrayList<>();
            GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
            for (int i = 0; i < gridSize; i++) {
                TextView currentBox = gridLayout.findViewWithTag("answer" + i);
                currentBox.setText("");
            }
            if(clickedBox!=-1) {
                if (clickedBox == 0) {
                    gridLayout.findViewWithTag("answer" + clickedBox).setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottomleft));
                } else {
                    gridLayout.findViewWithTag("answer" + clickedBox).setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottom));
                }
            }
            clickedBox = -1;
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                }
            }
            return new Object[]{operations, clickedBox};
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void notesOnGrid(final AppCompatActivity context, final View view, final int gridSize) {
        TextView clickedTV = (TextView) view;
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        String clickedNum = clickedTV.getText().toString();
        if (Objects.equals(clickedTV.getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.stroke_bg).getConstantState())) {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    if (tv.getText().toString().equals(clickedNum)) {
                        tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg_bluegreen));
                    }
                }
            }
        }
        else if (Objects.equals(clickedTV.getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.stroke_bg_bluegreen).getConstantState())) {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    if (tv.getText().toString().equals(clickedNum)) {
                        tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg_red));
                    }
                }
            }
        }
        else if (Objects.equals(clickedTV.getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.stroke_bg_red).getConstantState())) {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    if (tv.getText().toString().equals(clickedNum)) {
                        tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean checkAnswer(final AppCompatActivity context, final int gridSize, final JSONArray answer){
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        boolean checking=true;
        for(int i = 0; i < gridSize; i++){
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

    public static boolean[] draftClicked(final AppCompatActivity context, final int clickedBox, boolean[] draftModeActive){
        GridLayout numGrid = context.findViewById(R.id.numsGL_ga);
        GridLayout questionGrid = context.findViewById(R.id.gridGL_ga);
        if(clickedBox != -1){
            TextView currentClickedBox = questionGrid.findViewWithTag("answer"+ clickedBox);
            if(currentClickedBox.getText().toString().length() == 1){
                if(currentClickedBox.getTextSize() == 25){
                    currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                    for(int i = 0; i<10; i++){
                        ((Button)numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
                    }
                    draftModeActive[clickedBox] = true;
                }
                else{
                    currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
                    for(int i = 0; i<10; i++){
                        ((Button)numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                    }
                    draftModeActive[clickedBox] = false;
                }
            }
            else if (currentClickedBox.getText().toString().length() == 0) {
                if (draftModeActive[clickedBox]) {
                    currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
                    for (int i = 0; i < 10; i++) {
                        ((Button) numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    }
                    draftModeActive[clickedBox] = false;
                }
                else{
                    currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                    for(int i = 0; i<10; i++){
                        ((Button)numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
                    }
                    draftModeActive[clickedBox] = true;
                }
            }
            else{
                draftModeActive[clickedBox] = true;
            }
        }
        return draftModeActive;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    public static JSONArray seperateGridAnswer(final AppCompatActivity context, final JSONArray grid) throws JSONException {
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        JSONArray answer = (JSONArray) grid.get(grid.length() - 1);
        Log.i("jsonGrid",""+grid);
        for (int i = 0; i < grid.length()-1; i++){
            JSONArray row = (JSONArray) grid.get(i);
            for (int j = 0; j< row.length()-1; j++){
                TextView tv = gridLayout.findViewWithTag(Integer.toString(j)+ i);
                tv.setText(row.get(j).toString());
            }
            JSONArray rguide = (JSONArray)row.get(row.length()-1);
            TextView rGuideTV = gridLayout.findViewWithTag("g"+i);
            if(rguide.get(0).toString().equals("0")){
                rGuideTV.setText(rguide.get(1).toString());
            }
            else if(rguide.get(1).toString().equals("0")){
                rGuideTV.setText("+"+rguide.get(0).toString());
            }
            else{
                rGuideTV.setText("+"+rguide.get(0).toString()+"  "+rguide.get(1).toString());
            }
        }
        TextView guideanswer = gridLayout.findViewWithTag("answerguide");
        guideanswer.setText("+"+((JSONArray) answer.get(answer.length()-1)).get(0).toString());
        answer.remove(answer.length()-1);
        return answer;
    }


    /*              */
    /*    Sudoku    */
    /*              */

    public static String changeClicked(final AppCompatActivity context, final View view, final List<String> clueIndexes, final boolean[] draftModeActive, final int gridSize, final boolean undoing, String clickedBox){
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
        return clickedBox;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    public static Object[] numClicked(final AppCompatActivity context, final View view, final boolean[] draftModeActive, final int gridSize, final String clickedBox, List<List<String>> operations){
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
            return new Object[]{operations,isFull};
        }
        return null;
    }

    public static List<List<String>> deleteNum(final AppCompatActivity context, final String clickedBox, List<List<String>> operations){
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        if(!clickedBox.equals("-1")){
            TextView currentBox = gridLayout.findViewWithTag(clickedBox);
            if(!currentBox.getText().toString().equals("")){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox, "-1")));
                Log.i("operations",operations+"");
                currentBox.setText("");
            }
        }
        return operations;
    }

    public static Object[] undoOperation(final AppCompatActivity context, final View view, final List<String> clueIndexes, final int gridSize, boolean[] draftModeActive, List<List<String>> operations, String clickedBox){
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
                    draftModeActive = draftClicked(context, gridSize,clickedBox,draftModeActive);
                }
                else{
                    draftModeActive[index]=true;
                    draftClicked(context,gridSize,clickedBox,draftModeActive);
                }
                currentBox.setText(num);
            }
            boolean undoing=true;
            clickedBox = changeClicked(context,view,clueIndexes,draftModeActive,gridSize,undoing,clickedBox);
            undoing=false;
            return new Object[]{draftModeActive,operations,clickedBox,undoing};
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean checkAnswer(final JSONArray answer, final int gridSize, final GridLayout gridLayout){
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

    public static boolean[] draftClicked(final AppCompatActivity context, final int gridSize, final String clickedBox, boolean[] draftModeActive){
        ImageView draftBtn = context.findViewById(R.id.draftbutton_ga);
        GridLayout numGrid = context.findViewById(R.id.numsGL_ga);
        GridLayout questionGrid = context.findViewById(R.id.gridGL_ga);
        TextView currentClickedBox = questionGrid.findViewWithTag(clickedBox);
        if(!clickedBox.equals("-1")){
            int boxIndex = Integer.parseInt(clickedBox.substring(0,1))*gridSize+Integer.parseInt(clickedBox.substring(1));
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
        return draftModeActive;
    }

    public static Object[] seperateGridAnswer(final AppCompatActivity context, final JSONArray grid, List<String> clueIndexes) throws JSONException {
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        JSONArray answer = (JSONArray) grid.get(1);
        JSONArray question = (JSONArray) grid.get(0);
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
        return new Object[]{answer,question,clueIndexes};
    }
}
