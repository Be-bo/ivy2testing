package com.ivy2testing;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.ivy2testing.StaticDomainList.domain_list;

public class StudentSignUpActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    // Views
    private EditText email_editText;
    private EditText pass_editText;
    private EditText pass_confirm_editText;
    private Spinner degree_spinner;
    private Button register_button;

    //Strings for registration
    private String email;
    private String password;
    private String password_confirm;
    private String domain;
    private String degree;
    private String id;
    //
    private ProgressBar student_progress_bar;
    //Adapter for spinner
    private ArrayAdapter<CharSequence> degree_adapter;


    //Firebase
    private FirebaseFirestore db_reference = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private Map<String, Object> user_info = new HashMap<String, Object>();

    //  Made By ClydeB on 5/21/2020
    // Pictures wont be added during registration anymore, so all the picture related methods will be commented out. Leaving them at the bottom in case we need them at another point

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_signup);
        setHandlers();
        setListeners();
    }

    // Handlers + adapter for spinner are set
    private void setHandlers() {
        email_editText = findViewById(R.id.student_signup_email);
        pass_editText = findViewById(R.id.student_signup_pass);
        pass_confirm_editText = findViewById(R.id.student_signup_pass_confirm);
        degree_spinner = findViewById(R.id.student_signup_degree);
        register_button = findViewById(R.id.student_register_button);
        student_progress_bar = findViewById(R.id.signup_progressBar);
        register_button.setEnabled(false);


        // Creating and applying adapter to the spinner
        degree_adapter = ArrayAdapter.createFromResource(this, R.array.degree_list, android.R.layout.simple_spinner_item);
        degree_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        degree_spinner.setAdapter(degree_adapter);
    }


    // Listeners are set. EditTexts each have an on text changed listener + focus changed listener
    // Degree spinner has a special listener that is constantly firing. Be careful making methods that require it's input
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
                Objects.requireNonNull(getCurrentFocus()).clearFocus();
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

    // These methods decide what happens on spinner item selection.
    // The onItemSelected method constantly fires so beware. Changing the degree field will keep enabling the signup button. To cancel this, the other errorSetting functions are called on degree selection
    // When the user interacts with the other fields, the textWatchers will re-disable the signup button, and this function will wait for further input.

    // Bugs: (with text watchers enabling register button) choosing one item -> click signup -> receive error -> open spinner -> choose another item = crash
    //       (with empty editTexts) choose degree item -> click signup -> crash
    // Degree selector enabling Signup with incorrect EditText fields can cause crashes. The EditTexts don't have this issue.
    // Calling errorCheckers here will mitigate crashes by encouraging the user to fix input so the 2nd signup attempt is either correct or changed thus no crash.

    // Variables: degree is set here
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        degree = parent.getItemAtPosition(position).toString().trim();
        if (!degree.equals("Degree")) {
            register_button.setEnabled(true);
            emailCheck();
            passCheck();
            passConfirmCheck();
        }
        else {
            register_button.setEnabled(false);
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
                    !pass_confirm_input.isEmpty());
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

    // Creates HashMap for Firestore
    private void initializeProfile() {
        user_info.put("id", id);
        user_info.put("email", email);
        user_info.put("degree", degree);
        user_info.put("uni_domain", domain);
        user_info.put("registration_millis", System.currentTimeMillis());
        //TODO missing messaging token,
    }

    // Creates a profile in FireStore with user provided info
    // Else toasts an error and returns to login page
    private void registerInDB() {
        id = auth.getUid();
        if (id != null) {
            initializeProfile();
            // if (picture_selected) storePictureInDB();
            db_reference.collection("universities").document(domain).collection("users").document(id).set(user_info).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    openDialogComplete();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Profile creation failed. Please try again later.", Toast.LENGTH_LONG).show();
                    returnToLogin();
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


// Variables for picture selection
//    private StorageReference db_storage = FirebaseStorage.getInstance().getReference();
//    private final int PICK_IMAGE_INTERNAL = 450;
//    private Uri user_selected_image;
//    private boolean picture_selected;


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


