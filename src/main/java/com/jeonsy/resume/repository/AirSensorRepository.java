package com.jeonsy.resume.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jeonsy.resume.entity.T_AIRSENSOR;
import com.jeonsy.resume.entity.pk.T_AIRSENSORPK;

@Repository
public interface AirSensorRepository extends JpaRepository<T_AIRSENSOR, T_AIRSENSORPK> {

	/**
     * 특정 디바이스 ID의 데이터 중 저장 날짜 기준으로 가장 최근 데이터 1건을 조회합니다.
     * @param deviceId 디바이스 ID
     * @return Optional<T_AIRSENSOR>
     */
    Optional<T_AIRSENSOR> findTopById_DeviceIdOrderById_SaveDateDesc(String deviceId);

    /**
     * 특정 디바이스 ID의 데이터 중, 주어진 시작 시간과 종료 시간 사이의 모든 데이터를 조회합니다.
     */
    @Query("SELECT t FROM T_AIRSENSOR t WHERE t.id.deviceId = :deviceId AND t.id.saveDate BETWEEN :startDate AND :endDate")
    List<T_AIRSENSOR> findDataByDeviceIdAndDateRange(
            @Param("deviceId") String deviceId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
}
