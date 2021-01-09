package com.ivy2testing.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.entities.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public final class Utils {


    public static final String DAY_FORMAT = "hh:mm a";              // eg. 3:00 AM
    public static final String WEEK_FORMAT = "EEE " + DAY_FORMAT;   // eg. Monday 3:00 AM
    public static final String MONTH_FORMAT = "MMMM d";             // eg. July 1
    public static final String YEAR_FORMAT = "MMMM, yyyy";          // eg. July 2020


/* Campus
***************************************************************************************************/

    public static String getCampusUni(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared_preferences", MODE_PRIVATE);
        return sharedPreferences.getString("campus_domain", Constant.DEFAULT_UNI);
    }

    public static void setCampusUni(String uniDomain, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("campus_domain", uniDomain);
        editor.apply();
    }


/* Conversions
***************************************************************************************************/

    public static float dpToPixel(Context con, int dps) {
        final float scale = con.getResources().getDisplayMetrics().density;
        return dps * scale;
    }


    // Translate time in millis to a readable date format
    public static String millisToDateTime(long millis){
        // Format string in locale timezone base on device settings
        DateFormat formatter = new SimpleDateFormat(getPattern(millis), Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);

        return formatter.format(cal.getTime());
    }

    // Get a specific pattern depending on current time
    private static String getPattern(long millis){
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);

        if (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)){ // Same year?
            if (cal.get(Calendar.WEEK_OF_MONTH) == now.get(Calendar.WEEK_OF_MONTH)){ // Same week?
                if (cal.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)){ // Same day?
                    return DAY_FORMAT;
                } else return WEEK_FORMAT;
            } else return MONTH_FORMAT;
        } else return YEAR_FORMAT;
    }

    public static String getHumanTimeFromMillis(long timestamp) {
        long timeDif = System.currentTimeMillis() - timestamp;
        if(timeDif < Constant.MILLIS_IN_AN_HOUR){ //within the last hour
            return timeDif/Constant.MILLIS_IN_A_MINUTE+" m";
        } else if(timeDif < Constant.MILLIS_IN_A_DAY){ //within 24 hrs
            return timeDif/Constant.MILLIS_IN_AN_HOUR+" h";
        }else if(timeDif < Constant.MILLIS_IN_A_WEEK){ //within a week
            return timeDif/Constant.MILLIS_IN_A_DAY+" d";
        }else{ //beyond 1 week in the past
            return timeDif/Constant.MILLIS_IN_A_WEEK+" w";
        }
    }


/* Users
***************************************************************************************************/

    // For Messaging tokens
    public static User this_user = null;

    public static User getThis_user() {
        return this_user;
    }

    public static void setThis_user(User this_user) {
        Utils.this_user = this_user;
    }

    public static void deleteThis_user() {
        Utils.this_user = null;
    }

    public static void sendRegistrationToServer(String token) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (this_user != null)
            db.collection("users").document(this_user.getId()).update("messaging_token", token);
    }


/* Notifications
***************************************************************************************************/


    //For cancelling notifications

    public static ArrayList<HashMap<String, Integer>> notif_list = new ArrayList<>();

    public static void addNotif(HashMap<String, Integer> toAdd){
        notif_list.add(toAdd);
    }

    public static ArrayList<HashMap<String, Integer>> getNotif_list() {
        return notif_list;
    }

    public static void clearNotif_list(){ //is used
        notif_list = new ArrayList<>();
    }
    public static void setNotif_list(ArrayList<HashMap<String, Integer>> array){
        notif_list = array;
    }

    public static String getNotificationText(int notificationType, String authorName, String targetName) {
        String retVal = "Notification";
        switch (notificationType) {
            case Constant.NOTIFICATION_TYPE_CHAT:
                retVal = authorName + " sent you a message.";
                break;
            case Constant.NOTIFICATION_TYPE_COMMENT:
                retVal = authorName + " commented on " + targetName; //.substring(0, 10) + "...";
                break;
            case Constant.NOTIFICATION_TYPE_FEATURED:
                retVal = authorName + " featured their " + targetName;
                break;
            case Constant.NOTIFICATION_TYPE_ORG_EVENT:
                retVal = authorName + " added a new event: " + targetName;
                break;
            case Constant.NOTIFICATION_TYPE_ORG_POST:
                retVal = authorName + " posted: " + targetName;
                break;
        }
        return retVal;
    }

    public static String commentImagePath(String postId, String commentId){
        return "postfiles/"+postId+"/comments/"+commentId+".jpg";
    }



/* Spannable String
***************************************************************************************************/

    // Create a spannable string for a menuItem
    public static void colorMenuItem(MenuItem menuItem, int color_id){
        SpannableString str = new SpannableString(menuItem.getTitle());
        str.setSpan(new ForegroundColorSpan(color_id), 0, str.length(), 0);
        menuItem.setTitle(str);
    }



/* Firebase
***************************************************************************************************/

    // Get a simple onComplete Listener
    public static <T> OnCompleteListener<T> getSimpleOnCompleteListener(Context context, String success_msg, String failure_msg){
        return task -> {
            if (task.isSuccessful()) Toast.makeText(context, success_msg, Toast.LENGTH_SHORT).show();
            else {
                Toast.makeText(context, failure_msg, Toast.LENGTH_SHORT).show();
                Log.e(context.toString(), failure_msg, task.getException());
            }
        };
    }
}
