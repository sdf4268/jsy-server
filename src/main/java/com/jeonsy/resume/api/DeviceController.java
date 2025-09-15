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
import com.jeonsy.resume.dto.DeviceDto;
import com.jeonsy.resume.dto.DeviceRegistrationDto;
import com.jeonsy.resume.dto.ObdDataDto;
import com.jeonsy.resume.entity.T_AIRSENSOR;
import com.jeonsy.resume.service.AirSensorService;
import com.jeonsy.resume.service.ObdDataService;
import com.jeonsy.resume.service.SmartThingsApiService;

@RestController
@RequestMapping("/api")
public class DeviceController {

	@Autowired
	AirSensorService air_sensor_service;
	
	@Autowired
    private ObdDataService obdDataService;
	
	@Autowired
    private SmartThingsApiService smartThingsApiService;

	@PostMapping("/airsensor")
	public ResponseEntity<String> insert(@RequestBody AirSensorDto data){
		try {
			air_sensor_service.saveSensorData(data);
            return ResponseEntity.status(HttpStatus.CREATED).body("Data saved successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving data: " + e.getMessage());
        }
	}
	
	@GetMapping("/airsensor/latest/{deviceId}")
	public ResponseEntity<T_AIRSENSOR> getLatestData(@PathVariable String deviceId) {
		return air_sensor_service.getLatestDataByDeviceId(deviceId).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	
	@GetMapping("/airsensor/daily")
    public ResponseEntity<List<T_AIRSENSOR>> getDailyData(
            @RequestParam String deviceId,
            @RequestParam String date) {

        List<T_AIRSENSOR> dataList = air_sensor_service.getDailyData(deviceId, date);
        return ResponseEntity.ok(dataList); // 데이터가 없으면 빈 리스트 []가 반환됨
    }

    @PostMapping("/obd")
    public ResponseEntity<String> saveObdData(@RequestBody ObdDataDto obdDataDto) {
        try {
            // 1. 요청의 본문(body)에 담겨온 JSON 데이터를 DTO 객체로 받습니다.
            // 2. 받은 DTO를 Service의 saveObdData 메서드로 넘겨줍니다.
            obdDataService.saveObdData(obdDataDto);
            
            // 3. 성공적으로 처리되었음을 클라이언트에게 알립니다. (HTTP 201 Created)
            return ResponseEntity.status(HttpStatus.CREATED).body("OBD data saved successfully.");

        } catch (Exception e) {
            // 4. 처리 중 오류가 발생하면 서버 로그에 에러를 출력하고,
            e.printStackTrace(); 
            //    클라이언트에게 에러가 발생했음을 알립니다. (HTTP 500 Internal Server Error)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving OBD data: " + e.getMessage());
        }
    }
    
    @GetMapping("/smartthings/getAirconDevices")
    public ResponseEntity<?> getSmartThingsDeviceList() {
        try {
            // 1. Controller는 ApiService에게 "기기 목록 좀 가져와"라고 지시만 합니다.
            List<DeviceDto> deviceList = smartThingsApiService.getAirConditionerList();
            
            // 2. 받은 결과를 성공 상태(200 OK)와 함께 사용자에게 전달합니다.
            return ResponseEntity.ok(deviceList);
        } catch (Exception e) {
            // 3. 서비스 로직 수행 중 오류 발생 시 에러 메시지를 전달합니다.
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching SmartThings devices: " + e.getMessage());
        }
    }
    
    @PostMapping("/smartthings/register-device")
    public ResponseEntity<String> registerDevice(@RequestBody DeviceRegistrationDto registrationDto) {
        try {
        	smartThingsApiService.registerAirConditioner(registrationDto);
            return ResponseEntity.ok("Device registered successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error registering device: " + e.getMessage());
        }
    }
}
