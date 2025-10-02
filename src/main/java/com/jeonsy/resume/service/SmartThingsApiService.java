package com.jeonsy.resume.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jeonsy.resume.dto.DeviceControlDto;
import com.jeonsy.resume.dto.DeviceDto;
import com.jeonsy.resume.dto.DeviceRegistrationDto;
import com.jeonsy.resume.dto.DeviceStatusDto;
import com.jeonsy.resume.entity.T_AC_STATUS;
import com.jeonsy.resume.entity.T_LABEL;
import com.jeonsy.resume.entity.T_SMARTTHINGS_TOKEN;
import com.jeonsy.resume.repository.AirConditionerStatusRepository;
import com.jeonsy.resume.repository.LabelRepository;

import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다.
public class SmartThingsApiService {

    private static final Logger logger = LoggerFactory.getLogger(SmartThingsApiService.class);

    private final SmartThingsTokenService tokenService;
    
    private final LabelRepository labelRepository;
    
    private final AirConditionerStatusRepository acStatusRepository;
    
    private final OkHttpClient client = new OkHttpClient(); // 재사용을 위해 멤버 변수로 선언

    public List<DeviceDto> getAirConditionerList() throws IOException {
    	Set<String> registeredDeviceIds = labelRepository.findAll().stream()
                .map(T_LABEL::getDeviceId)
                .collect(Collectors.toSet());

        // 1. 토큰 가져오기 (기존과 동일)
        Optional<T_SMARTTHINGS_TOKEN> tokenOpt = tokenService.getLatestToken();
        if (tokenOpt.isEmpty()) {
            throw new IllegalStateException("Token not available.");
        }
        String accessToken = tokenOpt.get().getAccessToken();

        // 2. SmartThings API 호출 (기존과 동일)
        Request request = new Request.Builder()
            .url("https://api.smartthings.com/v1/devices")
            .addHeader("Authorization", "Bearer " + accessToken)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch devices: " + response.body().string());
            }
            
            String responseBody = response.body().string();
            
            JSONObject json = new JSONObject(responseBody);
            JSONArray items = json.getJSONArray("items");
            
