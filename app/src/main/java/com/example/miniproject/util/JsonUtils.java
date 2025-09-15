package com.example.miniproject.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class JsonUtils {

    private static final Gson gson = new Gson();

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static <T> List<T> fromJsonList(String json, Class<T> classOfT) {
        Type type = TypeToken.getParameterized(List.class, classOfT).getType();
        return gson.fromJson(json, type);
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }
}