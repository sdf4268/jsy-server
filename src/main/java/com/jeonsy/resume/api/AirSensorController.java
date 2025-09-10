package com.jeonsy.resume.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jeonsy.resume.dto.AirSensorDto;
import com.jeonsy.resume.entity.T_AIRSENSOR;
import com.jeonsy.resume.service.AirSensorService;

@RestController
@RequestMapping("/api/airsensor")
public class AirSensorController {

	@Autowired
	AirSensorService air_sensor_service;

	@PostMapping
	public ResponseEntity<String> insert(@RequestBody AirSensorDto data){
		try {
			air_sensor_service.saveSensorData(data);
            return ResponseEntity.status(HttpStatus.CREATED).body("Data saved successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving data: " + e.getMessage());
        }
	}
	
	@GetMapping("/latest/{deviceId}")
	public ResponseEntity<T_AIRSENSOR> getLatestData(@PathVariable String deviceId) {
		return air_sensor_service.getLatestDataByDeviceId(deviceId).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	
	@GetMapping("/daily")
    public ResponseEntity<List<T_AIRSENSOR>> getDailyData(
            @RequestParam String deviceId,
            @RequestParam String date) {

        List<T_AIRSENSOR> dataList = air_sensor_service.getDailyData(deviceId, date);
        return ResponseEntity.ok(dataList); // 데이터가 없으면 빈 리스트 []가 반환됨
    }
}
