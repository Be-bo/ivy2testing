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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.R;
import com.ivy2testing.util.SpinnerAdapter;

import static com.ivy2testing.util.StaticDomainList.available_domain_list;

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
    private Spinner uni_spinner;
    private Switch org_switch;

    // Firebase
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore dbRef = FirebaseFirestore.getInstance();

    // Other variables
    private String currentDomain = "";
    private SpinnerAdapter uni_adapter;


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
        setUpSpinner();
        setSwitchListener();
    }


/* Initialization Methods
***************************************************************************************************/

    // Initialize all textViews and buttons
    private void declareViews() {
        mEmailView = findViewById(R.id.login_email);
        mPasswordView = findViewById(R.id.login_password);
        mLoginButton = findViewById(R.id.login_logInButton);
        mProgressBar = findViewById(R.id.login_progressBar);
        uni_spinner = findViewById(R.id.login_uni_spinner);
        org_switch = findViewById(R.id.login_org_switch);
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
        mEmailView.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) setInputErrors(mEmailView, getString(R.string.error_invalidEmailFormat), emailOk());
        });
        // Check if password is correct after focus change
        mPasswordView.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) setInputErrors(mPasswordView, getString(R.string.error_invalidPasswordLength), passwordOk());
        });
    }

    private void setSwitchListener(){
        org_switch.setOnCheckedChangeListener(((compoundButton, b) -> {
            if(b) uni_spinner.setVisibility(View.VISIBLE);
            else uni_spinner.setVisibility(View.GONE);
        }));
    }

    private void setUpSpinner(){
        uni_adapter = new SpinnerAdapter(this, available_domain_list);
        uni_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        uni_spinner.setAdapter(uni_adapter);
        uni_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mLoginButton.setEnabled(!fieldsEmpty());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mLoginButton.setEnabled(!fieldsEmpty());
            }
        });
    }


/* OnClick Methods
***************************************************************************************************/

    // mLoginButton onClick method
    public void attemptLogin(View view) {
        barInteraction();
        if(org_switch.isChecked()) currentDomain = uni_spinner.getSelectedItem().toString();
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();
        loginToFirebaseAuth();
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
        boolean bool = mEmailView.getText().toString().isEmpty() || mPasswordView.getText().toString().isEmpty();
        if(!org_switch.isChecked()) return bool;
        else return bool || uni_spinner.getSelectedItem().toString().equals("university");
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
                        transToMainLoggedIn();
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
    private void transToMainLoggedIn(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
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
