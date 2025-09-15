package com.example.miniproject.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.miniproject.model.User;
import com.example.miniproject.service.CallBackService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.example.miniproject.R;

public class GetProfileTask extends AsyncTask<String, Void, String> {
    private final CallBackService<User> callBackService;
    private final Context context;

    public GetProfileTask(Context context, CallBackService<User> callBackService) {
        this.context = context;
        this.callBackService = callBackService;
    }

    @Override
    protected String doInBackground(String... params) {
        String userId = params[0];
        String result = null;

        try {
            String rootUrl = context.getString(R.string.root_url);
            String getProfileUrl = context.getString(R.string.get_profile);
            URL url = new URL(rootUrl + getProfileUrl + "?user_id=" + userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                result = response.toString();
            } else {
                Log.e("GetProfileTask", "HTTP error code: " + responseCode);
                result = null;
            }
        } catch (Exception e) {
            Log.e("GetProfileTask", "Error during getprofile", e);
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("GetProfileTask", "Response: " + result); // บันทึก Log เพื่อตรวจสอบ JSON
        if (result != null && !result.isEmpty()) {
            try {
                JSONObject jsonResponse = new JSONObject(result);
                int code = jsonResponse.getInt("code");
                if (code == 200) {
                    JSONObject resultObject = jsonResponse.getJSONObject("result");

                    // ตรวจสอบว่าคีย์ "id" มีอยู่ใน JSON หรือไม่
                    if (resultObject.has("id")) {
                        User user = new User(
                                resultObject.getString("firstname"),
                                resultObject.getString("lastname"),
                                resultObject.getString("email"),
                                "" // เพิ่มฟิลด์อื่น ๆ ตามต้องการ
                        );
                        List<User> userList = new ArrayList<>();
                        userList.add(user);
                        callBackService.onSuccess(userList);
                    } else {
                        callBackService.onError("ID not found in response");
                    }
                } else {
                    callBackService.onError("Failed to fetch profile: Code " + code);
                }
            } catch (JSONException e) {
                Log.e("GetProfileTask", "JSON parsing error", e);
                callBackService.onError("Failed to parse server response");
            }
        } else {
            callBackService.onError("Connection error or empty response");
        }
    }
}