package com.pkhh.projectcndd.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class SharedPrefUtil {
  public static final String SELECTED_PROVINCE_ID_KEY = "com.pkhh.projectcndd.selected_city_id";
  public static final String SELECTED_PROVINCE_NAME_KEY = "com.pkhh.projectcndd.selected_city_name";
  public static final String LANGUAGE_CODE_KEY = "com.pkhh.projectcndd.language_code_key";

  private volatile static SharedPrefUtil sInstance = null;
  private SharedPreferences sharedPreferences;

  private SharedPrefUtil() {}

  private SharedPrefUtil(Context context) {
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
  }

  @NonNull
  public static SharedPrefUtil getInstance(Context context) {
    if (sInstance == null) {
      synchronized (SharedPrefUtil.class) {
        if (sInstance == null) {
          sInstance = new SharedPrefUtil(context);
        }
      }
    }
    return sInstance;
  }

  public void saveSelectedProvinceId(@Nullable String id) {
    sharedPreferences.edit().putString(SELECTED_PROVINCE_ID_KEY, id).apply();
  }

  @Nullable
  public String getSelectedProvinceId(String defValue) {
    return sharedPreferences.getString(SELECTED_PROVINCE_ID_KEY, defValue);
  }

  public void saveSelectedProvinceName(@Nullable String name) {
    sharedPreferences.edit().putString(SELECTED_PROVINCE_NAME_KEY, name).apply();
  }

  @Nullable
  public String getSelectedProvinceName(String defValue) {
    return sharedPreferences.getString(SELECTED_PROVINCE_NAME_KEY, defValue);
  }

  public String getLanguageCode(String defValue) {
    return sharedPreferences.getString(LANGUAGE_CODE_KEY, defValue);
  }

  public void saveLanguageCode(String languageCode) {
    sharedPreferences.edit().putString(LANGUAGE_CODE_KEY, languageCode).apply();
  }
}
