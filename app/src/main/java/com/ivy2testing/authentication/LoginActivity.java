package com.ivy2testing.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.R;

import java.util.ArrayList;
import java.util.Objects;

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
    private ArrayList<String> domains = new ArrayList<>();
    private String currentDomain = "";


/* Override Methods
***************************************************************************************************/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        declareViews();
        attemptAutoLogin();
        getDomains();
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
                    if (emailOk())
                        setInputErrors(
                                mEmailView,
                                getString(R.string.error_invalidDomain),
                                domainOk());
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

    // Check to see if we still have user's Firebase Auth token if we do, attempt login
    private void attemptAutoLogin() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && auth.getUid() != null && user.isEmailVerified()) {
            loadPreferences();
            if (!currentDomain.equals("")) {
                transToMain();
            } else {
                toastError("Couldn't perform auto-login, please log in manually.");
            }
        }
    }


/* OnClick Methods
***************************************************************************************************/

    // mLoginButton onClick method
    public void login(View view) {
        barInteraction();
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

    // Check to see if email domain exists in database
    private boolean domainOk(){
        if (domains.contains(currentDomain)){
            mEmailView.setError(null);
            return true;
        }
        else mEmailView.setError(getString(R.string.error_invalidDomain));
        return false;
    }


/* Firebase related Methods
***************************************************************************************************/

    // Get list of users' domains
    private void getDomains(){

        dbRef.collection("universities").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){

                            // store domains in ArrayList
                            for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())){
                                String domain = String.valueOf(doc.get("domain"));
                                domains.add(domain);
                                Log.d(TAG, "Added new domain = " + domain);
                            }

                            // Continue with rest of sign up process
                            splashAnimation();
                            allowInteraction();
                            setTextWatcher();
                            setFocusListener();
                        }
                        else toastError("Failed in connecting to collection");
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
                        // Save uni domain for autologins and send off to MainActivity
                        barInteraction();
                        savePreferences();
                        transToMain();
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
    private void transToMain(){
        final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("this_user_id", auth.getUid());
        intent.putExtra("this_uni_domain", currentDomain);
        intent.putExtra("isStudent", getIntent().getBooleanExtra("isStudent",true));
        finish();
        startActivity(intent);
        allowInteraction();
    }


/* UI related Methods
***************************************************************************************************/

    // Loading screen to Login page Animation (currently unused)
    private void splashAnimation(){

        // Get hidden layouts
        final ConstraintLayout rootLayout = findViewById(R.id.login_rootLayout);
        final LinearLayout fieldsLayout = findViewById(R.id.login_fieldsLayout);
        final LinearLayout signupLayout = findViewById(R.id.login_singUps);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Change image constraints
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(rootLayout);
                //constraintSet.connect(R.id.login_images, ConstraintSet.BOTTOM, R.id.login_fieldsLayout, ConstraintSet.TOP,0);
                TransitionManager.beginDelayedTransition(rootLayout);
                constraintSet.applyTo(rootLayout);

                // Stop hiding other fields
                fieldsLayout.setVisibility(View.VISIBLE);
                signupLayout.setVisibility(View.VISIBLE);
            }
        };

        Handler handler = new Handler();
        handler.postDelayed(runnable, 1000); //delayMillis = timeout for splash
    }

/* Utility Methods
***************************************************************************************************/

    private void toastError(String msg){
        Log.w(TAG, msg);
        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

}
