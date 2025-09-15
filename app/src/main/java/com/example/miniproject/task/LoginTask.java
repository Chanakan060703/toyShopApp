package com.example.miniproject.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.miniproject.manager.UserManager;
import com.example.miniproject.model.User;
import com.example.miniproject.service.CallBackService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.example.miniproject.R;

public class LoginTask extends AsyncTask<String, Void, String> {

    private final CallBackService<String> callBackService;
    private final Context context;

    public LoginTask(Context context, CallBackService<String> callBackService) {
        this.context = context;
        this.callBackService = callBackService;
    }

    @Override
    protected String doInBackground(String... params) {
        String email = params[0];
        String password = params[1];
        String result;

        try {
            String rootUrl = context.getString(R.string.root_url);
            String loginUrl = context.getString(R.string.login_url);
            URL url = new URL(rootUrl + loginUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject jsonInput = new JSONObject();
            jsonInput.put("email", email);
            jsonInput.put("password", password);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
                result = response.toString();

                Log.d("LoginTask", "Response code: " + responseCode);
                Log.d("LoginTask", "Response body: " + result);
            } else {
                result = null;
            }
        } catch (Exception e) {
            Log.e("LoginTask", "Error during login", e);
            result = null;
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            try {
                Log.d("LoginTask", "Response: " + result);

                JSONObject jsonResponse = new JSONObject(result);
                int code = jsonResponse.getInt("code");
                if (code == 200) {
                    // ตรวจสอบว่า result เป็น JSONObject หรือเป็น string
                    Object resultObject = jsonResponse.get("result");

                    if (resultObject instanceof JSONObject) {
                        JSONObject userObject = (JSONObject) resultObject;
                        String userId = userObject.getString("id");
                        String firstname = userObject.getString("firstname");
                        String lastname = userObject.getString("lastname");
                        String email = userObject.getString("email");
                        String hashedPassword = userObject.getString("password");

                        UserManager userManager = new UserManager(context);
                        userManager.saveUser(new User(firstname, lastname, email, hashedPassword));

                        callBackService.onSuccess(List.of("Login successful"));
                    } else if (resultObject instanceof String) {
                        // ถ้า result เป็น string
                        String userId = (String) resultObject;
                        // คุณสามารถจัดการได้ตามต้องการ เช่นบันทึก userId ลงฐานข้อมูล หรือใช้งานตามที่ต้องการ
                        callBackService.onSuccess(List.of("Login successful, user ID: " + userId));
                    }
                } else {
                    callBackService.onError("Invalid email or password");
                }
            } catch (JSONException e) {
                Log.e("LoginTask", "Failed to parse server response", e);
                callBackService.onError("Failed to parse server response");
            }
        } else {
            callBackService.onError("Connection error");
        }
    }



}
