package com.example.user.hirebuddy_application;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {
    AnimationDrawable logoAnimation;
    RelativeLayout rel_layout1, rel_layout2, rel_layout3;
    ImageView imageView;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            rel_layout3.setVisibility(View.VISIBLE);
        }
    };
    private Button mCustomer, mMechanic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imgView_logo);
        imageView.setBackgroundResource(R.drawable.animation_test);
        logoAnimation = (AnimationDrawable) imageView.getBackground();

        rel_layout1 = (RelativeLayout) findViewById(R.id.relluay1);
        rel_layout2 = (RelativeLayout) findViewById(R.id.relluay2);
        rel_layout3 = (RelativeLayout) findViewById(R.id.relluay3);

        mCustomer = (Button) findViewById(R.id.customer);
        mMechanic = (Button) findViewById(R.id.mechanic);

        mCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CustomerLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mMechanic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MechanicLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        logoAnimation.start();
        handler.postDelayed(runnable, 3600); //Timeout Splash
    }
}

