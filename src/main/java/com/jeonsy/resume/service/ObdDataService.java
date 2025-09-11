package com.jeonsy.resume.service; // 본인의 패키지 경로에 맞게 수정하세요.

import com.jeonsy.resume.dto.ObdDataDto;
import com.jeonsy.resume.entity.T_OBDDATA;
import com.jeonsy.resume.repository.ObdDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ObdDataService {

    @Autowired
    private ObdDataRepository obdDataRepository;

    public void saveObdData(ObdDataDto dto) {
        // 1. DTO 객체를 Entity 객체로 변환합니다.
    	T_OBDDATA obdData = new T_OBDDATA();
        
        obdData.setVs(dto.getVs());
        obdData.setN(dto.getN());
        obdData.setCylPres(dto.getCylPres());
        obdData.setPvAvCan(dto.getPvAvCan());
        obdData.setLatAccel(dto.getLatAccel());
        obdData.setLongAccel(dto.getLongAccel());
        obdData.setLatitude(dto.getLatitude());
        obdData.setLongitude(dto.getLongitude());
        obdData.setNumSats(dto.getNumSats());

        // 2. Python에서 보낸 문자열 형식의 timestamp를 LocalDateTime 객체로 변환합니다.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime timestamp = LocalDateTime.parse(dto.getTimestamp(), formatter);
        obdData.setTimestamp(timestamp);

        // 3. Repository를 통해 변환된 Entity를 데이터베이스에 저장합니다.
        obdDataRepository.save(obdData);
    }
}