package com.jeonsy.resume.entity.pk;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class T_AIRSENSORPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String deviceId; // deviceId로 변경
    private Date saveDate;   // saveDate로 변경

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T_AIRSENSORPK that = (T_AIRSENSORPK) o;
        return Objects.equals(deviceId, that.deviceId) && Objects.equals(saveDate, that.saveDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, saveDate);
    }
}