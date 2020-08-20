package com.ivy2testing.util;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.ivy2testing.entities.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public final class Utils {

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

    public static float dpToPixel(Context con, int dps) {
        final float scale = con.getResources().getDisplayMetrics().density;
        return dps * scale;
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
}
