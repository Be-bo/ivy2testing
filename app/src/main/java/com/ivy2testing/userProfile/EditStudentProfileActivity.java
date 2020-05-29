package com.ivy2testing.userProfile;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ivy2testing.entities.Student;

/** @author Zahra Ghavasieh
 * Overview: Edit Student Profile from Student Profile Fragment
 */
class EditStudentProfileActivity {

    // Constants
    private final static String TAG = "StudentEditProfileActivity";

    // Views

    // Other Variables
    private Student student;
    private ImageAdapter adapter;


/* Override Methods
***************************************************************************************************/


/*
Notes:
make sure the birthday is saved in the appropriate variable based on the db schema.
It has to be saved as System.curentTimeInMillis() which is # of milliseconds since Jan 1 1970, i.e. epoch time.
Everything in there should function as expected (simlar to login...).
Don't try to style the nav bar at the top, just use the default.
You'll have to use a navbar style for that activity and set its parent to be the MainActivity in the manifest.
*/

}
