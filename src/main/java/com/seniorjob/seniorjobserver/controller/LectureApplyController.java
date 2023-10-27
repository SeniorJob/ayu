package com.seniorjob.seniorjobserver.controller;

import com.seniorjob.seniorjobserver.domain.entity.LectureApplyEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.LectureApplyDto;
import com.seniorjob.seniorjobserver.repository.LectureApplyRepository;
import com.seniorjob.seniorjobserver.repository.LectureRepository;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import com.seniorjob.seniorjobserver.service.LectureApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lectureapply")
public class LectureApplyController {

    private final LectureApplyService lectureApplyService;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final LectureApplyRepository lectureApplyRepository;

    @Autowired
    public LectureApplyController(LectureApplyService lectureApplyService, UserRepository userRepository, LectureRepository lectureRepository, LectureApplyRepository lectureApplyRepository) {
        this.lectureApplyService = lectureApplyService;
        this.userRepository = userRepository;
        this.lectureRepository = lectureRepository;
        this.lectureApplyRepository = lectureApplyRepository;
    }

    // 강좌 참여 신청 API
    // POST /apply/{lectureId}
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/apply/{lectureId}")
    public ResponseEntity<?> applyForLecture(@PathVariable Long lectureId,
                                             @RequestParam(required = false) String applyReason,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        LectureApplyEntity appliedData = lectureApplyService.applyForLecture(currentUser.getUid(), lectureId, applyReason);

        Map<String, Object> response = new HashMap<>();
        response.put("message", currentUser.getName() + " 님" + lectureId + " 번 강좌에 참여가 완료되었습니다. 이유 : "
                + applyReason);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 강좌 신청 취소 API
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/cancel/{lectureId}")
    public ResponseEntity<?> cancelLectureApply(
            @PathVariable Long lectureId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

            LectureApplyEntity canceledData = lectureApplyService.cancelLectureApply(currentUser.getUid(), lectureId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", currentUser.getName() + " 님, " + lectureId + " 번 강좌 신청이 취소되었습니다.");

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 해당 강좌에 신청한 회원 전체목록 조회 API
    // GET /list?lectureId=99
    @GetMapping("/list")
    public ResponseEntity<List<LectureApplyDto>> getApplicantsByLectureId(@RequestParam("lectureId") Long lectureId) {
        List<LectureApplyDto> applicants = lectureApplyService.getApplicantsByLectureId(lectureId);
        return ResponseEntity.ok(applicants);
    }

    // 기존 : 회원목록에서 승인이 된 회원들을 일괄 모집마감하는 api
    // 로그인된 회원이 개설한 강좌중 하나의 강좌를 골라 회원목록에서 승인이 된 회원들을 일괄 모집마감하는 API
    // PUT /close/{lectureId}
    @PutMapping("/close")
    public ResponseEntity<String> closeLectureApply(
            @RequestParam Long lectureId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        return lectureApplyService.closeLectureApply(lectureId, currentUser.getUid());
    }

    // 강좌참여신청 승인 상태 개별 변경 API
    // PUT /approve/{userId}/{lectureId}/{status}
    // 로그인된 회원이 개설한 강좌 참여신청 승인 상태 개별 변경 API로 수정 : 강좌참여신청을 한 user와 강좌를 받아 상태를 변경한다.
    @PutMapping("/approve")
    public ResponseEntity<String> updateLectureApplyStatus(
            @RequestParam Long userId,
            @RequestParam Long lectureId,
            @RequestParam LectureApplyEntity.LectureApplyStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 현재 사용자가 강좌의 생성자인지 확인
        LectureEntity lectureEntity = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강좌아이디 찾지못함 create_id: " + lectureId));
        if (!lectureEntity.getUser().equals(currentUser)) {
            throw new RuntimeException("해당 강좌의 참여신청 승인 상태 개별을 변경할 권한이 없습니다.");
        }

        try {
            Long loggedInUserId = Long.valueOf(((User)userDetails).getUsername());  // userDetails에서 ID 추출
            lectureApplyService.updateLectureApplyStatus(userId, lectureId, status, loggedInUserId);
            String message = String.format("강좌참여신청을 변경하였습니다. userId: %d, lectureId: %d", userId, lectureId);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
