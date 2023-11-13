package com.seniorjob.seniorjobserver.controller;

import com.seniorjob.seniorjobserver.config.JwtTokenProvider;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
    private AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public MypageApplyLectureController(LectureService lectureService, StorageService storageService, UserRepository userRepository, UserService userService, LectureRepository lectureRepository,
                                        LectureProposalService lectureProposalService, LectureApplyService lectureApplyService, LectureStepTwoService lectureStepTwoService,
                                        MyPageLectureApplyService myPageLectureApplyService, MypageService mypageService,
                                        AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
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
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 마이페이지(신청강좌) - 세션로그인후 자신이 신청한 강좌 전체 조화 API
    @GetMapping("/myApplyLectureAll")
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
    public ResponseEntity<Page<MyPageLectureApplyDto>> getMyAppliedLecturesWithFilter(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(defaultValue = "1", name = "page") int page,
            @RequestParam(defaultValue = "5", name = "size") int size,
            @RequestParam(value = "descending", defaultValue = "true") boolean descending,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 사용자가 신청한 전체 강좌 가져오기
        List<MyPageLectureApplyDto> myAppliedLectures = myPageLectureApplyService.getMyAppliedLectures(currentUser.getUid());

        // 필터링: 제목 검색
        if (title != null && !title.isEmpty()) {
            myAppliedLectures = myPageLectureApplyService.searchLecturesByTitle(currentUser.getUid(), title);
        }

        // 필터링: 조건에 따라 lectureList 필터링
        if (filter != null && !filter.isEmpty()) {
            myAppliedLectures = myPageLectureApplyService.filterLectures(myAppliedLectures, filter, descending);
        }

        // 필터링: 모집중, 개설대기중, 진행중 상태에 따라 필터링
        if (status != null && !status.isEmpty()) {
            LectureEntity.LectureStatus lectureStatus;

            switch (status) {
                case "모집중":
                    lectureStatus = LectureEntity.LectureStatus.신청가능상태;
                    break;
                case "개설대기중":
                    lectureStatus = LectureEntity.LectureStatus.개설대기상태;
                    break;
                case "진행중":
                    lectureStatus = LectureEntity.LectureStatus.진행상태;
                    break;
                case "완료강좌":
                    lectureStatus = LectureEntity.LectureStatus.완료상태;
                    break;
                default:
                    throw new IllegalArgumentException("잘못된 상태 키워드입니다.");
            }

            myAppliedLectures = myPageLectureApplyService.filterStatus(myAppliedLectures, lectureStatus);
        }

        // 검색결과에 해당하는 강좌가 없을 경우
        if (myAppliedLectures.isEmpty()) {
            throw new NoSuchElementException("검색결과에 해당하는 강좌가 없습니다.");
        }

        // 페이징
        Pageable pageable = PageRequest.of(page -1, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), myAppliedLectures.size());
        Page<MyPageLectureApplyDto> pagedMyAppliedLectures = new PageImpl<>(myAppliedLectures.subList(start, end), pageable, myAppliedLectures.size());

        return ResponseEntity.ok(pagedMyAppliedLectures);
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
//    @DeleteMapping("/deleteLectureApply/{lectureId}")
//    public ResponseEntity<?> deleteLectureApply(
//            @PathVariable Long lectureId,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        try {
//            UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
//                    .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
//
//            LectureApplyEntity cancelledLectureApply = lectureApplyService.cancelLectureApply(currentUser.getUid(), lectureId);
//
//            return ResponseEntity.ok("강좌 신청이 취소되었습니다. Lecture ID: " + cancelledLectureApply.getLecture().getCreate_id());
//        } catch (EntityNotFoundException e) {
//            log.error("Lecture not found. ID: " + lectureId, e);
//            return ResponseEntity.badRequest().body("강좌를 찾을 수 없습니다. ID: " + lectureId);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }

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



