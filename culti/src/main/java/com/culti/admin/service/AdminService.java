package com.culti.admin.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.culti.admin.dto.ContentFormDTO;
import com.culti.admin.dto.PerformancePriceDTO;
import com.culti.admin.dto.ScheduleFormDTO;
import com.culti.admin.dto.SinglePriceDTO;
import com.culti.auth.dto.UserDTO;
import com.culti.auth.entity.LoginLog;
import com.culti.auth.entity.User;
import com.culti.auth.repository.LoginLogRepository;
import com.culti.auth.repository.UserRepository;
import com.culti.booking.entity.Place;
import com.culti.booking.repository.PlaceRepository;
import com.culti.booking.repository.ScheduleRepository;
import com.culti.content.dto.ContentDTO;
import com.culti.content.dto.ScheduleDTO;
import com.culti.content.entity.Content;
import com.culti.content.entity.ContentPrice;
import com.culti.content.entity.Schedule;
import com.culti.content.repository.ContentPriceRepository;
import com.culti.content.repository.ContentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
	

    private final ContentRepository contentRepository;
    private final ScheduleRepository scheduleRepository;
    private final PlaceRepository placeRepository;
    private final ContentPriceRepository contentPriceRepository;
    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
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
    
    public List<Content> getAllContents() {
        return contentRepository.findAll();
    }

    public List<Content> getContentsByCategory(String category) {
        return contentRepository.findByCategory(category);
    }
    
    public List<Place> getAllPlaces() {
        return placeRepository.findAll();
    }
    
    @Transactional
    public void registerPerformancePrice(PerformancePriceDTO dto) {
        // 1. 어떤 콘텐츠인지 DB에서 찾기
        Content content = contentRepository.findById(dto.getContentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 콘텐츠가 없습니다. id=" + dto.getContentId()));

        // 2. 각 등급별로 엔티티를 만들어서 저장
        if (dto.getVipPrice() != null) savePrice(content, "VIP", dto.getVipPrice());
        if (dto.getRPrice() != null) savePrice(content, "R", dto.getRPrice());
        if (dto.getSPrice() != null) savePrice(content, "S", dto.getSPrice());
        if (dto.getAPrice() != null) savePrice(content, "A", dto.getAPrice());
    }

    // 중복 코드를 줄이기 위한 헬퍼 메서드
    private void savePrice(Content content, String grade, Integer price) {
        ContentPrice contentPrice = new ContentPrice();
        contentPrice.setContent(content);
        contentPrice.setGrade(grade);
        contentPrice.setPrice(price);
        contentPriceRepository.save(contentPrice);
    }
    
    public Content getContentById(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 콘텐츠를 찾을 수 없습니다. id=" + id));
    }
    
    @Transactional
    public void registerSinglePrice(SinglePriceDTO dto) {
        Content content = getContentById(dto.getContentId());
        
        // 1. 기존에 등록된 "일반" 가격이 있는지 찾기
        ContentPrice singlePrice = null;
        if (content.getContentPrices() != null) {
            for (ContentPrice cp : content.getContentPrices()) {
                if ("일반".equals(cp.getGrade())) {
                    singlePrice = cp;
                    break;
                }
            }
        }
        
        // 2. 없으면 새로 만들기
        if (singlePrice == null) {
            singlePrice = new ContentPrice();
            singlePrice.setContent(content);
            singlePrice.setGrade("일반"); // 전시/팝업은 모두 '일반' 등급으로 통일
        }
        
        // 3. 가격 덮어쓰고 저장
        singlePrice.setPrice(dto.getPrice());
        contentPriceRepository.save(singlePrice);
    }
    
    public void registerPlace(String name, String address) {
        Place place = new Place();
        place.setName(name);
        place.setAddress(address);
        
        placeRepository.save(place);
    }
    public PerformancePriceDTO getPerformancePrice(Long scheduleId){

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("스케줄 없음"));

        Content content = schedule.getContent();

        List<ContentPrice> prices =
                contentPriceRepository.findByContentId(content.getId());

        PerformancePriceDTO dto = new PerformancePriceDTO();

        for(ContentPrice p : prices){

            switch(p.getGrade()){

                case "VIP":
                    dto.setVipPrice(p.getPrice());
                    break;

                case "R":
                    dto.setRPrice(p.getPrice());
                    break;

                case "S":
                    dto.setSPrice(p.getPrice());
                    break;

                case "A":
                    dto.setAPrice(p.getPrice());
                    break;
            }
        }

        return dto;
    }
    
    @Transactional
    public void updateContent(Long id, ContentDTO dto, MultipartFile posterFile) {
        // 1. 기존 콘텐츠 꺼내오기
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 콘텐츠가 없습니다. id=" + id));
        
        // 2. 기본 정보 덮어쓰기
        content.setTitle(dto.getTitle());
        content.setCategory(dto.getCategory());
        content.setAgeLimit(dto.getAgeLimit());
        content.setRunningTime(dto.getRunningTime());
        content.setStartDate(dto.getStartDate());
        content.setEndDate(dto.getEndDate());
        content.setDescription(dto.getDescription());
        
        // 3. 포스터 이미지가 새로 올라왔다면 기존거 지우고 새걸로 교체
        if (posterFile != null && !posterFile.isEmpty()) {
            try { 
                String uploadDir = "C:/culti_upload/poster/";
                
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs(); 
                }

                String originalFilename = posterFile.getOriginalFilename();
                String uuid = UUID.randomUUID().toString();
                String savedFilename = uuid + "_" + originalFilename;

                File destFile = new File(uploadDir + savedFilename);
                posterFile.transferTo(destFile);

                String dbPath = "/poster/" + savedFilename;
                content.setPosterUrl(dbPath);

            } catch (IOException e) {
                throw new RuntimeException("포스터 이미지 저장 중 오류가 발생했습니다.", e);
            }
        }
        
        // 4. 회차(Schedule) 정보 통째로 갈아끼우기
        scheduleRepository.deleteAllByContent(content); 
        
        if (dto.getSchedules() != null) {
            for (ScheduleDTO sDto : dto.getSchedules()) {
                Schedule schedule = new Schedule();
                schedule.setContent(content);
                schedule.setSessionNum(sDto.getSessionNum());
                schedule.setRoomName(sDto.getRoomName());
                
                schedule.setShowTime(sDto.getStartTime());
                
                if (sDto.getStartTime() != null) {
                    schedule.setStartTime(sDto.getStartTime().toLocalTime());
                }
                if (sDto.getEndTime() != null) {
                    schedule.setEndTime(sDto.getEndTime().toLocalTime());
                }
                
                // 장소(Place) 세팅
                Place place = placeRepository.findById(dto.getPlaceId()).orElse(null);
                schedule.setPlace(place);
                
                scheduleRepository.save(schedule);
            }
        }
    }
    
    //User 목록 반환
    public List<UserDTO> getUserDTOs(String keyword) {
        List<User> users = userRepository.searchUsers(keyword);
        
        // 엔티티 → DTO 변환
        List<UserDTO> dtos = users.stream()
        	    .map(UserDTO::fromEntity)
        	    .toList();
        
        return dtos;
    }
    //사용자의 권한 바꾸는 기능
    public void toggleUserRole(Long id) {
        Optional<User> result = this.userRepository.findById(id);
        User user=null;
        
        if (result.isPresent()) {
			user=result.get();
		}
        
        if (user.getRole().equals("USER")) {
            user.setRole("ADMIN");
        } else {
            user.setRole("USER");
        }
        
        this.userRepository.save(user);
    }
    
    public List<LoginLog> findAllLog() {
        return this.loginLogRepository.findAllOrderByLoginTimeDesc();
    }
}
