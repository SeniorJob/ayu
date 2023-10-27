package com.seniorjob.seniorjobserver.controller;

import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.domain.entity.WeekEntity;
import com.seniorjob.seniorjobserver.dto.*;
import com.seniorjob.seniorjobserver.repository.LectureRepository;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import com.seniorjob.seniorjobserver.repository.WeekRepository;
import com.seniorjob.seniorjobserver.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lecturesStepTwo")
public class LectureStepTwoController {
    // 강좌개설 2단계API 구현
    private final LectureService lectureService;
    private final LectureStepTwoService lectureStepTwoService;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final UserService userService;
    private final WeekRepository weekRepository;
    private static final Logger log = LoggerFactory.getLogger(LectureController.class);

    @Autowired
    public LectureStepTwoController(LectureService lectureService, LectureStepTwoService lectureStepTwoService, StorageService storageService, UserRepository userRepository, UserService userService, LectureRepository lectureRepository, WeekRepository weekRepository) {
        this.lectureService = lectureService;
        this.lectureStepTwoService = lectureStepTwoService;
        this.storageService = storageService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.lectureRepository = lectureRepository;
        this.weekRepository = weekRepository;
    }

    // 주차별 제목Controller
    @PostMapping("/{lectureId}/weeks")
    public ResponseEntity<WeekDto> addWeekTitle(
            @PathVariable Long lectureId,
            @RequestParam String title,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 현재 사용자가 강좌의 생성자인지 확인
        LectureEntity lectureEntity = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + lectureId));
        if (!lectureEntity.getUser().equals(currentUser)) {
            throw new RuntimeException("해당 강좌의 주차별 제목을 작성할 권한이 없습니다.");
        }

        WeekEntity newWeek = lectureStepTwoService.addWeekTitle(lectureId, title);
        WeekDto responseDto = new WeekDto(newWeek);
        return ResponseEntity.ok(responseDto);
    }

    // 주차별 제목밑에 해당주차의 상세내용Controller
    @PostMapping("/lectures/{lectureId}/weeks/{weekId}/plans")
    public ResponseEntity<ExtendWeekPlanDto> addWeekPlan(
            @PathVariable Long lectureId,
            @PathVariable Long weekId,
            @RequestParam String detail,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 현재 사용자가 강좌의 생성자인지 확인
        LectureEntity lectureEntity = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + lectureId));
        if (!lectureEntity.getUser().equals(currentUser)) {
            throw new RuntimeException("해당 강좌의 제목의 주차별 상세내용을 작성할 권한이 없습니다.");
        }

        ExtendWeekPlanDto newPlanDto = lectureStepTwoService.addWeekPlan(lectureId, weekId, detail);
        return ResponseEntity.ok(newPlanDto);
    }

    // 수료출석조건등록Controller ex) 주 n회 이상 출석시 수료
    @PostMapping("/{lectureId}/attendance")
    public ResponseEntity<AttendanceDto> setAttendanceCondition(
            @PathVariable Long lectureId,
            @RequestParam int requiredAttendance,
            @AuthenticationPrincipal UserDetails userDetails){

        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 현재 사용자가 강좌의 생성자인지 확인
        LectureEntity lectureEntity = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + lectureId));

        if (!lectureEntity.getUser().equals(currentUser)) {
            throw new RuntimeException("해당 강좌의 수료조건을 작성할 권한이 없습니다.");
        }

        AttendanceDto attendanceDto = lectureStepTwoService.setAttendanceCondition(lectureId, requiredAttendance);
        return ResponseEntity.ok(attendanceDto);
    }

    // 강좌개설 3단계 : 1,2단게에서 입력한 모든 정보를 확인하는 API Controller
    @GetMapping("/{lectureId}/review")
    public ResponseEntity<CreateLectureFullInfoDto> reviewLectureInfo(
            @PathVariable Long lectureId){
            CreateLectureFullInfoDto lectureFullInfo = lectureStepTwoService.getFullLectureInfo(lectureId);
            return ResponseEntity.ok(lectureFullInfo);
    }

    // 강좌개설 3단계 : 1,2단게에서 입력한 모든 정보를 확인한뒤 "이전단계"로 돌아가 수정하는 로직

    // 1. weekTitle수정 API Controller
    @PutMapping("/{lectureId}/weeks/{weekId}/week-update")
    public ResponseEntity<WeekDto> updateWeekTitle(
            @PathVariable Long lectureId,
            @PathVariable Long weekId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String title){

        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 현재 사용자가 강좌의 생성자인지 확인
        LectureEntity lectureEntity = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + lectureId));
        if (!lectureEntity.getUser().equals(currentUser)) {
            throw new RuntimeException("해당 강좌의 주차별 제목을 수정할 권한이 없습니다.");
        }

        WeekDto updatedWeek = lectureStepTwoService.updateWeekTitle(lectureId, weekId, title, userDetails);
        return ResponseEntity.ok(updatedWeek);
    }

    // 2. weekTitle삭제API Controller
    // week삭제시 week를 참조하는 week_plan도 삭제된다.
    @DeleteMapping("/{lectureId}/weeks/{weekId}/week-delete")
    public ResponseEntity<String> deleteWeek(
            @PathVariable Long lectureId,
            @PathVariable Long weekId,
            @AuthenticationPrincipal UserDetails userDetails){

        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 현재 사용자가 강좌의 생성자인지 확인
        LectureEntity lectureEntity = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + lectureId));
        if (!lectureEntity.getUser().equals(currentUser)) {
            throw new RuntimeException("해당 강좌의 주차별 제목을 삭제할 권한이 없습니다.");
        }

        lectureStepTwoService.deleteWeek(lectureId, weekId, userDetails);
        return ResponseEntity.ok(String.format("%d강좌의 %d주차별제목과 해당 주차별 제목을 참조하는 " +
                "주차별 상세내용들이 삭제되었습니다.", lectureId, weekId));
    }

    // 2. weekPlan수정API Controller
    @PutMapping("{lectureId}/weeks/{weekId}/plans/{planId}/plan-update")
    public ResponseEntity<WeekPlanDto> updateWeekPlan(
            @PathVariable Long lectureId,
            @PathVariable Long weekId,
            @PathVariable Long planId,
            @RequestParam String detail,
            @AuthenticationPrincipal UserDetails userDetails){

        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 현재 사용자가 강좌의 생성자인지 확인
        LectureEntity lectureEntity = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + lectureId));
        if (!lectureEntity.getUser().equals(currentUser)) {
            throw new RuntimeException("해당 강좌 -> 주차별제목의 상세내용을 수정할 권한이 없습니다.");
        }

        WeekPlanDto updateWeekPlan = lectureStepTwoService.updateWeekPlan(lectureId, weekId, planId, detail);
        return ResponseEntity.ok(updateWeekPlan);
    }

    // 2. weekPlan삭제API Controller
    @DeleteMapping("{lectureId}/weeks/{weekId}/plans/{planId}/plan-delete")
    public ResponseEntity<String> deleteWeekPlan(
            @PathVariable Long lectureId,
            @PathVariable Long weekId,
            @PathVariable Long planId,
            @AuthenticationPrincipal UserDetails userDetails){

        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 현재 사용자가 강좌의 생성자인지 확인
        LectureEntity lectureEntity = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + lectureId));
        if (!lectureEntity.getUser().equals(currentUser)) {
            throw new RuntimeException("해당 강좌 -> 주차별제목의 상세내용을 삭제할 권한이 없습니다.");
        }

        lectureStepTwoService.deleteWeekPlan(lectureId, weekId, planId);
        return ResponseEntity.ok(String.format("강좌번호 : %d, 주차제목번호 : %d, " +
                "주차별상세내용 번호 %d 가 삭제되었습니다.", lectureId, weekId, planId));
    }

    // 3. attendance수정API Controller 출석조건은 수정만가능하다.
    @PutMapping("{lectureId}/update-attendance")
    public ResponseEntity<AttendanceDto> updateAttendanceCondition(
            @PathVariable Long lectureId,
            @RequestParam int requiredAttendance,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 현재 사용자가 강좌의 생성자인지 확인
        LectureEntity lectureEntity = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + lectureId));
        if (!lectureEntity.getUser().equals(currentUser)) {
            throw new RuntimeException("해당 강좌의 출석조건을 수정할 권한이 없습니다.");
        }

        AttendanceDto updatedAttendance = lectureStepTwoService.updateAttendanceCondition(lectureId, requiredAttendance);
        return ResponseEntity.ok(updatedAttendance);
    }

    // 강좌개설 3단계 : 1,2단게에서 입력한 모든 정보를 확인한뒤 "강좌개설" 클릭시 강좌가 최종 개설되는 API Contorller

}
