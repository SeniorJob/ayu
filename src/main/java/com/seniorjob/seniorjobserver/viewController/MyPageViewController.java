package com.seniorjob.seniorjobserver.viewController;

import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.dto.LectureApplyDto;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import com.seniorjob.seniorjobserver.service.LectureApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/mypage")
public class MyPageViewController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LectureApplyService lectureApplyService;

    // 로그인한 회원이 신청강좌목록
    // 자신이 신청한 강좌 제목 가져오는 메서드 추가
    // 강좌 상태 가져오는 메서드 추가
    @GetMapping("/applied-lectures")
    public String getAppliedLectures(
            @AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserEntity currentUser = userRepository.findByPhoneNumber(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        List<LectureApplyDto> myAppliedLectures = lectureApplyService.getMyAppliedLectures(currentUser.getUid());
        model.addAttribute("appliedLectures", myAppliedLectures);
        return "mypage/applied-lectures";
    }

    // 강좌신청 이유수정
    @GetMapping("/edit-apply-reason/{id}")
    public String editApplyReason(@PathVariable Long id, Model model) {
        LectureApplyDto lectureApply = lectureApplyService.getLectureApplyById(id);
        model.addAttribute("lectureId", lectureApply.getLectureId());
        model.addAttribute("applyReason", lectureApply.getApplyReason());
        return "mypage/edit-apply-reason";
    }

}
