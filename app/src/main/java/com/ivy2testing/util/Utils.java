package com.ivy2testing.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Locale;

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

    public static String getHumanTimeFromMillis(long timestamp){
        Calendar cal = Calendar.getInstance();
        long timeDif = System.currentTimeMillis() - timestamp;

        if(timeDif < Constant.MILLIS_IN_AN_HOUR){ //within the last hour
            return timeDif/Constant.MILLIS_IN_A_MINUTE+" min";
        } else if(timeDif < Constant.MILLIS_IN_A_DAY){ //within 24 hrs
            return timeDif/Constant.MILLIS_IN_AN_HOUR+" h";
        }else if(timeDif < Constant.MILLIS_IN_A_WEEK){ //but within a week
            return timeDif/Constant.MILLIS_IN_A_DAY+" d";
        }else{ //beyond 1 week in the past
            cal.setTimeInMillis(System.currentTimeMillis());
            int currentYear = cal.get(Calendar.YEAR);
            cal.setTimeInMillis(timestamp);
            int stampYear = cal.get(Calendar.YEAR);
            if(currentYear != stampYear){ //if not in the current year, show the year after date
                return cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.CANADA)+" "+cal.get(Calendar.DAY_OF_MONTH)+" "+cal.get(Calendar.YEAR);
            }else{ //if current year, show date only
                return cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.CANADA)+" "+cal.get(Calendar.DAY_OF_MONTH);
            }
        }
    }
}
