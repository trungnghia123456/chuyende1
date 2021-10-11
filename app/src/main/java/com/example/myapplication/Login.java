package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private TextView register;
    private CallbackManager callbackManager;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth mFirebaseAuth;
    private TextView textViewUser;
    private AccessTokenTracker accessTokenTracker;
    private ImageView imgProfile;
    private LoginButton loginFacebook;
    private static final String TAO = "FacebookAuthentication";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        register = (TextView) findViewById(R.id.tvRegister);
        register.setOnClickListener(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());
        imgProfile = findViewById(R.id.imgProfile);
        textViewUser = findViewById(R.id.tvUser);
        loginFacebook = findViewById(R.id.loginFB);
        loginFacebook.setReadPermissions("email","public_profile");
        callbackManager = CallbackManager.Factory.create();
        loginFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAO,"onSuccess" + loginResult);
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAO,"OnCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAO,"OnError" + error);
            }
        });
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user!=null)
                {
                    updateUI(user);
                }
                else
                {
                    updateUI(null);
                }
            }
        };
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null)
                {
                    mFirebaseAuth.signOut();
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookToken(AccessToken accessToken) {
        Log.d(TAO,"handleFacebookToken" + accessToken);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    Log.d(TAO,"Sign in with credential: successful");
                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                    updateUI(user);
                }
                else{
                    Log.d(TAO,"Sign in with credential: failed",task.getException());
                    Toast.makeText(Login.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null)
        {
            textViewUser.setText((user.getDisplayName()));
            if (user.getPhotoUrl() != null)
            {
                String photoURL = user.getPhotoUrl().toString();
                photoURL = photoURL + "?type=large";
                Picasso.get().load(photoURL).into(imgProfile);
            }
        }
        else
        {
            textViewUser.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.tvRegister:
                startActivity(new Intent(this,RegisterUser.class));
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener!=null)
        {
            mFirebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}