package com.jeonsy.resume.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "T_SMARTTHINGS_TOKEN")
public class T_SMARTTHINGS_TOKEN {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
	
	@Column(name = "ACCESS_TOKEN", nullable = false)
    private String accessToken;

    @Column(name = "REFRESH_TOKEN", nullable = false)
    private String refreshToken;

    @Column(name = "EXPIRES_IN")
    private Integer expiresIn;

    @Column(name = "INSTALLED_APP_ID")
    private String installedAppId;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;
}
