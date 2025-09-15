package com.jeonsy.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString // 테스트용
public class DeviceDto {
    private String deviceId;
    private String label;
    private String deviceTypeName;
}