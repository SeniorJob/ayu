package com.seniorjob.seniorjobserver.service;
import com.seniorjob.seniorjobserver.domain.entity.LectureProposalEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.LectureProposalDto;
import com.seniorjob.seniorjobserver.repository.LectureProposalRepository;
import com.seniorjob.seniorjobserver.repository.LectureRepository;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LectureProposalService {
    private final LectureProposalRepository lectureProposalRepository;
    private final UserRepository userRepository;

    @Autowired
    public LectureProposalService(LectureProposalRepository lectureProposalRepository, UserRepository userRepository, LectureRepository lectureRepository) {
        this.lectureProposalRepository = lectureProposalRepository;
        this.userRepository = userRepository;
    }

    // 강좌제안개설
    public LectureProposalDto createLectureProposal(UserEntity user, LectureProposalDto lectureProposalDto) {
        LocalDateTime currentDate = LocalDateTime.now();

        LectureProposalEntity lectureProposalEntity = lectureProposalDto.toEntity();
        lectureProposalEntity.setUser(user);
        lectureProposalEntity.setCreated_date(currentDate);

        lectureProposalDto.validateFields();

        LectureProposalEntity savedLectureProposal = lectureProposalRepository.save(lectureProposalEntity);

        return LectureProposalDto.convertToDto(savedLectureProposal);
    }

    // 제안된강좌 전체목록 조회
    public List<LectureProposalDto> getAllProposals() {
        List<LectureProposalEntity> lectureProposals = lectureProposalRepository.findAll();

        return lectureProposals.stream()
                .map(LectureProposalDto::new)
                .collect(Collectors.toList());
    }

    // 제안된강좌 상세보기
    public LectureProposalDto getDetail(Long proposal_id) {
        LectureProposalEntity entity = lectureProposalRepository.findById(proposal_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("조회하신 %d는 없는 강좌입니다.", proposal_id)));

        return LectureProposalDto.convertToDto(entity);
    }

    // 제안된 강좌 수정
    public LectureProposalDto updateLectureProposal(UserEntity user, Long proposal_id, LectureProposalDto lectureProposalDto) {
        LectureProposalEntity lectureProposal = findLectureProposalById(proposal_id);
        validateUserPermission(user, lectureProposal);

        lectureProposalDto.validateFields();
        updateProposalDetails(lectureProposal, lectureProposalDto);

        LectureProposalEntity updatedProposal = lectureProposalRepository.save(lectureProposal);
        return new LectureProposalDto(updatedProposal);
    }

    private LectureProposalEntity findLectureProposalById(Long proposal_id) {
        return lectureProposalRepository.findById(proposal_id)
                .orElseThrow(() -> new RuntimeException("해당 강좌 제안을 찾을 수 없습니다. ID: " + proposal_id));
    }

    private void validateUserPermission(UserEntity user, LectureProposalEntity lectureProposal) {
        if (!lectureProposal.getUser().getUid().equals(user.getUid())) {
            throw new IllegalArgumentException("강좌제안 개설자와 일치하지 않습니다.");
        }
    }

    private void updateProposalDetails(LectureProposalEntity lectureProposal, LectureProposalDto lectureProposalDto) {
        lectureProposal.setTitle(lectureProposalDto.getTitle());
        lectureProposal.setCategory(lectureProposalDto.getCategory());
        lectureProposal.setRegion(lectureProposalDto.getRegion());
        lectureProposal.setContent(lectureProposalDto.getContent());
    }

    // 제안된강좌 삭제
    public String deleteLectureProposal(UserEntity user, Long proposal_id) {
        LectureProposalEntity lectureProposal = lectureProposalRepository.findById(proposal_id)
                .orElseThrow(() -> new RuntimeException("해당 강좌 제안을 찾을 수 없습니다. ID: " + proposal_id));

        // 사용자 확인
        if (!lectureProposal.getUser().getUid().equals(user.getUid())) {
            throw new IllegalArgumentException("강좌제안 개설자만 해당 강좌 제안을 삭제할 수 있습니다.");
        }

        lectureProposalRepository.deleteById(proposal_id);
        return "강좌제안 " + proposal_id + "를 삭제하였습니다.";
    }

    // 필터링 : 제목검색 -> 최신순,오래된순
    public List<LectureProposalDto> filterLecture(List<LectureProposalDto> lectureProposalList, String filter, boolean descending) {
        switch (filter){
            case "latest":
                return sortLecturesByCreatedDate(lectureProposalList, descending);
            default:
                throw new IllegalArgumentException("잘못된 필터조건");
        }
    }

    // 강좌정렬
    // 최신순으로 강좌 정렬 최신 = true 오래된 = false
    public List<LectureProposalDto> sortLecturesByCreatedDate(List<LectureProposalDto> lectureProposalList, boolean descending) {
        lectureProposalList.sort((a, b) -> descending ?
                b.getCreateDate().compareTo(a.getCreateDate()) :
                a.getCreateDate().compareTo(b.getCreateDate()));
        return lectureProposalList;
    }

    //페이징
    public Page<LectureProposalEntity> getLectureProposal(Pageable pageable) { return lectureProposalRepository.findAll(pageable); }

    private LectureProposalDto convertToLectureProposalDto(LectureProposalEntity entity) {
        LectureProposalDto lectureProposalDto = new LectureProposalDto();
        lectureProposalDto.setTitle(entity.getTitle());
        lectureProposalDto.setProposalId(entity.getProposal_id());
        lectureProposalDto.setRegion(entity.getRegion());
        lectureProposalDto.setContent(entity.getContent());
        lectureProposalDto.setCategory(entity.getCategory());
        lectureProposalDto.setCreateDate(entity.getCreated_date());
        lectureProposalDto.setUser(entity.getUser());
        return lectureProposalDto;
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
