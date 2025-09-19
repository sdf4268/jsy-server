package com.jeonsy.resume.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jeonsy.resume.entity.T_AC_STATUS;

@Repository
public interface AirConditionerStatusRepository extends JpaRepository<T_AC_STATUS, Long> {

    Optional<T_AC_STATUS> findTopByDeviceIdOrderByDataCollectionTimestampDesc(String deviceId);
}
