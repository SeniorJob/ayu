package com.seniorjob.seniorjobserver.controller;

import com.seniorjob.seniorjobserver.dto.LectureProposalDto;
import com.seniorjob.seniorjobserver.repository.LectureRepository;
import com.seniorjob.seniorjobserver.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/myProposalLecture")
public class MypageProposalLectureController {
    private final LectureService lectureService;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(LectureController.class);
    private final LectureProposalService lectureProposalService;
    private final LectureApplyService lectureApplyService;

    public MypageProposalLectureController(LectureService lectureService, StorageService storageService, UserRepository userRepository, UserService userService, LectureRepository lectureRepository ,
                                           LectureProposalService lectureProposalService, LectureApplyService lectureApplyService) {
        this.lectureService = lectureService;
        this.storageService = storageService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.lectureRepository = lectureRepository;
        this.lectureProposalService = lectureProposalService;
        this.lectureApplyService = lectureApplyService;
    }

    // 세션로그인후 자신이 개설한 강좌제안 글 전체 조화 API(제안강좌)
    @GetMapping("/myProposalAll")
    public ResponseEntity<?> getMyProposalAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        List<LectureProposalDto> myProposalAll = lectureProposalService.getMyProposalAll(currentUser.getUid());

        if (myProposalAll.isEmpty()) {
            return ResponseEntity.ok("제안된 강좌가 없습니다.");
        }

        return ResponseEntity.ok(myProposalAll);
    }


    // 세션로그인후 자신이 개설한 강좌 제안 상세보기 API (참여강좌)
    @GetMapping("/myProposalDetail/{id}")
    public ResponseEntity<LectureProposalDto> getMyProposalDetail(
            @PathVariable("id") Long proposalId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

            LectureProposalDto proposalDto = lectureProposalService.getMyProposalDetail(proposalId);
            return ResponseEntity.ok(proposalDto);
        } catch (ResponseStatusException ex) {
            // NOT_FOUND 예외가 발생한 경우
            return ResponseEntity.status(ex.getStatus()).body(null);
        }
    }
}
