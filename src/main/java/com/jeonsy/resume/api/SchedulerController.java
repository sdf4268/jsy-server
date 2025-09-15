package com.jeonsy.resume.api;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.jeonsy.resume.entity.T_SMARTTHINGS_TOKEN;
import com.jeonsy.resume.service.SmartThingsTokenService;

@Component
@EnableScheduling
public class SchedulerController {
	
	ThreadPoolTaskExecutor executor;
	
	@Autowired
	private SmartThingsTokenService tokenService;
	
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
}
