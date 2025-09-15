package com.example.miniproject.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.example.miniproject.service.CallBackService;
import com.example.miniproject.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DeleteAccountTask extends AsyncTask<String, Void, String> {

    private final CallBackService<String> callBackService;
    private final Context context;

    public DeleteAccountTask(Context context, CallBackService<String> callBackService) {
        this.context = context;
        this.callBackService = callBackService;
    }

    @Override
    protected String doInBackground(String... params) {
        String userId = params[0];
        String result;

        try {
            String rootUrl = context.getString(R.string.root_url);
            String deleteAccountUrl = context.getString(R.string.delete_user);
            URL url = new URL(rootUrl + deleteAccountUrl + "?user_id=" + userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");

            // อ่าน Response จากเซิร์ฟเวอร์
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
            Log.e("DeleteAccountTask", "Error during delete account", e);
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