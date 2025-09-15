package com.jeonsy.resume.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AirSensorDto {
	private String deviceId;
    private double temp;
    private double humi;
    private int co2;
    private int dust10;
    private int dust25;
    private int dust100;
    private int raw03;
    private int raw05;
    private int raw10;
    private int raw25;
    private int raw50;
    private int raw100;
}
