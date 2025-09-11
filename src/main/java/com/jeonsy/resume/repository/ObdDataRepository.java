package com.jeonsy.resume.repository; // 본인의 패키지 경로에 맞게 수정하세요.

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jeonsy.resume.entity.T_OBDDATA;


@Repository
public interface ObdDataRepository extends JpaRepository<T_OBDDATA, Long> {
    
}