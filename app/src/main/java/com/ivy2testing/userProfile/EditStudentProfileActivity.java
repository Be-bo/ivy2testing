package com.ivy2testing.userProfile;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.ivy2testing.R;
import com.ivy2testing.entities.Student;
import com.ivy2testing.main.MainActivity;

/** @author Zahra Ghavasieh
 * Overview: Edit Student Profile from Student Profile Fragment
 */
public class EditStudentProfileActivity extends Activity {

    // Constants
    private final static String TAG = "StudentEditProfileActivity";

    // Views
    ImageView mImg;
    EditText mName;
    Spinner mDegree;
    DatePicker mBirthDay;
    Button mSaveButton;

    // Other Variables
    private Student student;
    private String this_uni_domain;
    private String this_user_id;


/* Override Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_studentprofile);

        getIntentExtras();
        declareViews();
        mSaveButton.setEnabled(true);
    }




/* Initialization Methods
***************************************************************************************************/

    private void getIntentExtras() {
        if(getIntent() != null) {
            this_uni_domain = getIntent().getStringExtra("this_uni_domain");
            this_user_id = getIntent().getStringExtra("this_user_id");

            if (this_uni_domain == null || this_user_id == null)
                Log.w(TAG, "One of the UserID or Domain is null!");


        }
    }

    private void declareViews(){
        mImg = findViewById(R.id.editStudent_img);
        mName = findViewById(R.id.editStudent_name);
        mDegree = findViewById(R.id.editStudent_degree);
        mBirthDay = findViewById(R.id.editStudent_birthdayDatePicker);
        mSaveButton = findViewById(R.id.editStudent_saveButton);
    }




/* OnClick Methods
***************************************************************************************************/

    public void saveStudentProfileChange(View view) {
        saveStudentInfo();
        backToMain();
    }


/* Field Checking Methods
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


/* Firebase Related Methods
***************************************************************************************************/

    private void getStudentInfo(){
        //TODO
    }

    private void saveStudentInfo(){
        //TODO
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
