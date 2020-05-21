package com.ivy2testing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/** @author = Zahra Ghavasieh
 * Overview: First activity user encounters when launching app
 * Features: realtime fields check, using Firebase auth for authentication
*/
public class LoginActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = "LoginActivity";

    // Views
    private EditText mEmailView;
    private EditText mPasswordView;
    private Button mLoginButton;
    private TextView mSignUpView;

    // Firebase
    private FirebaseAuth auth = FirebaseAuth.getInstance();


/* Override Methods
***************************************************************************************************/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }


/* OnClick Methods
***************************************************************************************************/

    // mLoginButton onClick method
    public void login(View view) {
    }

    // mSgignUpView onClick method
    public void signup(View view) {
        startActivity(new Intent(this, StudentSignUpActivity.class));
    }


/* Input Checking Methods
***************************************************************************************************/
    //TODO: input checking: .setError(), email.trim()

    //TODO: disable button if conditions not set (TextWatcher)


/* Firebase related Methods
***************************************************************************************************/
    //TODO: sign in to Firebase


}
