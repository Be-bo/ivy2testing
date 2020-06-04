package com.ivy2testing.userProfile;


import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toolbar;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.UUID;

/** @author Zahra Ghavasieh
 * Overview: Edit Student Profile from Student Profile Fragment
 * Notes: Image [crop, compression] not implemented yet
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
        getStudentInfo();       // Load student info from Firestore
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Returning from choosing an image
        if (resultCode == RESULT_OK && requestCode == PICKIMAGE_REQUEST_CODE
                && data != null && data.getData() != null) {

            imgUri = data.getData();

            Picasso.get()
                    .load(data.getData())
                    .fit()
                    .centerCrop()
                    .into(mImg);
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

        // Action bar
        setActionBar((Toolbar) findViewById(R.id.editStudent_toolBar));
        ActionBar actionBar = getActionBar();
        if (actionBar != null){
            actionBar.setTitle("Edit Profile");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        else Log.e(TAG, "no actionbar");
    }

    // Preset fields with current Student info
    private void setFields() {
        loadImage();                                                // Image
        mName.setText(student.getName());                           // Name
        millisToDatePicker(mBirthDay, student.getBirth_millis());   // Calendar

        // Spinner
        int degreeIndex = findStringPosition(student.getDegree().trim(), getResources().getStringArray(R.array.degree_list));
        if (degreeIndex != -1) mDegree.setSelection(degreeIndex);
    }

    // Automatically enable button if name is not empty
    private void setTextWatcher() {
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!student.getName().contentEquals(s)) mSaveButton.setEnabled(nameOk());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Set up focus listener for for real time error checking
    private void setFocusListener(){
        mName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) setInputErrors(mName, getString(R.string.error_invalidName), nameOk());
            }
        });
    }

    // Listener for birthday change
    private void setBirthDayChangeListener(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBirthDay.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    if (student.getBirth_millis() != datePickerToMillis(view)) mSaveButton.setEnabled(true);
                }
            });
        }
    }

    // Listener for degree change
    private void setDegreeChangeListener(){
        mDegree.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (student.getDegree() != mDegree.getSelectedItem()) mSaveButton.setEnabled(degreeOk());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

/* OnClick Methods
***************************************************************************************************/

    // OnClick for Save Button
    public void saveStudentProfileChange(View view) {
        barInteraction();
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

        // Get field values
        String degree = mDegree.getSelectedItem().toString().trim();

        // Check if ok
        setInputErrors(mName, getString(R.string.error_invalidName), nameOk());


        // Save to student
        student.setName(mName.getText().toString().trim());
        student.setBirth_millis(datePickerToMillis(mBirthDay));
        if (!degree.equals("Degree")) student.setDegree(degree);

        saveImage();
    }

    // OnClick for edit image (upload an image from gallery)
    public void editImage(View v) {

        // Set up intent to go to gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};       // Only accept jpeg/png images
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        // onActivityResult is triggered when coming back
        startActivityForResult(intent, PICKIMAGE_REQUEST_CODE);
    }

/* Input Checking Methods
***************************************************************************************************/

    // Set error on an editText view based on a condition
    private void setInputErrors(EditText editText, String error_msg, boolean check){
        if (check) editText.setError(null);
        else editText.setError(error_msg);
    }

    // Make sure Name field is not empty
    private boolean nameOk() {
        return !mName.getText().toString().trim().isEmpty();
    }

    // Make sure Degree field is not chosen as "Degree"
    private boolean degreeOk() {
        return !mDegree.getSelectedItem().toString().trim().equals("Degree");
    }


/* Transition Methods
***************************************************************************************************/

    // Go back to Main Activity
    private void backToMain(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("this_uni_domain", this_uni_domain);
        intent.putExtra("this_user_id", this_user_id);
        intent.putExtra("return_fragId", "p"); //TODO doesn't transmit properly??
        Log.d(TAG, "GIVING CHAR: " + 'p');
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

                    // Set fields with current values and initiate listeners
                    setFields();
                    setTextWatcher();
                    setFocusListener();
                    setBirthDayChangeListener();
                    setDegreeChangeListener();
                }
                else Log.e(TAG,"getStudentInfo: unsuccessful!");
            }
        });
    }

    // Load student profile picture
    // Will throw an exception if file doesn't exist in storage but app continues to work fine
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
                            else {
                                Log.w(TAG, task.getException());
                                student.setProfile_picture(""); // image doesn't exist
                            }
                            allowInteraction();
                        }
                    });
        }
        else allowInteraction();
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
        if (imgUri == null || getCompressedImageBytes() == null){
            saveStudentInfo();
            return;
        }


        // Build storage path
        final String path = "userfiles/" + this_user_id + "/" + UUID.randomUUID().toString() +"JPG";
        StorageReference storageReference = base_storage_ref.child(path);

        // Upload image to storage and proceed to save other user info
        UploadTask uploadTask = storageReference.putBytes(getCompressedImageBytes());
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

    // Compress image
    private byte[] getCompressedImageBytes(){
        if (mImg.getDrawable() != null) {
            BitmapDrawable bitmapDrawable = ((BitmapDrawable) mImg.getDrawable());
            Bitmap bitmap = bitmapDrawable.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        }
        else return null;
    }
}
