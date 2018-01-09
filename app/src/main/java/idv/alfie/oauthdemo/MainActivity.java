package idv.alfie.oauthdemo;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    /*google*/
    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "OauthDemo";
    private GoogleSignInClient mGoogleSignInClient;
    /*facebook*/
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*google*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //自訂登入按鈕
        Button gSignIn = (Button)findViewById(R.id.btgin);
        gSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        //自訂登出按鈕
        Button gSignOut = (Button)findViewById(R.id.btgout);
        gSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        /*facebook*/
        //printhashkey();

        FacebookSdk.sdkInitialize(this.getApplicationContext());

        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(
                mCallbackManager,
                new FacebookCallback < LoginResult > () {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // Handle success
                        System.out.println("onSuccess");

                        String accessToken = loginResult.getAccessToken()
                                .getToken();
                        Log.i("accessToken", accessToken);

                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {@Override
                                public void onCompleted(JSONObject object,
                                                        GraphResponse response) {

                                    Log.i("LoginActivity",
                                            object.toString());
                                    try {
                                        String id = object.getString("id");
                                        try {
                                            URL profile_pic = new URL(
                                                    "http://graph.facebook.com/" + id + "/picture?type=large");
                                            Log.i("profile_pic",
                                                    profile_pic + "");

                                        } catch (MalformedURLException e) {
                                            e.printStackTrace();
                                        }
                                        String fields = object.getString("fields");
                                        String name = object.getString("name");
                                        String email = object.getString("email");
                                        String gender = object.getString("gender");
                                        String birthday = object.getString("birthday");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields","id,name,email,gender,birthday");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException exception) {
                    }
                }
        );
        //自訂登入按鈕
        Button fSignIn = (Button)findViewById(R.id.btfin);
        fSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(
                        MainActivity.this,
                        Arrays.asList("user_friends", "email", "public_profile")
                );
            }
        });
        //自訂登出按鈕
        Button fSignOut = (Button)findViewById(R.id.btfout);
        fSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
            }
        });



    }


    @Override
    protected void onStart() {
        /*google*/
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
        super.onStart();
    }

    /*google*/
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    /*google*/
    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateUI(null);
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*google*/
        if (requestCode == RC_SIGN_IN) {
            //登入成功後取得資料
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /*google*/
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            //登入成功更新UI
            updateUI(account);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            //登入失敗更新UI
            updateUI(null);
        }
    }

    /*google*/
    private void updateUI(GoogleSignInAccount account) {
        TextView tv_name = (TextView)findViewById(R.id.tvName);
        TextView tv_email = (TextView)findViewById(R.id.tvEmail);
        TextView tv_oauthId = (TextView)findViewById(R.id.tvOauthId);
        ImageView iv_picture = (ImageView)findViewById(R.id.ivPicture);

        if (account != null){
            String name = account.getDisplayName();
            String email = account.getEmail();
            String oauthId = account.getId();
            String picture = String.valueOf(account.getPhotoUrl());
            if (account.getPhotoUrl() == null){
                picture = "https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg";
            }

            tv_name.setText(name);
            tv_email.setText(email);
            tv_oauthId.setText(oauthId);
            new DownloadImageTask(iv_picture).execute(picture);
        }else {
            tv_name.setText("name");
            tv_email.setText("email");
            tv_oauthId.setText("id");
            iv_picture.setImageResource(R.mipmap.ic_launcher_round);
        }

    }





    /*facebook*/
    public void printhashkey(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "idv.alfie.oauthdemo",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }
}
