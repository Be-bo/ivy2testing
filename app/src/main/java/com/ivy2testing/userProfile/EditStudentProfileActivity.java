package com.ivy2testing.userProfile;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;

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
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.UUID;

/** @author Zahra Ghavasieh
 * Overview: Edit Student Profile from Student Profile Fragment
 */
public class EditStudentProfileActivity extends Activity {

    // Constants
    private final static String TAG = "StudEditProfileActivity";
    private static final int PICKIMAGE_REQUEST_CODE = 456;

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
    private Uri imgUri;


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
        if (resultCode == RESULT_OK && requestCode == PICKIMAGE_REQUEST_CODE
                && data != null && data.getData() != null) {

            imgUri = data.getData();
            mImg.setImageURI(imgUri);
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
        millisToDatePicker(mBirthDay, student.getBirth_millis());   // Calendar

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

        // Check if ok TODO

        // Save to student
        student.setName(name);
        student.setBirth_millis(birthday);
        if (!degree.equals("Degree")) student.setDegree(degree);

        saveImage();
    }

    // TODO OnClick for edit image (upload an image from gallery)
    public void editImage(View v) {

        // Set up intent to go to gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};       // Only accept jpeg/png images
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        // onActivityResult is triggered when coming back
        startActivityForResult(intent, PICKIMAGE_REQUEST_CODE);
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

    // Load student profile picture
    private void loadImage(){
        // Make sure student has a profile image already
        if (student.getProfile_picture() != null){
            base_storage_ref.child(student.getProfile_picture()).getDownloadUrl()
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                Uri path = task.getResult();
                                Picasso.get().load(path).into(mImg);
                            }
                        }
                    });
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

    // Rewrite old Pic adn save to storage
    private void saveImage(){

        // Skip image saving if image hasn't changed
        if (imgUri == null){
            saveStudentInfo();
            return;
        }

        // Build storage path
        final String path = "userfiles/" + this_user_id + "/" + UUID.randomUUID().toString() + getFileExt(imgUri);
        StorageReference storageReference = base_storage_ref.child(path);

        // Upload image to storage and proceed to save other user info
        UploadTask uploadTask = storageReference.putFile(imgUri);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                // Task failed
                if (!task.isSuccessful()) {
                    EditStudentProfileActivity.this.allowInteraction();
                    Log.e(TAG, "Profile Upload Failed.");
                }

                // Task was successful
                else if (task.getResult() != null)
                    student.setProfile_picture(path);     // Save download URL of profile picture


                // Add user profile to database
                EditStudentProfileActivity.this.saveStudentInfo();
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

    // Get file Extension
    private String getFileExt(Uri file){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(file));
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
