package com.ivy2testing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.Objects;

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
    }

    // Set up textWatchers for real time error checking
    private void setTextWatcher() {
        // Set up a general textWatcher
        TextWatcher generalTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (allOk())  mLoginButton.setEnabled(true);
                else  mLoginButton.setEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        // Add textWatcher to fields
        mEmailView.addTextChangedListener(generalTextWatcher);
        mPasswordView.addTextChangedListener(generalTextWatcher);
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

    // Check conditions one by one so ALL errors are set/cleared
    private boolean allOk(){
        boolean checkEmail = emailOk();
        boolean checkPassword = passwordOk();
        return checkEmail && checkPassword;
    }

    // Make sure email has a correct format and is not empty
    private boolean emailOk() {
        String email = mEmailView.getText().toString().trim();

        // Check domain if email format is fine
        if (email.length() > 5 && email.contains("@") && !email.contains(" ") && email.contains(".")) {
            mEmailView.setError(null);
            currentDomain = email.substring(email.indexOf("@") + 1).trim();
            return domainOk();
        }
        else mEmailView.setError(getString(R.string.error_invalidEmailFormat));
        return false;
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

    // Make sure password field is at lest 6 characters long
    private boolean passwordOk() {
        String password = mPasswordView.getText().toString();

        if (password.length() > 5) {
            mPasswordView.setError(null);
            return true;
        }
        else mPasswordView.setError(getString(R.string.error_invalidPasswordLength));
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
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void barInteraction() {
        // Animation? TODO
        closeKeyboard();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
        finish();
        startActivity(intent);
        allowInteraction();
    }


/* UI related Methods
***************************************************************************************************/

    // Loading screen to Login page Animation
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
                constraintSet.connect(R.id.login_images, ConstraintSet.BOTTOM, R.id.login_fieldsLayout, ConstraintSet.TOP,0);
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
