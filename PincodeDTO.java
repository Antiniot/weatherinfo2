package com.weatherapi.dto;

import lombok.Data;

@Data
public class PincodeDTO {

    private String pincode;
    private Double latitude;
    private Double longitude;
}