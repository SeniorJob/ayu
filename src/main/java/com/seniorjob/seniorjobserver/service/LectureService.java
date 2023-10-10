package com.seniorjob.seniorjobserver.service;

import com.seniorjob.seniorjobserver.controller.LectureController;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.LectureDto;
import com.seniorjob.seniorjobserver.repository.LectureRepository;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity.LectureStatus;
import org.springframework.scheduling.annotation.Scheduled;
import javax.transaction.Transactional;
import java.time.ZoneId;
import java.util.ArrayList;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LectureService {
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(LectureController.class);

    public LectureService(LectureRepository lectureRepository, UserRepository userRepository) {
        this.lectureRepository = lectureRepository;
        this.userRepository = userRepository;
    }

    public List<LectureDto> getAllLectures() {
        List<LectureEntity> lectureEntities = lectureRepository.findAll();
        return lectureEntities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        UserEntity user = userRepository.findByPhoneNumber(currentPrincipalName)
                .orElseThrow(() -> new RuntimeException("로그인된 사용자를 찾을 수 없습니다."));
        System.out.println("Current user: " + user);

        return user;
    }

    // 스케줄링
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void updateLectureStatus(){
        log.info("Update LectureStatus");
        List<LectureEntity> lectures = lectureRepository.findAll();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        for(LectureEntity lecture : lectures){
            LectureStatus previousStatus = lecture.getStatus(); // 이전 상태

            // 철회상태: 모집 마감 요청이 없고 모집 마감 날짜가 지났다면
            if(!lecture.isRecruitmentClosed() && now.isAfter(lecture.getRecruitEnd_date())) {
                lecture.setStatus(LectureStatus.철회상태);
            }
            // 개설대기상태: 강좌 개설자가 모집 마감 요청을 했으면
            else if(lecture.isRecruitmentClosed() && now.isBefore(lecture.getStart_date())) {
                lecture.setStatus(LectureStatus.개설대기상태);
            }
            // 진행상태: 현재 상태가 개설대기상태이며, 현재 시간이 강좌 시작 날짜 이후라면
            else if(lecture.getStatus() == LectureStatus.개설대기상태 && now.isAfter(lecture.getStart_date()) && now.isBefore(lecture.getEnd_date())) {
                lecture.setStatus(LectureStatus.진행상태);
            }
            // 완료상태: 진행상태에서 시간이 강좌 종료 날짜라면
            else if(lecture.getStatus() == LectureStatus.진행상태 && now.isAfter(lecture.getEnd_date())){
                lecture.setStatus(LectureStatus.완료상태);
            }

            // 상태가 변경되었는지 확인하고, 변경된 경우 로그를 기록
            if(!lecture.getStatus().equals(previousStatus)) {
                log.info("강좌 ID: " + lecture.getCreate_id() + "의 상태가 " + previousStatus + "에서 " + lecture.getStatus() + "로 변경되었습니다.");
            }
        }

        lectureRepository.saveAll(lectures);
        log.info("Update LectureStatus");
    }

    // 강좌개설
    public LectureDto createLecture(LectureDto lectureDto, UserEntity userEntity) {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime startDate = lectureDto.getStart_date();
        LocalDateTime endDate = lectureDto.getEnd_date();
        LocalDateTime recruitEndDate = lectureDto.getRecruitEnd_date();
        LectureEntity lectureEntity = lectureDto.toEntity(userEntity);
        lectureEntity.setUser(userEntity);

        // 시작 날짜 조건 확인
        if (startDate.isBefore(currentDate) || startDate.isBefore(recruitEndDate)) {
            throw new IllegalArgumentException("시작 날짜는 현재 날짜 이후 그리고 모집 마감 날짜 이후로 설정되어야 합니다.");
        }

        // 종료 날짜 조건 확인
        if (endDate.isBefore(currentDate) || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료 날짜는 오늘 이후의 날짜이고 시작 날짜 이후로 설정되어야 합니다.");
        }

        // 강좌 모집 마감 날짜 조건 확인
        if (recruitEndDate.isBefore(currentDate) || recruitEndDate.isAfter(startDate)) {
            throw new IllegalArgumentException("강좌 모집 마감 날짜는 현재 날짜 이후 그리고 시작 날짜 이전으로 설정되어야 합니다.");
        }

        // 강좌모집인원은 50명을 초과할수 없다. 초과할경우 예외
        if (lectureDto.getMax_participants() > 50) {
            throw new IllegalArgumentException("모집 인원은 50명을 초과할 수 없습니다.");
        }

        lectureEntity.setUser(userEntity);
        lectureEntity.updateStatus();
        LectureEntity savedLecture = lectureRepository.save(lectureEntity);

        return convertToDto(lectureEntity);
    }

    // 로그인된 유저의 강좌수정
    public LectureDto updateLecture(Long create_id, LectureDto lectureDto) {
        LectureEntity existingLecture = lectureRepository.findById(create_id)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + create_id));

        existingLecture.setCreator(lectureDto.getCreator());
        existingLecture.setMaxParticipants(lectureDto.getMax_participants());
        existingLecture.setCurrentParticipants(lectureDto.getCurrent_participants());
        existingLecture.setCategory(lectureDto.getCategory());
        existingLecture.setBank_name(lectureDto.getBank_name());
        existingLecture.setAccount_name(lectureDto.getAccount_name());
        existingLecture.setAccount_number(lectureDto.getAccount_number());
        existingLecture.setPrice(lectureDto.getPrice());
        existingLecture.setTitle(lectureDto.getTitle());
        existingLecture.setContent(lectureDto.getContent());
        //existingLecture.setCycle(lectureDto.getCycle());
        existingLecture.setWeek(lectureDto.getWeek());
        existingLecture.setLearningTarget(lectureDto.getLearning_target());
        existingLecture.setAttendanceRequirements(lectureDto.getAttendance_requirements());
        //existingLecture.setCount(lectureDto.getCount());
        existingLecture.setStart_date(lectureDto.getStart_date());
        existingLecture.setEnd_date(lectureDto.getEnd_date());
        existingLecture.setRecruitEnd_date(lectureDto.getRecruitEnd_date());
        existingLecture.setRegion(lectureDto.getRegion());
        existingLecture.setImage_url(lectureDto.getImage_url());

        LectureEntity updatedLecture = lectureRepository.save(existingLecture);
        return convertToDto(updatedLecture);
    }

    // 강좌삭제
    public void deleteLecture(Long create_id) {
        lectureRepository.deleteById(create_id);
    }

    // 강좌상세보기
    public LectureDto getDetailLectureById(Long create_id) {
        LectureEntity lectureEntity = lectureRepository.findById(create_id)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + create_id));
        return convertToDto(lectureEntity);
    }

    // 세션로그인후 자신이 개설한 강좌목록 전체조회
    public List<LectureDto> getMyLectureAll(Long userId){
        UserEntity user = userRepository.findById(userId).orElseThrow(()-> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        List<LectureEntity> myLectureAll = lectureRepository.findAllByUser(user);
        return myLectureAll.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    // 세션로그인후 자신이 개설한 강좌 상세보기
    public LectureDto getMyLectureDetail(Long id, Long userId) {
        LectureEntity lectureEntity = lectureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾을 수 없습니다.. create_id: " + id));

        if (!lectureEntity.getUser().getUid().equals(userId)) {
            throw new RuntimeException("해당 강좌를 조회할 권한이 없습니다.");
        }

        return convertToDto(lectureEntity);
    }

    // 강좌ID기반 강좌상태 가져오는 메서드
    public LectureEntity.LectureStatus getLectureStatus(Long create_id) {
        LectureEntity lectureEntity = lectureRepository.findById(create_id)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + create_id));
        return lectureEntity.getStatus();
    }

    // 강좌검색 : 제목
    public List<LectureDto> searchLecturesByTitle(String title) {
        List<LectureDto> lectureList = new ArrayList<>();
        if (title.length() >= 2) { // 2글자 이상인 경우에만 검색 수행
            List<LectureEntity> lectureEntities = lectureRepository.findByTitleContaining(title);
            lectureList = lectureEntities.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("검색어는 \"2글자\" 이상 입력해주세요!");
        }
        // 검색 결과가 없는 경우
        if (lectureList.isEmpty()) {
            throw new NoSuchElementException("검색결과에 해당하는 강좌가 없습니다.ㅠㅠ");
        }
        return lectureList;
    }

    // 강좌정렬
    // 최신순으로 강좌 정렬 최신 = true 오래된 = false
    public List<LectureDto> sortLecturesByCreatedDate(List<LectureDto> lectureList, boolean descending) {
        lectureList.sort((a, b) -> descending ?
                b.getCreatedDate().compareTo(a.getCreatedDate()) :
                a.getCreatedDate().compareTo(b.getCreatedDate()));
        return lectureList;
    }

    // 기존코드 인기순 : max_participant가많은순 -> 강좌 참여하기를 만들때 실제참여자가 많은순으로 변경할것임
    // 수정된 인기순 : 강좌에 참여한 사람이 많은 강좌순 : 참여자수 current_participants
    public List<LectureDto> sortLecturesByPopularity(List<LectureDto> lectureList, boolean descending) {
        lectureList.sort((a, b) -> descending ?
                b.getCurrent_participants() - a.getCurrent_participants() :
                a.getCurrent_participants() - b.getCurrent_participants());
        return lectureList;
    }

    // 가격순 : prices(낮은순 높은순)
    public List<LectureDto> sortLecturesByPrice(List<LectureDto> lectureList, boolean descending) {
        lectureList.sort((a, b) -> descending ?
                b.getPrice().compareTo(a.getPrice()) :
                a.getPrice().compareTo(b.getPrice()));
        return lectureList;
    }

    // 지역검색
    // 필터링 : 지역검색
    public List<LectureDto> filterRegion(List<LectureDto> lectureList, String region) {
        List<LectureDto> filteredList = new ArrayList<>();
        if(region.length() >= 2){
            for (LectureDto lectureDto : lectureList){
                if (lectureDto.getRegion().contains(region)){
                    filteredList.add(lectureDto);
                }
            }
        }else {
            throw new IllegalArgumentException("검색어는 \"2글자\" 이상 입력해주세요!");
        }
        if(filteredList.isEmpty()){
            throw new NoSuchElementException("검색결과에 해당하는 강좌가 없습니다..");
        }
        return filteredList;
    }

    // 강좌상태검색
    public List<LectureDto> filterStatus(List<LectureDto> lectureList, LectureEntity.LectureStatus status) {
        return lectureList.stream()
                .filter(lecture -> lecture.getStatus() == status)
                .collect(Collectors.toList());
    }

    // 필터링 : 제목검색 -> 최신순,오래된순, 가격높은순, 가격낮은순, 인기순, 지역(시,군),
    // 상좌상태(모집중 = 신청가능상태,  개설대기중 = 개설대기상태, 진행중 = 진행상태), 카테고리
    public List<LectureDto> filterLectures(List<LectureDto> lectureList, String filter, boolean descending) {
        switch (filter){
            case "latest":
                return sortLecturesByCreatedDate(lectureList, descending);
            case "price":
                return sortLecturesByPrice(lectureList, descending);
            case  "popularity":
                return sortLecturesByPopularity(lectureList, descending);
            default:
                throw new IllegalArgumentException("잘못된 필터조건");
        }
    }

    // 필터링 : 카테고리
    public List<LectureDto> filterCategory(List<LectureDto> lectureList, String category) {
        List<LectureDto> filteredList = new ArrayList<>();
        for (LectureDto lectureDto : lectureList){
            if(lectureDto.getCategory().equals(category)){
                filteredList.add(lectureDto);
            }
        }
        return filteredList;
    }

    //페이징
    public Page<LectureEntity> getLectures(Pageable pageable) {
        return lectureRepository.findAll(pageable);
    }

    private LectureDto convertToDto(LectureEntity lectureEntity) {
        return LectureDto.builder()
                .create_id(lectureEntity.getCreate_id())

                .creator(lectureEntity.getCreator())
                .max_participants(lectureEntity.getMaxParticipants())
                .current_participants(lectureEntity.getCurrentParticipants())
                .category(lectureEntity.getCategory())
                .bank_name(lectureEntity.getBank_name())
                .account_name(lectureEntity.getAccount_name())
                .account_number(lectureEntity.getAccount_number())
                .price(lectureEntity.getPrice())
                .title(lectureEntity.getTitle())
                .content(lectureEntity.getContent())
                //.cycle(lectureEntity.getCycle())
                .week(lectureEntity.getWeek())
                .learning_target(lectureEntity.getLearningTarget())
                .attendance_requirements(lectureEntity.getAttendanceRequirements())
                //.count(lectureEntity.getCount())
                .start_date(lectureEntity.getStart_date())
                .end_date(lectureEntity.getEnd_date())
                .region(lectureEntity.getRegion())
                .status(lectureEntity.getStatus())
                .image_url(lectureEntity.getImage_url())
                .createdDate(lectureEntity.getCreatedDate())
                .recruitEnd_date(lectureEntity.getRecruitEnd_date())
                .build();
    }

    // 강좌상태
    // 강좌 모집 마감 기능
    public void closeRecruitment(Long lectureId) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강좌를 찾을 수 없습니다. id: " + lectureId));

        // 이미 모집 마감되었거나 시작된 강좌인 경우 예외 처리
        if (lecture.getStatus() == LectureStatus.개설대기상태 || lecture.getStatus() == LectureStatus.진행상태) {
            lecture.setRecruitmentClosed(true);
            throw new RuntimeException("이미 모집 마감되었거나 진행 중인 강좌입니다.");
        }

        LocalDateTime currentDate = LocalDateTime.now();
        if (currentDate.isAfter(lecture.getRecruitEnd_date())) {
            throw new RuntimeException("모집 마감일이 지났습니다. 강좌를 진행 상태로 변경할 수 없습니다.");
        }

        lecture.setStatus(LectureStatus.진행상태); // 강좌 상태를 진행 중으로 변경
        lectureRepository.save(lecture);
    }

    public Boolean isLectureClosed(Long lectureId) {
        return lectureRepository.findById(lectureId)
                .map(lecture -> Optional.ofNullable(lecture.getRecruitmentClosed()).orElse(false))
                .orElse(false);
    }
}
