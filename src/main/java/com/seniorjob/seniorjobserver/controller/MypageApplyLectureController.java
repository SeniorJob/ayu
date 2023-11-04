package com.seniorjob.seniorjobserver.controller;

import com.seniorjob.seniorjobserver.controller.LectureController;
import com.seniorjob.seniorjobserver.domain.entity.LectureApplyEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.*;
import com.seniorjob.seniorjobserver.repository.LectureRepository;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import com.seniorjob.seniorjobserver.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api/mypageApplyLecture")
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
    private final MyPageLectureApplyService myPageLectureApplyService;
    private final MypageService mypageService;

    public MypageApplyLectureController(LectureService lectureService, StorageService storageService, UserRepository userRepository, UserService userService, LectureRepository lectureRepository,
                                        LectureProposalService lectureProposalService, LectureApplyService lectureApplyService, LectureStepTwoService lectureStepTwoService,
                                        MyPageLectureApplyService myPageLectureApplyService, MypageService mypageService) {
        this.lectureService = lectureService;
        this.storageService = storageService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.lectureRepository = lectureRepository;
        this.lectureProposalService = lectureProposalService;
        this.lectureApplyService = lectureApplyService;
        this.lectureStepTwoService = lectureStepTwoService;
        this.myPageLectureApplyService = myPageLectureApplyService;
        this.mypageService = mypageService;
    }

    // 마이페이지(신청강좌) - 세션로그인후 자신이 신청한 강좌 전체 조화 API
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

    // 마이페이지(신청강좌) - 세션로그인후 자신이 신청한 강좌 전체 조화 필터링API
    // 필터링 : 제목 + 강좌상태 + 정렬(최신순, 오래된순, 인기순, 가격높은순, 가격낮은순)
    @GetMapping("/filter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyAppliedLecturesWithFilter(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "status", required = false) LectureEntity.LectureStatus status,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(defaultValue = "1", name = "page") int page,
            @RequestParam(defaultValue = "12", name = "size") int size,
            @RequestParam(value = "descending", defaultValue = "true") boolean descending,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

            Page<MyPageLectureApplyDto> myFilteredAppliedLectures = myPageLectureApplyService.getFilteredMyAppliedLectures(currentUser.getUid(), title, status, filter, page, size);
            return ResponseEntity.ok(myFilteredAppliedLectures);
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

    // 마이페이지(신청강좌) - 자신이 신청한 강좌 상세보기
    @GetMapping("/myAppliedLectureDetail/{lectureId}")
    public ResponseEntity<?> getMyAppliedLectureDetails(
            @PathVariable Long lectureId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

            MyPageLectureApplyDetailDto lectureDetails = myPageLectureApplyService.getMyAppliedLectureDetail(currentUser.getUid(), lectureId);

            return ResponseEntity.ok(lectureDetails);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}



