package com.example.miniproject.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.example.miniproject.service.CallBackService;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.miniproject.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class UpdateProfileTask extends AsyncTask<String, Void, String> {

    private final CallBackService<String> callBackService;
    private final Context context;

    public UpdateProfileTask(Context context, CallBackService<String> callBackService) {
        this.context = context;
        this.callBackService = callBackService;
    }

    @Override
    protected String doInBackground(String... params) {
        String userId = params[0];
        String firstname = params[1];
        String lastname = params[2];
        String email = params[3];
        String result;

        try {
            String rootUrl = context.getString(R.string.root_url);
            String updateProfileUrl = context.getString(R.string.update_user);
            URL url = new URL(rootUrl + updateProfileUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject jsonInput = new JSONObject();
            jsonInput.put("user_id", userId);
            jsonInput.put("firstname", firstname);
            jsonInput.put("lastname", lastname);
            jsonInput.put("email", email);

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
            } else {
                result = null;
            }
        } catch (Exception e) {
            Log.e("UpdateProfileTask", "Error during update profile", e);
            result = null;
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            callBackService.onSuccess(List.of(result));
        } else {
            callBackService.onError("Connection error");
        }
    }
}