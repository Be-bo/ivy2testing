package com.ivy2testing.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.R;
import com.ivy2testing.main.MainActivity;

/** @author = Zahra Ghavasieh
 * Overview: First activity user encounters when launching app
 * Features: realtime fields check, using Firebase auth for authentication
 * Note: domain check is based on domains currently existing under Firebase.Database/universities
*/
public class LoginActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = "LoginActivity";

    // Views
    private EditText mEmailView;
    private EditText mPasswordView;
    private Button mLoginButton;
    private ProgressBar mProgressBar;

    // Firebase
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore dbRef = FirebaseFirestore.getInstance();

    // Other variables
    private String currentDomain = "";


/* Override Methods
***************************************************************************************************/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        declareViews();

        // Continue with rest of sign up process
        allowInteraction();
        setTextWatcher();
        setFocusListener();
    }


/* Initialization Methods
***************************************************************************************************/

    // Initialize all textViews and buttons
    private void declareViews() {
        mEmailView = findViewById(R.id.login_email);
        mPasswordView = findViewById(R.id.login_password);
        mLoginButton = findViewById(R.id.login_logInButton);
        mProgressBar = findViewById(R.id.login_progressBar);
        mProgressBar.setVisibility(View.GONE);
    }

    // Set up textWatchers for real time error checking
    private void setTextWatcher() {
        // Set up a general textWatcher
        TextWatcher generalTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mLoginButton.setEnabled(!fieldsEmpty());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        // Add textWatcher to fields
        mEmailView.addTextChangedListener(generalTextWatcher);
        mPasswordView.addTextChangedListener(generalTextWatcher);
    }

    // Set up focus listener for for real time error checking
    private void setFocusListener(){
        // Check if email is correct after focus has changed (if format is good, check domain)
        mEmailView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    setInputErrors(
                            mEmailView,
                            getString(R.string.error_invalidEmailFormat),
                            emailOk());
                }
            }
        });
        // Check if password is correct after focus change
        mPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    setInputErrors(
                            mPasswordView,
                            getString(R.string.error_invalidPasswordLength),
                            passwordOk());
            }
        });
    }


/* OnClick Methods
***************************************************************************************************/

    // mLoginButton onClick method
    public void login(View view) {
        barInteraction();
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();
        domainExists();
    }

    // student sign up onClick method
    public void studentSignUp(View view) {
        startActivity(new Intent(this, StudentSignUpActivity.class));
    }

    // student sign up onClick method
    public void orgSignUp(View view) {
        startActivity(new Intent(this, OrganizationSignUpActivity.class));
    }


/* Input Checking Methods
***************************************************************************************************/

    // Set error on an editText view based on a condition
    private void setInputErrors(EditText editText, String error_msg, boolean check){
        if (check) editText.setError(null);
        else editText.setError(error_msg);
    }

    // Check to see if any of fields are empty
    private boolean fieldsEmpty(){
        return
                mEmailView.getText().toString().isEmpty() ||
                mPasswordView.getText().toString().isEmpty();
    }

    // Make sure email has a correct format and is not empty
    private boolean emailOk() {
        String email = mEmailView.getText().toString().trim();
        currentDomain = email.substring(email.indexOf("@") + 1).trim();
        return email.length() > 5 && email.contains("@") && !email.contains(" ") && email.contains(".");
    }

    // Make sure password field is at lest 6 characters long
    private boolean passwordOk() {
        String password = mPasswordView.getText().toString();
        return password.length() > 5;
    }


/* Firebase related Methods
***************************************************************************************************/

    // Get list of users' domains
    private void domainExists(){

        dbRef.collection("universities").document(currentDomain).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc != null && doc.exists()) loginToFirebaseAuth();     // YAY!
                            else {
                                mEmailView.setError(getString(R.string.error_invalidDomain)); // Domain doesn't exist in DB
                                allowInteraction();
                            }
                        } else {
                            toastError("Domain get() failed");
                            allowInteraction();
                        }
                    }
                });
    }

    // Attempt to log in to Firebase Auth
    private void loginToFirebaseAuth(){

        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString();

        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = auth.getCurrentUser();
                    if(user!=null && user.isEmailVerified()){
                        // Save uni domain for auto-logins and send off to MainActivity
                        barInteraction();
                        savePreferences();
                        backToMain();
                    }
                    else {
                        toastError("Email not verified yet!");
                        allowInteraction();
                    }
                } else {
                    mLoginButton.setError(getString(R.string.error_loginInvalid));
                    allowInteraction();
                }
            }
        });
    }


/* Transition Methods
***************************************************************************************************/

    private void allowInteraction(){
        mProgressBar.setVisibility(View.GONE);
        mLoginButton.setVisibility(View.VISIBLE);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void barInteraction() {
        closeKeyboard();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mLoginButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }


    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Save preferences for auto-login
    private void savePreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("domain", currentDomain);
        editor.apply();
    }

    // Load the university domain for auto login
    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE);
        currentDomain = sharedPreferences.getString("domain", "");
    }

    // Go back to main activity
    private void backToMain(){
        final Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("this_user_id", auth.getUid());
        intent.putExtra("this_uni_domain", currentDomain);
        setResult(RESULT_OK, intent);
        finish();
        allowInteraction();
    }


/* Utility Methods
***************************************************************************************************/

    private void toastError(String msg){
        Log.w(TAG, msg);
        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
