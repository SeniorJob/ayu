package com.seniorjob.seniorjobserver.service;

import com.seniorjob.seniorjobserver.domain.entity.LectureApplyEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.LectureApplyDto;
import com.seniorjob.seniorjobserver.repository.LectureApplyRepository;
import com.seniorjob.seniorjobserver.repository.LectureRepository;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LectureApplyService {

    private final LectureRepository lectureRepository;
    private final LectureApplyRepository lectureApplyRepository;
    private final UserRepository userRepository;

    @Autowired
    public LectureApplyService(UserRepository userRepository, LectureRepository lectureRepository, LectureApplyRepository lectureApplyRepository) {
        this.userRepository = userRepository;
        this.lectureRepository = lectureRepository;
        this.lectureApplyRepository = lectureApplyRepository;
    }

    // 강좌 참여 신청
    public LectureApplyEntity applyForLecture(Long userId, Long lectureId, String applyReason) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. id: " + userId));

        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강좌를 찾을 수 없습니다. id: " + lectureId));

        // 강좌의 상태가 '신청가능상태'가 아닌 경우 예외 처리
        if (lecture.getStatus() != LectureEntity.LectureStatus.신청가능상태) {
            throw new RuntimeException("현재 이 강좌는 '신청가능상태'가 아닙니다.");
        }

        // 이미 강좌에 참여한 경우 예외 처리
        if (lectureApplyRepository.existsByUserAndLecture(user, lecture)) {
            throw new RuntimeException(lectureId + " 이미 참여하신 강좌입니다.");
        }

        // 모집마감된 경우 예외 처리
        if (lectureApplyRepository.findByLectureAndRecruitmentClosed(lecture, true).isPresent()) {
            throw new RuntimeException("모집이 마감된 강좌에는 신청할 수 없습니다.");
        }

        // 강좌 참여 생성
        LectureApplyEntity lectureApply = LectureApplyEntity.builder()
                .lecture(lecture)
                .user(user)
                .createdDate(LocalDateTime.now())
                .applyReason(applyReason)
                .build();
        lecture.increaseCurrentParticipants();
        lectureApply.setLectureApplyStatus(LectureApplyEntity.LectureApplyStatus.승인);
        lectureApplyRepository.save(lectureApply);
        return lectureApply;
    }

    // 강좌참여신청취소
    public LectureApplyEntity cancelLectureApply(Long userId, Long lectureId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("강좌를 찾을 수 없습니다. id: " + lectureId));

        LectureApplyEntity lectureApply = lectureApplyRepository.findByUserAndLecture(user, lecture)
                .orElseThrow(() -> new RuntimeException("신청된 강좌를 찾을 수 없습니다. userId: " + userId + ", lectureId: " + lectureId));

        lecture.decreaseCurrentParticipants();

        lectureApplyRepository.delete(lectureApply);

        return lectureApply;
    }

    // 해당 강좌에 신청한 회원 목록 조회
    public List<LectureApplyDto> getApplicantsByLectureId(Long lectureId) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("해당 강좌를 찾을 수 없습니다. id: " + lectureId));

        List<LectureApplyEntity> applicants = lectureApplyRepository.findByLecture(lecture);

        if (applicants.isEmpty()) {
            throw new RuntimeException("해당 강좌에 신청한 회원이 없습니다. 강좌 ID: " + lectureId);
        }

        return applicants.stream()
                .map(LectureApplyDto::new)
                .collect(Collectors.toList());
    }

    // 기존 : 회원목록에서 승인이 된 회원들을 일괄 모집마감하는 api
    // 로그인된 회원이 개설한 강좌중 하나의 강좌를 골라 회원목록에서 승인이 된 회원들을 일괄 모집마감하는 API
    public ResponseEntity<String> closeLectureApply(Long lectureId, Long userId) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("해당 강좌를 찾을 수 없습니다. id: " + lectureId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. id: " + userId));

        // 로그인된 사용자가 강좌를 개설한 사용자인지 확인
        if (!lecture.getUser().getUid().equals(user.getUid())) {
            return ResponseEntity.badRequest().body("이 강좌를 개설한 사용자만 모집을 마감할 수 있습니다.");
        }

        List<LectureApplyEntity> approvedApplicants = lectureApplyRepository.findByLectureAndLectureApplyStatus(lecture, LectureApplyEntity.LectureApplyStatus.승인);

        if (approvedApplicants.isEmpty()) {
            return ResponseEntity.badRequest().body("해당 강좌에 승인된 회원이 없습니다. 강좌 ID: " + lectureId);
        }

        for (LectureApplyEntity applicant : approvedApplicants) {
            applicant.setRecruitmentClosed(true);
            lectureApplyRepository.save(applicant);
        }

        lecture.setRecruitmentClosed(true);
        lecture.setStatus(LectureEntity.LectureStatus.개설대기상태); // 강좌의 상태를 개설대기상태로 변경
        lectureRepository.save(lecture);

        return ResponseEntity.ok("일괄 모집마감이 완료되었습니다.");
    }

    // 강좌참여신청 승인 상태 개별 변경
    // 로그인된 회원이 개설한 강좌 참여신청 승인 상태 개별 변경 API로 수정
    public void updateLectureApplyStatus(Long userId, Long lectureId, LectureApplyEntity.LectureApplyStatus status, Long loggedInUserId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. id: " + userId));

        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("해당 강좌를 찾을 수 없습니다. id: " + lectureId));

        // 로그인된 사용자가 이 강좌의 개설자인지 확인
        if (!lecture.getUser().getUid().equals(user.getUid())) {
            throw new RuntimeException("이 강좌를 개설한 사용자만 승인 상태를 변경할 수 있습니다.");
        }

        LectureApplyEntity lectureApply = lectureApplyRepository.findByUserAndLecture(user, lecture)
                .orElseThrow(() -> new RuntimeException("해당 회원의 신청한 강좌를 찾을 수 없습니다."));

        lectureApply.setLectureApplyStatus(status);
        lectureApplyRepository.save(lectureApply);
    }
}
