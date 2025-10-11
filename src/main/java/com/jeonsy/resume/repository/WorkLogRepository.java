package com.jeonsy.resume.repository;

import com.jeonsy.resume.entity.T_WORK_LOG;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkLogRepository extends JpaRepository<T_WORK_LOG, Long> {
    // 생성일 기준 내림차순으로 모든 로그 찾기
    List<T_WORK_LOG> findAllByOrderByCreatedAtDesc();
}