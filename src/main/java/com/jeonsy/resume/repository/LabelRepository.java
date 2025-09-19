package com.jeonsy.resume.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jeonsy.resume.entity.T_LABEL;

@Repository
public interface LabelRepository extends JpaRepository<T_LABEL, Long> {
	Optional<T_LABEL> findByDeviceId(String deviceId);
	List<T_LABEL> findByType(int type);
	
	@Transactional
    void deleteByDeviceId(String deviceId);
}
