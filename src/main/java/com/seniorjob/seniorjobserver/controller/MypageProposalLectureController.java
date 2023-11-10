package com.seniorjob.seniorjobserver.controller;

import com.seniorjob.seniorjobserver.config.JwtTokenProvider;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureProposalEntity;
import com.seniorjob.seniorjobserver.dto.LectureProposalDto;
import com.seniorjob.seniorjobserver.dto.MypageLectureProposalDto;
import com.seniorjob.seniorjobserver.repository.LectureProposalRepository;
import com.seniorjob.seniorjobserver.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/myProposalLecture")
public class MypageProposalLectureController {
    private final MyPageLectureProposalService myPageLectureProposalService;
    private final UserRepository userRepository;
    private final LectureProposalRepository lectureProposalRepository;
    private AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public MypageProposalLectureController(MyPageLectureProposalService myPageLectureProposalService, UserRepository userRepository, LectureProposalRepository lectureProposalRepository,
                                           AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider){
        this.myPageLectureProposalService = myPageLectureProposalService;
        this.userRepository = userRepository;
        this.lectureProposalRepository = lectureProposalRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 마이페이지(제안강좌) - 세션로그인후 자신이 개설한 강좌 제안 상세보기 API (참여강좌)
    @GetMapping("/myProposalDetail/{id}")
    public ResponseEntity<LectureProposalDto> getMyProposalDetail(
            @PathVariable("id") Long proposalId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

            // 현재 사용자가 강좌제안의 생성자인지 확인
            LectureProposalEntity lectureProposalEntity = lectureProposalRepository.findById(proposalId)
                    .orElseThrow(() -> new RuntimeException("강좌제안 아이디 찾지못함 create_id: " + proposalId));
            if (!lectureProposalEntity.getUser().equals(currentUser)) {
                throw new RuntimeException("해당 강좌제안를 확인할 권한이 없습니다.");
            }
            LectureProposalDto proposalDto = myPageLectureProposalService.getMyProposalDetail(proposalId);
            return ResponseEntity.ok(proposalDto);
        } catch (ResponseStatusException ex) {

            return ResponseEntity.status(ex.getStatus()).body(null);
        }
    }

    // 마이페이지(제안강좌) - 세션로그인후 자신이 개설한 강좌제안 글 전체 조회 API
    // 모든강좌조회 : /api/myProposalLecture/filter?page=0&size=12&filter=latest&descending=true
    @GetMapping("/filter")
    public ResponseEntity<Page<MypageLectureProposalDto>> getMyProposedLectures(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(defaultValue = "1", name = "page") int page,
            @RequestParam(defaultValue = "5", name = "size") int size,
            @RequestParam(value = "descending", defaultValue = "false") boolean descending,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        List<MypageLectureProposalDto> myProposedLectures = myPageLectureProposalService.getMyProposalAll(currentUser.getUid());

        // 필터링: 제목 검색
        if (title != null && !title.isEmpty()) {
            myProposedLectures = myPageLectureProposalService.filterLecturesByTitle(myProposedLectures, title);
        }

        // 필터링: 조건에 따라 강좌 리스트 필터링
        if (filter != null && !filter.isEmpty()) {
            myProposedLectures = myPageLectureProposalService.filterLectures(myProposedLectures, filter, descending);
        }


        if (myProposedLectures.isEmpty()) {
            throw new NoSuchElementException("검색결과에 해당하는 강좌제안이 없습니다.");
        }

        // 페이징
        Pageable pageable = PageRequest.of(page-1, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), myProposedLectures.size());
        Page<MypageLectureProposalDto> pagedLectureDto = new PageImpl<>(myProposedLectures.subList(start, end), pageable, myProposedLectures.size());
        return ResponseEntity.ok(pagedLectureDto);
    }
}
