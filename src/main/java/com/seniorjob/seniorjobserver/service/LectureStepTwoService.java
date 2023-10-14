package com.seniorjob.seniorjobserver.service;

import com.seniorjob.seniorjobserver.domain.entity.AttendanceEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.WeekEntity;
import com.seniorjob.seniorjobserver.domain.entity.WeekPlanEntity;
import com.seniorjob.seniorjobserver.dto.*;
import com.seniorjob.seniorjobserver.repository.*;
import com.seniorjob.seniorjobserver.repository.CompletionLectureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LectureStepTwoService {
    // 강좌개설 2단계API 구현
    private final WeekRepository weekRepository;
    private final WeekPlanRepository weekPlanRepository;
    private final CompletionLectureRepository completionLectureRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final LectureService lectureService;

    @Autowired
    public LectureStepTwoService(WeekRepository weekRepository, WeekPlanRepository weekPlanRepository,
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

    // 강좌개설 2단계API 구현

    // 주차별 제목Service
    public WeekEntity addWeekTitle(Long lectureId, String weekTitle) {
        if (weekTitle == null || weekTitle.trim().isEmpty()){
            throw new IllegalArgumentException("제목을 입력해주세요! 입력하지 않으시면 삭제해주세요!");
        }
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("해당하는 강좌를 찾을 수 없습니다."));

        List<WeekEntity> weeks = weekRepository.findByCreate_idOrderByWeekNumberAsc(lecture);
        int nextWeekNumber = weeks.size() + 1;

        WeekEntity newWeek = WeekEntity.builder()
                .create_id(lecture)
                .week_number(nextWeekNumber)
                .week_title(weekTitle)
                .build();

        return weekRepository.save(newWeek);
    }

    // 주차별 제목밑에 해당주차의 상세내용 Service
    public ExtendWeekPlanDto addWeekPlan(Long weekId, String detail) {
        if(detail == null || detail.trim().isEmpty()){
            throw new IllegalArgumentException("상세내용을 입력해주세요! 입력하지 않으시면 삭제해주세요!");
        }
        WeekEntity week = weekRepository.findById(weekId)
                .orElseThrow(() -> new ResourceNotFoundException("강좌개설2단계에서 해당 주차를 찾을 수 없습니다."));

        int nextDetailNumber = week.getPlans().size() + 1;

        WeekPlanEntity weekPlan = WeekPlanEntity.builder()
                .week(week)
                .detail_number(nextDetailNumber)
                .detail(detail)
                .build();

        week.getPlans().add(weekPlan);

        weekRepository.save(week);

        List<WeekPlanDto> details = week.getPlans().stream()
                .map(WeekPlanDto::new)
                .collect(Collectors.toList());

        return new ExtendWeekPlanDto(
                week.getWeek_id(),
                week.getWeek_title(),
                details
        );
    }

    // 강좌개설 2단계에서 수료 출석 조건 설정 Service
    public AttendanceDto setAttendanceCondition(Long lectureId, int requireAttendance){
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(()-> new ResourceNotFoundException("강좌개설2단계에서 해당 주차를 찾을 수 없습니다."));
        if(requireAttendance <= 0 || requireAttendance >= 5){
            throw new IllegalArgumentException("수료에 필요한 출석 회수를 1회이상 5회 이하로 설정해 주세요");
        }
        AttendanceEntity attendanceEntity = AttendanceEntity.builder()
                .create_id(lecture)
                .required_attendance(requireAttendance)
                .build();
        attendanceRepository.save(attendanceEntity);

        return new AttendanceDto(attendanceEntity);
    }

    // 강좌개설 3단계 : 1,2단게에서 입력한 모든 정보를 확인하는 API Service
    public CreateLectureFullInfoDto getFullLectureInfo(Long lectureId) {
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


        // 최종적으로 모든 정보를 하나의 DTO에 담아 반환
        return new CreateLectureFullInfoDto(lectureDto, weekDtos, weekPlanDtos);
    }

    // 강좌개설 3단계 : 1,2단게에서 입력한 모든 정보를 확인한뒤 "이전단계"로 돌아가 수정하는 API Service


    // 강좌개설 3단계 : 1,2단게에서 입력한 모든 정보를 확인한뒤 "강좌개설" 클릭시 강좌가 최종 개설되는 API Service


    private LectureDto convertToDto(LectureEntity lecture) {
        return LectureDto.builder()
                .create_id(lecture.getCreate_id())
                .creator(lecture.getCreator())
                .max_participants(lecture.getMaxParticipants())
                .current_participants(lecture.getCurrentParticipants())
                .category(lecture.getCategory())
                .bank_name(lecture.getBank_name())
                .account_name(lecture.getAccount_name())
                .account_number(lecture.getAccount_number())
                .price(lecture.getPrice())
                .title(lecture.getTitle())
                .content(lecture.getContent())
                //.cycle(lectureEntity.getCycle())
                .week(lecture.getWeek())
                .learning_target(lecture.getLearningTarget())
                //.attendance_requirements(lectureEntity.getAttendanceRequirements())
                //.count(lectureEntity.getCount())
                .start_date(lecture.getStart_date())
                .end_date(lecture.getEnd_date())
                .region(lecture.getRegion())
                .status(lecture.getStatus())
                .image_url(lecture.getImage_url())
                .createdDate(lecture.getCreatedDate())
                .recruitEnd_date(lecture.getRecruitEnd_date())
                .build();
    }

}
