package com.seniorjob.seniorjobserver.service;

import com.seniorjob.seniorjobserver.domain.entity.*;
import com.seniorjob.seniorjobserver.dto.*;
import com.seniorjob.seniorjobserver.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class MyPageLectureApplyService {

    private final LectureRepository lectureRepository;
    private final LectureApplyRepository lectureApplyRepository;
    private final LectureApplyService lectureApplyService;
    private final UserRepository userRepository;
    private final WeekRepository weekRepository;
    private final WeekPlanRepository weekPlanRepository;
    //private final AttendanceRepository attendanceRepository;

    @Autowired
    public MyPageLectureApplyService(UserRepository userRepository, LectureRepository lectureRepository, LectureApplyRepository lectureApplyRepository,
                                     WeekRepository weekRepository, WeekPlanRepository weekPlanRepository,
                                     LectureApplyService lectureApplyService) {
        this.userRepository = userRepository;
        this.lectureRepository = lectureRepository;
        this.lectureApplyRepository = lectureApplyRepository;
        this.weekRepository = weekRepository;
        this.weekPlanRepository = weekPlanRepository;
        //this.attendanceRepository = attendanceRepository;
        this.lectureApplyService = lectureApplyService;
    }

    public LectureApplyDto getLectureApplyById(Long id) {
        LectureApplyEntity lectureApply = lectureApplyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 ID에 맞는 강좌 신청을 찾을 수 없습니다: " + id));

        return new LectureApplyDto(lectureApply);
    }

    // 로그인후 자신이 신청한 강좌 상세보기
    public MyPageLectureApplyDetailDto getMyAppliedLectureDetail(Long userId, Long create_id) {

        // 1. 사용자 정보 조회
        UserEntity currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. id: " + userId));

        // 2. 강좌 정보 조회
        LectureEntity lectureEntity = lectureRepository.findById(create_id)
                .orElseThrow(() -> new ResourceNotFoundException("해당하는 강좌를 찾을 수 없습니다."));

        // Convert LectureEntity to LectureDto


        // 3. 강좌 신청 정보 조회
        LectureApplyEntity lectureApplyEntity = lectureApplyRepository.findByUserAndLecture(currentUser, lectureEntity)
                .orElseThrow(() -> new ResourceNotFoundException("신청된 강좌 정보를 찾을 수 없습니다."));

        Long leId = lectureApplyEntity.getLeId();

        LectureDto lectureDto = convertToDto(lectureEntity, leId);

        LectureApplyDto lectureApplyDto = new LectureApplyDto(lectureApplyEntity);

        // 4. 주차별 정보 및 상세 내용 조회
        List<WeekEntity> weeks = weekRepository.findByCreateId(create_id);
        List<WeekDto> weekDtos = weeks.stream().map(WeekDto::new).collect(Collectors.toList());
        List<WeekPlanDto> weekPlanDtos = weeks.stream()
                .flatMap(week -> week.getPlans().stream())
                .map(WeekPlanDto::new)
                .collect(Collectors.toList());

        // 5. 출석 조건 정보 조회
//        AttendanceEntity attendanceEntity = attendanceRepository.findByCreate_id(lectureEntity)
//                .orElseThrow(() -> new ResourceNotFoundException("출석 조건 정보를 찾을 수 없습니다."));
//        AttendanceDto attendanceDto = new AttendanceDto(attendanceEntity);

        // 모든 정보를 DTO에 담아 반환
        return new MyPageLectureApplyDetailDto(lectureApplyDto, lectureDto, weekDtos, weekPlanDtos);
    }

    // 마이페이지(신청강좌) - 목록필터링

    // 강좌ID기반 강좌상태 가져오는 메서드
    public LectureEntity.LectureStatus getLectureStatus(Long create_id) {
        LectureEntity lectureEntity = lectureRepository.findById(create_id)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + create_id));
        return lectureEntity.getStatus();
    }

    // 마이페이지(신청강좌) - 자신이 신청한 강좌목록
    public List<MyPageLectureApplyDto> getMyAppliedLectures(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. id: " + userId));

        List<LectureApplyEntity> myApplyLectures = lectureApplyRepository.findByUser(user);
        if (myApplyLectures.isEmpty()) {
            throw new RuntimeException("신청하신 강좌가 없습니다.");
        }

        return myApplyLectures.stream()
                .map(lectureApply -> new MyPageLectureApplyDto(
                        lectureApply,
                        lectureApply.getLecture() // LectureEntity 객체 전달
                ))
                .collect(Collectors.toList());
    }

    // 강좌검색 : 제목
    public List<MyPageLectureApplyDto> searchLecturesByTitle(Long uid, String title) {
        if (title == null || title.length() < 2) {
            throw new IllegalArgumentException("검색어는 \"2글자\" 이상 입력해주세요!");
        }

        List<MyPageLectureApplyDto> resultList = new ArrayList<>();

        // 사용자가 신청한 강좌 목록을 가져옴
        List<LectureApplyEntity> userAppliedLectures = lectureApplyRepository.findByUserUid(uid);

        // 제목에 해당하는 강좌만 필터링
        userAppliedLectures.stream()
                .filter(lectureApply -> lectureApply.getLecture().getTitle().contains(title))
                .forEach(lectureApply -> {
                    resultList.add(new MyPageLectureApplyDto(lectureApply, lectureApply.getLecture()));
                });

        if (resultList.isEmpty()) {
            throw new NoSuchElementException("검색결과에 해당하는 강좌가 없습니다.ㅠㅠ");
        }

        return resultList;
    }

    // 강좌정렬
    // 최신순으로 강좌 정렬 최신 = true 오래된 = false
    public List<MyPageLectureApplyDto> sortLecturesByCreatedDate(List<MyPageLectureApplyDto> appliedLectures, boolean descending) {
        appliedLectures.sort((a, b) -> descending ?
                b.getCreatedDate().compareTo(a.getCreatedDate()) :
                a.getCreatedDate().compareTo(b.getCreatedDate()));
        return appliedLectures;
    }

    // 기존코드 인기순 : max_participant가많은순 -> 강좌 참여하기를 만들때 실제참여자가 많은순으로 변경할것임
    // 수정된 인기순 : 강좌에 참여한 사람이 많은 강좌순 : 참여자수 current_participants
    public List<MyPageLectureApplyDto> sortLecturesByPopularity(List<MyPageLectureApplyDto> appliedLectures, boolean descending) {
        appliedLectures.sort((a, b) -> descending ?
                b.getCurrent_participants() - a.getCurrent_participants() :
                a.getCurrent_participants() - b.getCurrent_participants());
        return appliedLectures;
    }

    // 가격순 : prices(낮은순 높은순)
    public List<MyPageLectureApplyDto> sortLecturesByPrice(List<MyPageLectureApplyDto> appliedLectures, boolean descending) {
        appliedLectures.sort((a, b) -> descending ?
                b.getPrice().compareTo(a.getPrice()) :
                a.getPrice().compareTo(b.getPrice()));
        return appliedLectures;
    }

    // 강좌상태검색
    public List<MyPageLectureApplyDto> filterStatus(List<MyPageLectureApplyDto> appliedLectures, LectureEntity.LectureStatus status) {
        return appliedLectures.stream()
                .filter(lecture -> lecture.getStatus() == status)
                .collect(Collectors.toList());
    }

    // 필터링 : 제목검색 -> 최신순,오래된순, 가격높은순, 가격낮은순, 인기순, 지역(시,군),
    public List<MyPageLectureApplyDto> filterLectures(List<MyPageLectureApplyDto> appliedLectures, String filter, boolean descending) {
        switch (filter){
            case "latest":
                return sortLecturesByCreatedDate(appliedLectures, descending);
            case "price":
                return sortLecturesByPrice(appliedLectures, descending);
            case "popularity":
                return sortLecturesByPopularity(appliedLectures, descending);
            default:
                throw new IllegalArgumentException("잘못된 필터조건");
        }
    }

    //페이징
    public Page<LectureEntity> getLectures(Pageable pageable) {
        return lectureRepository.findAll(pageable);
    }

    private LectureDto convertToDto(LectureEntity lectureEntity, Long leId) {
        UserEntity userEntity = lectureEntity.getUser();
        return LectureDto.builder()
                .create_id(lectureEntity.getCreate_id())
                .uid(userEntity.getUid())
                .leId(leId)
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
