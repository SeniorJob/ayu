package com.seniorjob.seniorjobserver.service;
import com.seniorjob.seniorjobserver.domain.entity.LectureProposalEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.LectureProposalDto;
import com.seniorjob.seniorjobserver.dto.MypageLectureProposalDto;
import com.seniorjob.seniorjobserver.repository.LectureProposalRepository;
import com.seniorjob.seniorjobserver.repository.LectureRepository;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class MyPageLectureProposalService {
    private final LectureProposalRepository lectureProposalRepository;
    private final UserRepository userRepository;

    @Autowired
    public MyPageLectureProposalService(LectureProposalRepository lectureProposalRepository, UserRepository userRepository, LectureRepository lectureRepository) {
        this.lectureProposalRepository = lectureProposalRepository;
        this.userRepository = userRepository;
    }

    // 현재 로그인한 사용자가 제안한 강좌 목록 조회
    public List<MypageLectureProposalDto> getMyProposalAll(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        List<LectureProposalEntity> myProposalAll = lectureProposalRepository.findAllByUser(user);
        return myProposalAll.stream()
                .map(this::convertToMypageDto)
                .collect(Collectors.toList());
    }

    // 세션로그인 후 자신의 제안강좌 상세보기
    public LectureProposalDto getMyProposalDetail(Long proposal_id) {
        LectureProposalEntity entity = lectureProposalRepository.findById(proposal_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("조회하신 %d는 없는 강좌입니다.", proposal_id)));

        return LectureProposalDto.convertToDto(entity);
    }

    // 필터링 : 제목검색 -> 최신순,오래된순, 가격높은순, 가격낮은순, 인기순, 지역(시,군),
    // 상좌상태(모집중 = 신청가능상태,  개설대기중 = 개설대기상태, 진행중 = 진행상태), 카테고리
    public List<MypageLectureProposalDto> filterLectures(List<MypageLectureProposalDto> lectureProposalList, String filter, boolean descending) {
        switch (filter){
            case "latest":
                return sortLecturesByCreatedDate(lectureProposalList, descending);
            default:
                throw new IllegalArgumentException("잘못된 필터조건");
        }
    }
    public List<MypageLectureProposalDto> filterLecturesByTitle(List<MypageLectureProposalDto> lectureProposalList, String title) {
        if (title.length() >= 2) {
            List<MypageLectureProposalDto> filteredLectures = lectureProposalList.stream()
                    .filter(lecture -> lecture.getTitle().contains(title))
                    .collect(Collectors.toList());

            if (filteredLectures.isEmpty()) {
                throw new NoSuchElementException("검색결과에 해당하는 강좌제안이 없습니다.ㅠㅠ");
            }

            return filteredLectures;
        } else {
            throw new IllegalArgumentException("검색어는 \"2글자\" 이상 입력해주세요!");
        }
    }
    // 강좌정렬
    // 최신순으로 강좌 정렬 최신 = true 오래된 = false
    public List<MypageLectureProposalDto> sortLecturesByCreatedDate(List<MypageLectureProposalDto> lectureProposalList, boolean descending) {
        lectureProposalList.sort((a, b) -> descending ?
                b.getCreatedDate().compareTo(a.getCreatedDate()) :
                a.getCreatedDate().compareTo(b.getCreatedDate()));
        return lectureProposalList;
    }

    //페이징
    public Page<LectureProposalEntity> getLectures(Pageable pageable) { return lectureProposalRepository.findAll(pageable); }

    private MypageLectureProposalDto convertToMypageDto(LectureProposalEntity entity) {
        MypageLectureProposalDto mypageDto = new MypageLectureProposalDto();
        mypageDto.setTitle(entity.getTitle());
        mypageDto.setProposalId(entity.getProposal_id());
        mypageDto.setRegion(entity.getRegion());
        mypageDto.setContent(entity.getContent());
        mypageDto.setCategory(entity.getCategory());
        mypageDto.setCreatedDate(entity.getCreated_date());
        mypageDto.setUser(entity.getUser());
        // 나머지 필드를 복사 또는 설정
        return mypageDto;
    }

    private LectureProposalDto convertToDto(LectureProposalEntity lectureProposalEntity) {
        return LectureProposalDto.builder()
                .proposalId(lectureProposalEntity.getProposal_id())
                .userName(lectureProposalEntity.getUser().getName())
                .category(lectureProposalEntity.getCategory())
                .region(lectureProposalEntity.getRegion())
                .title(lectureProposalEntity.getTitle())
                .content(lectureProposalEntity.getContent())
                .createDate(lectureProposalEntity.getCreated_date())
                .build();
    }
}
