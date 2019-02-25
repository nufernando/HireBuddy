package com.example.user.hirebuddy_application;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CustomerLoginActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {


    private EditText cEmail, cPassword;
    private TextView signInText;
    private Button cLogin, cRegistration, social;
    private String name, email, photoURL;
    private FirebaseAuth cAuth;
    private FirebaseAuth.AuthStateListener cFirebaseAuthListner;
    private SignInButton signIn;
    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;
    private RelativeLayout fillLayout;
    private Animation slideUp;
    private Animation slideOut;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);
        social = (Button) findViewById(R.id.connect_social);
        signIn = (SignInButton) findViewById(R.id.googlesign_in);
        signInText = (TextView) findViewById(R.id.googlesign_text);
        cEmail = (EditText)findViewById(R.id.email);
        cPassword = (EditText)findViewById(R.id.password);
        fillLayout = (RelativeLayout) findViewById(R.id.relluay1);

        // Animation
        slideUp = AnimationUtils.loadAnimation(this, R.anim.attribute_slide_up);
        slideOut = AnimationUtils.loadAnimation(this, R.anim.attribute_slide_out);

        //Google SignIn
        signIn.setOnClickListener(this);
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,signInOptions).build();

        social.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    social.startAnimation(slideOut);
                    social.setVisibility(View.INVISIBLE);

                    signIn.setVisibility(View.VISIBLE);
                    signIn.startAnimation(slideUp);

                    signInText.setVisibility(View.VISIBLE);
                    signInText.startAnimation(slideUp);
            }
        });

        //Firebase Connection
        cAuth=FirebaseAuth.getInstance();
        cFirebaseAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged( FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(CustomerLoginActivity.this, CustomerMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        /**
         * Customer Registration Process
         */
        cRegistration = (Button) findViewById(R.id.registration);
        cRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = cEmail.getText().toString();
                final  String password = cPassword.getText().toString();

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(CustomerLoginActivity.this, "Cannot proceed without mandatory fields.", Toast.LENGTH_SHORT).show();
                }else {
                    cAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(CustomerLoginActivity.this, "Hey Buddy, Sign Up Error", Toast.LENGTH_SHORT).show();
                            }else{
                                String customerID = cAuth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerID);
                                current_user_db.setValue(true);

                                DatabaseReference customerDetailsRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerID);
                                HashMap hashMap = new HashMap();
                                hashMap.put("CustomerEmail", email);
                                hashMap.put("CustomerName", name);
                                hashMap.put("CustomerProfileImage", photoURL);
                                customerDetailsRef.updateChildren(hashMap);
                            }
                        }
                    });
                }

            }
        });

        /**
         * Customer Login Process
         */
        cLogin = (Button) findViewById(R.id.login);
        cLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = cEmail.getText().toString();
                final  String password = cPassword.getText().toString();
                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(CustomerLoginActivity.this, "Cannot proceed without mandatory fields.", Toast.LENGTH_SHORT).show();
                }else {
                    cAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete( Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(CustomerLoginActivity.this, "Hey Buddy, Sign In Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        cAuth.addAuthStateListener(cFirebaseAuthListner);
    }

    @Override
    protected void onStop() {
        super.onStop();
        cAuth.removeAuthStateListener(cFirebaseAuthListner);
    }

    @Override
    public void onClick(View v) {
        Intent gIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(gIntent, REQ_CODE);

        signIn.startAnimation(slideOut);
        signIn.setVisibility(View.INVISIBLE);

        signInText.startAnimation(slideOut);
        signInText.setVisibility(View.INVISIBLE);

        handler.postDelayed(runnable, 1500); //Timeout Splash

        fillLayout.setVisibility(View.VISIBLE);
        fillLayout.startAnimation(slideUp);

        cRegistration.setVisibility(View.VISIBLE);
        fillLayout.startAnimation(slideUp);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void handleResult(GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            name = account.getDisplayName();
            email = account.getEmail();



            if(account.getPhotoUrl() == null){
                photoURL = "https://lh3.googleusercontent.com/-j0_tfWvEL-c/XG_n6hCX_OI/AAAAAAAAATQ/AX6Nw6cu994oQzUFxSUNgXlE4LNTsWrSQCEwYBhgL/w140-h139-p";
            } else {
                photoURL = account.getPhotoUrl().toString(); //photo_url is String
            }


            //  photoURL = account.getPhotoUrl().toString();

            cEmail.setText(email);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_CODE){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }
}
