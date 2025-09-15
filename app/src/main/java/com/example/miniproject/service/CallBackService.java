package com.example.miniproject.service;

import com.example.miniproject.model.User;

import java.util.List;

public interface CallBackService<T> {
    void onSuccess(List<T> data);

    void onSuccess(User user);

    void onSuccess(String message);

    void onError(String error);
}