package com.ivy2testing.util;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public final class Utils {

    public static String getCampusUni(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared_preferences", MODE_PRIVATE);
        return sharedPreferences.getString("campus_domain", Constant.DEFAULT_UNI);
    }

    public static void setCampusUni(String uniDomain, Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("campus_domain", uniDomain);
        editor.apply();
    }

    public static float dpToPixel(Context con, int dps){
        final float scale = con.getResources().getDisplayMetrics().density;
        return dps * scale;
    }

    public static String getNotificationText(int notificationType, String authorName, String targetName){
        String retVal = "Notification";
        switch(notificationType){
            case Constant.NOTIFICATION_TYPE_CHAT:
                retVal = authorName + " sent you a message.";
                break;
            case Constant.NOTIFICATION_TYPE_COMMENT:
                retVal = authorName + " commented on " + targetName.substring(0, 10) + "...";
                break;
            case Constant.NOTIFICATION_TYPE_FEATURED:
                retVal = authorName + " featured their " + targetName;
                break;
            case Constant.NOTIFICATION_TYPE_ORG_EVENT:
                retVal = authorName + " added a new event: " + targetName;
                break;
            case Constant.NOTIFICATION_TYPE_ORG_POST:
                retVal = authorName + " posted " + targetName;
                break;
        }
        return retVal;
    }
}
