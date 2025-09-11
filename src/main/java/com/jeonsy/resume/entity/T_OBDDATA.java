package com.jeonsy.resume.entity; // 본인의 패키지 경로에 맞게 수정하세요.

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // JPA는 기본 생성자를 필요로 합니다.
@Entity
@Table(name = "T_OBDDATA") // 데이터베이스에 생성될 테이블 이름
public class T_OBDDATA{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 데이터베이스에 위임 (Auto-increment)
    @Column(name = "ID")
    private Long id;

    // python-can 라이브러리에서 받은 CAN 데이터
    @Column(name = "VS")
    private Double vs; // 차량 속도 (Vehicle Speed)

    @Column(name = "N")
    private Double n; // 엔진 회전수 (RPM)

    @Column(name = "CYL_PRES")
    private Double cylPres; // 브레이크 압력 (Cylinder Pressure)

    @Column(name = "PV_AV_CAN")
    private Double pvAvCan; // 가속 페달 위치 (Accelerator Pedal Position)
    
    @Column(name = "LAT_ACCEL")
    private Double latAccel; // 횡가속도 (Lateral Acceleration)

    @Column(name = "LONG_ACCEL")
    private Double longAccel; // 종가속도 (Longitudinal Acceleration)

    // GPS 모듈에서 받은 데이터
    @Column(name = "LATITUDE")
    private Double latitude; // 위도

    @Column(name = "LONGITUDE")
    private Double longitude; // 경도
    
    @Column(name = "NUM_SATS")
    private Integer numSats; // 위성 수 (Number of Satellites)

    // 데이터가 서버에 저장된 시간
    @Column(name = "TIMESTAMP")
    private LocalDateTime timestamp; // 타임스탬프
}