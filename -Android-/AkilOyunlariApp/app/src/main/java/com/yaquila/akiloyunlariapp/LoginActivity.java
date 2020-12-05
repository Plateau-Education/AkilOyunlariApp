package com.yaquila.akiloyunlariapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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

import com.android.volley.AuthFailureError;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    GoogleSignInClient mGoogleSignInClient;
    String signInUpStatus = "SignUp";
    boolean alreadyHaveID = false;
    boolean hasNicknameB = false;
    EditText usernameET;
    EditText passwordET;
    LoadingDialog loadingDialog;

    public class PostRequest extends AsyncTask<String, Void, String> {

        RequestQueue requestQueue;
        String result = null;
        String resId;

        @Override
        protected String doInBackground(String... strings) {
            try {
                result = "{\""+strings[3]+"\":\""+ strings[1] + "\", \"Token\":"+ "\""+strings[2]+ "\"}";
                String URL = strings[0];
                Log.i("URL",URL);
                requestQueue = Volley.newRequestQueue(getApplicationContext());
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject objres = new JSONObject(response);
                            resId = objres.getString("Message");
                            SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
                            sharedPreferences.edit().putString("id",resId).apply();

                            GetRequest getRequest = new GetRequest();
                            //noinspection deprecation
                            getRequest.execute("https://akiloyunlariapp.herokuapp.com","fx!Ay:;<p6Q?C8N{");

                            Log.i("ResId",resId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this, "Wrong Username or Password", Toast.LENGTH_SHORT).show();
                            usernameET.setText("");
                            usernameET.setText("");
                        }
                        Log.i("Volley", response);

                    }
                }, new Response.ErrorListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try{
                            loadingDialog.dismissDialog();
                            if(error.getMessage()==null){
                                Toast.makeText(LoginActivity.this, "This username is already taken. Please choose a different username.", Toast.LENGTH_LONG).show();
                                usernameET.setText("");
                                passwordET.setText("");
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
                    public byte[] getBody() throws AuthFailureError {
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

    @SuppressLint("StaticFieldLeak")
    public class GetRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                StringBuilder result = new StringBuilder();
                SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
                String id = sharedPreferences.getString("id", "non");
                URL reqURL = new URL(strings[0] + "/" + id + "?Token=" +strings[1]);
                HttpURLConnection connection = (HttpURLConnection) reqURL.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setDoOutput(false);
                connection.connect();
                InputStream in;
                int status = connection.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK)  {
                    in = connection.getErrorStream();
                }
                else  {
                    in = connection.getInputStream();
                }
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {

                    char current = (char) data;
                    result.append(current);
                    data = reader.read();
                }
                Log.i("result", result.toString());
                return result.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(String result) {
            //noinspection deprecation
            super.onPostExecute(result);

            try {
                org.json.JSONObject jb = new org.json.JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1).replace("\\",""));
                String id = jb.getString("_id");
                Log.i("idFromJB",id);
                try{
                    String nick = jb.getString("\"Nick\"");
                    Log.i("nickFromJB",nick+"");
                    hasNicknameB = true;
                } catch (Exception e){
                    e.printStackTrace();
                    hasNicknameB=false;
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                if(hasNicknameB)
                    intent.putExtra("hasNickname","Has");
                else
                    intent.putExtra("hasNickname","Doesnt");
                startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
            }
            loadingDialog.dismissDialog();
        }
    }

    public boolean isPasswordAndUsernameSuitable(String username, String password) {
        if(username.length()==0 || password.length()==0){
            return false;
        } else if(username.length() < 4 || username.length() > 12 || password.length() < 4 || password.length() > 12){
            Toast.makeText(this, "Username and password should be 4-12 words.", Toast.LENGTH_LONG).show();
            return false;
        } else if (username.equals("favicon.ico")){
            Toast.makeText(this, "Try another username.", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    public void signClicked(View view){
        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();
        if(!isPasswordAndUsernameSuitable(username,password)){
            return;
        }

        if(signInUpStatus.equals("SignIn")){
            PostRequest postRequest = new PostRequest();
            String postMessage = null;
            try {
                loadingDialogFunc();
                //noinspection deprecation
                postMessage = postRequest.execute("https://akiloyunlariapp.herokuapp.com/" + username, password, "fx!Ay:;<p6Q?C8N{", "SignIn").get();
                Log.i("postId", postMessage);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else{
            PostRequest postRequest = new PostRequest();
            String postMessage = null;
            try {
                loadingDialogFunc();
                //noinspection deprecation
                postMessage = postRequest.execute("https://akiloyunlariapp.herokuapp.com/" + username, password, "fx!Ay:;<p6Q?C8N{", "SignUp").get();
                Log.i("postId", postMessage);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
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
            intent.putExtra("hasNickname","Has");
            startActivity(intent);
            overridePendingTransition(R.anim.do_nothing,R.anim.do_nothing);
        }


        if(!alreadyHaveID) {
            findViewById(R.id.signInChangeButton).setOnClickListener(this);
            usernameET = findViewById(R.id.usernameEditText);
            passwordET = findViewById(R.id.passwordEditText);
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
                //            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                Log.i("account", Objects.requireNonNull(account.getDisplayName()));
                PostRequest postRequest = new PostRequest();
                String postMessage = null;
                try {
                    loadingDialogFunc();
                    //noinspection deprecation
                    postMessage = postRequest.execute("https://akiloyunlariapp.herokuapp.com/" + account.getEmail(), account.getDisplayName(), "fx!Ay:;<p6Q?C8N{", "Google").get();
                    Log.i("postId", postMessage);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
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
                //TODO Change some UI maybe
            } else {
                signInUpStatus = "SignUp";
                ((Button)findViewById(R.id.usernameJoinButton)).setText(R.string.SignUp);
                ((TextView)findViewById(R.id.signInChangeButton)).setText(R.string.HaveAccount);
                //TODO Change some UI maybe
            }
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