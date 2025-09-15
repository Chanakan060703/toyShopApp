package com.example.miniproject.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.example.miniproject.model.ProductType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.example.miniproject.R;

public class FetchProductTypesTask extends AsyncTask<Void, Void, List<ProductType>> {

    private Context context;
    private OnProductTypesFetchedListener listener;

    public FetchProductTypesTask(Context context, OnProductTypesFetchedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected List<ProductType> doInBackground(Void... voids) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();

        try {
            // ดึง URL จาก strings.xml
            String rootUrl = context.getString(R.string.root_url);
            String productTypesUrl = context.getString(R.string.type_list);

            // สร้าง URL เต็มรูปแบบ
            URL url = new URL(rootUrl + productTypesUrl);

            // สร้างการเชื่อมต่อ
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // ตั้งค่า timeout
            connection.setReadTimeout(5000);

            // ตรวจสอบสถานะการเชื่อมต่อ
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // อ่านข้อมูล
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // ตรวจสอบว่า response ไม่ว่าง
                if (response.length() == 0) {
                    Log.e("FetchProductTypesTask", "Empty response from server");
                    return null;
                }

                // แปลง JSON เป็น List<ProductType>
                JSONObject jsonObject = new JSONObject(response.toString());
                if (!jsonObject.has("result")) {
                    Log.e("FetchProductTypesTask", "Invalid JSON structure: 'result' not found");
                    return null;
                }

                JSONArray jsonArray = jsonObject.getJSONArray("result");
                Gson gson = new Gson();
                return gson.fromJson(jsonArray.toString(), new TypeToken<List<ProductType>>() {}.getType());
            } else {
                Log.e("FetchProductTypesTask", "HTTP error code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            Log.e("FetchProductTypesTask", "Error fetching product types: " + e.getMessage());
            return null;
        } finally {
            // ปิดการเชื่อมต่อและ reader
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    Log.e("FetchProductTypesTask", "Error closing reader: " + e.getMessage());
                }
            }
        }
    }

    @Override
    protected void onPostExecute(List<ProductType> productTypes) {
        if (listener == null) {
            Log.e("FetchProductTypesTask", "Listener is null");
            return;
        }

        if (productTypes != null && !productTypes.isEmpty()) {
            listener.onProductTypesFetched(productTypes);
        } else {
            listener.onError("Failed to fetch product types");
        }
    }

    public interface OnProductTypesFetchedListener {
        void onProductTypesFetched(List<ProductType> productTypes);
        void onError(String errorMessage);
    }
}