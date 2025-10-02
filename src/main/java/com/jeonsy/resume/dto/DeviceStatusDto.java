package com.jeonsy.resume.dto;

import com.jeonsy.resume.entity.T_AC_STATUS;
import com.jeonsy.resume.entity.T_LABEL;
import lombok.Getter;

@Getter
public class DeviceStatusDto {
    // 기기 정보
    private String deviceId;
    private String label; // 프론트엔드에서 사용할 기기 이름

    // 실시간 상태 정보
    private String power;
    private String mode;
    private String fanMode;
    private Double currentTemperature;
    private Double targetTemperature;

    // T_LABEL과 T_AC_STATUS 객체를 합쳐서 새로운 DTO를 만드는 생성자
    public DeviceStatusDto(T_LABEL device, T_AC_STATUS status) {
        this.deviceId = device.getDeviceId();
        this.label = device.getLabel(); // T_LABEL의 label 필드 사용

        if (status != null) {
            this.power = status.getPower();
            this.mode = status.getMode();
            this.fanMode = status.getFanMode();
            this.currentTemperature = status.getCurrentTemperature();
            this.targetTemperature = status.getTargetTemperature();
        }
    }
}