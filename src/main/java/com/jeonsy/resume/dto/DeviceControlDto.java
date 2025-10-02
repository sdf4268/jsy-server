package com.jeonsy.resume.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceControlDto {

    private String deviceId;
    private String powerState;
    private String mode;
    private Integer temperature;
}