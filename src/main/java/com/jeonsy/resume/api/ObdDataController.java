package com.jeonsy.resume.api; // 본인의 패키지 경로에 맞게 수정하세요.

import com.jeonsy.resume.dto.ObdDataDto;
import com.jeonsy.resume.service.ObdDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/obd") // 이 컨트롤러는 "/api/obd" 경로의 요청을 처리합니다.
public class ObdDataController {

    @Autowired
    private ObdDataService obdDataService; // 이전에 만든 Service를 주입받습니다.

    @PostMapping // "/api/obd" 경로로 들어오는 POST 요청을 이 메서드가 처리합니다.
    public ResponseEntity<String> saveObdData(@RequestBody ObdDataDto obdDataDto) {
        try {
            // 1. 요청의 본문(body)에 담겨온 JSON 데이터를 DTO 객체로 받습니다.
            // 2. 받은 DTO를 Service의 saveObdData 메서드로 넘겨줍니다.
            obdDataService.saveObdData(obdDataDto);
            
            // 3. 성공적으로 처리되었음을 클라이언트에게 알립니다. (HTTP 201 Created)
            return ResponseEntity.status(HttpStatus.CREATED).body("OBD data saved successfully.");

        } catch (Exception e) {
            // 4. 처리 중 오류가 발생하면 서버 로그에 에러를 출력하고,
            e.printStackTrace(); 
            //    클라이언트에게 에러가 발생했음을 알립니다. (HTTP 500 Internal Server Error)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving OBD data: " + e.getMessage());
        }
    }
}