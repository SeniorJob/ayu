package com.seniorjob.seniorjobserver.controller;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
    private final MyPageCreateLectureService myPageCreateLectureService;
    public MypageCreateLectureController(LectureService lectureService, StorageService storageService, UserRepository userRepository, UserService userService, LectureRepository lectureRepository ,
                            LectureProposalService lectureProposalService, LectureApplyService lectureApplyService, LectureStepTwoService lectureStepTwoService, MypageService mypageService,
                                         MyPageCreateLectureService myPageCreateLectureService) {
        this.lectureService = lectureService;
        this.storageService = storageService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.lectureRepository = lectureRepository;
        this.lectureProposalService = lectureProposalService;
        this.lectureApplyService = lectureApplyService;
        this.lectureStepTwoService = lectureStepTwoService;
        this.mypageService = mypageService;
        this.myPageCreateLectureService = myPageCreateLectureService;
    }

    // 마이페이지(개설강좌) - 세션로그인후 자신이 개설한 강좌목록 전체조회API Refactoring
    @GetMapping("/myCreateLectureAll")
    public ResponseEntity<?> getMyPageCreateLectureAll(@AuthenticationPrincipal UserDetails userDetails) {
        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        List<MyPageCreateLectureDto> myLectureAll = myPageCreateLectureService.getMyPageCreateLectureAll(currentUser.getUid());

        if (myLectureAll.isEmpty()) {
            return ResponseEntity.ok("개설된 강좌가 없습니다.");
        }

        return ResponseEntity.ok(myLectureAll);
    }

    // 마이페이지(개설강좌) - 세션로그인후 자신이 개설한 강좌상세보기API Refactoring
    // 수정, 삭제 가능
    @GetMapping("/myCreateLectureDetail/{lectureId}")
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

    // 마이페이지(개설강좌) - 세션로그인 후 자신이 개설한 강좌 검색 + 필터링 API Refactoring
    // 제목 + 정렬(최신순, 오래된순, 인기순, 가격높은순, 가격낮은순) + 필터링(강좌상태)
    // api/mypageCreateLecture/filter == 모든강좌조회
    // api/mypageCreateLecture/filter?title="강좌제목" == 제목만으로 검색
    @GetMapping("/filter")
    public ResponseEntity<Page<MyPageCreateLectureDto>> filterAndPaginateLectures2(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "12", name = "size") int size,
            @RequestParam(value = "descending", defaultValue = "false") boolean descending,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 사용자가 개설한 전체 강좌 가져오기
        List<MyPageCreateLectureDto> myLectureAll = myPageCreateLectureService.getMyPageCreateLectureAll(currentUser.getUid());

        // 필터링: 제목 검색
        if (title != null && !title.isEmpty()) {
            myLectureAll = myPageCreateLectureService.searchLecturesByTitle(title);
        }

        // 필터링: 조건에 따라 lectureList 필터링
        if (filter != null && !filter.isEmpty()) {
            myLectureAll = myPageCreateLectureService.filterLectures(myLectureAll, filter, descending);
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
                default:
                    throw new IllegalArgumentException("잘못된 상태 키워드입니다.");
            }

            myLectureAll = myPageCreateLectureService.filterStatus(myLectureAll, lectureStatus);
        }

        // 검색결과에 해당하는 강좌가 없을 경우
        if (myLectureAll.isEmpty()) {
            throw new NoSuchElementException("검색결과에 해당하는 강좌가 없습니다.");
        }

        // 페이징
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), myLectureAll.size());
        Page<MyPageCreateLectureDto> pagedLectureDto = new PageImpl<>(myLectureAll.subList(start, end), pageable, myLectureAll.size());
        return ResponseEntity.ok(pagedLectureDto);
    }


}
