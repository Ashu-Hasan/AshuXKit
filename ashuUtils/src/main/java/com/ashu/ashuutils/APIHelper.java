package com.ashu.ashuutils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import retrofit2.Response;

public interface APIHelper {
    static final Gson gson = new Gson();
    public static JSONObject getResponseData(String TAG,
                                             Response<JsonObject> response) {

        JSONObject resultData = null;

        try {

            int httpCode = response.code();

            Messages.showTestLog(TAG, "Raw Response Code: " + httpCode);
            Messages.showTestLog(TAG, "Raw Response Body: " + response.body());
            Messages.showTestLog(TAG, "Raw Error Body: " +
                            (response.errorBody() != null ? response.errorBody().toString() : "null"));

            if (response.isSuccessful() && response.body() != null) {

                resultData = new JSONObject(response.body().toString());
                resultData.put("http_code", httpCode);   // ✅ Inject here

                Messages.showTestLog(TAG, TAG + " Json Response: " + resultData);
                return resultData;

            } else if (response.errorBody() != null) {

                String errorJson = response.errorBody().string();
                resultData = new JSONObject(errorJson);
                resultData.put("http_code", httpCode);   // ✅ Inject here

                Messages.showTestLog(TAG, TAG + " errorJson: " + resultData);
                return resultData;

            } else {

                JSONObject error = new JSONObject();
                error.put("status", false);
                error.put("message", "No response from server");
                error.put("http_code", httpCode);   // ✅ Inject here

                Messages.showTestLog(TAG, TAG + " errorJson: " + error);
                return error;
            }

        } catch (Exception e) {

            e.printStackTrace();

            try {
                JSONObject error = new JSONObject();
                error.put("status", false);
                error.put("message", "Parse error: " + e.getMessage());
                error.put("http_code", response != null ? response.code() : -1);  // ✅ Safe fallback

                Messages.showTestLog(TAG, TAG + " errorJson: " + error);
                return error;

            } catch (Exception ignored) { }
        }

        return resultData;
    }



    // Generic method to convert JSONObject to any Data Model
    public static <T> T convertJsonToModel(JSONObject jsonObject, Class<T> modelClass) {
        try {
            return gson.fromJson(jsonObject.toString(), modelClass);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null; // Return null in case of an error
        }
    }

    public static <T> ArrayList<T> convertJsonArrayToList(JSONArray jsonArray, Class<T> modelClass) {
        Gson gson = new Gson();
        Type listType = TypeToken.getParameterized(ArrayList.class, modelClass).getType();
        return gson.fromJson(jsonArray.toString(), listType);
    }

    public static ArrayList<String> convertStringToArrayList(String TAG, String jsonString) {
        ArrayList<String> arrayList = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                arrayList.add(jsonArray.getString(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "convertStringToArrayList error:- " + e.getMessage());
        }

        return arrayList;
    }
}
