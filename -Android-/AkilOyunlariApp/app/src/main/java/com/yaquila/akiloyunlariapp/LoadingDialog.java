package com.yaquila.akiloyunlariapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

public class LoadingDialog {

    Activity activity;
    AlertDialog dialog;
    View dialogView;

    LoadingDialog(Activity myActivity, View myDialogView){
        activity = myActivity;
        dialogView = myDialogView;
    }

    public void startLoadingAnimation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(dialogView);
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }
    public void dismissDialog(){
        dialog.dismiss();
    }

}
