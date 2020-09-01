package com.ivy2testing.authentication;

import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ivy2testing.R;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.SpinnerAdapter;

import java.util.Arrays;
import java.util.List;

import static com.ivy2testing.util.StaticDomainList.available_domain_list;

/** @author = Zahra Ghavasieh
 * Overview: First activity user encounters when launching app
 * Features: realtime fields check, using Firebase auth for authentication
 * Note: domain check is based on domains currently existing under Firebase.Database/universities
*/
public class LoginActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = "LoginActivityTag";

    // Views
    private EditText email_edittext;
    private EditText password_edittext;
    private Button login_button;
    private ProgressBar progress_bar;
    private TextView resend_email_textview;

    // Firebase
    private FirebaseAuth auth = FirebaseAuth.getInstance();













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
        email_edittext = findViewById(R.id.login_email);
        password_edittext = findViewById(R.id.login_password);
        login_button = findViewById(R.id.login_logInButton);
        progress_bar = findViewById(R.id.login_progressBar);
        resend_email_textview = findViewById(R.id.login_resendEmail);
        progress_bar.setVisibility(View.GONE);
    }

    // Set up textWatchers for real time error checking
    private void setTextWatcher() {
        // Set up a general textWatcher
        TextWatcher generalTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInput();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        // Add textWatcher to fields
        email_edittext.addTextChangedListener(generalTextWatcher);
        password_edittext.addTextChangedListener(generalTextWatcher);
    }

    // Set up focus listener for for real time error checking
    private void setFocusListener(){
        // Check if email is correct after focus has changed (if format is good, check domain)
        email_edittext.setOnFocusChangeListener((v, hasFocus) -> {
            checkInput();
        });
        // Check if password is correct after focus change
        password_edittext.setOnFocusChangeListener((v, hasFocus) -> {
            checkInput();
        });
    }











/* OnClick Methods
***************************************************************************************************/

    // mLoginButton onClick method
    public void attemptLogin(View view) {
        barInteraction();
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();
        loginToFirebaseAuth();
    }

    // student sign up onClick method
    public void studentSignUp(View view) {
        Intent intent = new Intent(this, StudentSignUpActivity.class);
        startActivity(intent);
    }

    // student sign up onClick method
    public void orgSignUp(View view) {
        Intent intent = new Intent(this, OrganizationSignUpActivity.class);
        startActivity(intent);
    }













/* Input Checking Methods
***************************************************************************************************/

    // Set error on an editText view based on a condition
    private void setInputErrors(EditText editText, String error_msg){
        editText.setError(error_msg);
    }

    // Check to see if any of fields are empty
    private boolean emptyFields(){
        return email_edittext.getText().toString().trim().isEmpty() || password_edittext.getText().toString().trim().isEmpty();
    }

    // Make sure email has a correct format and is not empty
    private boolean emailOk() {
        String email = email_edittext.getText().toString().trim();
        return email.length() > 5 && email.contains("@") && !email.contains(" ") && email.contains(".");
    }

    // Make sure password field is at lest 6 characters long
    private boolean passwordOk() {
        String password = password_edittext.getText().toString();
        return password.length() > 5;
    }

    private boolean allInputOk(){
        if(emptyFields()){
            setInputErrors(email_edittext, "Some fields are empty");
            return false;
        }
        if(!emailOk()){
            setInputErrors(email_edittext, "Invalid email format");
            return false;
        }
        if(!passwordOk()){
            setInputErrors(password_edittext, "Invalid password format");
            return false;
        }
//        if(!uniOk()){
//            setInputErrors(email_edittext, "Invalid domain or domain selection");
//            return false;
//        }
        return true;
    }

    private void checkInput(){
        if(allInputOk()){
            email_edittext.setError(null);
            password_edittext.setError(null);
            login_button.setEnabled(true);
        }else login_button.setEnabled(false);
    }











/* Firebase related Methods
***************************************************************************************************/

    // Attempt to log in to Firebase Auth
    private void loginToFirebaseAuth(){

        String email = email_edittext.getText().toString().trim();
        String password = password_edittext.getText().toString();

        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                FirebaseUser user = auth.getCurrentUser();
                if(user!=null && user.isEmailVerified()){
                    // Save uni domain for auto-logins and send off to MainActivity
                    barInteraction();
                    transToMainLoggedIn();
                } else {
                    toastMessage("Email not verified yet!");
                    showResendEmail(user);
                    auth.signOut();
                    allowInteraction();
                }
            } else {
                toastMessage(getString(R.string.error_loginInvalid));
                allowInteraction();
            }
        });
    }

    private void showResendEmail(FirebaseUser user){
        resend_email_textview.setVisibility(View.VISIBLE);
        resend_email_textview.setOnClickListener(view -> {
            if(user!=null){
                user.sendEmailVerification().addOnCompleteListener(task -> {
                   if(task.isSuccessful()){
                       toastMessage("Verification email sent!");
                       resend_email_textview.setVisibility(View.GONE);
                   }
                   else {
                       toastMessage("Error sending verification email :-( Try restarting the app.");
                       Log.e(TAG, "Error sending verification", task.getException());
                   }
                });
            }
        });
    }















/* Transition Methods
***************************************************************************************************/

    private void allowInteraction(){
        progress_bar.setVisibility(View.GONE);
        login_button.setVisibility(View.VISIBLE);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void barInteraction() {
        closeKeyboard();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        login_button.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);
    }


    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Go back to main activity
    private void transToMainLoggedIn(){
        boolean signedOut = getIntent().getBooleanExtra("signed_out", false);
        if(!signedOut){ //the user came in from Main where they weren't signed in
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result_ok", true);
            setResult(RESULT_OK, returnIntent);
            finish();
            allowInteraction();
        }else{ //the user is changing accounts (i.e. they used the sign out option)
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("result_ok", true);
            setResult(RESULT_OK, intent);
            startActivity(intent);
            finish();
            allowInteraction();
        }

    }











/* Utility Methods
***************************************************************************************************/

    private void toastMessage(String msg){
        Log.w(TAG, msg);
        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
