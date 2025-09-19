package com.jeonsy.resume.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_AC_STATUS", indexes = @Index(name = "idx_device_id_timestamp", columnList = "deviceId, dataCollectionTimestamp DESC"))
@EqualsAndHashCode(of = {"power", "mode", "fanMode", "currentTemperature", "targetTemperature"})
public class T_AC_STATUS {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String deviceId;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime dataCollectionTimestamp; // 이 레코드가 생성(수집)된 시간

	// --- 개별 상태 값 및 해당 상태의 실제 변경 시간 ---

	@Column(length = 10)
	private String power;
	private LocalDateTime powerTimestamp; // 전원이 변경된 실제 시간

	@Column(length = 20)
	private String mode;
	private LocalDateTime modeTimestamp; // 모드가 변경된 실제 시간

	@Column(length = 20)
	private String fanMode;
	private LocalDateTime fanModeTimestamp; // 팬 모드가 변경된 실제 시간

	private Double currentTemperature;
	private LocalDateTime currentTemperatureTimestamp; // 현재 온도가 변경된 실제 시간

	private Double targetTemperature;
	private LocalDateTime targetTemperatureTimestamp; // 목표 온도가 변경된 실제 시간
}