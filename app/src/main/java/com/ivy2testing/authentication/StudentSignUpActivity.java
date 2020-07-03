package com.ivy2testing.authentication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ivy2testing.R;
import com.ivy2testing.entities.Student;
import com.ivy2testing.util.SpinnerAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.ivy2testing.util.StaticDomainList.available_domain_list;
import static com.ivy2testing.util.StaticDomainList.domain_list;
import static com.ivy2testing.util.StaticDegreesList.degree_array;

public class StudentSignUpActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText email_editText;
    private EditText pass_editText;
    private EditText pass_confirm_editText;
    private Spinner degree_spinner;
    private Button register_button;

    private String email;
    private String password;
    private String password_confirm;
    private String domain;
    private String degree;
    private String id;

    private ProgressBar student_progress_bar;
    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private Student this_student;

    //  Made By ClydeB on 5/21/2020


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_signup);
        setHandlers();
        setListeners();
    }

    private void setHandlers() {
        email_editText = findViewById(R.id.student_signup_email);
        pass_editText = findViewById(R.id.student_signup_pass);
        pass_confirm_editText = findViewById(R.id.student_signup_pass_confirm);
        degree_spinner = findViewById(R.id.student_signup_degree);
        register_button = findViewById(R.id.student_register_button);
        student_progress_bar = findViewById(R.id.signup_progressBar);
        register_button.setEnabled(false);

        SpinnerAdapter degree_adapter = new SpinnerAdapter(this, degree_array);
        degree_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        degree_spinner.setAdapter(degree_adapter);
    }

    private void setListeners() {
        email_editText.addTextChangedListener(tw);
        pass_editText.addTextChangedListener(tw);
        pass_confirm_editText.addTextChangedListener(tw);
        degree_spinner.setOnItemSelectedListener(this); // listeners set at onItemSelected & onNothingSelected

        // Focus Changed listeners
        email_editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    emailCheck();
                }
            }
        });

        pass_editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    passCheck();
                }
            }
        });

        pass_confirm_editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    passConfirmCheck();
                }
            }
        });

        // The Register button becomes visible after A) all editTexts have input, or B) a degree has been selected
        // Clicking on the button pulls focus from other fields, finalizing their input.

        // On click it will check if the editTexts are correct (or else place an error + toast + become disabled)
        // Then check if the degree field is okay (or else place an error + toast + become disabled)

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO this line is causing the degree null -> degree selected issue  issue
                if(getCurrentFocus()!=null){
                    Objects.requireNonNull(getCurrentFocus()).clearFocus();
                }
                if (emailCheck() && passCheck() && passConfirmCheck()) {
                    if (!degree.equals("Degree")) {
                        barInteraction();
                        createNewUser();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please choose a degree.", Toast.LENGTH_LONG).show();
                        ((TextView) degree_spinner.getSelectedView()).setError("Please choose a degree.");
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "One or more fields are incorrect. ", Toast.LENGTH_LONG).show();
                }
                register_button.setEnabled(false);
            }
        });
    }

    // These mandatory methods decide what happens on spinner item selection.
    // If something other than degree is selected, either errors will be set on EditTexts, or the register button will be re-enabled with all proper input

    // Variables: degree is set here
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        degree = parent.getItemAtPosition(position).toString().trim();
        if(!degree.equals("Degree")&&
                emailCheck()&&
                passCheck()&&
                passConfirmCheck()){
            register_button.setEnabled(true);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // This text watcher is placed each EditText to only enable Signup after each field contains input
    private TextWatcher tw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String email_input = email_editText.getText().toString().trim();
            String pass_input = pass_editText.getText().toString().trim();
            String pass_confirm_input = pass_confirm_editText.getText().toString().trim();

            // as long as the fields all have input the button will be enabled
            register_button.setEnabled(!email_input.isEmpty() &&
                    !pass_input.isEmpty() &&
                    !pass_confirm_input.isEmpty()&&
                    !degree.equals("Degree"));
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    // EmailCheck will confirm that the necessary characters are contained then calls domainCheck, otherwise set an error.
    // Variables: email is set here
    private boolean emailCheck() {
        email = email_editText.getText().toString().trim();
        if (email.length() > 3 && email.contains("@") && email.contains(".")) {
            email_editText.setError(null);
            return domainCheck(email);
        } else {
            email_editText.setError("Please choose a valid email.");
            return false;
        }
    }

    // Domain check will split the string from emailcheck and check if the domain is equivalent to a domain imported from StaticDomainListArray. otherwise set an error.
    // Variables: domain is set here
    private boolean domainCheck(String email) {
        String[] email_array = email.split("@");
        for (String item : domain_list) {
            if (item.equals(email_array[1])) {

                email_editText.setError(null);
                domain = email_array[1];
                return true;
            }
        }
        email_editText.setError("Please choose a Valid University Domain.");
        return false;
    }


    // PassCheck will check if the password is at least 6 characters long, before calling pass confirm check
    // Variables: password is set here
    private boolean passCheck() {
        // Pass text was trimmed in the original app
        password = pass_editText.getText().toString();
        if (password.length() > 6) {
            pass_editText.setError(null);
            return true;
        } else {
            pass_editText.setError("Please choose a password over 6 characters.");
            return false;
        }
    }

    // PassConfirmCheck will check if the password confirm field matches password
    // Variables: passwordConfirm is set here
    private boolean passConfirmCheck() {
        password_confirm = pass_confirm_editText.getText().toString();
        if (password_confirm.equals(password)) {
            pass_confirm_editText.setError(null);
            return true;
        } else {
            pass_confirm_editText.setError("Passwords do not match.");
            return false;
        }
    }


    // Firebase Methods

    // Creates a user and sends email verification. Progress bar incremented after both are successful
    // Else creates error toast
    private void createNewUser() {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            if (auth.getCurrentUser() != null) {
                                auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        registerInDB();
                                    }
                                });
                            } else {
                                allowInteraction();
                                Toast.makeText(getApplicationContext(), "Registration Failed! We could not authenticate you.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            allowInteraction();
                            email_editText.setError("Email already registered");
                            Toast.makeText(getApplicationContext(), "Registration Failed! The email is already registered.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // Creates a profile in FireStore with user provided info
    // Else toasts an error and returns to login page
    private void registerInDB() {
        id = auth.getUid();
        if (id != null) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> { //cascade data reg by first getting the messaging token, when received, register the entire profile
                if(task.isSuccessful() && task.getResult() != null){
                    this_student = new Student(id, degree, email);
                    this_student.setMessaging_token(task.getResult().getToken());
                    Log.d("testudo", "token success, id: " + id);
                    db_reference.collection("universities").document(domain).collection("users").document(id).set(this_student).addOnSuccessListener(aVoid -> openDialogComplete()).addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Profile creation failed. Please try again later.", Toast.LENGTH_LONG).show();
                        returnToLogin();
                    });
                }
            });
        }
    }

    // Logs out user + clears activity and returns to Login
    private void returnToLogin() {
        auth.signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);
    }

    // Opens static dialogue on complete profile creation. Can be modified to be used for error messages
    private void openDialogComplete() {
        student_progress_bar.setVisibility(View.GONE);
        final Dialog infoDialog = new Dialog(this);
        infoDialog.setContentView(R.layout.activity_signup_dialog);
        Button okButton = infoDialog.findViewById(R.id.positive_button);
        //TextView infoText = infoDialog.findViewById(R.id.Info_textview);
        //infoText.setText(info);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoDialog.cancel();
                returnToLogin();
            }
        });
        ColorDrawable transparentColor = new ColorDrawable(Color.TRANSPARENT);
        if (infoDialog.getWindow() != null)
            infoDialog.getWindow().setBackgroundDrawable(transparentColor);
        infoDialog.setCancelable(true);
        infoDialog.show();
    }

    // Stop's the user from interacting with the page while firebase methods are working
    private void barInteraction() {
        student_progress_bar.setVisibility(View.VISIBLE);
        register_button.setVisibility(View.GONE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    // Re-allows interaction
    private void allowInteraction() {
        student_progress_bar.setVisibility(View.GONE);
        register_button.setVisibility(View.VISIBLE);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}


