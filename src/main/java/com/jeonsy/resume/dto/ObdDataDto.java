package com.jeonsy.resume.dto; // 본인의 패키지 경로에 맞게 수정하세요.

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ObdDataDto {

    // Python에서 보내는 JSON의 키 이름과 정확히 일치시켜야 합니다.
    @JsonProperty("VS")
    private Double vs;

    @JsonProperty("N")
    private Double n;

    @JsonProperty("CYL_PRES")
    private Double cylPres;

    @JsonProperty("PV_AV_CAN")
    private Double pvAvCan;

    @JsonProperty("LAT_ACCEL")
    private Double latAccel;

    @JsonProperty("LONG_ACCEL")
    private Double longAccel;

    private Double latitude;
    private Double longitude;

    @JsonProperty("num_sats") // JSON 키는 소문자/스네이크 케이스일 수 있으므로 명시
    private Integer numSats;

    private String timestamp;
}