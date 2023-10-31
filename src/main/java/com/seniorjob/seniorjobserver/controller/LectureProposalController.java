package com.seniorjob.seniorjobserver.controller;

import com.seniorjob.seniorjobserver.domain.entity.LectureProposalEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.LectureProposalDto;
import com.seniorjob.seniorjobserver.repository.LectureProposalRepository;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import com.seniorjob.seniorjobserver.service.LectureProposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lectureproposal")
public class LectureProposalController {

    private final LectureProposalService lectureProposalService;
    private final UserRepository userRepository;
    private final LectureProposalRepository lectureProposalRepository;

    @Autowired
    public LectureProposalController(LectureProposalService lectureProposalService, UserRepository userRepository, LectureProposalRepository lectureProposalRepository){
        this.lectureProposalService = lectureProposalService;
        this.userRepository = userRepository;
        this.lectureProposalRepository = lectureProposalRepository;
    }

    // 강좌제안API
    // POST /api/lecturesproposal/apply
    @PostMapping("/apply")
    public ResponseEntity<LectureProposalDto> createLectureProposal(
            @RequestBody LectureProposalDto lectureProposalDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 강좌 제안 개설 요청 처리
        LectureProposalDto createdProposal = lectureProposalService.createLectureProposal(currentUser, lectureProposalDto);

        return ResponseEntity.ok(createdProposal);
    }

    // 강좌제안 전체 목록API
    // GET /api/lectureproposal/all
    @GetMapping("/all")
    public List<LectureProposalDto> getAllProposals() {
        return lectureProposalService.getAllProposals();
    }

    // 제안강좌 - 전체목록(제목검색 + 정렬(최신순, 오래된순), 지역, 카테고리, 페이징) API
    // 모든강좌조회 : /api/lectureProposal/filter?page=0&size=12&filter=latest&descending=true
    @GetMapping("/filter")
    public ResponseEntity<Page<LectureProposalDto>> getProposedLectures(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "filter", required = false, defaultValue = "true") String filter,
            @RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "12", name = "size") int size,
            @RequestParam(value = "descending", defaultValue = "true") boolean descending) {

        if (title != null && title.length() < 2) {
            throw new IllegalArgumentException("제목검색은 2자리 이상입력해주세요!");
        }
        if (region != null && region.length() < 2) {
            throw new IllegalArgumentException("지역검색은 2자리 이상입력해주세요!");
        }
        if (category != null && category.length() < 2) {
            throw new IllegalArgumentException("카테고리 검색은 2자리 이상입력해주세요!");
        }

        List<LectureProposalDto> proposedLectures = lectureProposalService.getAllProposals();

        // 필터링: 제목 검색
        if (title != null && !title.isEmpty()) {
            proposedLectures = proposedLectures.stream()
                    .filter(lectureProposal -> lectureProposal.getTitle().contains(title))
                    .collect(Collectors.toList());
        }

        // 필터링: 지역
        if (region != null && !region.isEmpty()) {
            proposedLectures = proposedLectures.stream()
                    .filter(lectureProposal -> lectureProposal.getRegion().contains(region))
                    .collect(Collectors.toList());
        }

        // 필터링: 카테고리
        if (category != null && !category.isEmpty()) {
            proposedLectures = proposedLectures.stream()
                    .filter(lectureProposal -> lectureProposal.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }

        // 정렬
        proposedLectures = sortLectures(proposedLectures, filter, descending);

        if (proposedLectures.isEmpty()) {
            throw new NoSuchElementException("검색결과에 해당하는 강좌제안이 없습니다.");
        }

        // 페이징
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), proposedLectures.size());
        Page<LectureProposalDto> pagedLectureDto = new PageImpl<>(proposedLectures.subList(start, end), pageable, proposedLectures.size());
        return ResponseEntity.ok(pagedLectureDto);
    }

    public List<LectureProposalDto> sortLectures(List<LectureProposalDto> lectureProposalList, String filter, boolean descending) {
        switch (filter) {
            case "latest":
                lectureProposalList.sort((a, b) -> descending ?
                        b.getCreateDate().compareTo(a.getCreateDate()) :
                        a.getCreateDate().compareTo(b.getCreateDate()));
                break;
            // 추가적인 필터 조건은 여기에 추가
            default:
                throw new IllegalArgumentException("잘못된 필터조건");
        }
        return lectureProposalList;
    }

    // 강좌제안 상세보기API
    // GET /api/lectureproposal/detail/{proposal_id}
    @GetMapping("/detail/{proposal_id}")
    public ResponseEntity<LectureProposalDto> getLectureProposalDetail(@PathVariable Long proposal_id){
        LectureProposalDto lectureProposal = lectureProposalService.getDetail(proposal_id);
        return ResponseEntity.ok(lectureProposal);
    }

    // 로그인된 유저의 개설된 강좌제안 수정API
    // PUT /api/lectureproposal/
    @PutMapping("/update/{proposal_id}")
    public ResponseEntity<LectureProposalDto> updateLectureProposal(
            @PathVariable Long proposal_id,
            @RequestBody LectureProposalDto lectureProposalDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 현재 사용자가 강좌제안의 생성자인지 확인
        LectureProposalEntity lectureProposal = lectureProposalRepository.findById(proposal_id)
                .orElseThrow(() -> new RuntimeException("강좌제안 아이디 찾지못함 proposal_id: " + proposal_id));
        if (!lectureProposal.getUser().equals(currentUser)) {
            throw new RuntimeException("해당 강좌제안를 수정할 권한이 없습니다.");
        }

        LectureProposalDto updatedProposal = lectureProposalService.updateLectureProposal(currentUser, proposal_id, lectureProposalDto);
        return ResponseEntity.ok(updatedProposal);
    }

    // 강좌제안 삭제API
    // DELETE /api/lectureproposal/delete/{proposal_id}
    @DeleteMapping("/delete/{proposal_id}")
    public ResponseEntity<String> deleteLectureProposal(
            @PathVariable Long proposal_id, @AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        String responseMessage = lectureProposalService.deleteLectureProposal(user, proposal_id);
        return ResponseEntity.ok(responseMessage);
    }
}
