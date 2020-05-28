package com.ivy2testing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // just immediately jumping into the student sign up page to test it
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }
}
