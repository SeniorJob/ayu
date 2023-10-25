package com.seniorjob.seniorjobserver.controller;

import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.CreateLectureFullInfoDto;
import com.seniorjob.seniorjobserver.dto.LectureApplyDto;
import com.seniorjob.seniorjobserver.dto.LectureDetailDto;
import com.seniorjob.seniorjobserver.dto.LectureDto;
import com.seniorjob.seniorjobserver.repository.LectureRepository;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import com.seniorjob.seniorjobserver.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/api/mypageCreateLecture")
public class MypageCreateLectureController {
    private final LectureService lectureService;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(LectureController.class);
    private final LectureProposalService lectureProposalService;
    private final LectureApplyService lectureApplyService;
    private final LectureStepTwoService lectureStepTwoService;
    private final MypageService mypageService;
    public MypageCreateLectureController(LectureService lectureService, StorageService storageService, UserRepository userRepository, UserService userService, LectureRepository lectureRepository ,
                            LectureProposalService lectureProposalService, LectureApplyService lectureApplyService, LectureStepTwoService lectureStepTwoService, MypageService mypageService) {
        this.lectureService = lectureService;
        this.storageService = storageService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.lectureRepository = lectureRepository;
        this.lectureProposalService = lectureProposalService;
        this.lectureApplyService = lectureApplyService;
        this.lectureStepTwoService = lectureStepTwoService;
        this.mypageService = mypageService;
    }


    // 세션로그인후 자신이 개설한 강좌목록 전체조회API - 회원으로 이동 (개설강좌)
    @GetMapping("/myCreateLectureAll")
    public ResponseEntity<?> getMyLectureAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        List<LectureDto> myLectureAll = lectureService.getMyLectureAll(currentUser.getUid());

        if (myLectureAll.isEmpty()) {
            return ResponseEntity.ok("개설된 강좌가 없습니다.");
        }

        return ResponseEntity.ok(myLectureAll);
    }


    // 세션로그인 후 자신이 개설한 강좌 글 상세보기 API
    @GetMapping("/myLectureDetail/{id}")
    public ResponseEntity<LectureDto> getMyLectureDetail(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        LectureDto lecture = lectureService.getMyLectureDetail(id, currentUser.getUid());

        if (lecture != null) {
            LectureEntity.LectureStatus status = lectureService.getLectureStatus(id);
            lecture.setStatus(status);
            return ResponseEntity.ok(lecture);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    // 강좌개설 3단계 : 1,2단게에서 입력한 모든 정보를 확인하는 강좌 상세보기API
    @GetMapping("/myCreateLectureAlls/{lectureId}")
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

            LectureDetailDto mypageFullInfo = mypageService.getMypageInfo(lectureId);

            Map<String, Object> response = new HashMap<>();
            response.put("lectureDetails", mypageFullInfo);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
