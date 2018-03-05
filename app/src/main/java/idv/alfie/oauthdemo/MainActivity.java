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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    /*google*/
    private static final int G_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;
    /*facebook*/
    private CallbackManager mCallbackManager;
    /*UI*/
    private TextView tv_type;
    private TextView tv_name;
    private TextView tv_email;
    private TextView tv_oauthId;
    private ImageView iv_picture;
    /*type*/
    private String oauth = "GUEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*UI*/
        findViews();
        /*google*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        /*facebook*/
        //printhashkey();
        //FacebookSdk.sdkInitialize(this.getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
            new FacebookCallback < LoginResult > () {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    updateFacebookInfo(loginResult.getAccessToken());
                }
                @Override
                public void onCancel() {
                }
                @Override
                public void onError(FacebookException exception) {
                }
            }
        );
    }

    private void findViews() {
        tv_type = (TextView)findViewById(R.id.tvType);
        tv_name = (TextView)findViewById(R.id.tvName);
        tv_email = (TextView)findViewById(R.id.tvEmail);
        tv_oauthId = (TextView)findViewById(R.id.tvOauthId);
        iv_picture = (ImageView)findViewById(R.id.ivPicture);

        //google登入按鈕
        Button btGoogleSignin = (Button)findViewById(R.id.btgin);
        btGoogleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });
        //google登出按鈕
        Button btGoogleSignout = (Button)findViewById(R.id.btgout);
        btGoogleSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignOut();
            }
        });

        //facebook登入按鈕
        Button btFacebookSignin = (Button)findViewById(R.id.btfin);
        btFacebookSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookSignIn();
            }
        });
        //facebook登出按鈕
        Button btfacebookSignout = (Button)findViewById(R.id.btfout);
        btfacebookSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookSignOut();
            }
        });
    }

    @Override
    protected void onStart() {
        /*google*/
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateGoogleInfo(account);
        /*facebook*/
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        updateFacebookInfo(accessToken);
        super.onStart();
    }

    /*google*/
    private void googleSignIn() {
        if (oauth.equals("GUEST")){
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, G_SIGN_IN);
        }
    }
    /*google*/
    private void googleSignOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateGoogleInfo(null);
            }
        });
    }
    /*facebook*/
    private void facebookSignIn() {
        if (oauth.equals("GUEST")){
            LoginManager.getInstance().logInWithReadPermissions(MainActivity.this,
                    Arrays.asList("public_profile","email", "user_friends" )
            );
//            LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
//            loginButton.setReadPermissions("email");
//            loginButton.setReadPermissions("public_profile");
//            loginButton.registerCallback(mCallbackManager,
//                    new FacebookCallback < LoginResult > () {
//                        @Override
//                        public void onSuccess(LoginResult loginResult) {
//                            updateFacebookInfo(loginResult.getAccessToken());
//                        }
//                        @Override
//                        public void onCancel() {
//                        }
//                        @Override
//                        public void onError(FacebookException exception) {
//                        }
//                    });
//            loginButton.performClick();
        }
    }
    /*facebook*/
    private void facebookSignOut() {
        LoginManager.getInstance().logOut();
        updateFacebookInfo(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*google*/
        if (requestCode == G_SIGN_IN) {
            //登入成功後取得資料
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                //登入成功
                updateGoogleInfo(account);
            } catch (ApiException e) {
                //登入失敗
                updateGoogleInfo(null);
            }
        }
        /*facebook*/
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /*google*/
    private void updateGoogleInfo(GoogleSignInAccount account) {
        if (account != null){
            String name = account.getDisplayName();
            String email = account.getEmail();
            String id = account.getId();
            String picture = String.valueOf(account.getPhotoUrl());
            if (account.getPhotoUrl() == null){
                picture = "https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg";
            }
            updateUI(id, name, email, picture, "GOOGLE");
        }else {
            updateUI("reset","reset","reset","reset", "GOOGLE");
        }
    }
    /*facebook*/
    private void updateFacebookInfo(AccessToken accessToken) {
        if (accessToken != null) {
            GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    try {
                        //String id = object.getString("id");
                        String id = Profile.getCurrentProfile().getId();
                        //String name = object.getString("name");
                        String name = Profile.getCurrentProfile().getName();
                        String email = object.getString("email");
                        String picture = String.valueOf(Profile.getCurrentProfile().getProfilePictureUri(100, 100));
                        updateUI(id, name, email, picture, "FACEBOOK");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email");
            request.setParameters(parameters);
            request.executeAsync();

        }else {
            updateUI("reset","reset","reset","reset","FACEBOOK");
        }
    }
    /*UI*/
    private void updateUI(String id, String name, String email, String picture, String type) {
        if (id.equals("reset")){
            if (oauth.equals(type)){
                tv_oauthId.setText("id");
                tv_name.setText("name");
                tv_email.setText("email");
                iv_picture.setImageResource(R.mipmap.ic_launcher_round);
                oauth = "GUEST";
            }
        }else{
            tv_oauthId.setText(id);
            tv_name.setText(name);
            tv_email.setText(email);
            new DownloadImageTask(iv_picture).execute(picture);
            oauth = type;
        }
        tv_type.setText(oauth);
    }

    /*facebook*/
    public void printhashkey(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
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
