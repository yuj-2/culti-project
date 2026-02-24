package com.culti.mate.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.culti.mate.entity.MatePost;
import com.culti.mate.repository.MateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MateService {

	private final MateRepository mateRepository;
	
	public List<MatePost> getList(){
		return this.mateRepository.findAll();
	}
	
}
