package com.seniorjob.seniorjobserver.service;

import com.seniorjob.seniorjobserver.domain.entity.AttendanceEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.domain.entity.WeekEntity;
import com.seniorjob.seniorjobserver.dto.*;
import com.seniorjob.seniorjobserver.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class MypageService {

    private final WeekRepository weekRepository;
    private final WeekPlanRepository weekPlanRepository;
    private final CompletionLectureRepository completionLectureRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final LectureService lectureService;

    @Autowired
    public MypageService(WeekRepository weekRepository, WeekPlanRepository weekPlanRepository,
                                 CompletionLectureRepository completionLectureRepository, AttendanceRepository attendanceRepository,
                                 UserRepository userRepository, LectureRepository lectureRepository,
                                 LectureService lectureService) {
        this.weekRepository = weekRepository;
        this.weekPlanRepository = weekPlanRepository;
        this.completionLectureRepository = completionLectureRepository;
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.lectureRepository = lectureRepository;
        this.lectureService = lectureService;
    }

    // 강좌개설 3단계 : 1,2단게에서 입력한 모든 정보를 확인하는 API Service
    public LectureDetailDto getMypageInfo(Long lectureId) {
        // 강좌개설1단계 : 강좌 정보 가져오기
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("해당하는 강좌를 찾을 수 없습니다."));

        LectureDto lectureDto = convertToDto(lecture);

        // 강좌개설2단계 : 주차별 제목 정보 가져오기
        List<WeekEntity> weeks = weekRepository.findByCreate_idOrderByWeekNumberAsc(lecture);
        List<WeekDto> weekDtos = weeks.stream().map(WeekDto::new).collect(Collectors.toList());

        // 강좌개설2단계 : 주차별 상세 내용 정보 가져오기
        List<WeekPlanDto> weekPlanDtos = weeks.stream()
                .flatMap(week -> week.getPlans().stream())
                .map(WeekPlanDto::new)
                .collect(Collectors.toList());

        // 강좌개설2단계 : 출석 조건 정보 가져오기
        AttendanceEntity attendanceEntity = attendanceRepository.findByCreate_id(lecture)
                .orElseThrow(() -> new ResourceNotFoundException("출석 조건 정보를 찾을 수 없습니다."));
        AttendanceDto attendanceDto = new AttendanceDto(attendanceEntity);

        // 최종적으로 모든 정보를 하나의 DTO에 담아 반환
        return new LectureDetailDto(lectureDto, weekDtos, weekPlanDtos, attendanceDto);
    }

    private LectureDto convertToDto(LectureEntity lecture) {
        UserEntity userEntity = lecture.getUser();
        return LectureDto.builder()
                .create_id(lecture.getCreate_id())
                .userName(userEntity.getName())
                .uid(userEntity.getUid())
                .creator(lecture.getCreator())
                .userName(lecture.getUser().getName())
                .uid(lecture.getUser().getUid())
                .max_participants(lecture.getMaxParticipants())
                .current_participants(lecture.getCurrentParticipants())
                .category(lecture.getCategory())
                .bank_name(lecture.getBank_name())
                .account_name(lecture.getAccount_name())
                .account_number(lecture.getAccount_number())
                .price(lecture.getPrice())
                .title(lecture.getTitle())
                .content(lecture.getContent())
                .week(lecture.getWeek())
                .learning_target(lecture.getLearningTarget())
                .start_date(lecture.getStart_date())
                .end_date(lecture.getEnd_date())
                .region(lecture.getRegion())
                .image_url(lecture.getImage_url())
                .createdDate(lecture.getCreatedDate())
                .recruitEnd_date(lecture.getRecruitEnd_date())
                .status(lecture.getStatus())
                .build();
    }

}
