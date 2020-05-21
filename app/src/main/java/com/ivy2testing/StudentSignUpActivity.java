package com.ivy2testing;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class StudentSignUpActivity extends AppCompatActivity {
    private EditText email_editText;
    private EditText pass_editText;
    private EditText pass_confirm_editText;
    private ImageView pic_select;
    private Button register_button;


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
        email_editText.findViewById(R.id.student_signup_email);
        pass_editText.findViewById(R.id.student_signup_pass);
        pass_confirm_editText.findViewById(R.id.student_signup_pass_confirm);
        pic_select.findViewById(R.id.studen_picture_select);
        register_button.findViewById(R.id.student_register_button);

    }
    private void setListeners(){
        register_button.setEnabled(false);

    }

    private void emailCheck(){

    }
    private void domainCheck(){

    }
}
