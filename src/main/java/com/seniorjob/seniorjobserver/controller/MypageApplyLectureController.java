package com.seniorjob.seniorjobserver.controller;

import com.seniorjob.seniorjobserver.controller.LectureController;
import com.seniorjob.seniorjobserver.domain.entity.LectureApplyEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.CreateLectureFullInfoDto;
import com.seniorjob.seniorjobserver.dto.LectureApplyDto;
import com.seniorjob.seniorjobserver.repository.LectureRepository;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import com.seniorjob.seniorjobserver.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mypageApplyLecture") // 신청강좌
public class MypageApplyLectureController {
    private final LectureService lectureService;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(LectureController.class);
    private final LectureProposalService lectureProposalService;
    private final LectureApplyService lectureApplyService;

    private final LectureStepTwoService lectureStepTwoService;

    public MypageApplyLectureController(LectureService lectureService, StorageService storageService, UserRepository userRepository, UserService userService, LectureRepository lectureRepository,
                                        LectureProposalService lectureProposalService, LectureApplyService lectureApplyService, LectureStepTwoService lectureStepTwoService) {
        this.lectureService = lectureService;
        this.storageService = storageService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.lectureRepository = lectureRepository;
        this.lectureProposalService = lectureProposalService;
        this.lectureApplyService = lectureApplyService;
        this.lectureStepTwoService = lectureStepTwoService;
    }

    // 세션로그인후 자신이 신청한 강좌 전체 조화 API (신청강좌)
    @GetMapping("/myApplyLectureAll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyAppliedLectures(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

            List<LectureApplyDto> myAppliedLectures = lectureApplyService.getMyAppliedLectures(currentUser.getUid());
            return ResponseEntity.ok(myAppliedLectures);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 세션로그인후 자신이 신청이유 수정 API (신청강좌)
    @PutMapping("/updateLectureApplyReason")
    public ResponseEntity<?> updateLectureApplyReason(
            @RequestParam Long lectureId,
            @RequestParam String newApplyReason,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

            Long userId = currentUser.getUid();
            LectureApplyEntity updatedLectureApply = lectureApplyService.updateApplyReason(userId, lectureId, newApplyReason);

            if (updatedLectureApply != null) {
                String message = String.format("강좌참여신청 이유가 업데이트되었습니다. userId: %d, lectureId: %d", userId, lectureId);
                return ResponseEntity.ok(message);
            } else {
                return ResponseEntity.badRequest().body("강좌참여 이유를 수정할 권한이 없습니다.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 세션로그인후 자신이 신청한 강좌 삭제 API (신청강좌)
    @DeleteMapping("/deleteLectureApply/{lectureId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteLectureApply(
            @PathVariable Long lectureId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

            LectureApplyEntity cancelledLectureApply = lectureApplyService.cancelLectureApply(currentUser.getUid(), lectureId);

            return ResponseEntity.ok("강좌 신청이 취소되었습니다. Lecture ID: " + cancelledLectureApply.getLecture().getCreate_id());
        } catch (EntityNotFoundException e) {
            log.error("Lecture not found. ID: " + lectureId, e);
            return ResponseEntity.badRequest().body("강좌를 찾을 수 없습니다. ID: " + lectureId);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // 강좌개설 3단계 : 1,2단게에서 입력한 모든 정보를 확인하는 API Controller
    @GetMapping("/myApplyLectureAlls/{lectureId}")
    public ResponseEntity<?> getLectureDetailsAndAppliedLectures(
            @PathVariable Long lectureId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

            // 현재 사용자가 강좌의 생성자인지 확인
            LectureEntity lectureEntity = lectureRepository.findById(lectureId)
                    .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + lectureId));
            if (!lectureEntity.getUser().equals(currentUser)) {
                throw new RuntimeException("해당 강좌를 확인할 권한이 없습니다.");
            }

            CreateLectureFullInfoDto lectureFullInfo = lectureStepTwoService.getFullLectureInfo(lectureId);
            List<LectureApplyDto> myAppliedLectures = lectureApplyService.getMyAppliedLectures(currentUser.getUid());

            Map<String, Object> response = new HashMap<>();
            response.put("lectureDetails", lectureFullInfo);
            response.put("myAppliedLectures", myAppliedLectures);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}


