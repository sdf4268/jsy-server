package com.jeonsy.resume.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SmartThingsTokenDto {

	@JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("installed_app_id")
    private String installedAppId;

    // ✅ 기본 생성자
    public SmartThingsTokenDto() {}

    // ✅ Getter & Setter
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getInstalledAppId() {
        return installedAppId;
    }

    public void setInstalledAppId(String installedAppId) {
        this.installedAppId = installedAppId;
    }

    // ✅ toString()
    @Override
    public String toString() {
        return "SmartThingsTokenResponse{" +
                "accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", expiresIn=" + expiresIn +
                ", installedAppId='" + installedAppId + '\'' +
                '}';
    }

    // ✅ equals & hashCode (객체 비교를 위해)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmartThingsTokenDto that = (SmartThingsTokenDto) o;
        return expiresIn == that.expiresIn &&
                accessToken.equals(that.accessToken) &&
                refreshToken.equals(that.refreshToken) &&
                installedAppId.equals(that.installedAppId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(accessToken, refreshToken, expiresIn, installedAppId);
    }
}
