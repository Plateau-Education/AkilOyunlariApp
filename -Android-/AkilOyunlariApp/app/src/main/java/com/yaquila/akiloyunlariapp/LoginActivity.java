package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    GoogleSignInClient mGoogleSignInClient;
    String signInUpStatus = "SignUp";
    boolean alreadyHaveID = false;
    EditText usernameET;
    EditText passwordET;
    EditText displaynameET;
    EditText emailET;
    LoadingDialog loadingDialog;
    GoogleSignInAccount signInAccount;

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    public class PostRequest extends AsyncTask<String, Void, String> {

        RequestQueue requestQueue;
        String result = null;
        String resId;

        @Override
        protected String doInBackground(String... strings) {
            try {
                Map<String,String> info = new HashMap<>();
                if(strings[1].equals("Google")){
                    result = "{\""+"Info"+"\":\""+ strings[3] + "\", \"Token\":"+ "\""+strings[2]+ "\"}";
                } else if(strings[1].equals("SignIn")){
                    info.put("email", strings[3]);
                    info.put("password",strings[4]);
                    result = "{\"Info\":" + (new JSONObject(info)).toString() + ", \"Token\":"+ "\""+strings[2]+ "\"}";
                } else {
                    info.put("displayname",strings[3]);
                    info.put("username",strings[4]);
                    info.put("email",strings[5]);
                    info.put("password",strings[6]);
                    result = "{\"Info\":"+ (new JSONObject(info)).toString() + ", \"Token\":"+ "\""+strings[2]+ "\"}";
                }
                Log.i("request",result);
                String URL = strings[0]+strings[1];
                Log.i("URL",URL);
                requestQueue = Volley.newRequestQueue(getApplicationContext());
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        loadingDialog.dismissDialog();
                        try {
                            JSONObject objres = new JSONObject(response);
                            resId = objres.getString("Message");
                            if(resId.contains("User Already")){
                                Toast.makeText(LoginActivity.this, "A user with this email already exists.", Toast.LENGTH_SHORT).show();
                                usernameET.setText("");
                                passwordET.setText("");
                                displaynameET.setText("");
                                emailET.setText("");
                                return;
                            } else if (resId.contains("Not Found")){
                                signInUpStatus = "SignUp";
                                ((Button)findViewById(R.id.usernameJoinButton)).setText(R.string.SignUp);
                                ((TextView)findViewById(R.id.signInChangeButton)).setText(R.string.HaveAccount);
                                ((TextView)findViewById(R.id.signTV)).setText(R.string.SignUp);
                                displaynameET.setVisibility(View.VISIBLE);
                                usernameET.setVisibility(View.VISIBLE);
                                //TODO Change some UI maybe
                                passwordET.setVisibility(View.GONE);
                                displaynameET.setText(signInAccount.getDisplayName());
                                emailET.setText(signInAccount.getEmail());
                                usernameET.setText("");
                                passwordET.setText(getAlphaNumericString(20));
                                return;
                            }
                            SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
                            sharedPreferences.edit().putString("id",resId).apply();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            Log.i("ResId",resId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this, "Wrong Username or Password", Toast.LENGTH_SHORT).show();
                            usernameET.setText("");
                            passwordET.setText("");
                            displaynameET.setText("");
                            emailET.setText("");
                        }
                        Log.i("Volley", response);

                    }
                }, new Response.ErrorListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try{
                            loadingDialog.dismissDialog();
                            if((new String(error.networkResponse.data)).contains("Username")){
                                Toast.makeText(LoginActivity.this, "This username is already taken. Please choose a different username.", Toast.LENGTH_LONG).show();
                                usernameET.setText("");
                                passwordET.setText("");
                                displaynameET.setText("");
                                emailET.setText("");
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        Log.e("VolleyError", error.getMessage()+"");
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public byte[] getBody() {
                        return result == null ? null: result.getBytes(StandardCharsets.UTF_8);
                    }
                };
                requestQueue.add(stringRequest);
                Log.i("resId","id: "+resId);

            } catch (Exception e) {
                e.printStackTrace();
            }
            SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
            return sharedPreferences.getString("id","none");
        }
    }

    public boolean isPasswordAndUsernameSuitable(String username, String password, String email, String displayname) {
        if(signInUpStatus.equals("SignUp")) {
            if (username.length() == 0 || password.length() == 0 || email.length() == 0 || displayname.length() == 0) {
                return false;
            } else if (username.length() < 4 || username.length() > 20 || password.length() < 4 || password.length() > 20 || displayname.length() < 4 || displayname.length() > 20) {
                Toast.makeText(this, "Username, Password and Name&Surname should be 4-20 words.", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        else {
            if (password.length() == 0 || email.length() == 0) {
                return false;
            } else if (password.length() < 4 || password.length() > 20) {
                Toast.makeText(this, "Username, Password and Name&Surname should be 4-20 words.", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    public void signClicked(View view){
        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();
        String email = emailET.getText().toString();
        String displayname = displaynameET.getText().toString();
        if(!isPasswordAndUsernameSuitable(username,password,email,displayname)){
            return;
        }

        PostRequest postRequest = new PostRequest();
        if(signInUpStatus.equals("SignIn")){
            String postMessage;
            try {
                loadingDialogFunc();
                //noinspection deprecation
                postMessage = postRequest.execute("https://akiloyunlariapp.herokuapp.com/user", "SignIn", "fx!Ay:;<p6Q?C8N{", email, password).get();
                Log.i("postId", postMessage);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else{
            String postMessage;
            try {
                loadingDialogFunc();
                //noinspection deprecation
                postMessage = postRequest.execute("https://akiloyunlariapp.herokuapp.com/user" , "SignUp", "fx!Ay:;<p6Q?C8N{", displayname, username, email, password).get();
                Log.i("postId", postMessage);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("InflateParams")
    public void loadingDialogFunc(){
        loadingDialog = new LoadingDialog(LoginActivity.this, getLayoutInflater().inflate(R.layout.loading_dialog,null));
        ((TextView)loadingDialog.dialogView.findViewById(R.id.loadingTextView2)).setText(R.string.PleaseWait);
        loadingDialog.startLoadingAnimation();
    }

    public String getAlphaNumericString(int n){

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.

        SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        if(!"nottaken".equals(sharedPreferences.getString("id","nottaken"))){
            Log.i("user_ID", Objects.requireNonNull(sharedPreferences.getString("id", "nottaken")));
            alreadyHaveID = true;
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.do_nothing,R.anim.do_nothing);
        }


        if(!alreadyHaveID) {
            findViewById(R.id.signInChangeButton).setOnClickListener(this);
            usernameET = findViewById(R.id.usernameEditText);
            passwordET = findViewById(R.id.passwordEditText);
            emailET = findViewById(R.id.emailEditText);
            displaynameET = findViewById(R.id.displaynameEditText);
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            // Build a GoogleSignInClient with the options specified by gso.
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        if(!alreadyHaveID) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            updateUI(account);

            findViewById(R.id.sign_in_button).setOnClickListener(this);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateUI(GoogleSignInAccount account) {
        if(!alreadyHaveID) {
            if (account == null) {
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                Log.i("account", "account unavailable");
                findViewById(R.id.usernameLinearLayout).setVisibility(View.VISIBLE);
                //            final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //            new Handler().postDelayed(new Runnable() {
                //                @Override
                //                public void run() {
                //                    startActivity(intent);
                //                }
                //            },500);
            } else {
                //findViewById(R.id.sign_in_button).setVisibility(View.GONE);
//                Log.i("account", Objects.requireNonNull(account.getDisplayName()));
                signInAccount = account;
                PostRequest postRequest = new PostRequest();
                String postMessage;
                try {
                    loadingDialogFunc();
                    //noinspection deprecation
                    postMessage = postRequest.execute("https://akiloyunlariapp.herokuapp.com/user", "Google", "fx!Ay:;<p6Q?C8N{", account.getEmail()).get();
                    Log.i("postId", postMessage);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

                mGoogleSignInClient.signOut();
            }
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        signInUpStatus = "SignUp";
        ((Button)findViewById(R.id.usernameJoinButton)).setText(R.string.SignUp);
        ((TextView)findViewById(R.id.signInChangeButton)).setText(R.string.HaveAccount);
        ((TextView)findViewById(R.id.signTV)).setText(R.string.SignUp);
        displaynameET.setVisibility(View.VISIBLE);
        usernameET.setVisibility(View.VISIBLE);
        //TODO Change some UI maybe
        usernameET.setText("");
        passwordET.setText("");
        displaynameET.setText("");
        emailET.setText("");
        passwordET.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sign_in_button && !alreadyHaveID) {
            signIn();
            // ...
        } else if (view.getId() == R.id.signInChangeButton) {
            if(signInUpStatus.equals("SignUp")){
                signInUpStatus = "SignIn";
                ((Button)findViewById(R.id.usernameJoinButton)).setText(R.string.SignIn);
                ((TextView)findViewById(R.id.signInChangeButton)).setText(R.string.DontHaveAccount);
                ((TextView)findViewById(R.id.signTV)).setText(R.string.SignIn);
                displaynameET.setVisibility(View.GONE);
                usernameET.setVisibility(View.GONE);
                passwordET.setVisibility(View.VISIBLE);
                //TODO Change some UI maybe
            } else {
                signInUpStatus = "SignUp";
                ((Button)findViewById(R.id.usernameJoinButton)).setText(R.string.SignUp);
                ((TextView)findViewById(R.id.signInChangeButton)).setText(R.string.HaveAccount);
                ((TextView)findViewById(R.id.signTV)).setText(R.string.SignUp);
                displaynameET.setVisibility(View.VISIBLE);
                usernameET.setVisibility(View.VISIBLE);
                //TODO Change some UI maybe
            }
            usernameET.setText("");
            passwordET.setText("");
            displaynameET.setText("");
            emailET.setText("");
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(!alreadyHaveID) {
            // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
            if (requestCode == 1) {
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        if(!alreadyHaveID) {
            try {
                GoogleSignInAccount account = completedTask.getResult(ApiException.class);

                // Signed in successfully, show authenticated UI.
                updateUI(account);
            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.w("signInFail", "signInResult:failed code=" + e.getStatusCode());
                updateUI(null);
            }
        }
    }

}