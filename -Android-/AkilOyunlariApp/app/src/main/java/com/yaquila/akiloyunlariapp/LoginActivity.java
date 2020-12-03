package com.yaquila.akiloyunlariapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    GoogleSignInClient mGoogleSignInClient;
    String signInUpStatus = "SignUp";
    boolean alreadyHaveID = false;

    public class PostRequest extends AsyncTask<String, Void, String> {

        RequestQueue requestQueue;
        String result = null;
        String resId;

        @Override
        protected String doInBackground(String... strings) {
            try {
                result = "{\""+strings[3]+"\":\""+ strings[1] + "\"}";
                String URL = strings[0]+"?Token="+strings[2];
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
                            Log.i("ResId",resId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i("Volley", response);
                    }
                }, new Response.ErrorListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley", error.getMessage()+"");
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.

        SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        if(!"nottaken".equals(sharedPreferences.getString("id","nottaken"))){
            alreadyHaveID = true;
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.do_nothing,R.anim.do_nothing);
        }

        if(!alreadyHaveID) {
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
                //TODO Change some UI maybe
            } else {
                signInUpStatus = "SignUp";
                ((Button)findViewById(R.id.usernameJoinButton)).setText(R.string.SignUp);
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