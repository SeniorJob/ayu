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

import java.util.Optional;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class MyPageLectureApplyService {

    private final LectureRepository lectureRepository;
    private final LectureApplyRepository lectureApplyRepository;
    private final UserRepository userRepository;
    private final WeekRepository weekRepository;
    private final WeekPlanRepository weekPlanRepository;
    //private final AttendanceRepository attendanceRepository;

    @Autowired
    public MyPageLectureApplyService(UserRepository userRepository, LectureRepository lectureRepository, LectureApplyRepository lectureApplyRepository,
                                     WeekRepository weekRepository, WeekPlanRepository weekPlanRepository) {
        this.userRepository = userRepository;
        this.lectureRepository = lectureRepository;
        this.lectureApplyRepository = lectureApplyRepository;
        this.weekRepository = weekRepository;
        this.weekPlanRepository = weekPlanRepository;
        //this.attendanceRepository = attendanceRepository;
    }

    public LectureApplyDto getLectureApplyById(Long id) {
        LectureApplyEntity lectureApply = lectureApplyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 ID에 맞는 강좌 신청을 찾을 수 없습니다: " + id));

        return new LectureApplyDto(lectureApply);
    }

    // 마이페이지(신청강좌) - 전체목록
    public List<LectureApplyDto> getMyAppliedLectures(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. id: " + userId));

        List<LectureApplyEntity> myApplyLectures = lectureApplyRepository.findByUser(user);

        if (myApplyLectures.isEmpty()) {
            throw new RuntimeException("신청하신 강좌가 없습니다..");
        }
        return myApplyLectures.stream()
                .map(LectureApplyDto::new)
                .collect(Collectors.toList());
    }

    // 마이페이지(신청강좌) - 목록필터링
    public Page<MyPageLectureApplyDto> getFilteredMyAppliedLectures(Long userId, String title, LectureEntity.LectureStatus status, String sort, int page, int size) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. id: " + userId));

        List<LectureApplyEntity> myApplyLectures = lectureApplyRepository.findByUser(user);
        if (myApplyLectures.isEmpty()) {
            throw new RuntimeException("신청하신 강좌가 없습니다..");
        }

        List<MyPageLectureApplyDto> lectureApplyDtos = myApplyLectures.stream()
                .map(applyEntity -> {
                    MyPageLectureApplyDto dto = new MyPageLectureApplyDto();
                    dto.setCreate_id(applyEntity.getLecture().getCreate_id());
                    dto.setLe_id(applyEntity.getLeId());
                    dto.setUid(applyEntity.getUser().getUid());
                    dto.setCreator(applyEntity.getLecture().getCreator());
                    dto.setCategory(applyEntity.getLecture().getCategory());
                    dto.setTitle(applyEntity.getLecture().getTitle());
                    dto.setRecruitEnd_date(applyEntity.getLecture().getRecruitEnd_date());
                    dto.setStart_date(applyEntity.getLecture().getStart_date());
                    dto.setEnd_date(applyEntity.getLecture().getEnd_date());
                    dto.setMax_participants(applyEntity.getLecture().getMaxParticipants());
                    dto.setCurrent_participants(applyEntity.getLecture().getCurrentParticipants());
                    dto.setRegion(applyEntity.getLecture().getRegion());
                    dto.setPrice(applyEntity.getLecture().getPrice());
                    dto.setStatus(applyEntity.getLecture().getStatus());
                    dto.setCreatedDate(applyEntity.getLecture().getCreatedDate());
                    dto.getDaysUntilRecruitEndMessage();
                    return dto;
                })
                .collect(Collectors.toList());

        // 제목 필터링
        if (title != null && !title.isEmpty()) {
            lectureApplyDtos = lectureApplyDtos.stream()
                    .filter(lecture -> lecture.getTitle().contains(title))
                    .collect(Collectors.toList());
        }

        // 상태 필터링
        if (status != null) {
            lectureApplyDtos = lectureApplyDtos.stream()
                    .filter(lecture -> lecture.getStatus().equals(status))
                    .collect(Collectors.toList());
        }

        // 정렬
        switch (sort) {
            case "최신순":
                lectureApplyDtos.sort(Comparator.comparing(MyPageLectureApplyDto::getCreatedDate).reversed());
                break;
            case "오래된순":
                lectureApplyDtos.sort(Comparator.comparing(MyPageLectureApplyDto::getCreatedDate));
                break;
            case "인기순":
                lectureApplyDtos.sort(Comparator.comparing(MyPageLectureApplyDto::getCurrent_participants).reversed());
                break;
            case "가격높은순":
                lectureApplyDtos.sort(Comparator.comparing(MyPageLectureApplyDto::getPrice).reversed());
                break;
            case "가격낮은순":
                lectureApplyDtos.sort(Comparator.comparing(MyPageLectureApplyDto::getPrice));
                break;
        }

        // 페이지네이션
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), lectureApplyDtos.size());

        return new PageImpl<>(lectureApplyDtos.subList(start, end), pageable, lectureApplyDtos.size());
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
        LectureDto lectureDto = convertToDto(lectureEntity);

        // 3. 강좌 신청 정보 조회
        LectureApplyEntity lectureApplyEntity = lectureApplyRepository.findByUserAndLecture(currentUser, lectureEntity)
                .orElseThrow(() -> new ResourceNotFoundException("신청된 강좌 정보를 찾을 수 없습니다."));

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
