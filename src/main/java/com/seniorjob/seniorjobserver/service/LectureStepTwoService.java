package com.seniorjob.seniorjobserver.service;

import com.seniorjob.seniorjobserver.domain.entity.*;
import com.seniorjob.seniorjobserver.dto.*;
import com.seniorjob.seniorjobserver.repository.*;
import com.seniorjob.seniorjobserver.repository.CompletionLectureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    public ExtendWeekPlanDto addWeekPlan(Long lectureId, Long weekId, String detail) {
        if(detail == null || detail.trim().isEmpty()){
            throw new IllegalArgumentException("상세내용을 입력해주세요! 입력하지 않으시면 삭제해주세요!");
        }

        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 강좌를 찾을 수 없습니다."));

        WeekEntity week = weekRepository.findByIdAndCreateId(weekId, lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("강좌개설2단계에서 해당 주차를 찾을 수 없습니다."));

        int nextDetailNumber = week.getPlans().size() + 1;

        WeekPlanEntity weekPlan = WeekPlanEntity.builder()
                .week(week)
                .create_id(lecture)
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
                lecture.getCreate_id(),
                week.getWeek_title(),
                details);
    }

    // 강좌개설 2단계에서 수료 출석 조건 설정 Service
    public AttendanceDto setAttendanceCondition(Long lectureId, int requireAttendance){
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(()-> new ResourceNotFoundException("강좌개설2단계에서 해당 주차를 찾을 수 없습니다."));

        // 강좌 상태 검사
        if (lecture.getStatus() != LectureEntity.LectureStatus.신청가능상태) {
            throw new IllegalStateException("강좌 상태가 '신청가능상태'가 아닙니다. 수료조건 출석횟수를 설정할 수 없습니다.");
        }

        if(requireAttendance <= 0 || requireAttendance >= 10){
            throw new IllegalArgumentException("수료에 필요한 출석 회수를 1회이상 10회 이하로 설정해 주세요");
        }
        AttendanceEntity attendanceEntity = AttendanceEntity.builder()
                .create_id(lecture)
                .required_attendance(requireAttendance)
                .build();
        attendanceRepository.save(attendanceEntity);

        return new AttendanceDto(attendanceEntity);
    }

    // 강좌개설 3단계 : 1,2단게에서 입력한 모든 정보를 확인하는 API Service == 상세정보로 활용
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
        AttendanceEntity attendanceEntity = attendanceRepository.findByCreate_id(lecture)
                .orElseThrow(() -> new ResourceNotFoundException("출석 조건 정보를 찾을 수 없습니다."));
        AttendanceDto attendanceDto = new AttendanceDto(attendanceEntity);

        // 최종적으로 모든 정보를 하나의 DTO에 담아 반환
        return new CreateLectureFullInfoDto(lectureDto, weekDtos, weekPlanDtos, attendanceDto);
    }

    // 1. weekTitle수정 Service
    public WeekDto updateWeekTitle(Long lectureId, Long weekId, String title, UserDetails userDetails){
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("해당하는 강좌를 찾을 수 없습니다."));

        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(()-> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        if (title == null || title.trim().isEmpty()){
            throw new IllegalArgumentException("제목을 입력해주세요! 입력하지 않으시면 삭제해주세요!");
        }

        WeekEntity weekEntity = weekRepository.findByIdAndCreateId(weekId, lectureId)
                .orElseThrow(()-> new ResourceNotFoundException("해당 주차를 찾을 수 없습니다."));
        weekEntity.setWeek_title(title);
        weekRepository.save(weekEntity);
        return new WeekDto(weekEntity);
    }

    // 1. weekTitle삭제API Service
    public void deleteWeek(Long lectureId, Long weekId, UserDetails userDetails){
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("해당하는 강좌를 찾을 수 없습니다."));

        WeekEntity weekEntity = weekRepository.findByIdAndCreateId(weekId, lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 주차를 찾을 수 없습니다."));
        weekRepository.delete(weekEntity);
    }

    // 2. weekPlan수정 Service
    public WeekPlanDto updateWeekPlan(Long lectureId, Long weekId, Long planId, String detail) {
        if (detail == null || detail.trim().isEmpty()) {
            throw new IllegalArgumentException("상세내용을 입력해주세요! 입력하지 않으시면 삭제해주세요!");
        }

        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 강좌를 찾을 수 없습니다."));

        WeekEntity week = weekRepository.findByIdAndCreateId(weekId, lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 주차를 찾을 수 없습니다."));

        WeekPlanEntity weekPlan = weekPlanRepository.findPlanByIdAndWeek(planId, week)
                .orElseThrow(() -> new ResourceNotFoundException("강좌개설2단계에서 해당 상세주차별 내용을 찾을 수 없습니다."));

        weekPlan.setDetail(detail);
        weekPlanRepository.save(weekPlan);

        return new WeekPlanDto(weekPlan);
    }

    // 2. weekPlan삭제 Service
    public void deleteWeekPlan(Long lectureId, Long weekId, Long planId){
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 강좌를 찾을 수 없습니다."));

        WeekEntity week = weekRepository.findByIdAndCreateId(weekId, lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 주차를 찾을 수 없습니다."));

        WeekPlanEntity weekPlan = weekPlanRepository.findPlanByIdAndWeek(planId, week)
                .orElseThrow(() -> new ResourceNotFoundException("강좌개설2단계에서 해당 상세주차별 내용을 찾을 수 없습니다."));

        weekPlanRepository.deleteById(planId);
    }

    // 3. attendance수정 Service 출석조건은 수정만가능하다.
    public AttendanceDto updateAttendanceCondition(Long lectureId, int requiredAttendance) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 강좌를 찾을 수 없습니다."));

        // 강좌 상태 검사
        if (lecture.getStatus() != LectureEntity.LectureStatus.신청가능상태) {
            throw new IllegalStateException("강좌 상태가 '신청가능상태'가 아닙니다. 수료조건 출석횟수를 설정할 수 없습니다.");
        }

        if (requiredAttendance <= 0 || requiredAttendance >= 10) {
            throw new IllegalArgumentException("수료에 필요한 출석 회수를 1회이상 10회 이하로 설정해 주세요");
        }

        AttendanceEntity attendanceEntity = attendanceRepository.findByCreate_id(lecture)
                .orElseThrow(() -> new ResourceNotFoundException("출석 조건 정보를 찾을 수 없습니다."));

        attendanceEntity.setRequiredAttendance(requiredAttendance);
        attendanceRepository.save(attendanceEntity);

        return new AttendanceDto(attendanceEntity);
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
                .status(lecture.getStatus())
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
                .build();
    }

}
