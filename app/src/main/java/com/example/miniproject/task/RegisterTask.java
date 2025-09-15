package com.example.miniproject.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.miniproject.R;
import com.example.miniproject.model.User;
import com.example.miniproject.service.CallBackService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RegisterTask extends AsyncTask<User, Void, String> {
    private final CallBackService<String> callBackService;
    private final Context context;

    public RegisterTask(Context context, CallBackService<String> callBackService) {
        this.context = context;
        this.callBackService = callBackService;
    }

    @Override
    protected String doInBackground(User... users) {
        if (users.length == 0 || users[0] == null) {
            Log.e("RegisterTask", "User data is null");
            return "User data is null";
        }

        User user = users[0];

        // ตรวจสอบข้อมูลผู้ใช้
        if (user.getFirstname() == null || user.getFirstname().isEmpty() ||
                user.getLastname() == null || user.getLastname().isEmpty() ||
                user.getEmail() == null || user.getEmail().isEmpty() ||
                user.getPassword() == null || user.getPassword().isEmpty()) {
            return "Invalid user data";
        }

        // ตรวจสอบอีเมล
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(user.getEmail()).matches()) {
            return "Invalid email format";
        }

        try {
            String rootUrl = context.getString(R.string.root_url);
            String registerUrl = context.getString(R.string.register_url);
            URL url = new URL(rootUrl + registerUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            // เตรียมข้อมูล JSON
            JSONObject jsonInput = new JSONObject();
            jsonInput.put("firstname", user.getFirstname());
            jsonInput.put("lastname", user.getLastname());
            jsonInput.put("email", user.getEmail());
            jsonInput.put("password", user.getPassword());

            // ส่งข้อมูล
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // รับผลลัพธ์
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readStream(conn);
            } else {
                return readErrorStream(conn);
            }
        } catch (IOException e) {
            Log.e("RegisterTask", "Connection error", e);
            return "Connection error";
        } catch (JSONException e) {
            Log.e("RegisterTask", "JSON error", e);
            return "JSON error";
        } catch (Exception e) {
            Log.e("RegisterTask", "Unexpected error", e);
            return "Unexpected error";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            try {
                Log.d("RegisterTask", "Response JSON: " + result);
                JSONObject jsonResponse = new JSONObject(result);
                int code = jsonResponse.getInt("code");

                if (code == 200) {
                    JSONObject resultObject = jsonResponse.getJSONObject("result");
                    int userId = resultObject.getInt("user_id");
                    String email = resultObject.getString("email");
                    String hashedPassword = resultObject.getString("password");

                    saveUserData(userId, email, hashedPassword);
                    callBackService.onSuccess("Registration successful");
                } else {
                    callBackService.onError("Registration failed with code: " + code);
                }
            } catch (JSONException e) {
                callBackService.onError("Failed to parse server response");
            }
        } else {
            callBackService.onError("Connection error");
        }
    }

    private String readStream(HttpURLConnection conn) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (Exception e) {
            Log.e("RegisterTask", "Error reading response stream", e);
        }
        return null;
    }

    private String readErrorStream(HttpURLConnection conn) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            Log.e("RegisterTask", "Error response: " + response.toString());
            return response.toString();
        } catch (Exception e) {
            Log.e("RegisterTask", "Error reading error stream", e);
        }
        return null;
    }

    private void saveUserData(int userId, String email, String hashedPassword) {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt("user_id", userId)
                .putString("email", email)
                .putString("hashed_password", hashedPassword)
                .apply();
    }
}