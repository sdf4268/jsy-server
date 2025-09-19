package com.jeonsy.resume.api;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.jeonsy.resume.entity.T_LABEL;
import com.jeonsy.resume.entity.T_SMARTTHINGS_TOKEN;
import com.jeonsy.resume.service.SmartThingsApiService;
import com.jeonsy.resume.service.SmartThingsTokenService;

@Component
@EnableScheduling
public class SchedulerController {
	
	ThreadPoolTaskExecutor executor;
	
	@Autowired
	private SmartThingsTokenService tokenService;

	@Autowired
	private SmartThingsApiService smartThingsApiService;
	
	@Scheduled(fixedDelay = 600000, initialDelay = 1000)
	public void refreshTokenJob() {
        Optional<T_SMARTTHINGS_TOKEN> latestTokenOpt = tokenService.getLatestToken();
        T_SMARTTHINGS_TOKEN latestToken = latestTokenOpt.get();
        LocalDateTime lastUpdated = latestToken.getCreatedAt(); // 최근 갱신된 시간
        LocalDateTime now = LocalDateTime.now();

        if (Duration.between(lastUpdated, now).toHours() >= 5) {
            tokenService.refreshAccessToken();
        }
	}
	
	@Scheduled(fixedRate = 30000)
    public void collectAirConditionerStatus() {
        
        // 1. DB에서 관리 대상으로 등록된 모든 에어컨 목록 조회 (type=3이 에어컨이라고 가정)
        List<T_LABEL> acDevices = smartThingsApiService.getManagedAirConditioners();

        // 2. 각 에어컨의 상태를 확인하고 변경된 경우에만 저장
        for (T_LABEL device : acDevices) {
            try {
                smartThingsApiService.fetchAndSaveStatusIfChanged(device.getDeviceId());
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
    }
}
