package com.ivy2testing.authentication;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ivy2testing.R;
import com.ivy2testing.entities.Organization;
import com.ivy2testing.util.SpinnerAdapter;

import java.util.Objects;

import static com.ivy2testing.util.StaticDomainList.available_domain_list;

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
    private ProgressBar progress_bar;
    private Spinner uni_spinner;

    // Firebase
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore dbRef = FirebaseFirestore.getInstance();

    // Other Variables
    private boolean isClub = false;
    private String current_domain;
    private SpinnerAdapter uni_adapter;









/* Override Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_signup);
        declareViews();
        setTextWatcher();
        setFocusListener();
        setSwitchListener();
        setUpUniSpinner();
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
        progress_bar = findViewById(R.id.org_signup_progressBar);
        uni_spinner = findViewById(R.id.org_signup_uni_spinner);
    }


    // Set up textWatchers for real time error checking
    private void setTextWatcher() {
        // Set up a general textWatcher
        TextWatcher generalTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                register_button.setEnabled(!fieldsEmpty());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        // Add textWatcher to fields
        email_editText.addTextChangedListener(generalTextWatcher);
        pass_editText.addTextChangedListener(generalTextWatcher);
        pass_confirm_editText.addTextChangedListener(generalTextWatcher);
    }

    // Set up focus listener for for real time error checking
    private void setFocusListener(){
        // Check if email is correct after focus has changed
        email_editText.setOnFocusChangeListener((v, hasFocus) ->{
            if (!hasFocus) {
                setInputErrors(email_editText, getString(R.string.error_invalidEmailFormat), emailOk());
                if (emailOk()) setInputErrors(uni_spinner, getString(R.string.error_chooseUni), uniOk());
            }
        });
        // Check if password is correct after focus change
        pass_editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) setInputErrors(pass_editText, getString(R.string.error_invalidPasswordLength), passwordOk());
        });
        // Check if password confirmation matches after focus change
        pass_confirm_editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) setInputErrors(pass_confirm_editText, getString(R.string.error_invalidPasswordMatch), passConfirmOk());
        });
    }

    // Set up switch listener
    private void setSwitchListener(){
        isClub_switch.setOnCheckedChangeListener((buttonView, isChecked) -> isClub = isChecked);
    }

    private void setUpUniSpinner(){
        uni_adapter = new SpinnerAdapter(this, available_domain_list);
        uni_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        uni_spinner.setAdapter(uni_adapter);
    }












/* OnClick Methods
***************************************************************************************************/

    // register_button onClick method
    public void signUp(View view) {
        barInteraction();
        Objects.requireNonNull(getCurrentFocus()).clearFocus();

        // Set errors if any of the inputs is invalid
        setInputErrors(uni_spinner, getString(R.string.error_chooseUni), uniOk());
        setInputErrors(email_editText, getString(R.string.error_invalidEmailFormat), emailOk());
        setInputErrors(pass_editText, getString(R.string.error_invalidPasswordLength), passwordOk());
        setInputErrors(pass_confirm_editText, getString(R.string.error_invalidPasswordMatch), passConfirmOk());

        // Create user if input is valid
        if (emailOk() && passwordOk() && passConfirmOk() && uniOk()) createNewUser();
        else {
            toastError("Registration failed. Please check your input.");
            allowInteraction();
        }
    }










/* Input Checking Methods
***************************************************************************************************/

    // Set error on an editText view based on a condition
    private void setInputErrors(View view, String error_msg, boolean check){
        if(view instanceof EditText) {
            if (!check) ((EditText)view).setError(error_msg);
        }else if(view instanceof Spinner){
            if (!check){
                TextView errorText = (TextView) ((Spinner)view).getSelectedView();
                errorText.setError(error_msg);
            }
        }
    }

    // Check to see if any of fields are empty
    private boolean fieldsEmpty(){
        return
            email_editText.getText().toString().isEmpty() ||
            pass_editText.getText().toString().isEmpty() ||
            pass_confirm_editText.getText().toString().isEmpty();
    }

    // Make sure email has a correct format and is not empty
    private boolean emailOk() {
        String email = email_editText.getText().toString().trim();
        if (email.length() > 5 && email.contains("@") && !email.contains(" ") && email.contains(".")){
            email_editText.setError(null);
            current_domain = email.substring(email.indexOf("@") + 1).trim();
            return true;
        }
        else return false;
    }

    // Make sure domain is good
    private boolean uniOk() {
        return !uni_spinner.getSelectedItem().equals("university");
    }

    // Make sure password field is at lest 6 characters long
    private boolean passwordOk() {
        String password = pass_editText.getText().toString();
        if (password.length() > 5){
            pass_editText.setError(null);
            return true;
        }
        else return false;
    }

    // PassConfirmCheck will check if the password confirm field matches password
    private boolean passConfirmOk() {
        String password = pass_editText.getText().toString();
        String password_confirm = pass_confirm_editText.getText().toString();
        if (password_confirm.equals(password)){
            pass_confirm_editText.setError(null);
            return true;
        }
        else return false;
    }














/* Firebase related Methods
***************************************************************************************************/

    // Add User to FireBase Auth
    private void createNewUser(){
        String email = email_editText.getText().toString().trim();
        String password = pass_editText.getText().toString();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (auth.getCurrentUser() != null) {
                    registerInDB();
                } else {
                    toastError("Registration Failed! We could not authenticate you");
                    allowInteraction();
                }
            } else {
                toastError("Registration Failed! The email is already registered");
                allowInteraction();
            }
        });
    }
    
    private void registerInDB(){
        String id = auth.getUid();
        if (id != null){
            String email = email_editText.getText().toString().trim();

            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> { //cascade data push by first getting the token and when received push the entire profile
                if(task.isSuccessful() && task.getResult() != null){
                    Organization orgUser = new Organization(id, email, isClub);
                    orgUser.setUni_domain(uni_spinner.getSelectedItem().toString());
                    orgUser.setMessaging_token(task.getResult().getToken());

                    dbRef.collection("users").document(id).set(orgUser).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()){
                                    openDialogComplete();
                                    if (auth.getCurrentUser() != null)
                                        auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task2 -> Log.d(TAG,"Email verification sent."));
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Profile creation failed. Please try again later.", Toast.LENGTH_LONG).show();
                                    returnToLogin();
                                }
                                auth.signOut();
                            });
                }
            });
        }
        else Log.e(TAG, "Id was null!");
    }








/* Transition Methods
***************************************************************************************************/

    private void allowInteraction(){
        progress_bar.setVisibility(View.GONE);
        register_button.setVisibility(View.VISIBLE);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void barInteraction() {
        closeKeyboard();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        register_button.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);
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

    // Opens static dialogue on complete profile creation. Can be modified to be used for error messages
    private void openDialogComplete() {
        allowInteraction();
        final Dialog infoDialog = new Dialog(this);
        infoDialog.setContentView(R.layout.dialog_signup_success);
        Button okButton = infoDialog.findViewById(R.id.positive_button);
        okButton.setOnClickListener(v -> {
            infoDialog.cancel();
            returnToLogin();
        });
        ColorDrawable transparentColor = new ColorDrawable(Color.TRANSPARENT);
        if (infoDialog.getWindow() != null)
            infoDialog.getWindow().setBackgroundDrawable(transparentColor);
        infoDialog.setCancelable(true);
        infoDialog.show();
    }


/* Utility Methods
***************************************************************************************************/

    private void toastError(String msg){
        Log.w(TAG, msg);
        Toast.makeText(OrganizationSignUpActivity.this, msg, Toast.LENGTH_LONG).show();
    }
}
