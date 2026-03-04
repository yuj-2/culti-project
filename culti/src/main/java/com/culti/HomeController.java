package com.culti;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.culti.mate.DTO.MatePostDTO;
import com.culti.mate.entity.MatePost;
import com.culti.mate.service.MateService;

import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {


	private final MateService mateService; 

	
	@GetMapping("/home")
	public void home(Model model) {
		List<MatePost> posts = mateService.getLatestPosts(4);
		Map<Long, Long> acceptedCountMap = mateService.getAcceptedCountMap(posts);

		List<MatePostDTO> dtoList = posts.stream()
		    .map(p -> mateService.entityToDto(p, acceptedCountMap.getOrDefault(p.getPostId(), 0L)))
		    .toList();

		model.addAttribute("posts", dtoList);
	}
	

}
