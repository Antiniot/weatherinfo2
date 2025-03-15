package com.weatherapi.dto;

import lombok.Data;
import java.util.HashMap;

@Data
public class APIResponse<T> {
    private int statusCode;
    private String message;
    private T data;
    private HashMap<String, String> errors = new HashMap<>();
}