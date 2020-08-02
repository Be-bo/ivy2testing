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
}
