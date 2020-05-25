package com.ivy2testing.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.ivy2testing.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static  com.ivy2testing.StaticDomainList.domain_list;

public class StudentSignUpActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private EditText email_editText;
    private EditText pass_editText;
    private EditText pass_confirm_editText;
    private Spinner degree_spinner;
    //    private TextView pic_select_text;
//    private ImageView pic_select;
    private Button register_button;

    //Strings for registration
    private String email;
    private String password;
    private String password_confirm;
    private String domain;
    private String degree;
    private String id;

    //
    private ArrayAdapter<CharSequence> degree_adapter;


    private Map<String, String> domain_hash_map = new HashMap<>();


    // Variables for picture selection
//    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();
//    private final int PICK_IMAGE_INTERNAL = 450;
//    private Uri user_selected_image;
//    private boolean picture_selected;


    //Firebase
    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();


    private Map<String, Object> user_info = new HashMap<>();

    //Made By ClydeB on 5/21/2020

    // Pictures wont be added during registration anymore, so all the picture related methods will be commented out. Leaving them in just in case we want them at another point

    //TODO: input checking, no design just basic elements for now -> connect with the activity, pushing the data into Firestore
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_signup);

        setHandlers();
        setListeners();
    }


    //Handlers will be called and set in a separate method
    private void setHandlers() {
        email_editText = findViewById(R.id.student_signup_email);
        pass_editText = findViewById(R.id.student_signup_pass);
        pass_confirm_editText = findViewById(R.id.student_signup_pass_confirm);
        degree_spinner = findViewById(R.id.student_signup_degree);
        register_button = findViewById(R.id.student_register_button);
        register_button.setEnabled(false);

        // creating and applying adapter to the spinner class
        degree_adapter = ArrayAdapter.createFromResource(this, R.array.degree_list, android.R.layout.simple_spinner_item);
        degree_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        degree_spinner.setAdapter(degree_adapter);
    }

    private void setListeners() {
        email_editText.addTextChangedListener(tw);
        pass_editText.addTextChangedListener(tw);
        pass_confirm_editText.addTextChangedListener(tw);

        degree_spinner.setOnItemSelectedListener(this); // set listeners @ onItemSelected & onNothingSelected

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getCurrentFocus()).clearFocus();
                if (emailCheck() && passCheck() && passConfirmCheck()) {
                    Toast.makeText(getApplicationContext(), "all input is acceptable", Toast.LENGTH_LONG).show();
                    register_button.setEnabled(false);
                    createNewUser();
                  //  returnToLogin();
                } else {
                    Toast.makeText(getApplicationContext(), "One or more fields are incorrect", Toast.LENGTH_LONG).show();
                    register_button.setEnabled(false);
                }
            }
        });

    }

    // These methods decide what happens on spinner item selection
    // Variables: degree is set here
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        degree = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

            // as long as the fields all have input the button will be enabled
            register_button.setEnabled(!email_input.isEmpty() &&
                    !pass_input.isEmpty() &&
                    !pass_confirm_input.isEmpty() &&
                    degree != null);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    // EmailCheck will confirm that the necessary characters are contained then calls Domain check, otherwise sets an error.
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

    // Domain check will split the string from emailCheck and check if the domain is equivalent to a domain imported from StaticDomainListArray. Otherwise set an error.
    // Variables: domain is set here
    // This method currently uses a for each loop and is decently fast, but If domain_list was converted to an ArrayList and used.contains the thing might be faster.
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
        // Pass text not trimmed in original app?
        password = pass_editText.getText().toString();
        if (password.length() > 6) {
            pass_editText.setError(null);
            return passConfirmCheck();
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
                                        Toast.makeText(getApplicationContext(), "Registration Successful! Please check your email", Toast.LENGTH_LONG).show();
                                        registerInDB();
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "Registration Failed! We could not authenticate you", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Registration Failed! The email is already registered", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void initializeProfile() {
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
            // if (picture_selected) storePictureInDB();

            db_reference.collection("universities").document(domain).collection("users").document(id).set(user_info).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getApplicationContext(), "profile creation succesful", Toast.LENGTH_LONG).show();
                    returnToLogin();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "profile creation failed", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void returnToLogin() {
        auth.signOut();
       // wait(10000);
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        // what do these flags do
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        finish();
        startActivity(intent);

    }
}



    //These methods check if a picture is selected and prompt a user to go into their phone to choose (only) a picture
    // StorePictureInDB does exactly what its name says lol
/*


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
    */


