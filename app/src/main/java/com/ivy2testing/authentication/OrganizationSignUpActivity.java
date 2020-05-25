package com.ivy2testing.authentication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** @author Zahra Ghavasieh
 * Overview: Activity for registering organizations such as clubs. Very similar to student sign up
 * Features: realtime fields check, using Firebase for authentication and storage
 * Note: Firestore and Storage not implemented yet
 */
public class OrganizationSignUpActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = "OrganizationSignUpActivity";

    // Views
    private EditText email_editText;
    private EditText pass_editText;
    private EditText pass_confirm_editText;
    private Switch isClub_switch;
    private Button register_button;

    // Firebase
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore dbRef = FirebaseFirestore.getInstance();


/* Override Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_signup);
        declareViews();
        setTextWatcher();
    }


/* Initialization Methods
***************************************************************************************************/

    // Initialize all textViews and buttons
    private void declareViews() {
        email_editText = findViewById(R.id.org_signup_email);
        pass_editText = findViewById(R.id.org_signup_pass);
        pass_confirm_editText = findViewById(R.id.org_signup_pass_confirm);
        isClub_switch = findViewById(R.id.org_signup_switch);
        register_button = findViewById(R.id.org_signup_register_button);
    }

    // Set up textWatchers for real time error checking
    private void setTextWatcher() {
        // Set up a general textWatcher
        TextWatcher generalTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                register_button.setEnabled(allOk());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        // Add textWatcher to fields
        email_editText.addTextChangedListener(generalTextWatcher);
        pass_editText.addTextChangedListener(generalTextWatcher);
        pass_confirm_editText.addTextChangedListener(generalTextWatcher);
    }


/* OnClick Methods
***************************************************************************************************/

    // register_button onClick method
    public void signUp(View view) {
        barInteraction();
        Objects.requireNonNull(getCurrentFocus()).clearFocus();

        if (allOk()) createNewUser();
        else allowInteraction();
    }


/* Input Checking Methods
***************************************************************************************************/

    // Check conditions one by one so ALL errors are set/cleared
    private boolean allOk(){
        boolean checkEmail = emailOk();
        boolean checkPassword = passwordOk();
        boolean checkConfirm = passConfirmOk();
        return checkEmail && checkPassword && checkConfirm;
    }

    // Make sure email has a correct format and is not empty
    private boolean emailOk() {
        String email = email_editText.getText().toString().trim();

        // Check domain if email format is fine
        if (email.length() > 5 && email.contains("@") && !email.contains(" ") && email.contains(".")) {
            email_editText.setError(null);
            return true;
        }
        else email_editText.setError(getString(R.string.error_invalidEmailFormat));
        return false;
    }

    // Make sure password field is at lest 6 characters long
    private boolean passwordOk() {
        String password = pass_editText.getText().toString();

        if (password.length() > 5) {
            pass_editText.setError(null);
            return true;
        }
        else pass_editText.setError(getString(R.string.error_invalidPasswordLength));
        return false;
    }

    // PassConfirmCheck will check if the password confirm field matches password
    private boolean passConfirmOk() {
        String password = pass_editText.getText().toString();
        String password_confirm = pass_confirm_editText.getText().toString();

        if (password_confirm.equals(password)) {
            pass_confirm_editText.setError(null);
            return true;
        } else {
            pass_confirm_editText.setError(getString(R.string.error_invalidPasswordMatch));
            return false;
        }
    }




/* Firebase related Methods
***************************************************************************************************/

    // Add User to FireBase Auth
    private void createNewUser(){
        String email = email_editText.getText().toString().trim();
        String password = pass_editText.getText().toString();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (auth.getCurrentUser() != null) {
                        auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                toastError("Registration Successful! Please check your email");
                                registerInDB();
                            }
                        });
                    } else {
                        toastError("Registration Failed! We could not authenticate you");
                        allowInteraction();
                    }
                } else {
                    toastError("Registration Failed! The email is already registered");
                    allowInteraction();
                }
            }
        });
    }

    // Store user info in database (not implemented yet!)
    private void registerInDB(){
        String id = auth.getUid();
        if (id != null){
            Map<String, Object> user_info = makeProfile(id);
            toastError("Database registration not fully implemented yet!");
            // ADD TO DATABASE
        }
        returnToLogin();
    }

    // Store user info in a map and return it
    private Map<String, Object> makeProfile(String id){
        Map<String, Object> user_info = new HashMap<>();
        user_info.put("id", id);
        user_info.put("email", email_editText.getText().toString().trim());
        user_info.put("isClub", isClub_switch.isActivated());
        user_info.put("registration_millis", System.currentTimeMillis());
        return user_info;
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

    private void returnToLogin() {
        auth.signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);
    }


/* Utility Methods
***************************************************************************************************/

    private void toastError(String msg){
        Log.w(TAG, msg);
        Toast.makeText(OrganizationSignUpActivity.this, msg, Toast.LENGTH_LONG).show();
    }
}