            List<DeviceDto> deviceList = new ArrayList<>();
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);

                if (item.has("deviceTypeName")) {
                    String deviceTypeName = item.getString("deviceTypeName");
                    if (deviceTypeName.contains("Air Conditioner")) {
                        String deviceId = item.getString("deviceId");
                        String label = item.optString("label", item.getString("name"));
                        
                        // 2. 현재 기기가 DB에 등록되어 있는지 확인합니다.
                        boolean isRegistered = registeredDeviceIds.contains(deviceId);

                        // 3. isRegistered 값을 DTO에 담아 전달합니다.
                        deviceList.add(new DeviceDto(deviceId, label, deviceTypeName, isRegistered));
                    }
                }
            }
            
            return deviceList;
        }
    }
    
    /**
     * 등록된 모든 에어컨의 "실시간" 상태를 SmartThings API에서 직접 조회
     */
    public List<DeviceStatusDto> getManagedAirConditionersWithStatus() throws IOException {
        // 1. DB에서 제어 대상으로 등록된 에어컨 목록을 가져옵니다.
        List<T_LABEL> devices = labelRepository.findByType(3);
        List<DeviceStatusDto> deviceStatusList = new ArrayList<>();

        // 2. API 호출에 필요한 토큰을 미리 준비합니다.
        Optional<T_SMARTTHINGS_TOKEN> tokenOpt = tokenService.getLatestToken();
        if (tokenOpt.isEmpty()) {
            throw new IllegalStateException("Token not available.");
        }
        String accessToken = tokenOpt.get().getAccessToken();

        // 3. 각 기기별로 SmartThings API를 호출하여 실시간 상태를 조회합니다.
        for (T_LABEL device : devices) {
            Request request = new Request.Builder()
                .url("https://api.smartthings.com/v1/devices/" + device.getDeviceId() + "/status")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    // ❗️ 이미 만들어둔 parseStatus 메소드를 재활용하여 실시간 상태 객체 생성
                    T_AC_STATUS liveStatus = parseStatus(device.getDeviceId(), jsonResponse);
                    
                    // 4. 기기 정보(DB)와 실시간 상태(API)를 합쳐서 DTO 리스트에 추가
                    deviceStatusList.add(new DeviceStatusDto(device, liveStatus));
                } else {
                    logger.error("Failed to fetch status for device {}: {}", device.getDeviceId(), response.body().string());
                    // ❗️ 상태 조회에 실패한 기기도 목록에는 보이도록 null 상태로 추가
                    deviceStatusList.add(new DeviceStatusDto(device, null));
                }
            } catch (Exception e) {
                logger.error("Exception while fetching status for device {}", device.getDeviceId(), e);
                deviceStatusList.add(new DeviceStatusDto(device, null));
            }
        }
        return deviceStatusList;
    }
    
    public void controlAirConditioner(DeviceControlDto controlDto) throws IOException {
        // 1. 토큰 가져오기
        Optional<T_SMARTTHINGS_TOKEN> tokenOpt = tokenService.getLatestToken();
        if (tokenOpt.isEmpty()) {
            throw new IllegalStateException("Token not available.");
        }
        String accessToken = tokenOpt.get().getAccessToken();

        // 2. 전원, 모드, 온도 제어 명령 3개를 항상 함께 생성
        JSONObject powerCommand = new JSONObject();
        powerCommand.put("component", "main");
        powerCommand.put("capability", "switch");
        powerCommand.put("command", controlDto.getPowerState());
        powerCommand.put("arguments", new JSONArray());

        JSONObject modeCommand = new JSONObject();
        modeCommand.put("component", "main");
        modeCommand.put("capability", "airConditionerMode");
        modeCommand.put("command", "setAirConditionerMode");
        modeCommand.put("arguments", new JSONArray().put(controlDto.getMode()));

        JSONObject tempCommand = new JSONObject();
        tempCommand.put("component", "main");
        tempCommand.put("capability", "thermostatCoolingSetpoint");
        tempCommand.put("command", "setCoolingSetpoint");
        tempCommand.put("arguments", new JSONArray().put(controlDto.getTemperature()));

        JSONArray commands = new JSONArray();
        commands.put(modeCommand);   // 순서상 모드/온도 설정 후
        commands.put(tempCommand);
        commands.put(powerCommand);  // 전원 상태를 마지막에 변경하는 것이 더 안정적일 수 있음

        JSONObject payload = new JSONObject();
        payload.put("commands", commands);

        // 3. API 호출
        RequestBody body = RequestBody.create(
            payload.toString(), 
            MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
            .url("https://api.smartthings.com/v1/devices/" + controlDto.getDeviceId() + "/commands")
            .addHeader("Authorization", "Bearer " + accessToken)
            .post(body)
            .build();
            
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Failed to send command to device {}: {}", controlDto.getDeviceId(), response.body().string());
                throw new IOException("Failed to send command: " + response.body().string());
            }
            logger.info("Successfully sent command to device {}: {}", controlDto.getDeviceId(), payload.toString());
        }
    }
    
    public void registerAirConditioner(DeviceRegistrationDto dto) {
        T_LABEL newLabel = new T_LABEL();
        newLabel.setDeviceId(dto.getDeviceId());
        newLabel.setLabel(dto.getLabel());
        newLabel.setRoom(dto.getRoom());
        newLabel.setType(3); // 에어컨 타입은 3으로 고정
        labelRepository.save(newLabel);
    }
    


    public List<T_LABEL> getManagedAirConditioners() {
        // type 3이 에어컨이라는 비즈니스 로직은 Service가 알고 있는 것이 자연스럽습니다.
        return labelRepository.findByType(3); 
    }

    private String getValue(JSONObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.isNull(key)) {
            return null;
        }
        return obj.get(key).toString();
    }

    private Double getDoubleValue(JSONObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.isNull(key)) {
            return null;
        }
        return obj.getDouble(key);
    }

    private LocalDateTime getTimestamp(JSONObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.isNull(key)) {
            return null;
        }
        String utcTimestampStr = obj.getString(key);
        ZonedDateTime utcDateTime = ZonedDateTime.parse(utcTimestampStr);
        ZonedDateTime kstDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        return kstDateTime.toLocalDateTime();
    }
    
    public void fetchAndSaveStatusIfChanged(String deviceId) throws IOException {
        // 1. SmartThings API를 호출하여 현재 상태 데이터 가져오기
        Optional<T_SMARTTHINGS_TOKEN> tokenOpt = tokenService.getLatestToken();
        if (tokenOpt.isEmpty()) { 
            throw new IllegalStateException("Token not available."); 
        }
        String accessToken = tokenOpt.get().getAccessToken();

        Request request = new Request.Builder()
            .url("https://api.smartthings.com/v1/devices/" + deviceId + "/status")
            .addHeader("Authorization", "Bearer " + accessToken)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Failed to fetch status for device {}: {}", deviceId, response.body().string());
                return;
            }
            String jsonResponse = response.body().string();

            // 2. 현재 상태를 나타내는 Entity 객체 생성 (아직 DB 저장 전)
            T_AC_STATUS currentStatus = parseStatus(deviceId, jsonResponse);
            
            // 3. DB에서 가장 최근 상태 조회
            Optional<T_AC_STATUS> lastStatusOpt = acStatusRepository.findTopByDeviceIdOrderByDataCollectionTimestampDesc(deviceId);

            // 4. 상태 비교 후 조건부 저장
            if (lastStatusOpt.isEmpty() || !lastStatusOpt.get().equals(currentStatus)) {
                logger.info("[{}] 상태 변경 감지. DB에 저장합니다.", deviceId);
                acStatusRepository.save(currentStatus);
            } else {
                logger.info("[{}] 상태 변경 없음. 저장을 건너뜁니다.", deviceId);
            }
        }
    }
    
    public void deleteDevice(String deviceId) {
        labelRepository.deleteByDeviceId(deviceId);
        logger.info("기기 등록 해제 완료: {}", deviceId);
    }
    
    private T_AC_STATUS parseStatus(String deviceId, String jsonResponse) {
        JSONObject root = new JSONObject(jsonResponse);
        JSONObject main = root.getJSONObject("components").getJSONObject("main");

        JSONObject powerObj = main.optJSONObject("switch").optJSONObject("switch");
        JSONObject modeObj = main.optJSONObject("airConditionerMode").optJSONObject("airConditionerMode");
        JSONObject fanObj = main.optJSONObject("airConditionerFanMode").optJSONObject("fanMode");
        JSONObject currentTempObj = main.optJSONObject("temperatureMeasurement").optJSONObject("temperature");
        JSONObject targetTempObj = main.optJSONObject("thermostatCoolingSetpoint").optJSONObject("coolingSetpoint");

        return T_AC_STATUS.builder()
                .deviceId(deviceId)
                .power(getValue(powerObj, "value"))
                .powerTimestamp(getTimestamp(powerObj, "timestamp"))
                .mode(getValue(modeObj, "value"))
                .modeTimestamp(getTimestamp(modeObj, "timestamp"))
                .fanMode(getValue(fanObj, "value"))
                .fanModeTimestamp(getTimestamp(fanObj, "timestamp"))
                .currentTemperature(getDoubleValue(currentTempObj, "value"))
                .currentTemperatureTimestamp(getTimestamp(currentTempObj, "timestamp"))
                .targetTemperature(getDoubleValue(targetTempObj, "value"))
                .targetTemperatureTimestamp(getTimestamp(targetTempObj, "timestamp"))
                .build();
    }
}