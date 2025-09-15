package com.example.miniproject.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.miniproject.R;
import com.example.miniproject.model.Product;
import com.example.miniproject.service.CallBackService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ProductTask extends AsyncTask<Object, Void, String> {

    private Context context;
    private CallBackService<Product> callback;
    private String action;
    private Product product;
    private String keyword;
    private File imageFile;


    public ProductTask(Context context, CallBackService<Product> callback) {
        this.context = context;
        this.callback = callback;
        this.action = "load";
    }


    public ProductTask(Context context, CallBackService<Product> callback, String keyword) {
        this.context = context;
        this.callback = callback;
        this.action = "search";
        this.keyword = keyword;
    }


    public ProductTask(Context context, CallBackService<Product> callback, String action, Product product, File imageFile) {
        this.context = context;
        this.callback = callback;
        this.action = action;
        this.product = product;
        this.imageFile = imageFile;
    }

    @Override
    protected String doInBackground(Object... params) {
        HttpURLConnection connection = null;
        StringBuilder response = new StringBuilder();

        try {
            String rootUrl = context.getString(R.string.root_url);
            String productListUrl = context.getString(R.string.product_list);
            String searchUrl = context.getString(R.string.product_search);
            String addUrl = context.getString(R.string.product_add);
            String updateUrl = context.getString(R.string.product_update);

            // สร้าง URL เต็มรูปแบบ
            URL url;
            if (action.equals("search")) {
                String encodedKeyword = URLEncoder.encode(keyword, "UTF-8"); // เข้ารหัส keyword
                url = new URL(rootUrl + searchUrl + "?keyword=" + encodedKeyword);
            } else if (action.equals("add")) {
                url = new URL(rootUrl + addUrl);
            } else if (action.equals("update")) {
                url = new URL(rootUrl + updateUrl);
            } else {
                url = new URL(rootUrl + productListUrl);
            }

            // สร้างการเชื่อมต่อ
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // กำหนด HTTP Method
            if (action.equals("add") || action.equals("update")) {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                if (imageFile != null && imageFile.exists()) {
                    String boundary = "*****";
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                    try (OutputStream outputStream = connection.getOutputStream()) {
                        writeFormData(outputStream, boundary, "product_name", product.getProduct_name());
                        writeFormData(outputStream, boundary, "price", String.valueOf(product.getPrice()));
                        writeFormData(outputStream, boundary, "detail", product.getDetail());
                        writeFormData(outputStream, boundary, "product_qty", String.valueOf(product.getProduct_qty()));
                        writeFormData(outputStream, boundary, "type_id", String.valueOf(product.getType_id()));

                        // สำหรับโหมดแก้ไข: ส่ง product_id ด้วย
                        if (action.equals("update")) {
                            writeFormData(outputStream, boundary, "product_id", String.valueOf(product.getProduct_id()));
                        }

                        // เขียนไฟล์รูปภาพ
                        writeFile(outputStream, boundary, "pic_product", imageFile);

                        // สิ้นสุดการเขียนข้อมูล
                        outputStream.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    }
                } else {
                    // ใช้ application/json สำหรับการส่งข้อมูลสินค้า
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                    // สร้าง JSON object จากข้อมูลสินค้า
                    JSONObject jsonProduct = new JSONObject();
                    jsonProduct.put("product_id", product.getProduct_id());
                    jsonProduct.put("product_name", product.getProduct_name());
                    jsonProduct.put("price", product.getPrice());
                    jsonProduct.put("detail", product.getDetail());
                    jsonProduct.put("product_qty", product.getProduct_qty());
                    jsonProduct.put("type_id", product.getType_id());

                    // สำหรับโหมดแก้ไข: ส่ง product_id ด้วย
                    if (action.equals("update")) {
                        jsonProduct.put("product_id", product.getProduct_id());
                    }

                    // ส่ง JSON ไปยังเซิร์ฟเวอร์
                    try (OutputStream outputStream = connection.getOutputStream()) {
                        outputStream.write(jsonProduct.toString().getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    }
                }
            } else {
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            }

            // ตรวจสอบสถานะการเชื่อมต่อ
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // อ่านข้อมูล
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            } else {
                return "Error: " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } finally {
            // ปิดการเชื่อมต่อ
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response.toString();
    }

    // เขียนข้อมูลแบบ form-data
    private void writeFormData(OutputStream outputStream, String boundary, String fieldName, String value) throws Exception {
        outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write((value + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    // เขียนไฟล์รูปภาพ
    private void writeFile(OutputStream outputStream, String boundary, String fieldName, File file) throws Exception {
        outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + file.getName() + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Type: image/jpeg\r\n\r\n").getBytes(StandardCharsets.UTF_8));

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("API Response", result); // พิมพ์ข้อมูล JSON ที่ได้จากเซิร์ฟเวอร์
        if (result.startsWith("Error")) {
            Log.e("ProductTask", result);
            callback.onError(result);
        } else {
            List<Product> products = parseJson(result);
            if (products != null && !products.isEmpty()) {
                callback.onSuccess(products);
            } else {
                callback.onError("No products found");
            }
        }
    }

    private List<Product> parseJson(String json) {
        List<Product> products = new ArrayList<>();
        try {
            // แปลง JSON ทั้งหมดเป็น JSONObject
            JSONObject jsonObject = new JSONObject(json);

            // ดึงค่า "result" ซึ่งเป็น JSONArray
            JSONArray jsonArray = jsonObject.getJSONArray("result");

            // วนลูปเพื่อดึงข้อมูลสินค้า
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject productJson = jsonArray.getJSONObject(i);

                // ดึงข้อมูลจาก JSON
                int productId = productJson.getInt("product_id");
                String productName = productJson.getString("product_name");
                double price = productJson.getDouble("price");
                String detail = productJson.getString("detail");
                int productQty = productJson.getInt("product_qty");
                String picProduct = productJson.getString("pic_product");
                int typeId = productJson.getInt("type_Id"); // ตรงนี้ต้องตรงกับ key ใน JSON

                Product product = new Product(productId, productName, price, detail, productQty, picProduct, typeId);
                products.add(product);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }
}