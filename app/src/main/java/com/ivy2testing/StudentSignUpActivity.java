package com.ivy2testing;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class StudentSignUpActivity extends AppCompatActivity {
    private EditText email_editText;
    private EditText pass_editText;
    private EditText pass_confirm_editText;
    private EditText degree_editText;
    private ImageView pic_select;
    private Button register_button;

    //Strings for registration
    private String email;
    private String password;
    private String password_confirm;
    private String domain;
    private String degree;


    //Clyde

    //TODO: input checking, no design just basic elements for now -> connect with the activity, pushing the data into Firestore
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_signup);


        setHandlers();

        setListeners();
    }


    //Handlers will be called and set in a separate method
    private void setHandlers(){
       email_editText = findViewById(R.id.student_signup_email);
       pass_editText = findViewById(R.id.student_signup_pass);
       pass_confirm_editText = findViewById(R.id.student_signup_pass_confirm);
       degree_editText = findViewById(R.id.student_signup_degree);
       pic_select = findViewById(R.id.studen_picture_select);
       register_button = findViewById(R.id.student_register_button);
       register_button.setEnabled(false);

    }
    private void setListeners(){
        email_editText.addTextChangedListener(tw);
        pass_editText.addTextChangedListener(tw);
        pass_confirm_editText.addTextChangedListener(tw);
        degree_editText.addTextChangedListener(tw);

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailCheck();
                passCheck();
                passConfirmCheck();
            }
        });

    }

    // This text watcher will be placed on all edit texts to only enable the button after all has been entered
    private TextWatcher tw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String email_input = email_editText.getText().toString().trim();
            String pass_input = pass_editText.getText().toString().trim();
            String pass_confirm_input = pass_confirm_editText.getText().toString().trim();
            String degree_input = degree_editText.getText().toString().trim();

            // as long as the fields all have input the button will be enabled
            register_button.setEnabled(!email_input.isEmpty() &&
                                        !pass_input.isEmpty() &&
                                        !pass_confirm_input.isEmpty() &&
                                        !degree_input.isEmpty() );


        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    // EmailCheck will confirm that the necessary characters are contained then calls Domain check, otherwise sets an error.
    // Variables: email is set here
    private boolean emailCheck(){
        email = email_editText.getText().toString().trim();
        if (email.length() > 3 && email.contains("@") &&  email.contains(".") ){
            email_editText.setError(null);
            return domainCheck(email);
        }
        else {
            email_editText.setError("Please choose a valid email.");
            return false;
        }
    }

    // Domain check will split the string from emailcheck and check if the domain is equiavlent to ucalgary. Otherwise set an error.
    // Variables: domain is set here
    private boolean domainCheck(String email){
        String[] email_array = email.split("@");
        if (email_array[1].equals("ucalgary.ca")){

            email_editText.setError(null);
            domain = email_array[1];
            return true;

        }
        else{
            email_editText.setError("Please choose a Valid University Domain.");
            return false;
        }
    }


    // PassCheck will check if the password is at least 6 characters long, before calling pass confirm check
    // Variables: password is set here
    private boolean passCheck(){
        // Pass text not trimmed in original app?
        password = pass_editText.getText().toString();
        if(password.length() > 6){
            pass_editText.setError(null);
            return passConfirmCheck();
        }
        else{
            pass_editText.setError("Please choose a password over 6 characters.");
            return false;
        }
    }
    // PassConfirmCheck will check if the password confirm field matches password
    // Variables: passwordConfirm is set here
    private boolean passConfirmCheck(){
        password_confirm = pass_confirm_editText.getText().toString().trim();
        if(password_confirm.equals(password)){
            pass_confirm_editText.setError(null);
            return true;
        }
        else{
            pass_confirm_editText.setError("Passwords do not match.");
            return false;
        }
    }

}
