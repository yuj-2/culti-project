package com.culti.mate.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass 
@EntityListeners(value = { AuditingEntityListener.class })
@Getter
public class PostBaseEntity {

	@CreatedDate  
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
	
}
