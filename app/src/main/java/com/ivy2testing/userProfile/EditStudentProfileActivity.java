package com.ivy2testing.userProfile;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivy2testing.R;
import com.ivy2testing.entities.Student;
import com.ivy2testing.main.MainActivity;
import com.ivy2testing.util.Constant;
import com.ivy2testing.util.ImageUtils;
import com.ivy2testing.util.SpinnerAdapter;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;


/** @author Zahra Ghavasieh
 * Overview: Edit Student Profile from Student Profile Fragment
 */
public class EditStudentProfileActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "EditStudProfileActivity";

    // Views
    private ImageView mImg;
    private EditText mName;
    private Spinner mDegree;
    private DatePicker mBirthDay;
    private Button mSaveButton;
    private ProgressBar mProgressBar;

    // Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference base_storage_ref = FirebaseStorage.getInstance().getReference();

    // Other Variables
    private Student student;
    private Uri imgUri;


/* Override Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_studentprofile);

        // Initialization
        declareViews();
        barInteraction();       // Don't allow user to do anything yet
        getIntentExtras();      // Get address of student in database via intent extras
        setFields();

        // Set Listeners
        setTextWatcher();
        setFocusListener();
        setBirthDayChangeListener();
        setDegreeChangeListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            switch(requestCode){

                // Coming back from picking an image, on to Crop!
                case Constant.PICK_IMAGE_REQUEST_CODE:
                    if(data != null && data.getData() != null){
                        Uri destinationUri = Uri.fromFile(new File(this.getCacheDir(), "img_" + System.currentTimeMillis()));
                        UCrop.of(data.getData(), destinationUri)
                                .withAspectRatio(1,1)
                                .withMaxResultSize(ImageUtils.IMAGE_MAX_DIMEN, ImageUtils.IMAGE_MAX_DIMEN)
                                .start(this);
                    }
                    break;

                // Coming back from crop, save image as preview and fullsized (profile)
                case UCrop.REQUEST_CROP:
                    imgUri = UCrop.getOutput(data);
                    Picasso.get().load(imgUri).into(mImg);
                    break;
            }
        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Failed to get image. :-(", Toast.LENGTH_LONG).show();
        }
    }


/* Initialization Methods
***************************************************************************************************/

    // Get student values
    private void getIntentExtras() {
        if(getIntent() != null)
            student = getIntent().getParcelableExtra("student");

        if (student == null) {
            Log.e(TAG, "Student Parcel was null!");
            backToMain();
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
        SpinnerAdapter degree_adapter = new SpinnerAdapter(this, getResources().getStringArray(R.array.degree_list));
            ArrayAdapter.createFromResource(this, R.array.degree_list, android.R.layout.simple_spinner_item);
        degree_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDegree.setAdapter(degree_adapter);

        // Action bar
        setSupportActionBar(findViewById(R.id.editStudent_toolBar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setTitle(null);
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
        mName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) setInputErrors(mName, getString(R.string.error_invalidName), nameOk());
        });
    }

    // Listener for birthday change
    private void setBirthDayChangeListener(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBirthDay.setOnDateChangedListener((view, year, monthOfYear, dayOfMonth) -> {
                if (student.getBirth_millis() != datePickerToMillis(view)) mSaveButton.setEnabled(true);
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
        setInputErrors((TextView) mDegree.getSelectedView(), "", degreeOk());

        // Save to student
        if (nameOk() && degreeOk()) {
            String old_name = student.getName();
            student.setName(mName.getText().toString().trim());
            student.setBirth_millis(datePickerToMillis(mBirthDay));
            if (!degree.equals("Degree")) student.setDegree(degree);
            saveImage(old_name.equals(student.getName()));    // Save to database
        }
        else allowInteraction(); // There was an error. So try Again!
    }

    // OnClick for edit image (upload an image from gallery)
    public void editImage(View v) {

        // Set up intent to go to gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};       // Only accept jpeg/png images
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        // onActivityResult is triggered when coming back
        startActivityForResult(intent, Constant.PICK_IMAGE_REQUEST_CODE);
    }

/* Input Checking Methods
***************************************************************************************************/

    // Set error on an editText view based on a condition
    private void setInputErrors(TextView text, String error_msg, boolean check){
        if (check) text.setError(null);
        else text.setError(error_msg);
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
        Log.d(TAG, "Going back to main");
        Intent intent = new Intent(this, MainActivity.class);
        setResult(RESULT_OK, intent);
        finish();
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

    // Load student profile picture
    // Will throw an exception if file doesn't exist in storage but app continues to work fine
    private void loadImage(){
        base_storage_ref.child(ImageUtils.getProfilePath(student.getId())).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Uri path = task.getResult();
                        Picasso.get().load(path).into(mImg);
                    }
                    else {
                        Log.w(TAG, task.getException());
                        student.setProfile_picture(""); // image doesn't exist TODO delete?
                    }
                    allowInteraction();
                });
    }

    // Rewrite old student document with new info
    private void saveStudentInfo(boolean name_changed){
        String address = "universities/" + student.getUni_domain() + "/users/" + student.getId();
        if (address.contains("null")){
            Log.e(TAG, "Student Address has null values.");
            return;
        }

        // Save student info in /users
        db.document(address).set(student).addOnCompleteListener(task -> {
            if (task.isSuccessful()) Log.d(TAG, "Changes saved.");
            else Log.e(TAG, "Something went wrong when trying to save changes.\n" + task.getException());
            allowInteraction();
            backToMain();
        });

        // No need to worry about posts and comments if name hasn't changed
        if (!name_changed) return;

        // Update posts associated with Student if student name has changed
        address = "universities/" + student.getUni_domain() + "/posts";
        db.collection(address).whereEqualTo("author_id", student.getId())
            .get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null){
                    for (DocumentSnapshot doc : task.getResult())
                        doc.getReference().update("author_name",student.getName());
                } else Log.e(TAG, "Post Update unsuccessful, or Student has no posts");
            });
    }

    // Rewrite old Pic and save to storage
    private void saveImage(boolean name_changed){

        // Skip image saving if image hasn't changed
        if (imgUri == null){
            saveStudentInfo(name_changed);
            return;
        }

        // Build storage path
        String profPicPath = ImageUtils.getProfilePath(student.getId());
        String previewPath = ImageUtils.getPreviewPath(student.getId());

        try {
            // Compress image for preview and profile view
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
            byte[] previewBytes = ImageUtils.compressAndGetPreviewBytes(bitmap);
            byte[] standardBytes = ImageUtils.compressAndGetBytes(bitmap);

            base_storage_ref.child(profPicPath).putBytes(standardBytes).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Profile Image saved successfully.");
                    base_storage_ref.child(previewPath).putBytes(previewBytes).addOnCompleteListener(task1 -> {

                        if(task1.isSuccessful()) Log.d(TAG, "Preview Image saved successfully.");
                        else Toast.makeText(this, "Failed to save image. :-(", Toast.LENGTH_LONG).show();

                        saveStudentInfo(name_changed); // Save student Info either way
                    });
                } else {
                    Toast.makeText(this, "Failed to save image. :-(", Toast.LENGTH_LONG).show();
                    saveStudentInfo(name_changed);  // Save student info either way
                }
            });
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save image. :-(", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            saveStudentInfo(name_changed);  // Save student info either way
        }
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
}
