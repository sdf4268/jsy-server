package com.jeonsy.resume.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "T_LABEL")
@NoArgsConstructor
@Getter
@Setter
public class T_LABEL {
	@Id // 이 필드가 Primary Key임을 나타냅니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 ID를 자동으로 생성하고 관리하도록 합니다 (auto-increment).
    @Column(name = "sequence") // 'sequence' 컬럼과 매핑됩니다.
    private Long id;

    @Column(name = "device_id", nullable = false, unique = true) // 'device_id' 컬럼과 매핑됩니다. 중복을 허용하지 않습니다.
    private String deviceId;

    @Column(name = "label") // 'label' 컬럼과 매핑됩니다. (예: "안방 에어컨")
    private String label;

    @Column(name = "room") // 'room' 컬럼과 매핑됩니다. (어느 공간에 있는지 구분)
    private Integer room;

    @Column(name = "type") // 'type' 컬럼과 매핑됩니다. (기기 종류 구분)
    private Integer type;

}
