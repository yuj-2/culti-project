package com.culti.content.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.culti.booking.entity.Seat;
import com.culti.content.entity.Content;
import com.culti.content.repository.ContentRepository;
import com.culti.content.service.ContentService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ContentController {

   private final ContentService contentService;
   private final ContentRepository contentRepository;
   
   @Value("${kakao.api.js-key}")
   private String kakaoJsKey;

   /*
   @GetMapping("/reservation")
    public String reservationPage() {
        return "reservation/reservation"; 
    }
   */
   
   /*
   @GetMapping("/content/api/list")
    @ResponseBody
    public List<Content> getContentsApi() {
        return this.contentService.getList();
    }
    */
   
    @GetMapping("/content/api/list")
    @ResponseBody
    public Page<Content> getList(
            @RequestParam(value = "category", defaultValue = "영화") String category,
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "sort", defaultValue = "인기순") String sort,
            @PageableDefault(size = 16) Pageable pageable
    ) {
        Sort jpaSort;
        
        if ("최신순".equals(sort)) {
            jpaSort = Sort.by(Sort.Direction.DESC, "createdAt");
        } else if ("가나다순".equals(sort)) {
            jpaSort = Sort.by(Sort.Direction.ASC, "title");
        } else {
            jpaSort = Sort.by(Sort.Direction.DESC, "bookingCount"); 
        }

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), jpaSort);

        return contentRepository.findByCategoryAndTitleContainingIgnoreCase(category, keyword, pageRequest);
    }
   
   @GetMapping("/reservation/detail")
   public String detailPage() {
      return "/reservation/content-detail";
   }
   
   @GetMapping("/reservation/detail/{id}")
   public String detailPage(Model model, @PathVariable("id") Long id) {
      Content content = this.contentService.getContent(id);
      model.addAttribute("content", content);
      
      Map<String, Integer> priceInfo = new LinkedHashMap<>();
      
      if (!content.getSchedules().isEmpty() && content.getSchedules().get(0).getPlace() != null) {
          for (Seat seat : content.getSchedules().get(0).getPlace().getSeats()) {
              priceInfo.put(seat.getGrade(), seat.getBasePrice());
          }
      }
      
      model.addAttribute("priceInfo", priceInfo);
      model.addAttribute("kakaoJsKey", kakaoJsKey);
      
      return "reservation/content-detail";
   }
   
   @GetMapping("/reservation")
   public String reservationList(
           @PageableDefault(size = 16, sort = "id", direction = Sort.Direction.DESC) Pageable pageable, 
           Model model) {
       
       Page<Content> contentPage = contentService.getContentList(pageable);
       
       model.addAttribute("contentPage", contentPage);
       
       return "reservation/reservation";
   }
   
}
