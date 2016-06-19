//***************************************************************************************************
//***************************************************************************************************
//      Project name                    		: Zivame
//      Class Name                              : SharedPreferenceUtils
//      Author                                  : PurpleTalk, Inc.
//***************************************************************************************************
//      Description: Manages all shared preferences keys with getter and setter methods.
//***************************************************************************************************
//***************************************************************************************************

package com.pebblego;

import android.content.Context;
import android.content.SharedPreferences;

/**

 */
public class SharedPreferenceUtils {


    private static String Preference_Name = "PebbleGoSharedPreferences";

    static SharedPreferences prefs;

    public static void writeString(Context context, String key, String value) {
        if (context != null) {
            getSharedPreference(context).edit().putString(key, value).commit();
        }
    }

    public static String readString(Context context, String key, String defaultValue) {
        if (context != null) {
            return getSharedPreference(context).getString(key, defaultValue);
        } else {
            return defaultValue;
        }
    }

    public static void writeBoolean(Context context, String key, boolean value) {
        if (context != null) {
            getSharedPreference(context).edit().putBoolean(key, value).commit();
        }
    }

    public static boolean readBoolean(Context context, String key, boolean defaultValue) {
        if (context != null) {
            return getSharedPreference(context).getBoolean(key, defaultValue);
        } else {
            return defaultValue;
        }
    }

    public static void writeInteger(Context context, String key, int value) {
        if (context != null) {
            getSharedPreference(context).edit().putInt(key, value).commit();
        }
    }

    public static int readInteger(Context context, String key, int defaultValue) {
        if (context != null) {
            return getSharedPreference(context).getInt(key, defaultValue);
        } else {
            return defaultValue;
        }
    }

/*    public static void writeSetData(Context context, String key, Set<String> value) {
        if (context != null) {
            getSharedPreference(context).edit().putStringSet(key, value).commit();
        }
    }

    public static ArrayList<String> readSetData(Context context, String key, LinkedHashSet<String> defaultValue) {
        ArrayList<String> list = new ArrayList<>();
        if (context != null) {
            Set<String> set = getSharedPreference(context).getStringSet(key, defaultValue);
            if (set != null)
                list = new ArrayList<>(set);
            return list;
        } else {
            return list;
        }
    }*/

    public static void clear(Context context) {
        if (context != null) {
            getSharedPreference(context).edit().clear().commit();
        }
    }

    private static SharedPreferences getSharedPreference(Context context) {
        if(prefs==null){
            prefs = context.getSharedPreferences(Preference_Name, Context.MODE_PRIVATE);
        }
        return prefs;
        //return context.getSharedPreferences(Preference_Name, Context.MODE_PRIVATE);
    }
}