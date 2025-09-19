package com.jeonsy.resume.service;

import java.io.IOException;
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

import com.jeonsy.resume.dto.DeviceDto;
import com.jeonsy.resume.dto.DeviceRegistrationDto;
import com.jeonsy.resume.entity.T_LABEL;
import com.jeonsy.resume.entity.T_SMARTTHINGS_TOKEN;
import com.jeonsy.resume.repository.LabelRepository;

import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다.
public class SmartThingsApiService {

    private static final Logger logger = LoggerFactory.getLogger(SmartThingsApiService.class);

    private final SmartThingsTokenService tokenService;
    
    private final LabelRepository labelRepository;
    
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
    
    public void registerAirConditioner(DeviceRegistrationDto dto) {
        T_LABEL newLabel = new T_LABEL();
        newLabel.setDeviceId(dto.getDeviceId());
        newLabel.setLabel(dto.getLabel());
        newLabel.setRoom(dto.getRoom());
        newLabel.setType(3); // 에어컨 타입은 3으로 고정
        labelRepository.save(newLabel);
    }
}