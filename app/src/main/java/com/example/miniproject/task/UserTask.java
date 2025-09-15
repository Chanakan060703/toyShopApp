package com.example.miniproject.task;

import android.content.Context;
import android.os.AsyncTask;
import com.example.miniproject.model.User;
import com.example.miniproject.service.CallBackService;
import com.example.miniproject.util.JsonUtils;
import com.example.miniproject.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class UserTask extends AsyncTask<String, Void, String> {

    private final CallBackService<List<User>> callBackService;
    private final Context context;
    private final String action;

    public UserTask(Context context, String action, CallBackService<List<User>> callBackService) {
        this.context = context;
        this.action = action;
        this.callBackService = callBackService;
    }

    @Override
    protected String doInBackground(String... params) {
        String result;
        try {
            String rootUrl = context.getString(R.string.root_url);
            URL url = new URL(rootUrl + action);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            result = response.toString();
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            List<User> users = JsonUtils.fromJsonList(result, User.class);
            if (users != null) {
                callBackService.onSuccess(Collections.singletonList(users));
            } else {
                callBackService.onError("No users found");
            }
        } else {
            callBackService.onError("Connection error");
        }
    }
}