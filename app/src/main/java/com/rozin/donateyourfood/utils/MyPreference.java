package com.rozin.donateyourfood.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Set;

public class MyPreference {

    public static Builder with(Context context) {
        return new Builder(context);
    }

    public static Builder with(Context context, String prefName) {
        return new Builder(context, prefName);
    }

    public static class Builder {
        SharedPreferences preferences;
        SharedPreferences.Editor editor;

        public Builder(Context context) {
            preferences = context.getSharedPreferences("EasyPreferencePref", Context.MODE_PRIVATE);
            editor = preferences.edit();
        }

        public Builder(Context context, String prefName) {
            preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
            editor = preferences.edit();
        }

        public Builder addBoolean(String key, boolean value) {
            editor.putBoolean(key, value);
            return this;
        }

        public Builder addString(String key, String value) {
            editor.putString(key, value);
            return this;
        }

        public Builder addInt(String key, int value) {
            editor.putInt(key, value);
            return this;
        }

        public Builder addFloat(String key, float value) {
            editor.putFloat(key, value);
            return this;
        }

        public Builder addLong(String key, long value) {
            editor.putLong(key, value);
            return this;
        }

        public Builder addStringSet(String key, Set<String> value) {
            editor.putStringSet(key, value);
            return this;
        }

        public Builder addObject(String key, Object value) {
            Gson gson = new Gson();
            editor.putString(key, gson.toJson(value));
            return this;
        }

        public Builder save() {
            editor.commit();
            return this;
        }

        public boolean getBoolean(String key, boolean defalutValue) {
            return preferences.getBoolean(key, defalutValue);
        }

        public String getString(String key, String defalutValue) {
            return preferences.getString(key, defalutValue);
        }

        public int getInt(String key, int defalutValue) {
            return preferences.getInt(key, defalutValue);
        }

        public float getFloat(String key, float defalutValue) {
            return preferences.getFloat(key, defalutValue);
        }

        public long getLong(String key, long defalutValue) {
            return preferences.getLong(key, defalutValue);
        }

        public Set<String> getStringSet(String key, Set<String> defalutValue) {
            return preferences.getStringSet(key, defalutValue);
        }

        public <GenericClass> GenericClass getObject(String key, Class<GenericClass> classType) {
            if (preferences.contains(key)) {
                Gson gson = new Gson();
                return gson.fromJson(preferences.getString(key, ""), classType);
            }
            return null;
        }


        public Builder remove(String key) {
            editor.remove(key);
            editor.commit();
            return this;
        }

        public Builder clearAll() {
            editor.clear();
            editor.apply();
            return this;
        }

        public void saveArrayList(ArrayList<String> list, String key, Context context){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(list);
            editor.putString(key, json);
            editor.apply();     // This line is IMPORTANT !!!
        }

        public ArrayList<String> getArrayList(String key, Context context){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Gson gson = new Gson();
            String json = prefs.getString(key, null);
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            return gson.fromJson(json, type);
        }


    }
}
