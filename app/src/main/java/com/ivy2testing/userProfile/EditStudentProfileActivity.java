package com.ivy2testing.userProfile;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ivy2testing.R;
import com.ivy2testing.entities.Student;
import com.ivy2testing.main.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.UUID;

/** @author Zahra Ghavasieh
 * Overview: Edit Student Profile from Student Profile Fragment
 */
public class EditStudentProfileActivity extends Activity {

    // Constants
    private final static String TAG = "StudEditProfileActivity";
    private static final int GALLERY_REQUEST_CODE = 456;

    // Views
    ImageView mImg;
    EditText mName;
    Spinner mDegree;
    DatePicker mBirthDay;
    Button mSaveButton;
    ProgressBar mProgressBar;

    // Firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private Student student;
    private String this_uni_domain;
    private String this_user_id;
    private Uri img_filePath;
    private byte[] raw_bitmap;


/* Override Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_studentprofile);
        declareViews();
        barInteraction();       // Don't allow user to do anything yet
        getIntentExtras();      // Get address of student in database via intent extras
        mSaveButton.setEnabled(true); //TODO delete when implemented textwatchers
        getStudentInfo();       // Load student info from Firestore
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Returning from choosing an image
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST_CODE
            && data != null && data.getData() != null) {

            img_filePath = data.getData();
                try {
                    // Setting image on image view using Bitmap
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), img_filePath);
                    mImg.setImageBitmap(bitmap);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }


/* Initialization Methods
***************************************************************************************************/

    // Get address of student in database
    private void getIntentExtras() {
        if(getIntent() != null) {
            this_uni_domain = getIntent().getStringExtra("this_uni_domain");
            this_user_id = getIntent().getStringExtra("this_user_id");

            if (this_uni_domain == null || this_user_id == null)
                Log.e(TAG, "One of UserID or Domain is null!");
        }
    }

    private void declareViews(){
        mImg = findViewById(R.id.editStudent_img);
        mName = findViewById(R.id.editStudent_name);
        mDegree = findViewById(R.id.editStudent_degree);
        mBirthDay = findViewById(R.id.editStudent_birthdayDatePicker);
        mSaveButton = findViewById(R.id.editStudent_saveButton);
        mProgressBar = findViewById(R.id.editStudent_progressBar);

        // Create and apply a degree adapter to the spinner
        ArrayAdapter<CharSequence> degree_adapter =
            ArrayAdapter.createFromResource(this, R.array.degree_list, android.R.layout.simple_spinner_item);
        degree_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDegree.setAdapter(degree_adapter);
    }

    // Preset fields with current Student info
    private void setFields() {

        loadImage();                                            // Image
        mName.setText(student.getName());                       // Name
        millisToDatePicker(mBirthDay, student.getBirthday());   // Calendar

        // Spinner
        int degreeIndex = findStringPosition(student.getDegree().trim(), getResources().getStringArray(R.array.degree_list));
        if (degreeIndex != -1) mDegree.setSelection(degreeIndex);
    }

    private void setTextWatcher() {
        // TODO
    }

    private void setFocusListener(){
        // TODO
    }

/* OnClick Methods
***************************************************************************************************/

    // OnClick for Save Button
    public void saveStudentProfileChange(View view) {
        barInteraction();

        // Get field values
        long birthday = datePickerToMillis(mBirthDay);
        String name = mName.getText().toString();
        String degree = mDegree.getSelectedItem().toString().trim();
        // image? //TODO


        // Check if ok TODO

        // Save to student
        student.setName(name);
        student.setBirthday(birthday);
        if (!degree.equals("Degree")) student.setDegree(degree);

        saveImage();
    }

    // TODO OnClick for edit image (upload an image from gallery)
    public void editImage(View v) {

        // Set up intent to go to gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        // onActivityResult is triggered when coming back
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

/* UI Related Methods
***************************************************************************************************/




/* Transition Methods
***************************************************************************************************/

    // Go back to Main Activity
    private void backToMain(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("this_uni_domain", this_uni_domain);
        intent.putExtra("this_user_id", this_user_id);
        intent.putExtra("isStudent",true);
        intent.putExtra("returning_fragId", R.id.tab_bar_profile);
        finish();
        startActivity(intent);
    }

    private void allowInteraction(){
        mProgressBar.setVisibility(View.GONE);
        mSaveButton.setVisibility(View.VISIBLE);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void barInteraction() {
        closeKeyboard();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mSaveButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }


    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


/* Firebase Related Methods
***************************************************************************************************/

    // Load student document from database
    private void getStudentInfo(){
        db.collection("universities").document(this_uni_domain).collection("users").document(this_user_id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc == null){
                        Log.e(TAG, "Document doesn't exist");
                        return;
                    }
                    student = doc.toObject(Student.class);
                    if (student == null) Log.e(TAG, "Student object obtained from database is null!");
                    else student.setId(this_user_id);

                    // Continue with rest of sign up process
                    setFields();
                    setTextWatcher();
                    setFocusListener();
                }
                else Log.e(TAG,"getStudentInfo: unsuccessful!");
                allowInteraction();
            }
        });
    }

    // Load student profile picture TODO doesn't work rn
    private void loadImage(){
        // Make sure student has a profile image already
        if (student.getProfile_picture() != null){
            StorageReference storageReference = base_storage_ref.child(student.getProfile_picture());
            //Glide.with(this).load(storageReference).into(mImg);
        }
    }

    // Rewrite old student document with new info
    private void saveStudentInfo(){
        db.collection("universities").document(this_uni_domain).collection("users").document(this_user_id)
                .set(student).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) Log.d(TAG, "Changes saved.");
                else Log.e(TAG, "Something went wrong when trying to save changes.\n" + task.getException());
                allowInteraction();
                backToMain();
            }
        });
    }

    // Rewrite old Pic
    private void saveImage(){

        // Build storage path
        final String path = "userfiles/" + this_user_id + "/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference storageReference = base_storage_ref.child(path);

        // Upload image to storage and proceed to save other user info
        UploadTask uploadTask = storageReference.putFile(img_filePath);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                // Task failed
                if (!task.isSuccessful()) {
                    EditStudentProfileActivity.this.allowInteraction();
                    Log.e(TAG, "Profile Upload Failed.");
                    return;
                }

                // Task was successful
                student.setProfile_picture(path);                   // Save path of profile picture
                EditStudentProfileActivity.this.saveStudentInfo();  // Add user profile to database
            }
        });
    }


/* Utility Methods
***************************************************************************************************/

    // Convert date from a datePicker to milliseconds
    private long datePickerToMillis(DatePicker datePicker){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
        cal.set(Calendar.MONTH, datePicker.getMonth());
        cal.set(Calendar.YEAR, datePicker.getYear());
        return cal.getTimeInMillis();
    }

    // Convert milliseconds to dates on a datePicker
    private void millisToDatePicker(DatePicker datePicker, long millis){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    // Find the index of a (trimmed) string in a string array
    private int findStringPosition(String str, String[] array){
        for (int i = 0; i < array.length; i++){
            if (array[i].trim().equals(str)) return i;
        }
        return -1; // Not found
    }

/*
Notes:
make sure the birthday is saved in the appropriate variable based on the db schema.
It has to be saved as System.curentTimeInMillis() which is # of milliseconds since Jan 1 1970, i.e. epoch time.
Everything in there should function as expected (simlar to login...).
Don't try to style the nav bar at the top, just use the default.
You'll have to use a navbar style for that activity and set its parent to be the MainActivity in the manifest.
*/

}
