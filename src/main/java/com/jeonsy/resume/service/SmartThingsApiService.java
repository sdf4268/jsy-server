package com.jeonsy.resume.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.jeonsy.resume.dto.DeviceDto;
import com.jeonsy.resume.entity.T_SMARTTHINGS_TOKEN;

import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다.
public class SmartThingsApiService {

    // @Autowired 대신 생성자 주입 방식을 사용하는 것이 더 권장됩니다.
    private final SmartThingsTokenService tokenService; 
    
    private final OkHttpClient client = new OkHttpClient(); // 재사용을 위해 멤버 변수로 선언

    public List<DeviceDto> getAirConditionerList() throws IOException {
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

                // 1. "deviceTypeName" 키가 존재하는지 먼저 확인합니다.
                if (item.has("deviceTypeName")) {
                    String deviceTypeName = item.getString("deviceTypeName");

                    // 2. 키가 존재할 경우에만 "Air Conditioner"가 포함되어 있는지 확인합니다.
                    if (deviceTypeName.contains("Air Conditioner")) {
                        String deviceId = item.getString("deviceId");
                        // label이 없을 경우를 대비해 name을 대체 값으로 사용합니다.
                        String label = item.optString("label", item.getString("name")); 

                        deviceList.add(new DeviceDto(deviceId, label, deviceTypeName));
                    }
                }
            }
            
            return deviceList;
        }
    }    
}