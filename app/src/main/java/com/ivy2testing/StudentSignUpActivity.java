package com.ivy2testing;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class StudentSignUpActivity extends AppCompatActivity {
    private EditText email_editText;
    private EditText pass_editText;
    private EditText pass_confirm_editText;
    private EditText degree_editText;
    private TextView pic_select_text;
    private ImageView pic_select;
    private Button register_button;

    //Strings for registration
    private String email;
    private String password;
    private String password_confirm;
    private String domain;
    private String degree;

    private final int PICK_IMAGE_INTERNAL = 450;
    private Uri user_selected_image;
    private boolean picture_selected;
    private String id;


    //Firebase
    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();
    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private Map<String, Object> user_info = new HashMap<String, Object>();

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
       pic_select_text = findViewById(R.id.student_picture_select_text);
       pic_select = findViewById(R.id.studen_picture_select);
       register_button = findViewById(R.id.student_register_button);
       register_button.setEnabled(false);

    }
    private void setListeners(){
        email_editText.addTextChangedListener(tw);
        pass_editText.addTextChangedListener(tw);
        pass_confirm_editText.addTextChangedListener(tw);
        degree_editText.addTextChangedListener(tw);

        pic_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPictureInternal();
            }
        });

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getCurrentFocus()).clearFocus();
                //picCheck();
                if(emailCheck() && passCheck() && degreeCheck() && passConfirmCheck() ){
                    Toast.makeText(getApplicationContext(), "all input is acceptable", Toast.LENGTH_LONG).show();
                    register_button.setEnabled(false);
                    createNewUser();
                }
                else{
                    Toast.makeText(getApplicationContext(), "One or more fields are incorrect", Toast.LENGTH_LONG).show();
                }
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
        password_confirm = pass_confirm_editText.getText().toString();
        if(password_confirm.equals(password)){
            pass_confirm_editText.setError(null);
            return true;
        }
        else{
            pass_confirm_editText.setError("Passwords do not match.");
            return false;
        }
    }

    // degreeCheck currently just checks that degree is set to test
    // Variables: degree is set here
    private boolean degreeCheck(){
        degree = degree_editText.getText().toString().trim();
        if(!degree.equals( "test")){
            degree_editText.setError("type test");
            return false;
        }
        else{
            return true;
        }
    }

    private boolean picCheck(){
        if (picture_selected){
            pic_select_text.setError(null);
            return true;
        }
        else{
            pic_select_text.setError("Please select a Picture.");
            return false;
        }
    }
    private void selectPictureInternal(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_INTERNAL);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_INTERNAL) {
            user_selected_image = data.getData();
            pic_select.setImageURI(user_selected_image);
            picture_selected = true;
        }
    }



    // Firebase Methods
    private void createNewUser(){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            if (auth.getCurrentUser()!= null){
                                auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getApplicationContext(),"Registration Successful! Please check your email", Toast.LENGTH_LONG).show();
                                        registerInDB();
                                    }
                                });
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"Registration Failed! We could not authenticate you", Toast.LENGTH_LONG).show();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Registration Failed! The email is already registered", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void initializeProfile(){
        user_info.put("id", id);
        user_info.put("email", email);
        user_info.put("degree", degree);
        user_info.put("uni_domain", domain);
        user_info.put("registration_millis", System.currentTimeMillis());

        //TODO missing messaging token,

    }

    private void registerInDB() {
        id = auth.getUid();
        if (id != null) {
            initializeProfile();
            if (picture_selected) storePictureInDB();

            db_reference.collection("universities").document(domain).collection("users").document(id).set(user_info).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getApplicationContext(), "profile creation succesful", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "profile creation failed", Toast.LENGTH_LONG).show();

                }
            });


        }
    }



    private void storePictureInDB(){
        String newUUID = UUID.randomUUID().toString();
        final String path = "userimages/" + id + "/" + newUUID + ".jpg";
        StorageReference user_image_storage = db_storage.child(path);
        user_image_storage.putFile(user_selected_image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Toast.makeText(getApplicationContext(), "photo upload succesful", Toast.LENGTH_LONG).show();
                user_info.put("profile_picture", path);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "photo upload failed", Toast.LENGTH_LONG).show();
                //
                user_info.put("profile_picture", "upload failed");
            }
        });

    }

}
