package com.culti.auth.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass 
@EntityListeners(value = { AuditingEntityListener.class })
@Getter
public class BaseEntity {
	
	@CreatedDate  
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
	

}
