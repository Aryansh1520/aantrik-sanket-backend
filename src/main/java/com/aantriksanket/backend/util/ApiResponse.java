package com.aantriksanket.backend.util;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {
    private final boolean success;
    private final Map<String, Object> data;

    public ApiResponse(boolean success, Map<String, Object> data) {
        this.success = success;
        this.data = data != null ? data : new HashMap<>();
    }

    public static ApiResponse success() {
        return new ApiResponse(true, new HashMap<>());
    }

    public static ApiResponse success(Map<String, Object> data) {
        return new ApiResponse(true, data);
    }

    public static ApiResponse success(String key, Object value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);
        return new ApiResponse(true, data);
    }

    public static ApiResponse failure() {
        return new ApiResponse(false, new HashMap<>());
    }

    public static ApiResponse failure(Map<String, Object> data) {
        return new ApiResponse(false, data);
    }

    public static ApiResponse failure(String key, Object value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);
        return new ApiResponse(false, data);
    }

    public boolean isSuccess() {
        return success;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
