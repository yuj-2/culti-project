package com.culti.admin.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.culti.admin.dto.ContentFormDTO;
import com.culti.admin.dto.ScheduleFormDTO;
import com.culti.booking.entity.Place;
import com.culti.booking.repository.PlaceRepository;
import com.culti.content.entity.Content;
import com.culti.content.entity.Schedule;
import com.culti.content.repository.ContentRepository;
import com.culti.booking.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final ContentRepository contentRepository;
    private final ScheduleRepository scheduleRepository;
    private final PlaceRepository placeRepository;

    @Transactional
    public void registerContent(ContentFormDTO formDTO) throws IOException {
        
        // ==========================================
        // 1. 이미지 파일 저장 로직 (C:\culti_upload\poster)
        // ==========================================
        String posterUrl = "";
        MultipartFile posterFile = formDTO.getPosterFile();

        if (posterFile != null && !posterFile.isEmpty()) {
            String uploadPath = "C:\\culti_upload\\poster";

            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 파일명 중복을 막기 위해 랜덤한 UUID를 앞에 붙임
            String originalFilename = posterFile.getOriginalFilename();
            String savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;

            // 실제 C드라이브 폴더에 파일 저장
            File savedFile = new File(uploadPath, savedFilename);
            posterFile.transferTo(savedFile);

            // DB에는 나중에 웹에서 불러오기 편하게 가짜 경로(/poster/파일명)로 저장
            posterUrl = "/poster/" + savedFilename; 
        }

        // ==========================================
        // 2. Content 엔티티 생성 및 DB 저장
        // ==========================================
        Content content = new Content();
        content.setCategory(formDTO.getCategory());
        content.setTitle(formDTO.getTitle());
        content.setAgeLimit(formDTO.getAgeLimit());
        content.setRunningTime(formDTO.getRunningTime());
        content.setStartDate(formDTO.getStartDate());
        content.setEndDate(formDTO.getEndDate());
        content.setDescription(formDTO.getDescription());
        content.setPosterUrl(posterUrl); // 방금 뽑아낸 경로!
        content.setCreatedAt(LocalDateTime.now());
        content.setBookingCount(0); 

        Content savedContent = contentRepository.save(content);


        // ==========================================
        // 3. 화면에서 선택한 Place(장소) 엔티티 찾아오기
        // ==========================================
        Place place = placeRepository.findById(formDTO.getPlaceId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다."));


        // ==========================================
        // 4. Schedule(회차) 엔티티들 생성 및 DB 저장
        // ==========================================
        if (formDTO.getSchedules() != null) {
            for (ScheduleFormDTO sDto : formDTO.getSchedules()) {
                Schedule schedule = new Schedule();
                
                // 어떤 콘텐츠의 어떤 장소에서 열리는 회차인지
                schedule.setContent(savedContent); 
                schedule.setPlace(place);          
                
                schedule.setSessionNum(sDto.getSessionNum());
                schedule.setRoomName(sDto.getRoomName());
                
                schedule.setStartTime(sDto.getStartTime().toLocalTime()); 
                schedule.setEndTime(sDto.getEndTime().toLocalTime());
                schedule.setShowTime(sDto.getStartTime()); 

                scheduleRepository.save(schedule);
            }
        }

        log.info("성공적으로 등록되었습니다! Content ID: {}", savedContent.getId());
    }
}
