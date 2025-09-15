package com.jeonsy.resume.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jeonsy.resume.entity.T_SMARTTHINGS_TOKEN;

@Repository
public interface SmartThingsTokenRepository extends JpaRepository<T_SMARTTHINGS_TOKEN, Long> {

	Optional<T_SMARTTHINGS_TOKEN> findTopByOrderByCreatedAtDesc();

}
