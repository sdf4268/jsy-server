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
    private int dust1_0;
    private int dust2_5;
    private int dust10;
    private int raw0_3;
    private int raw0_5;
    private int raw1_0;
    private int raw2_5;
    private int raw5_0;
    private int raw10_0;
}
