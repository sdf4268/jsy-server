package com.jeonsy.resume.entity;

import com.jeonsy.resume.entity.pk.T_AIRSENSORPK; // 패키지 경로 확인!
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "T_AIRSENSOR")
public class T_AIRSENSOR {

    @EmbeddedId
    @AttributeOverrides({
        @AttributeOverride(name = "deviceId", column = @Column(name = "DEVICE_ID")),
        @AttributeOverride(name = "saveDate", column = @Column(name = "SAVE_DATE"))
    })
    private T_AIRSENSORPK id; // t_airsensorpk -> id 로 변경

    @Column(name = "TEMP", columnDefinition = "DECIMAL(4, 2)")
    private Double temp;

    @Column(name = "HUMI", columnDefinition = "DECIMAL(4, 2)")
    private Double humi;

    @Column(name = "CO2")
    private Integer co2;

    @Column(name = "DUST1_0")
    private Integer dust10; // dust1_0 -> dust10

    @Column(name = "DUST2_5")
    private Integer dust25; // dust2_5 -> dust25

    @Column(name = "DUST10")
    private Integer dust100; // dust10 -> dust100

    @Column(name = "RAW0_3")
    private Integer raw03; // raw0_3 -> raw03

    @Column(name = "RAW0_5")
    private Integer raw05;

    @Column(name = "RAW1_0")
    private Integer raw10;

    @Column(name = "RAW2_5")
    private Integer raw25;

    @Column(name = "RAW5_0")
    private Integer raw50;

    @Column(name = "RAW10_0")
    private Integer raw100;
}