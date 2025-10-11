package com.jeonsy.resume.api;

import com.jeonsy.resume.dto.WorkLogDto;
import com.jeonsy.resume.entity.T_WORK_LOG;
import com.jeonsy.resume.repository.WorkLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/worklogs")
public class WorkLogController {

    private final WorkLogRepository workLogRepository;

    @GetMapping
    public ResponseEntity<List<T_WORK_LOG>> getAllLogs() {
        return ResponseEntity.ok(workLogRepository.findAllByOrderByCreatedAtDesc());
    }

    @PostMapping
    public ResponseEntity<T_WORK_LOG> addLog(@RequestBody WorkLogDto dto) {
        T_WORK_LOG newLog = T_WORK_LOG.builder()
                .content(dto.getContent())
                .status(dto.getStatus())
                .build();
        T_WORK_LOG savedLog = workLogRepository.save(newLog);
        return ResponseEntity.ok(savedLog);
    }
}