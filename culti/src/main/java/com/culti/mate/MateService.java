package com.culti.mate;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MateService {

	private final MateRepository mateRepository;
	
	public List<MatePost> getList(){
		return this.mateRepository.findAll();
	}
	
}
