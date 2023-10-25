package com.seniorjob.seniorjobserver.service;

import com.seniorjob.seniorjobserver.controller.LectureController;
import com.seniorjob.seniorjobserver.domain.entity.*;
import com.seniorjob.seniorjobserver.dto.*;
import com.seniorjob.seniorjobserver.repository.*;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity.LectureStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import javax.transaction.Transactional;
import java.time.ZoneId;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final LectureApplyRepository lectureApplyRepository;
    private final LectureProposalRepository lectureProposalRepository;
    private final LectureProposalApplyRepository lectureProposalApplyRepository;
    private final WeekRepository weekRepository;
    private final WeekPlanRepository weekPlanRepository;
    private final AttendanceRepository attendanceRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(LectureController.class);

    public LectureService(LectureRepository lectureRepository, UserRepository userRepository,
                          WeekRepository weekRepository,
                          WeekPlanRepository weekPlanRepository, AttendanceRepository attendanceRepository,
                          LectureApplyRepository lectureApplyRepository,
                          LectureProposalRepository lectureProposalRepository, LectureProposalApplyRepository lectureProposalApplyRepository) {
        this.lectureRepository = lectureRepository;
        this.userRepository = userRepository;
        this.weekRepository = weekRepository;
        this.weekPlanRepository = weekPlanRepository;
        this.attendanceRepository = attendanceRepository;
        this.lectureApplyRepository = lectureApplyRepository;
        this.lectureProposalRepository = lectureProposalRepository;
        this.lectureProposalApplyRepository = lectureProposalApplyRepository;
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

    // 강좌개설1단계 service
    public LectureDto createLecture(LectureDto lectureDto, UserEntity userEntity) {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime startDate = lectureDto.getStart_date();
        LocalDateTime endDate = lectureDto.getEnd_date();
        LocalDateTime recruitEndDate = lectureDto.getRecruitEnd_date();
        LectureEntity lectureEntity = lectureDto.toEntity(userEntity);
        lectureEntity.setUser(userEntity);
        lectureDto.setUid(userEntity.getUid());
        lectureDto.setUserName(userEntity.getName());
        validateRequiredFields(lectureDto);

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

    // 강좌개설3단계 완료처리
    public void createLecture(CompleteLectureDataDto completeLectureDataDto) {
        // 강좌 정보 유효성 검사
        validateLectureData(completeLectureDataDto);
        UserEntity userEntity = userRepository.findById(completeLectureDataDto.getUid())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 1단계 강좌 정보 저장
        LectureEntity lectureEntity = LectureEntity.builder()
                .creator(completeLectureDataDto.getCreator())
                .category(completeLectureDataDto.getCategory())
                .image_url(completeLectureDataDto.getImageUrl())
                .title(completeLectureDataDto.getTitle())
                .content(completeLectureDataDto.getContent())
                .learningTarget(completeLectureDataDto.getLearningTarget())
                .week(completeLectureDataDto.getWeek())
                .recruitEnd_date(completeLectureDataDto.getRecruitEndDate())
                .start_date(completeLectureDataDto.getStartDate())
                .end_date(completeLectureDataDto.getEndDate())
                .maxParticipants(completeLectureDataDto.getMaxParticipants())
                .region(completeLectureDataDto.getRegion())
                .price(completeLectureDataDto.getPrice())
                .bank_name(completeLectureDataDto.getBankName())
                .account_name(completeLectureDataDto.getAccountName())
                .account_number(completeLectureDataDto.getAccountNumber())
                .build();

        // 데이터베이스에 저장
        lectureRepository.save(lectureEntity);

        // 로깅 혹은 추가적인 처리 (옵션)
        log.info("강좌가 성공적으로 생성되었습니다: {}", lectureEntity.getCreate_id());
    }

    // 강좌개설1단계 데이터 유효성 검사 메소드
    private void validateRequiredFields(LectureDto lectureDto) {
        if(lectureDto.getTitle() == null || lectureDto.getTitle().trim().isEmpty()){
            throw new IllegalArgumentException("제목을 입력해주세요!");
        }
        if(lectureDto.getContent() == null || lectureDto.getContent().trim().isEmpty()){
            throw new IllegalArgumentException("강좌소개를 입력해주세요!");
        }
        if(lectureDto.getLearning_target() == null || lectureDto.getLearning_target().trim().isEmpty()){
            throw new IllegalArgumentException("학습대상을 입력해주세요!");
        }
        if(lectureDto.getWeek() == null || lectureDto.getWeek() <= 0){
            throw new IllegalArgumentException("주 횟수를 입력해주세요! 그리고 주 횟수는 양수여야 합니다!");
        }
        if(lectureDto.getMax_participants() == null || lectureDto.getMax_participants() <= 0){
            throw new IllegalArgumentException("참가자수를 입력해주세요! 그리고 참가자수는 양수여야 합니다!");
        }
        if(lectureDto.getPrice() == null || lectureDto.getPrice() <= 0){
            throw new IllegalArgumentException("가격을 입력해주세요! 그리고 가격은 양수여야 합니다!");
        }
        if(lectureDto.getAccount_name() == null || lectureDto.getAccount_name().trim().isEmpty()){
            throw new IllegalArgumentException("계좌주를 입력해주세요!");
        }
        if(lectureDto.getAccount_number() == null || lectureDto.getAccount_number().trim().isEmpty()){
            throw new IllegalArgumentException("계좌번호를 입력해주세요!");
        }
    }

    // 강좌1단계 수정 데이터 유효성 검사 메소드
    private void validateLectureData(CompleteLectureDataDto data) {
        if (data.getCategory() == null || data.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리를 선택해주세요!");
        }
        if (data.getTitle() == null || data.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("제목을 입력해주세요!");
        }
        if (data.getContent() == null || data.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("강좌소개을 입력해주세요!");
        }
        if (data.getLearningTarget() == null || data.getLearningTarget().trim().isEmpty()) {
            throw new IllegalArgumentException("학습대상을 입력해주세요!");
        }
    }

    // 로그인된 유저의 강좌개설 2단계에서 "이전단계" 강좌개설 1단계 정보를 불러와 수정 Service
    // 로그인된 유저의 개설된 강좌에서 강좌개설 1단계 정보를 불러와 수정API Service
    public LectureDto updateLecture(Long create_id, LectureDto lectureDto) {
        LectureEntity existingLecture = lectureRepository.findById(create_id)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + create_id));

        existingLecture.setCategory(lectureDto.getCategory());
        existingLecture.setTitle(lectureDto.getTitle());
        existingLecture.setContent(lectureDto.getContent());
        existingLecture.setLearningTarget(lectureDto.getLearning_target());
        existingLecture.setWeek(lectureDto.getWeek());
        existingLecture.setRecruitEnd_date(lectureDto.getRecruitEnd_date());
        existingLecture.setStart_date(lectureDto.getStart_date());
        existingLecture.setEnd_date(lectureDto.getEnd_date());
        existingLecture.setMaxParticipants(lectureDto.getMax_participants());
        existingLecture.setRegion(lectureDto.getRegion());
        existingLecture.setPrice(lectureDto.getPrice());
        existingLecture.setBank_name(lectureDto.getBank_name());
        existingLecture.setAccount_name(lectureDto.getAccount_name());
        existingLecture.setAccount_number(lectureDto.getAccount_number());
        existingLecture.setImage_url(lectureDto.getImage_url());

        LectureEntity updatedLecture = lectureRepository.save(existingLecture);
        return convertToDto(updatedLecture);
    }

    // 강좌삭제
    @Transactional
    public String deleteLectureCreatedByUser(Long create_id, String username) {
        UserEntity user = userRepository.findByPhoneNumber(username)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        LectureEntity lecture = lectureRepository.findById(create_id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 강좌를 찾을 수 없습니다."));

        if (!lecture.getUser().getUid().equals(user.getUid())) {
            throw new RuntimeException("해당 강좌를 삭제할 권한이 없습니다.");
        }

        String lectureTitle = lecture.getTitle();

        // 해당 강좌에 대한 모든 신청 정보를 삭제
        lectureApplyRepository.deleteByLecture(lecture);

        // 마지막으로 강좌 삭제
        lectureRepository.deleteById(create_id);

        return lectureTitle;
    }

    // 강좌상세보기
    public LectureDetailDto getDetailLectureById(Long create_id) {
        LectureEntity lectureEntity = lectureRepository.findById(create_id)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + create_id));
        LectureDto lectureDto = convertToDto(lectureEntity);

        List<WeekDto> weekDtos = weekRepository.findByCreateId(create_id)
                .stream()
                .map(WeekDto::new)
                .collect(Collectors.toList());

        List<WeekPlanDto> weekPlanDtos = weekRepository.findByCreateId(create_id)
                .stream()
                .flatMap(week -> week.getPlans().stream())
                .map(WeekPlanDto::new)
                .collect(Collectors.toList());

        AttendanceEntity attendanceEntity = attendanceRepository.findByCreate_id(lectureEntity)
                .orElseThrow(() -> new RuntimeException("출석 정보가 없습니다."));
        AttendanceDto attendanceDto = new AttendanceDto(attendanceEntity);

        return new LectureDetailDto(lectureDto, weekDtos, weekPlanDtos, attendanceDto);
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

    private LectureDto convertToDto(LectureEntity lectureEntity) {
        UserEntity userEntity = lectureEntity.getUser();
        return LectureDto.builder()
                .create_id(lectureEntity.getCreate_id())
                .uid(userEntity.getUid())
                .userName(userEntity.getName())
                .creator(lectureEntity.getCreator())
                .category(lectureEntity.getCategory())
                .image_url(lectureEntity.getImage_url())
                .title(lectureEntity.getTitle())
                .content(lectureEntity.getContent())
                .learning_target(lectureEntity.getLearningTarget())
                .week(lectureEntity.getWeek())
                .recruitEnd_date(lectureEntity.getRecruitEnd_date())
                .start_date(lectureEntity.getStart_date())
                .end_date(lectureEntity.getEnd_date())
                .max_participants(lectureEntity.getMaxParticipants())
                .current_participants(lectureEntity.getCurrentParticipants())
                .region(lectureEntity.getRegion())
                .price(lectureEntity.getPrice())
                .bank_name(lectureEntity.getBank_name())
                .account_name(lectureEntity.getAccount_name())
                .account_number(lectureEntity.getAccount_number())
                .status(lectureEntity.getStatus())
                .createdDate(lectureEntity.getCreatedDate())
                .build();
    }
}
