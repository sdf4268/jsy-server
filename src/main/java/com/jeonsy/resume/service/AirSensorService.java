package com.jeonsy.resume.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jeonsy.resume.dto.AirSensorDto;
import com.jeonsy.resume.entity.T_AIRSENSOR;
import com.jeonsy.resume.entity.pk.T_AIRSENSORPK;
import com.jeonsy.resume.repository.AirSensorRepository;

@Service
public class AirSensorService {
	
	@Autowired
	private AirSensorRepository airSensorRepository;
	
	public void saveSensorData(AirSensorDto dto) {
        // 1. Entity와 PK 객체 생성
        T_AIRSENSOR sensorData = new T_AIRSENSOR();
        T_AIRSENSORPK pk = new T_AIRSENSORPK();

        // 2. PK 값 설정 (디바이스 ID와 현재 시간)
        pk.setDeviceId(dto.getDeviceId());
        pk.setSaveDate(new Date()); // 저장되는 현재 시간

        // 3. Entity에 DTO 데이터와 PK 매핑
        sensorData.setId(pk);
        sensorData.setTemp(dto.getTemp());
        sensorData.setHumi(dto.getHumi());
        sensorData.setCo2(dto.getCo2());
        sensorData.setDust10(dto.getDust1_0());
        sensorData.setDust25(dto.getDust2_5());
        sensorData.setDust100(dto.getDust10());
        sensorData.setRaw03(dto.getRaw0_3());
        sensorData.setRaw05(dto.getRaw0_5());
        sensorData.setRaw10(dto.getRaw1_0());
        sensorData.setRaw25(dto.getRaw2_5());
        sensorData.setRaw50(dto.getRaw5_0());
        sensorData.setRaw100(dto.getRaw10_0());

        // 4. Repository를 통해 데이터베이스에 저장
        airSensorRepository.save(sensorData);
    }
	
	public Optional<T_AIRSENSOR> getLatestDataByDeviceId(String deviceId) {
        return airSensorRepository.findTopById_DeviceIdOrderById_SaveDateDesc(deviceId);
    }
	
	public List<T_AIRSENSOR> getDailyData(String deviceId, String dateString) {
        // "yyyy-MM-dd" 형식의 문자열을 LocalDate 객체로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateString, formatter);

        // 해당 날짜의 시작 시간(00:00:00)과 끝 시간(23:59:59) 계산
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        // LocalDateTime을 Date 객체로 변환 (DB와 타입 맞춤)
        Date startDate = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());

        return airSensorRepository.findDataByDeviceIdAndDateRange(deviceId, startDate, endDate);
    }
}
