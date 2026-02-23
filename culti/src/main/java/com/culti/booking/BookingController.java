package com.culti.booking;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.*;

@Controller
public class BookingController {

    @GetMapping("/reservation/booking")
    public String bookingPage(Model model) {
        
        // 1. 영화 목록 (이미지 왼쪽 섹션)
        List<Map<String, String>> contents = new ArrayList<>();
        contents.add(Map.of("id", "1", "age", "12세", "title", "인터스텔라 리마스터"));
        contents.add(Map.of("id", "2", "age", "15세", "title", "듄 파트2"));
        model.addAttribute("contents", contents);

        // 2. 지역 목록 (수정됨: String 리스트 -> Map 리스트)
        // HTML에서 r.name으로 접근하기 때문에 키값을 "name"으로 설정합니다.
        List<Map<String, String>> regions = new ArrayList<>();
        regions.add(Map.of("name", "서울"));
        regions.add(Map.of("name", "경기"));
        regions.add(Map.of("name", "인천"));
        regions.add(Map.of("name", "부산"));
        model.addAttribute("regions", regions);

        // 3. 날짜 목록 (이미지 상단 슬라이더)
        List<Map<String, String>> dates = new ArrayList<>();
        String[] days = {"월", "화", "수", "목", "금", "토", "일"};
        for (int i = 0; i < 14; i++) {
            Map<String, String> dateMap = new HashMap<>();
            dateMap.put("dayName", days[i % 7]);
            // 날짜가 28일을 넘어가면 다시 1일부터 시작하도록 로직을 살짝 보정하면 더 좋습니다.
            int displayDate = 23 + i; 
            dateMap.put("date", String.valueOf(displayDate)); 
            dateMap.put("isToday", i == 0 ? "true" : "false");
            dates.add(dateMap);
        }
        model.addAttribute("dates", dates);

        return "reservation/booking"; 
    }
}