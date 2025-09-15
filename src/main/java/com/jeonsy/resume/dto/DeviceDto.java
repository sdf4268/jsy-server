package com.jeonsy.resume.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString // 테스트용
public class DeviceDto {
    private String deviceId;
    private String label;
    private String deviceTypeName;
    private boolean isRegistered;
    
    public DeviceDto(String deviceId, String label, String deviceTypeName, boolean isRegistered) {
        this.deviceId = deviceId;
        this.label = label;
        this.deviceTypeName = deviceTypeName;
        this.isRegistered = isRegistered;
    }
}